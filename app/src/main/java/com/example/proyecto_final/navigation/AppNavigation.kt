package com.example.proyecto_final.navigation

import android.util.Log
import androidx.compose.runtime.*
import com.example.proyecto_final.interfaz.biometric.BiometricScreen
import com.example.proyecto_final.interfaz.home.HomeScreen
import com.example.proyecto_final.interfaz.login.LoginScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation() {
    val auth = remember { FirebaseAuth.getInstance() }
    var usuario by remember { mutableStateOf(auth.currentUser) }
    var biometriaValidada by remember { mutableStateOf(false) }
    var procesandoLogin by remember { mutableStateOf(false) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth -> usuario = firebaseAuth.currentUser
            Log.d( "FLUJO_APP", "AuthStateListener: ${ firebaseAuth.currentUser?.uid ?: "SIN SESIÓN" }")
            // Registro iniciado reemplazando las coroutines
            if (!procesandoLogin) {
                biometriaValidada = false
            }
        }
        auth.addAuthStateListener(listener)
        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

    when {
        // Mostrar LoginScreen si no hay sesión
        usuario == null || procesandoLogin -> {
            LoginScreen(
                onLoginStarted = {
                    procesandoLogin = true
                },
                onLoginSuccess = {
                    usuario = auth.currentUser
                    biometriaValidada = false
                    procesandoLogin = false

                    Log.d("FLUJO_APP","Login y registro finalizados correctamente")
                },
                onLoginFailed = {
                    procesandoLogin = false
                    usuario = auth.currentUser
                    Log.e("FLUJO_APP","Falló el inicio de sesión o el registro")
                }
            )
        }

        // Sesión válida sin biometría.
        !biometriaValidada -> {
            BiometricScreen(
                onBiometricSuccess = {
                    biometriaValidada = true
                }
            )
        }

        // Acceso autorizado.
        else -> {
            HomeScreen(
                usuario = usuario!!,
                onLogout = {
                    biometriaValidada = false
                    auth.signOut()
                }
            )
        }
    }
}