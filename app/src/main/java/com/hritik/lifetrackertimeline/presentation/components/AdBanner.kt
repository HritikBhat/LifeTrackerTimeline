package com.hritik.lifetrackertimeline.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.hritik.lifetrackertimeline.helper.Global
import com.hritik.lifetrackertimeline.helper.LocalIsPremium

@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    val isPremium = LocalIsPremium.current
    val context = LocalContext.current
    
    if (!isPremium) {
        AndroidView(
            modifier = modifier.fillMaxWidth(),
            factory = { ctx ->
                AdView(ctx).apply {
                    // Use adaptive banner size for full width
                    val adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                        ctx,
                        (ctx.resources.displayMetrics.widthPixels / ctx.resources.displayMetrics.density).toInt()
                    )
                    setAdSize(adSize)

                    // Use Test Ad Unit ID if app is on test, otherwise use production ID
                    adUnitId = if (Global.isAppOnTest) {
                        "ca-app-pub-3940256099942544/6300978111"
                    } else {
                        "ca-app-pub-4549313342341988/1904518303"
                    }
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}
