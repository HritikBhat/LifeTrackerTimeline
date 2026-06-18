package com.hritik.lifetrackertimeline.presentation.components

import android.app.Activity
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.hritik.lifetrackertimeline.helper.Global
import com.hritik.lifetrackertimeline.helper.LocalIsPremium
import kotlin.let
import kotlin.run

@Composable
fun InterstitialAdHandler() {
    val isPremium = LocalIsPremium.current
    Log.d("isPremz","Flag: $isPremium")
    if (isPremium) return

    val context = LocalContext.current
    var interstitialAd by remember { mutableStateOf<InterstitialAd?>(null) }
    var isAdShowing by remember { mutableStateOf(false) }
    var wasInBackground by remember { mutableStateOf(false) }

    val loadAd = {
        if (interstitialAd == null && !isAdShowing) {
            val adUnitId = if (Global.isAppOnTest) {
                "ca-app-pub-3940256099942544/1033173712" // Test Interstitial ID
            } else {
                // Replace with your real production Ad Unit ID
                "ca-app-pub-4549313342341988/6250957425"
            }

            InterstitialAd.load(
                context,
                adUnitId,
                AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                    }
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        interstitialAd = null
                    }
                }
            )
        }
    }

    // Load ad initially
    LaunchedEffect(Unit) {
        loadAd()
    }

    // Lifecycle observer to show ad when returning to app (foreground)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    if (!isAdShowing) {
                        wasInBackground = true
                    }
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (wasInBackground && !isAdShowing) {
                        interstitialAd?.let { ad ->
                            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                                override fun onAdDismissedFullScreenContent() {
                                    interstitialAd = null
                                    isAdShowing = false
                                    wasInBackground = false
                                    loadAd()
                                }

                                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                                    interstitialAd = null
                                    isAdShowing = false
                                    wasInBackground = false
                                    loadAd()
                                }

                                override fun onAdShowedFullScreenContent() {
                                    isAdShowing = true
                                    wasInBackground = false
                                }
                            }
                            isAdShowing = true
                            ad.show(context as Activity)
                        } ?: run {
                            wasInBackground = false
                            loadAd()
                        }
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
