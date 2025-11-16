package com.fairsplit.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.fairsplit.ui.components.*
import com.fairsplit.ui.theme.CustomShapes
import com.fairsplit.ui.theme.FairSplitTheme
import com.fairsplit.ui.theme.Spacing
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.userProfileChangeRequest

/**
 * Create Account Screen (Sign Up)
 * 
 * User registration interface matching the design:
 * - Logo and app name header
 * - Email input field
 * - Password input field with validation
 * - Confirm Password input field
 * - "Sign Up" button
 * - OR divider
 * - Google sign-in button
 * - Apple sign-in button
 * - "Already have an account? Sign In" link
 * 
 * @param onNavigateToLogin Callback when user wants to sign in
 * @param onNavigateToDashboard Callback when registration successful
 * @param onGoogleSignUp Callback for Google authentication
 * @param onAppleSignUp Callback for Apple authentication
 */
@Composable
fun CreateAccountScreen(
    onNavigateToLogin: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onGoogleSignUp: () -> Unit = {},
    onAppleSignUp: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }
    
    val auth = FirebaseAuth.getInstance()
    
    // Email validation
    fun validateEmail(): Boolean {
        return when {
            email.isEmpty() -> {
                emailError = "Email is required"
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                emailError = "Invalid email format"
                false
            }
            else -> {
                emailError = null
                true
            }
        }
    }
    
    // Password validation
    fun validatePassword(): Boolean {
        return when {
            password.isEmpty() -> {
                passwordError = "Password is required"
                false
            }
            password.length < 8 -> {
                passwordError = "Password must be at least 8 characters"
                false
            }
            !password.any { it.isDigit() } -> {
                passwordError = "Password must contain at least one number"
                false
            }
            !password.any { it.isUpperCase() } -> {
                passwordError = "Password must contain at least one uppercase letter"
                false
            }
            else -> {
                passwordError = null
                true
            }
        }
    }
    
    // Confirm password validation
    fun validateConfirmPassword(): Boolean {
        return when {
            confirmPassword.isEmpty() -> {
                confirmPasswordError = "Please confirm your password"
                false
            }
            confirmPassword != password -> {
                confirmPasswordError = "Passwords do not match"
                false
            }
            else -> {
                confirmPasswordError = null
                true
            }
        }
    }
    
    // Handle sign up
    fun handleSignUp() {
        android.util.Log.d("CreateAccountScreen", "handleSignUp called - email: $email, password length: ${password.length}")
        val isEmailValid = validateEmail()
        val isPasswordValid = validatePassword()
        val isConfirmPasswordValid = validateConfirmPassword()
        
        android.util.Log.d("CreateAccountScreen", "Validation - email: $isEmailValid, password: $isPasswordValid, confirm: $isConfirmPasswordValid")
        
        if (isEmailValid && isPasswordValid && isConfirmPasswordValid) {
            isLoading = true
            generalError = null
            
            android.util.Log.d("CreateAccountScreen", "Creating Firebase account...")
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    android.util.Log.d("CreateAccountScreen", "Account created successfully!")
                    // Set display name to email username
                    val userName = email.substringBefore("@")
                    val profileUpdates = userProfileChangeRequest {
                        displayName = userName
                    }
                    
                    authResult.user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener {
                            android.util.Log.d("CreateAccountScreen", "Profile updated, navigating to dashboard")
                            isLoading = false
                            onNavigateToDashboard()
                        }
                }
                .addOnFailureListener { exception ->
                    android.util.Log.e("CreateAccountScreen", "Sign up FAILED", exception)
                    isLoading = false
                    when (exception) {
                        is FirebaseAuthUserCollisionException -> {
                            emailError = "An account with this email already exists"
                        }
                        is FirebaseAuthWeakPasswordException -> {
                            passwordError = exception.message ?: "Password is too weak"
                        }
                        else -> {
                            generalError = exception.message ?: "Sign up failed. Please try again."
                        }
                    }
                }
        } else {
            android.util.Log.w("CreateAccountScreen", "Validation failed - showing errors")
        }
    }
    
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
            Spacer(modifier = Modifier.height(Spacing.xl))
            
            // Logo and App Name
            FSAuthHeader(
                modifier = Modifier.padding(vertical = Spacing.lg)
            )
            
            Spacer(modifier = Modifier.height(Spacing.lg))
            
            // Welcome Text
            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Text(
                text = "Sign up to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.xs)
            )
            
            Spacer(modifier = Modifier.height(Spacing.lg))
            
            // General Error Message
            if (generalError != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = CustomShapes.Card
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.md),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = generalError!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.md))
            }
            
            // Email Field
            FSEmailField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                isError = emailError != null,
                errorMessage = emailError,
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onNext = { /* Move to password field */ }
                )
            )
            
            Spacer(modifier = Modifier.height(Spacing.md))
            
            // Password Field
            FSPasswordField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = null
                    // Re-validate confirm password if it's already filled
                    if (confirmPassword.isNotEmpty() && confirmPassword != it) {
                        confirmPasswordError = "Passwords do not match"
                    } else if (confirmPassword == it) {
                        confirmPasswordError = null
                    }
                },
                label = "Password",
                placeholder = "Create a password",
                helperText = "Minimum 8 characters, 1 number, 1 uppercase",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Password",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                isError = passwordError != null,
                errorMessage = passwordError,
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onNext = { /* Move to confirm password field */ }
                ),
                imeAction = ImeAction.Next
            )
            
            Spacer(modifier = Modifier.height(Spacing.md))
            
            // Confirm Password Field
            FSPasswordField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    if (it.isNotEmpty() && it != password) {
                        confirmPasswordError = "Passwords do not match"
                    } else {
                        confirmPasswordError = null
                    }
                },
                label = "Confirm Password",
                placeholder = "Re-enter password",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Confirm Password",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                isError = confirmPasswordError != null,
                errorMessage = confirmPasswordError,
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onDone = { handleSignUp() }
                ),
                imeAction = ImeAction.Done
            )
            
            Spacer(modifier = Modifier.height(Spacing.lg))
            
            // Sign Up Button
            FSButton(
                onClick = { 
                    android.util.Log.d("CreateAccountScreen", "Sign Up button clicked!")
                    handleSignUp() 
                },
                text = "Sign Up",
                enabled = email.isNotEmpty() && 
                         password.isNotEmpty() && 
                         confirmPassword.isNotEmpty() && 
                         !isLoading,
                loading = isLoading
            )
            
            Spacer(modifier = Modifier.height(Spacing.lg))
            
            // OR Divider
            FSOrDivider()
            
            Spacer(modifier = Modifier.height(Spacing.lg))
            
            // Google Sign Up
            FSGoogleButton(
                onClick = onGoogleSignUp,
                enabled = !isLoading
            )
            
            Spacer(modifier = Modifier.height(Spacing.md))
            
            // Apple Sign Up
            FSAppleButton(
                onClick = onAppleSignUp,
                enabled = !isLoading
            )
            
            Spacer(modifier = Modifier.height(Spacing.xl))
            
            // Sign In Link
            val annotatedString = buildAnnotatedString {
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                    append("Already have an account? ")
                }
                pushStringAnnotation(tag = "SIGNIN", annotation = "signin")
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append("Sign In")
                }
                pop()
            }
            
            ClickableText(
                text = annotatedString,
                style = MaterialTheme.typography.bodyMedium,
                onClick = { offset ->
                    annotatedString.getStringAnnotations(tag = "SIGNIN", start = offset, end = offset)
                        .firstOrNull()?.let {
                            onNavigateToLogin()
                        }
                }
            )
            
            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CreateAccountScreenPreview() {
    FairSplitTheme {
        CreateAccountScreen()
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CreateAccountScreenDarkPreview() {
    FairSplitTheme(darkTheme = true) {
        CreateAccountScreen()
    }
}
