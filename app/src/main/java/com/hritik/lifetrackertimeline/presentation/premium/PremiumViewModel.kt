package com.hritik.lifetrackertimeline.presentation.premium

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hritik.lifetrackertimeline.data.repository.BillingRepository
import com.hritik.lifetrackertimeline.helper.PremiumManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val premiumManager: PremiumManager,
    private val billingRepository: BillingRepository
) : ViewModel() {

    val isPremium = premiumManager.isPremium.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val products = billingRepository.products

    fun buyPremium(activity: Activity) {
        val productDetails = products.value.find { it.productId == "ad_free_lifetime" }
        productDetails?.let {
            billingRepository.launchBillingFlow(activity, it)
        }
    }

    fun restorePurchases() {
        // Implementation for restoring purchases if available in BillingRepository
    }
}
