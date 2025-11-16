package com.fairsplit.auth

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

/**
 * Google Sign-In Helper
 * 
 * Manages Google authentication flow for FairSplit.
 * Handles sign-in, account selection, and Firebase integration.
 */
class GoogleSignInHelper(private val context: Context) {
    
    private val auth = FirebaseAuth.getInstance()
    
    /**
     * Get configured Google Sign-In client
     * 
     * Uses default web client ID from google-services.json
     */
    fun getSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getWebClientId())
            .requestEmail()
            .requestProfile()
            .build()
        
        return GoogleSignIn.getClient(context, gso)
    }
    
    /**
     * Get sign-in intent for launching activity
     */
    fun getSignInIntent(): Intent {
        return getSignInClient().signInIntent
    }
    
    /**
     * Handle sign-in result from activity
     * 
     * @param data Intent data from activity result
     * @param onSuccess Callback when authentication successful
     * @param onFailure Callback when authentication fails
     */
    fun handleSignInResult(
        data: Intent?,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            
            if (account != null) {
                firebaseAuthWithGoogle(account, onSuccess, onFailure)
            } else {
                onFailure(Exception("Google Sign-In failed: Account is null"))
            }
        } catch (e: ApiException) {
            android.util.Log.e("GoogleSignInHelper", "Google sign-in failed", e)
            onFailure(Exception("Google Sign-In failed: ${e.message}"))
        }
    }
    
    /**
     * Authenticate with Firebase using Google credentials
     */
    private fun firebaseAuthWithGoogle(
        account: GoogleSignInAccount,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                android.util.Log.d("GoogleSignInHelper", "Firebase auth successful for user")
                onSuccess()
            }
            .addOnFailureListener { exception ->
                android.util.Log.e("GoogleSignInHelper", "Firebase auth failed", exception)
                onFailure(exception)
            }
    }
    
    /**
     * Sign out from both Google and Firebase
     */
    fun signOut(onComplete: () -> Unit = {}) {
        getSignInClient().signOut().addOnCompleteListener {
            auth.signOut()
            onComplete()
        }
    }
    
    /**
     * Get web client ID from resources
     * 
     * This is configured in res/values/google_sign_in.xml
     * You MUST set this up in Firebase Console first!
     * 
     * @throws IllegalStateException if the resource is missing
     */
    private fun getWebClientId(): String {
        return try {
            context.getString(com.fairsplit.R.string.default_web_client_id)
        } catch (e: Exception) {
            android.util.Log.e("GoogleSignInHelper", "Failed to get web client ID from resources", e)
            throw IllegalStateException(
                "Missing default_web_client_id resource - Google Sign-In cannot be configured. " +
                "Please configure Firebase in the Firebase Console and download google-services.json",
                e
            )
        }
    }
}
