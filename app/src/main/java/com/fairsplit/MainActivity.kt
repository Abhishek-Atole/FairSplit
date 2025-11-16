package com.fairsplit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.fairsplit.auth.GoogleSignInHelper
import com.fairsplit.ui.navigation.NavGraph
import com.fairsplit.ui.theme.FairSplitTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private lateinit var googleSignInHelper: GoogleSignInHelper
    private var onGoogleSignInSuccess: () -> Unit = {}
    private var onGoogleSignInFailure: (Exception) -> Unit = {}
    
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        googleSignInHelper.handleSignInResult(
            data = result.data,
            onSuccess = onGoogleSignInSuccess,
            onFailure = onGoogleSignInFailure
        )
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        googleSignInHelper = GoogleSignInHelper(this)
        
        setContent {
            FairSplitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Navigation graph with auth flow
                    NavGraph(
                        onLaunchGoogleSignIn = { onSuccess, onFailure ->
                            onGoogleSignInSuccess = onSuccess
                            onGoogleSignInFailure = onFailure
                            googleSignInLauncher.launch(googleSignInHelper.getSignInIntent())
                        }
                    )
                }
            }
        }
    }
}

