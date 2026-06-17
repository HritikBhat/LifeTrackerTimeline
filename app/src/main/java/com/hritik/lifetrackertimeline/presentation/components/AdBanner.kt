package com.hritik.lifetrackertimeline.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.hritik.lifetrackertimeline.helper.Global

@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                // Test Ad Unit ID or real ID based on isAppOnTest
                adUnitId = if (Global.isAppOnTest) "ca-app-pub-3940256099942544/6300978111" else "ca-app-pub-4549313342341988/1904518303"
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
