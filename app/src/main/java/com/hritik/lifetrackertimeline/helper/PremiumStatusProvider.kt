package com.hritik.lifetrackertimeline.helper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue

val LocalIsPremium = compositionLocalOf { false }

@Composable
fun PremiumStatusProvider(
    premiumManager: PremiumManager,
    content: @Composable () -> Unit
) {
    val isPremium by premiumManager.isPremium.collectAsState(initial = false)
    
    CompositionLocalProvider(LocalIsPremium provides isPremium) {
        content()
    }
}
