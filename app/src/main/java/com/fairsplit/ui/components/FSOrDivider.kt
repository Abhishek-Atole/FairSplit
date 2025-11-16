package com.fairsplit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fairsplit.ui.theme.CustomTextStyles
import com.fairsplit.ui.theme.Spacing

/**
 * OR Divider Component
 * 
 * Horizontal divider with "OR" text in the center.
 * Used in auth screens between form and social auth buttons.
 * 
 * @param modifier Modifier for the component
 * @param text Text to display (default "OR")
 */
@Composable
fun FSOrDivider(
    modifier: Modifier = Modifier,
    text: String = "OR"
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.orDividerSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Divider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outline,
            thickness = 1.dp
        )
        
        Text(
            text = text,
            style = CustomTextStyles.HelperText,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = Spacing.md)
        )
        
        Divider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outline,
            thickness = 1.dp
        )
    }
}
