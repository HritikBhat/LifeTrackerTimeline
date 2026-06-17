package com.hritik.lifetrackertimeline.helper

import com.hritik.lifetrackertimeline.data.repository.BillingRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PremiumManager @Inject constructor(
    private val billingRepository: BillingRepository
) {
    /**
     * Observe the premium status as a StateFlow.
     */
    val isPremium: StateFlow<Boolean> = billingRepository.isPremium

    /**
     * Check current premium status.
     */
    fun isPremiumNow(): Boolean {
        // If app is on test, you can override here if needed
        return billingRepository.isPremium.value || (Global.isAppOnTest && false)
    }
}
