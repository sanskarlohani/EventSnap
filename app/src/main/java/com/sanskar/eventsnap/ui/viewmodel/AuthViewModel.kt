package com.sanskar.eventsnap.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val user: FirebaseUser? = null,
    val message: String? = null,
    val error: String? = null
)

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private val _uiState = MutableStateFlow(AuthUiState(user = auth.currentUser))
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email.trim(), message = null, error = null)
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password, message = null, error = null)
    }

    fun signIn() {
        val email = _uiState.value.email
        val password = _uiState.value.password

        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Email and password are required")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null, message = null)

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    user = result.user,
                    message = "Signed in successfully"
                )
            }
            .addOnFailureListener { e ->
                val msg = when (e) {
                    is FirebaseAuthInvalidUserException -> "User not found. Please sign up."
                    is FirebaseAuthInvalidCredentialsException -> "Invalid email or password."
                    else -> e.message ?: "Sign-in failed"
                }
                _uiState.value = _uiState.value.copy(isLoading = false, error = msg)
            }
    }

    fun signUp() {
        val email = _uiState.value.email
        val password = _uiState.value.password

        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Email and password are required")
            return
        }
        if (password.length < 6) {
            _uiState.value = _uiState.value.copy(error = "Password must be at least 6 characters")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null, message = null)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    user = result.user,
                    message = "Account created successfully"
                )
            }
            .addOnFailureListener { e ->
                val msg = when (e) {
                    is FirebaseAuthUserCollisionException -> "Account already exists. Please sign in."
                    else -> e.message ?: "Sign-up failed"
                }
                _uiState.value = _uiState.value.copy(isLoading = false, error = msg)
            }
    }

    fun signOut() {
        auth.signOut()
        _uiState.value = _uiState.value.copy(user = null, message = "Signed out")
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
