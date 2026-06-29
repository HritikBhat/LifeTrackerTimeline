package com.hritik.lifetrackertimeline.data.repository

import android.app.Activity
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import kotlinx.coroutines.flow.StateFlow

interface BillingRepository {
    val isPremium: StateFlow<Boolean>
    val products: StateFlow<List<ProductDetails>>
    val purchases: StateFlow<List<Purchase>>
    
    suspend fun startConnection()
    suspend fun queryProducts()
    suspend fun queryPurchases()
    suspend fun restorePurchases()
    suspend fun processPurchases(purchases: List<Purchase>)
    fun getProductDetails(productId: String): ProductDetails?
    fun launchBillingFlow(activity: Activity, productDetails: ProductDetails)
}
