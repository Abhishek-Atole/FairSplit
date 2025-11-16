package com.fairsplit.ui.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fairsplit.ui.components.FSAuthHeader
import com.fairsplit.ui.theme.CustomShapes
import com.fairsplit.ui.theme.FairSplitTheme
import com.fairsplit.ui.theme.Spacing
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

/**
 * Splash Screen
 * 
 * Initial loading screen matching the design:
 * - Centered logo (96dp circle, teal background)
 * - App name "FairSplit"
 * - Progress bar (65% filled)
 * - "Signing in..." or "Loading..." text
 * - Checks Firebase Auth state
 * - Auto-navigates after 2 seconds
 * 
 * @param onNavigateToLogin Callback when navigation to login should happen
 * @param onNavigateToDashboard Callback when navigation to dashboard should happen (if logged in)
 */
@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {}
) {
    // Simulate checking auth state
    var progress by remember { mutableFloatStateOf(0f) }
    var statusText by remember { mutableStateOf("Loading...") }
    
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "progress"
    )
    
    LaunchedEffect(Unit) {
        // Animate progress to 65% (as shown in design)
        progress = 0.65f
        
        // Check if user is logged in
        delay(1000)
        val currentUser = FirebaseAuth.getInstance().currentUser
        
        if (currentUser != null) {
            statusText = "Signing in..."
            delay(1000)
            onNavigateToDashboard()
        } else {
            statusText = "Loading..."
            delay(1000)
            onNavigateToLogin()
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo and App Name
            FSAuthHeader(
                showLogo = true,
                showAppName = true
            )
            
            Spacer(modifier = Modifier.height(Spacing.xxxl))
            
            // Progress Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.6f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LinearProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CustomShapes.ProgressBar),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                
                Spacer(modifier = Modifier.height(Spacing.md))
                
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SplashScreenPreview() {
    FairSplitTheme {
        SplashScreen()
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SplashScreenDarkPreview() {
    FairSplitTheme(darkTheme = true) {
        SplashScreen()
    }
}
