package com.mansi.focusway.ui.auth

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// import com.google.firebase.auth.FirebaseAuth
// import com.google.firebase.auth.FirebaseUser
// import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
// import kotlinx.coroutines.tasks.await

/**
 * Temporary stub ViewModel for authentication without Firebase
 */
class AuthViewModel : ViewModel() {
    // private val auth = FirebaseAuth.getInstance()
    // private val firestore = FirebaseFirestore.getInstance()
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    fun registerUser(context: Context, name: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                // Simulate network delay
                kotlinx.coroutines.delay(1500)
                
                // Simulate successful registration
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    currentUser = null,
                    errorMessage = null
                )
                
                Toast.makeText(
                    context,
                    "Registration successful (Firebase disabled)",
                    Toast.LENGTH_SHORT
                ).show()
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
                
                Toast.makeText(
                    context,
                    "Registration failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    fun loginUser(context: Context, email: String, password: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                // Simulate network delay
                kotlinx.coroutines.delay(1500)
                
                // Simulate successful login
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    currentUser = null,
                    errorMessage = null
                )
                
                Toast.makeText(
                    context,
                    "Login successful (Firebase disabled)",
                    Toast.LENGTH_SHORT
                ).show()
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
                
                Toast.makeText(
                    context,
                    "Login failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    fun logoutUser() {
        // auth.signOut()
        _uiState.value = AuthUiState()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val currentUser: Any? = null,  // Changed from FirebaseUser to Any
    val errorMessage: String? = null
) 