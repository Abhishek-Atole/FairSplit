package com.fairsplit.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fairsplit.ui.theme.CustomShapes
import com.fairsplit.ui.theme.CustomTextStyles
import com.fairsplit.ui.theme.Spacing

/**
 * FairSplit Primary Button Component
 * 
 * Primary action button matching the design system.
 * - Height: 56dp (from designs)
 * - Shape: 12dp rounded corners
 * - Primary color background
 * - Full width by default
 * 
 * @param onClick Click handler
 * @param modifier Modifier for the button
 * @param text Button text
 * @param enabled Whether the button is enabled
 * @param loading Whether to show loading indicator
 * @param leadingIcon Optional leading icon
 * @param trailingIcon Optional trailing icon
 */
@Composable
fun FSButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(Spacing.buttonHeight),
        enabled = enabled && !loading,
        shape = CustomShapes.Button,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        contentPadding = PaddingValues(horizontal = Spacing.buttonPadding)
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null) {
                    leadingIcon()
                    Spacer(modifier = Modifier.width(Spacing.buttonIconSpacing))
                }
                
                Text(
                    text = text,
                    style = CustomTextStyles.ButtonText
                )
                
                if (trailingIcon != null) {
                    Spacer(modifier = Modifier.width(Spacing.buttonIconSpacing))
                    trailingIcon()
                }
            }
        }
    }
}

/**
 * FairSplit Outlined Button Component
 * 
 * Secondary button with outlined style.
 * - Height: 56dp (from designs)
 * - Shape: 12dp rounded corners
 * - Transparent background with border
 * 
 * @param onClick Click handler
 * @param modifier Modifier for the button
 * @param text Button text
 * @param enabled Whether the button is enabled
 * @param loading Whether to show loading indicator
 * @param leadingIcon Optional leading icon
 * @param trailingIcon Optional trailing icon
 */
@Composable
fun FSOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(Spacing.buttonHeight),
        enabled = enabled && !loading,
        shape = CustomShapes.OutlinedButton,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
        ),
        contentPadding = PaddingValues(horizontal = Spacing.buttonPadding)
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null) {
                    leadingIcon()
                    Spacer(modifier = Modifier.width(Spacing.buttonIconSpacing))
                }
                
                Text(
                    text = text,
                    style = CustomTextStyles.ButtonText
                )
                
                if (trailingIcon != null) {
                    Spacer(modifier = Modifier.width(Spacing.buttonIconSpacing))
                    trailingIcon()
                }
            }
        }
    }
}
