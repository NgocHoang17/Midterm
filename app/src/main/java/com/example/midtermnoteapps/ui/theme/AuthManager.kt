package com.example.midtermnoteapps.ui.theme

import com.google.firebase.auth.FirebaseAuth

object AuthManager {
    private val auth = FirebaseAuth.getInstance()

    fun registerUser(email: String, password: String, callback: (Boolean) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            callback(it.isSuccessful)
        }
    }

    fun loginUser(email: String, password: String, callback: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            callback(it.isSuccessful)
        }
    }

    fun logout() {
        auth.signOut()
    }
}

