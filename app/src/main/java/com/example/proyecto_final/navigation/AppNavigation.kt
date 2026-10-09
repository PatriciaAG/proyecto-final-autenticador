package com.example.proyecto_final.navigation

import androidx.compose.runtime.*
import com.example.proyecto_final.interfaz.biometric.BiometricScreen
import com.example.proyecto_final.interfaz.home.HomeScreen
import com.example.proyecto_final.interfaz.login.LoginScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation() {

    val auth = remember {
        FirebaseAuth.getInstance()
    }

    var usuario by remember {
        mutableStateOf(auth.currentUser)
    }

    var biometriaValidada by remember {
        mutableStateOf(false)
    }

    DisposableEffect(auth) {

        val listener =
            FirebaseAuth.AuthStateListener { firebaseAuth ->

                usuario = firebaseAuth.currentUser

                // Si cambia el usuario,
                // volvemos a exigir biometría
                biometriaValidada = false
            }

        auth.addAuthStateListener(listener)

        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

    when {

        // ==========================================
        // NO HAY SESIÓN
        // ==========================================
        usuario == null -> {

            LoginScreen(
                onLoginSuccess = {
                    // FirebaseAuth actualizará
                    // automáticamente el estado
                }
            )
        }

        // ==========================================
        // HAY SESIÓN PERO NO PASÓ BIOMETRÍA
        // ==========================================
        !biometriaValidada -> {
            BiometricScreen(
                onBiometricSuccess = {
                    biometriaValidada = true
                }
            )
        }

        else -> {

            HomeScreen(
                usuario = usuario!!,

                onLogout = {

                    auth.signOut()

                    biometriaValidada = false
                }
            )
        }
    }
}