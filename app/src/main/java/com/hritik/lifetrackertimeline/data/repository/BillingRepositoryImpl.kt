package com.hritik.lifetrackertimeline.data.repository

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
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
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.Date
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
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
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
            override fun onBillingServiceDisconnected() {
                // Connection lost. Consider retry logic.
            }
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
                
                // Requirement 4 & 5: Check 'isPro' field
                val isPro = snapshot?.getBoolean("isPro") ?: false
                _isPremium.value = isPro

                // Update local Room database for offline access
                scope.launch {
                    val localUser = userRepository.user.firstOrNull()
                    if (localUser != null && localUser.uid == uid && localUser.isPremium != isPro) {
                        userRepository.saveUser(localUser.copy(isPremium = isPro))
                    }
                }
            }
    }

    override suspend fun queryProducts() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PREMIUM_PRODUCT_ID)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()

        billingClient.queryProductDetailsAsync(params) { result, queryProductDetailsResult ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                _products.value = queryProductDetailsResult.productDetailsList
            }
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

    override suspend fun restorePurchases() {
        // This triggers a fresh query from Play Store and syncs with Firestore
        queryPurchases()
    }

    override fun getProductDetails(productId: String): ProductDetails? =
        _products.value.find { it.productId == productId }

    override fun launchBillingFlow(activity: Activity, productDetails: ProductDetails) {
        val user = firebaseAuth.currentUser ?: return
        val email = user.email ?: ""
        
        // Requirement 1: Use SHA-256 hash of the currently logged-in Firebase user's email
        val hashedEmail = email.sha256()
        
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )

        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .setObfuscatedAccountId(hashedEmail) 
            .build()
            
        billingClient.launchBillingFlow(activity, params)
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            _purchases.value = purchases
            scope.launch { processPurchases(purchases) }
        }
    }

    override suspend fun processPurchases(purchases: List<Purchase>) {
        val currentUser = firebaseAuth.currentUser ?: return
        val expectedObfuscatedId = (currentUser.email ?: "").sha256()

        for (purchase in purchases) {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                // Requirement 2 & 6: Verify that the purchase belongs to the currently logged-in user
                val purchaseObfuscatedId = purchase.accountIdentifiers?.obfuscatedAccountId
                
                if (purchaseObfuscatedId == expectedObfuscatedId) {
                    if (!purchase.isAcknowledged) {
                        val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()
                        billingClient.acknowledgePurchase(acknowledgeParams) { result ->
                            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                                scope.launch { savePurchaseToFirestore(purchase) }
                            }
                        }
                    } else {
                        savePurchaseToFirestore(purchase)
                    }
                } else {
                    Global.log(TAG, "Purchase mismatch: Obfuscated ID does not match current user.")
                }
            }
        }
    }

    private suspend fun savePurchaseToFirestore(purchase: Purchase) {
        val uid = firebaseAuth.currentUser?.uid ?: return
        
        // Requirement 4: Store specific fields in Firestore
        val purchaseData = hashMapOf(
            "isPro" to true,
            "purchaseToken" to purchase.purchaseToken,
            "productId" to purchase.products.firstOrNull(),
            "purchaseTime" to Timestamp(Date(purchase.purchaseTime)),
            "obfuscatedAccountId" to purchase.accountIdentifiers?.obfuscatedAccountId,
            "orderId" to purchase.orderId,
            "acknowledged" to purchase.isAcknowledged
        )

        try {
            firestore.collection("users").document(uid)
                .set(purchaseData, SetOptions.merge())
                .await()
            Global.log(TAG, "Purchase synced to Firestore for UID: $uid")
        } catch (e: Exception) {
            Global.log(TAG, "Failed to sync purchase to Firestore", e)
        }
    }

    private fun String.sha256(): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(this.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
