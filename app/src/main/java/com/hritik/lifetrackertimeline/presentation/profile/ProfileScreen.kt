package com.hritik.lifetrackertimeline.presentation.profile

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.hritik.lifetrackertimeline.data.repository.BillingRepository
import com.hritik.lifetrackertimeline.data.repository.UserRepository
import com.hritik.lifetrackertimeline.helper.PremiumManager
import com.hritik.lifetrackertimeline.presentation.auth.AuthViewModel

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    userRepository: UserRepository,
    premiumManager: PremiumManager,
    billingRepository: BillingRepository // Added for triggering purchase
) {
    val context = LocalContext.current
    val user by userRepository.user.collectAsState(initial = null)
    val isPremium by premiumManager.isPremium.collectAsState(initial = false)
    val products by billingRepository.products.collectAsState()
    var notificationsEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FE))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // User Profile Header
        Spacer(modifier = Modifier.height(24.dp))
        AsyncImage(
            model = user?.photoUrl,
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = user?.displayName ?: "User Name",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = user?.email ?: "email@example.com",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Sections
        ProfileItem(
            icon = Icons.Default.Notifications,
            title = "Notifications",
            trailing = {
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
            }
        )

        ProfileItem(
            icon = Icons.Default.Star,
            title = if (isPremium) "Pro Version Active" else "Get a Pro",
            onClick = {
                if (!isPremium) {
                    val productDetails = products.find { it.productId == "ad_free_lifetime" }
                    productDetails?.let {
                        billingRepository.launchBillingFlow(context as Activity, it)
                    }
                }
            }
        )

        ProfileItem(
            icon = Icons.Default.Share,
            title = "Share App",
            onClick = {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Check out LifeTracker Timeline: https://play.google.com/store/apps/details?id=${context.packageName}")
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                context.startActivity(shareIntent)
            }
        )

        ProfileItem(
            icon = Icons.AutoMirrored.Filled.Logout,
            title = "Logout",
            titleColor = Color.Red,
            onClick = {
                authViewModel.logout()
            }
        )
    }
}

@Composable
fun ProfileItem(
    icon: ImageVector,
    title: String,
    titleColor: Color = Color.Black,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = if (titleColor == Color.Red) Color.Red else Color(0xFF673AB7))
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = titleColor,
                modifier = Modifier.weight(1f)
            )
            if (trailing != null) {
                trailing()
            } else if (onClick != null) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowRight, contentDescription = null, tint = Color.Gray)
            }
        }
    }
}
