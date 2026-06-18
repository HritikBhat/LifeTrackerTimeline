package com.hritik.lifetrackertimeline

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.ads.MobileAds
import com.hritik.lifetrackertimeline.data.local.DataStoreManager
import com.hritik.lifetrackertimeline.helper.PremiumManager
import com.hritik.lifetrackertimeline.helper.PremiumStatusProvider
import com.hritik.lifetrackertimeline.navigation.LifeTrackerNavGraph
import com.hritik.lifetrackertimeline.ui.theme.LifeTrackerTimelineTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var premiumManager: PremiumManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Mobile Ads
        MobileAds.initialize(this) {}

        enableEdgeToEdge()
        setContent {
            LifeTrackerTimelineTheme {
                PremiumStatusProvider(premiumManager = premiumManager) {
                    val navController = rememberNavController()
                    LifeTrackerNavGraph(navController)
                }
            }
        }
    }
}