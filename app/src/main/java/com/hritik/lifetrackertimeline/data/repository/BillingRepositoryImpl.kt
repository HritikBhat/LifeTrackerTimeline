package com.hritik.lifetrackertimeline.data.repository

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.hritik.lifetrackertimeline.helper.Global
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userRepository: UserRepository
) : BillingRepository, PurchasesUpdatedListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val _isPremium = MutableStateFlow(false)
    override val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _products = MutableStateFlow<List<ProductDetails>>(emptyList())
    override val products: StateFlow<List<ProductDetails>> = _products.asStateFlow()

    private val _purchases = MutableStateFlow<List<Purchase>>(emptyList())
    override val purchases: StateFlow<List<Purchase>> = _purchases.asStateFlow()

    private var premiumListener: ListenerRegistration? = null

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    init {
        scope.launch {
            startConnection()
        }
    }

    companion object {
        const val PREMIUM_PRODUCT_ID = "ad_free_lifetime"
        private const val TAG = "BillingRepository"
    }

    override suspend fun startConnection() {
        firebaseAuth.addAuthStateListener { auth ->
            val uid = auth.currentUser?.uid
            if (uid != null) {
                observePremiumStatus(uid)
            } else {
                premiumListener?.remove()
                _isPremium.value = false
            }
        }

        if (billingClient.isReady) return

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    scope.launch {
                        queryProducts()
                        queryPurchases()
                    }
                }
            }
            override fun onBillingServiceDisconnected() {}
        })
    }

    private fun observePremiumStatus(uid: String) {
        premiumListener?.remove()
        premiumListener = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Global.log(TAG, "Error listening to premium status", error)
                    return@addSnapshotListener
                }
                val premium = snapshot?.getBoolean("isPremium") ?: false
                _isPremium.value = premium
                
                // Update local Room database
                scope.launch {
                    val localUser = userRepository.user.firstOrNull()
                    if (localUser != null && localUser.isPremium != premium) {
                        userRepository.saveUser(localUser.copy(isPremium = premium))
                        Global.log(TAG, "Local premium status updated: $premium")
                    }
                }
            }
    }

    override suspend fun queryProducts() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(PREMIUM_PRODUCT_ID)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )).build()

        val result = billingClient.queryProductDetails(params)
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            _products.value = result.productDetailsList ?: emptyList()
        }
    }

    override suspend fun queryPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(params) { result, list ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                _purchases.value = list
                scope.launch { processPurchases(list) }
            }
        }
    }

    override suspend fun processPurchases(purchases: List<Purchase>) {
        val hasPremium = purchases.any { 
            it.products.contains(PREMIUM_PRODUCT_ID) && it.purchaseState == Purchase.PurchaseState.PURCHASED 
        }
        if (hasPremium) updatePremiumStatusInFirestore(true)
    }

    override fun getProductDetails(productId: String): ProductDetails? = _products.value.find { it.productId == productId }

    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            _purchases.value = purchases
            scope.launch { processPurchases(purchases) }
        }
    }

    private fun updatePremiumStatusInFirestore(isPremium: Boolean) {
        val uid = firebaseAuth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).update("isPremium", isPremium)
            .addOnSuccessListener {
                Global.log(TAG, "Premium status successfully updated in Firestore")
            }
            .addOnFailureListener { e -> Global.log(TAG, "Failed to update premium status", e) }
    }

    override fun launchBillingFlow(activity: Activity, productDetails: ProductDetails) {
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(productDetails).build()
            )).build()
        billingClient.launchBillingFlow(activity, params)
    }
}
