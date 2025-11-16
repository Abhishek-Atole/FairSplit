package com.fairsplit.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Profile ViewModel
 * Handles profile updates and password changes
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    private val _passwordChangeState = MutableStateFlow<PasswordChangeState>(PasswordChangeState.Idle)
    val passwordChangeState: StateFlow<PasswordChangeState> = _passwordChangeState.asStateFlow()

    /**
     * Update user's display name
     */
    fun updateDisplayName(newName: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _profileState.value = ProfileState.Error("User not authenticated")
            return
        }

        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            
            try {
                val profileUpdates = userProfileChangeRequest {
                    displayName = newName
                }
                
                currentUser.updateProfile(profileUpdates).await()
                _profileState.value = ProfileState.Success("Profile updated successfully")
                
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error(e.message ?: "Failed to update profile")
            }
        }
    }

    /**
     * Change user's password
     */
    fun changePassword(currentPassword: String, newPassword: String) {
        val currentUser = auth.currentUser
        if (currentUser == null || currentUser.email == null) {
            _passwordChangeState.value = PasswordChangeState.Error("User not authenticated")
            return
        }

        viewModelScope.launch {
            _passwordChangeState.value = PasswordChangeState.Loading
            
            try {
                // Re-authenticate user first
                val credential = EmailAuthProvider.getCredential(currentUser.email!!, currentPassword)
                currentUser.reauthenticate(credential).await()
                
                // Update password
                currentUser.updatePassword(newPassword).await()
                _passwordChangeState.value = PasswordChangeState.Success("Password changed successfully")
                
            } catch (e: Exception) {
                when {
                    e.message?.contains("password is invalid") == true -> {
                        _passwordChangeState.value = PasswordChangeState.Error("Current password is incorrect")
                    }
                    e.message?.contains("network") == true -> {
                        _passwordChangeState.value = PasswordChangeState.Error("Network error. Please check your connection")
                    }
                    else -> {
                        _passwordChangeState.value = PasswordChangeState.Error(e.message ?: "Failed to change password")
                    }
                }
            }
        }
    }

    fun resetProfileState() {
        _profileState.value = ProfileState.Idle
    }

    fun resetPasswordChangeState() {
        _passwordChangeState.value = PasswordChangeState.Idle
    }
}

sealed class ProfileState {
    object Idle : ProfileState()
    object Loading : ProfileState()
    data class Success(val message: String) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

sealed class PasswordChangeState {
    object Idle : PasswordChangeState()
    object Loading : PasswordChangeState()
    data class Success(val message: String) : PasswordChangeState()
    data class Error(val message: String) : PasswordChangeState()
}
