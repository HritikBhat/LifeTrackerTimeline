package com.hritik.lifetrackertimeline.presentation.profile

import android.Manifest
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.hritik.lifetrackertimeline.presentation.auth.AuthViewModel
import com.hritik.lifetrackertimeline.presentation.main.MainViewModel

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel,
    onNavigateToPremium: () -> Unit
) {
    val context = LocalContext.current
    val user by mainViewModel.userRepository.user.collectAsState(initial = null)
    val isPremium by mainViewModel.premiumManager.isPremium.collectAsState(initial = false)
    val notificationInterval by mainViewModel.dataStoreManager.notificationInterval.collectAsState(initial = "Never")
    
    var showIntervalDialog by remember { mutableStateOf(false) }
    val intervals = listOf("Every 30 mins", "Every 1 hour", "2 hour", "Never")

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Notification permission is required for reminders", Toast.LENGTH_SHORT).show()
        }
    }

    if (showIntervalDialog) {
        AlertDialog(
            onDismissRequest = { showIntervalDialog = false },
            title = { Text("Select Notification Interval") },
            text = {
                Column {
                    intervals.forEach { interval ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (interval != "Never" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                    mainViewModel.updateNotificationInterval(interval)
                                    showIntervalDialog = false
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = interval == notificationInterval,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = interval)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showIntervalDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

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
            title = "Notification Interval",
            trailing = {
                Text(
                    text = notificationInterval,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF673AB7)
                )
            },
            onClick = { showIntervalDialog = true }
        )

        ProfileItem(
            icon = Icons.Default.Star,
            title = if (isPremium) "Premium Member" else "Get a Pro",
            onClick = onNavigateToPremium
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
