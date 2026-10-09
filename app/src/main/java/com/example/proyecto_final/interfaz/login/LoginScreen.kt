package com.example.proyecto_final.interfaz.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.proyecto_final.services.autenticarConBiometria
import com.example.proyecto_final.services.GoogleAuthHelper
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    //Biométrica
    val activity = context as FragmentActivity

    val scope = rememberCoroutineScope()
    val googleAuthHelper = remember {
        GoogleAuthHelper(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Mi Aplicación",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Iniciar sesión",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                // Google
                scope.launch {
                    val result = googleAuthHelper.signInWithGoogle()
                    result
                        .onSuccess { nombre ->
                            println(
                                "Login exitoso: $nombre"
                            )
                        }
                        .onFailure { error ->
                            println(
                                "Error Google: ${error.message}"
                            )
                        }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            Text("Continuar con Google")
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                // BIOMETRÍA
                autenticarConBiometria(
                    activity = activity,

                    onSuccess = {
                        println("Biometría correcta")
                        onLoginSuccess()
                    },

                    onError = { error ->
                        println("Error biométrico: $error")
                    }
                )
                      },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ingresar con huella / biometría")
        }
    }
}