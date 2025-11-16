package com.fairsplit.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fairsplit.R
import com.fairsplit.ui.theme.CustomShapes
import com.fairsplit.ui.theme.Spacing

/**
 * Auth Header Component
 * 
 * Logo and app name display for authentication screens.
 * Matches the splash screen design.
 * 
 * @param modifier Modifier for the component
 * @param showLogo Whether to show the logo (default true)
 * @param showAppName Whether to show app name (default true)
 */
@Composable
fun FSAuthHeader(
    modifier: Modifier = Modifier,
    showLogo: Boolean = true,
    showAppName: Boolean = true
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (showLogo) {
            // Logo container - circular background with icon
            Box(
                modifier = Modifier
                    .size(Spacing.logoContainerSize)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Scale,
                    contentDescription = "FairSplit Logo",
                    modifier = Modifier.size(Spacing.iconSizeLarge),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        
        if (showAppName) {
            Spacer(modifier = Modifier.height(Spacing.md))
            
            Text(
                text = "FairSplit",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
