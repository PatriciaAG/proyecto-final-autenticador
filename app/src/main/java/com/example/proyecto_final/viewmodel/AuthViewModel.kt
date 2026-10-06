package com.example.proyecto_final.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    fun cerrarSesion() {
        auth.signOut()
    }

    fun usuarioActual() =
        auth.currentUser
}