package com.fairsplit.ui.preview

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.fairsplit.ui.components.*
import com.fairsplit.ui.theme.FairSplitTheme
import com.fairsplit.ui.theme.Spacing

/**
 * Component Preview Screen
 * 
 * Showcases all the newly created FairSplit components
 * for testing and visual verification on device.
 */
@Composable
fun ComponentPreviewScreen() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            FSAuthHeader(
                modifier = Modifier.padding(vertical = Spacing.lg)
            )
            
            Spacer(modifier = Modifier.height(Spacing.lg))
            
            Text(
                text = "Component Preview",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(Spacing.lg))
            
            // Email Input Field
            FSEmailField(
                value = email,
                onValueChange = { email = it },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
            
            Spacer(modifier = Modifier.height(Spacing.md))
            
            // Password Field
            FSPasswordField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                placeholder = "Enter password",
                helperText = "Minimum 8 characters",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Password",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
            
            Spacer(modifier = Modifier.height(Spacing.md))
            
            // Password Field with Error
            FSPasswordField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirm Password",
                placeholder = "Re-enter password",
                isError = confirmPassword.isNotEmpty() && confirmPassword != password,
                errorMessage = if (confirmPassword.isNotEmpty() && confirmPassword != password) 
                    "Passwords do not match" else null,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Confirm Password",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
            
            Spacer(modifier = Modifier.height(Spacing.lg))
            
            // Primary Button
            FSButton(
                onClick = { 
                    isLoading = true
                    // Simulate loading
                },
                text = "Sign In",
                enabled = email.isNotEmpty() && password.isNotEmpty()
            )
            
            Spacer(modifier = Modifier.height(Spacing.md))
            
            // Loading Button
            FSButton(
                onClick = { },
                text = "Loading...",
                loading = true
            )
            
            Spacer(modifier = Modifier.height(Spacing.md))
            
            // Outlined Button
            FSOutlinedButton(
                onClick = { },
                text = "Create Account"
            )
            
            Spacer(modifier = Modifier.height(Spacing.lg))
            
            // OR Divider
            FSOrDivider()
            
            Spacer(modifier = Modifier.height(Spacing.lg))
            
            // Google Button
            FSGoogleButton(
                onClick = { }
            )
            
            Spacer(modifier = Modifier.height(Spacing.md))
            
            // Apple Button
            FSAppleButton(
                onClick = { }
            )
            
            Spacer(modifier = Modifier.height(Spacing.xl))
            
            // Color Palette Preview
            Text(
                text = "Color Palette",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = Spacing.md)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ColorSwatch("Primary", MaterialTheme.colorScheme.primary)
                ColorSwatch("Secondary", MaterialTheme.colorScheme.secondary)
                ColorSwatch("Error", MaterialTheme.colorScheme.error)
            }
            
            Spacer(modifier = Modifier.height(Spacing.xxl))
        }
    }
}

@Composable
fun ColorSwatch(name: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(Spacing.iconContainerSize),
            color = color,
            shape = MaterialTheme.shapes.medium
        ) {}
        Spacer(modifier = Modifier.height(Spacing.xxs))
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ComponentPreviewScreenPreview() {
    FairSplitTheme {
        ComponentPreviewScreen()
    }
}
