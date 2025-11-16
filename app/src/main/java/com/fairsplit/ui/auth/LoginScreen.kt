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
import androidx.compose.ui.unit.dp
import com.fairsplit.ui.components.*
import com.fairsplit.ui.theme.CustomShapes
import com.fairsplit.ui.theme.FairSplitTheme
import com.fairsplit.ui.theme.Spacing
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

/**
 * Login Screen
 * 
 * User login interface matching the design:
 * - Logo and app name header
 * - Email input field
 * - Password input field
 * - "Forgot Password?" link
 * - "Sign In" button
 * - OR divider
 * - Google sign-in button
 * - Apple sign-in button
 * - "Don't have an account? Sign Up" link
 * 
 * @param onNavigateToSignUp Callback when user wants to create account
 * @param onNavigateToDashboard Callback when login successful
 * @param onNavigateToForgotPassword Callback when user taps forgot password
 * @param onGoogleSignIn Callback for Google authentication
 * @param onAppleSignIn Callback for Apple authentication
 */
@Composable
fun LoginScreen(
    onNavigateToSignUp: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToForgotPassword: () -> Unit = {},
    onGoogleSignIn: () -> Unit = {},
    onAppleSignIn: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
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
            else -> {
                passwordError = null
                true
            }
        }
    }
    
    // Handle sign in
    fun handleSignIn() {
        android.util.Log.d("LoginScreen", "handleSignIn called - email: $email, password length: ${password.length}")
        val isEmailValid = validateEmail()
        val isPasswordValid = validatePassword()
        
        android.util.Log.d("LoginScreen", "Validation - email valid: $isEmailValid, password valid: $isPasswordValid")
        
        if (isEmailValid && isPasswordValid) {
            isLoading = true
            generalError = null
            
            android.util.Log.d("LoginScreen", "Attempting Firebase sign in...")
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    // Login successful, navigate to dashboard
                    android.util.Log.d("LoginScreen", "Sign in SUCCESS - user: ${it.user?.email}")
                    isLoading = false
                    onNavigateToDashboard()
                }
                .addOnFailureListener { exception ->
                    android.util.Log.e("LoginScreen", "Sign in FAILED", exception)
                    isLoading = false
                    when (exception) {
                        is FirebaseAuthInvalidUserException -> {
                            emailError = "No account found with this email"
                        }
                        is FirebaseAuthInvalidCredentialsException -> {
                            passwordError = "Incorrect password"
                        }
                        else -> {
                            generalError = exception.message ?: "Login failed. Please try again."
                        }
                    }
                }
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
            
            Spacer(modifier = Modifier.height(Spacing.xl))
            
            // Welcome Text
            Text(
                text = "Welcome Back!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Text(
                text = "Sign in to continue",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.xs)
            )
            
            Spacer(modifier = Modifier.height(Spacing.xl))
            
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
                },
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
                    onDone = { handleSignIn() }
                ),
                imeAction = ImeAction.Done
            )
            
            // Forgot Password Link
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.xs),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onNavigateToForgotPassword) {
                    Text(
                        text = "Forgot Password?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(Spacing.lg))
            
            // Sign In Button
            FSButton(
                onClick = { 
                    android.util.Log.d("LoginScreen", "Sign In button clicked!")
                    handleSignIn() 
                },
                text = "Sign In",
                enabled = email.isNotEmpty() && password.isNotEmpty() && !isLoading,
                loading = isLoading
            )
            
            Spacer(modifier = Modifier.height(Spacing.lg))
            
            // OR Divider
            FSOrDivider()
            
            Spacer(modifier = Modifier.height(Spacing.lg))
            
            // Google Sign In
            FSGoogleButton(
                onClick = onGoogleSignIn,
                enabled = !isLoading
            )
            
            Spacer(modifier = Modifier.height(Spacing.md))
            
            // Apple Sign In
            FSAppleButton(
                onClick = onAppleSignIn,
                enabled = !isLoading
            )
            
            Spacer(modifier = Modifier.height(Spacing.xl))
            
            // Sign Up Link
            val annotatedString = buildAnnotatedString {
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                    append("Don't have an account? ")
                }
                pushStringAnnotation(tag = "SIGNUP", annotation = "signup")
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append("Sign Up")
                }
                pop()
            }
            
            ClickableText(
                text = annotatedString,
                style = MaterialTheme.typography.bodyMedium,
                onClick = { offset ->
                    annotatedString.getStringAnnotations(tag = "SIGNUP", start = offset, end = offset)
                        .firstOrNull()?.let {
                            onNavigateToSignUp()
                        }
                }
            )
            
            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    FairSplitTheme {
        LoginScreen()
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenDarkPreview() {
    FairSplitTheme(darkTheme = true) {
        LoginScreen()
    }
}
