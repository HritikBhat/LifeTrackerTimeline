package com.hritik.lifetrackertimeline.presentation.main

import androidx.lifecycle.ViewModel
import com.hritik.lifetrackertimeline.data.repository.BillingRepository
import com.hritik.lifetrackertimeline.data.repository.UserRepository
import com.hritik.lifetrackertimeline.helper.PremiumManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val userRepository: UserRepository,
    val premiumManager: PremiumManager,
    val billingRepository: BillingRepository
) : ViewModel()
