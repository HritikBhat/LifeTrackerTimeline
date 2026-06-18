package com.hritik.lifetrackertimeline.presentation.profile

import android.Manifest
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import coil.compose.AsyncImage
import com.hritik.lifetrackertimeline.R
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
    val selectedLanguage by mainViewModel.dataStoreManager.selectedLanguage.collectAsState(initial = "en")
    
    var showIntervalDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    
    val intervals = listOf("Every 30 mins", "Every 1 hour", "2 hour", "Never")
    
    val languages = listOf(
        stringResource(R.string.lang_english) to "en",
        stringResource(R.string.lang_hindi) to "hi",
        stringResource(R.string.lang_german) to "de",
        stringResource(R.string.lang_spanish) to "es",
        stringResource(R.string.lang_french) to "fr",
        stringResource(R.string.lang_russian) to "ru",
        stringResource(R.string.lang_japanese) to "ja"
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, context.getString(R.string.notification_permission_required), Toast.LENGTH_SHORT).show()
        }
    }

    if (showIntervalDialog) {
        AlertDialog(
            onDismissRequest = { showIntervalDialog = false },
            title = { Text(stringResource(R.string.select_notification_interval)) },
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
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.select_language)) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    languages.forEach { (name, code) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    mainViewModel.updateLanguage(code)
                                    AppCompatDelegate.setApplicationLocales(
                                        LocaleListCompat.forLanguageTags(code)
                                    )
                                    showLanguageDialog = false
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = code == selectedLanguage,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = name)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.cancel))
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
            contentDescription = stringResource(R.string.profile_picture),
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = user?.displayName ?: stringResource(R.string.user_name),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = user?.email ?: stringResource(R.string.email_placeholder),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Sections
        ProfileItem(
            icon = Icons.Default.Notifications,
            title = stringResource(R.string.notification_interval),
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
            title = if (isPremium) stringResource(R.string.premium_member) else stringResource(R.string.get_a_pro),
            onClick = onNavigateToPremium
        )

        ProfileItem(
            icon = Icons.Default.Language,
            title = stringResource(R.string.change_language),
            trailing = {
                Text(
                    text = languages.find { it.second == selectedLanguage }?.first ?: stringResource(R.string.lang_english),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF673AB7)
                )
            },
            onClick = { showLanguageDialog = true }
        )

        ProfileItem(
            icon = Icons.Default.Share,
            title = stringResource(R.string.share_app),
            onClick = {
                val shareText = context.getString(R.string.share_text, context.packageName)
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                context.startActivity(shareIntent)
            }
        )

        ProfileItem(
            icon = Icons.AutoMirrored.Filled.Logout,
            title = stringResource(R.string.logout),
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
