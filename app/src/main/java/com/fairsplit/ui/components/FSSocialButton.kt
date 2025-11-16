package com.fairsplit.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.fairsplit.R
import com.fairsplit.ui.theme.AppleBlack
import com.fairsplit.ui.theme.CustomShapes
import com.fairsplit.ui.theme.CustomTextStyles
import com.fairsplit.ui.theme.GoogleRed
import com.fairsplit.ui.theme.Spacing

/**
 * Social Authentication Button
 * 
 * Used for Google and Apple sign-in buttons.
 * - Height: 48dp (from designs)
 * - Shape: 12dp rounded corners
 * - Custom brand colors
 * 
 * @param onClick Click handler
 * @param modifier Modifier for the button
 * @param text Button text
 * @param icon Icon painter for the social platform
 * @param backgroundColor Background color
 * @param contentColor Content color
 * @param enabled Whether the button is enabled
 * @param loading Whether to show loading indicator
 */
@Composable
fun FSSocialButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    icon: Painter,
    backgroundColor: Color,
    contentColor: Color,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(Spacing.socialButtonHeight),
        enabled = enabled && !loading,
        shape = CustomShapes.SocialButton,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        contentPadding = PaddingValues(horizontal = Spacing.buttonPadding)
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(Spacing.buttonIconSpacing))
                
                Text(
                    text = text,
                    style = CustomTextStyles.ButtonText
                )
            }
        }
    }
}

/**
 * Google Sign-In Button
 * Pre-configured with Google branding
 */
@Composable
fun FSGoogleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    FSOutlinedButton(
        onClick = onClick,
        modifier = modifier,
        text = "Continue with Google",
        enabled = enabled,
        loading = loading,
        leadingIcon = {
            // Note: You'll need to add google icon to res/drawable
            // For now using a placeholder
            Icon(
                painter = painterResource(id = android.R.drawable.ic_dialog_info),
                contentDescription = "Google",
                modifier = Modifier.size(20.dp),
                tint = Color.Unspecified
            )
        }
    )
}

/**
 * Apple Sign-In Button
 * Pre-configured with Apple branding
 */
@Composable
fun FSAppleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(Spacing.socialButtonHeight),
        enabled = enabled && !loading,
        shape = CustomShapes.SocialButton,
        colors = ButtonDefaults.buttonColors(
            containerColor = AppleBlack,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = Spacing.buttonPadding)
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Note: You'll need to add apple icon to res/drawable
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_dialog_info),
                    contentDescription = "Apple",
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
                
                Spacer(modifier = Modifier.width(Spacing.buttonIconSpacing))
                
                Text(
                    text = "Continue with Apple",
                    style = CustomTextStyles.ButtonText
                )
            }
        }
    }
}
