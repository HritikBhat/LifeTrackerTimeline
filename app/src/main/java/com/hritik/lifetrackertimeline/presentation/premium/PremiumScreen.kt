package com.hritik.lifetrackertimeline.presentation.premium

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.hritik.lifetrackertimeline.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    navController: NavController,
    viewModel: PremiumViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isPremium by viewModel.isPremium.collectAsState()
    val products by viewModel.products.collectAsState()

    // Dialog state
    var showConfirmationDialog by remember { mutableStateOf(false) }
    val userEmail = remember { FirebaseAuth.getInstance().currentUser?.email ?: "unknown account" }

    val primaryColor = Color(0xFF673AB7)
    val backgroundColor = Color(0xFFF8F9FE)

    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.premium_confirm_purchase_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.premium_confirmation_message, userEmail),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmationDialog = false
                        (context as? Activity)?.let { viewModel.buyPremium(it) }
                    }
                ) {
                    Text(stringResource(R.string.continue_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmationDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.premium_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color(0xFFFFD700)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isPremium) stringResource(R.string.premium_member) else stringResource(R.string.upgrade_to_premium),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.premium_description),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(40.dp))

            PremiumFeatureItem(stringResource(R.string.feature_ad_free), primaryColor)
            PremiumFeatureItem(stringResource(R.string.feature_priority_support), primaryColor)
            PremiumFeatureItem(stringResource(R.string.feature_lifetime_access), primaryColor)

            Spacer(modifier = Modifier.weight(1f))

            if (!isPremium) {
                val premiumProduct = products.find { it.productId == "ad_free_lifetime" }
                Button(
                    onClick = { 
                        showConfirmationDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text(
                        text = premiumProduct?.let { 
                            stringResource(R.string.buy_premium_with_price, it.oneTimePurchaseOfferDetails?.formattedPrice ?: "")
                        } ?: stringResource(R.string.loading),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE8F5E9)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.premium_active),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { viewModel.restorePurchases() }) {
                Text(stringResource(R.string.restore_purchases), color = primaryColor)
            }
        }
    }
}

@Composable
fun PremiumFeatureItem(text: String, primaryColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = primaryColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}
