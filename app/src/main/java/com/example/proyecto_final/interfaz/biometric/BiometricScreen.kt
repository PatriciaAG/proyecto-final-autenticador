package com.example.proyecto_final.interfaz.biometric

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import com.example.proyecto_final.services.autenticarConBiometria

@Composable
fun BiometricScreen(
    onBiometricSuccess: () -> Unit
) {

    val context = LocalContext.current

    val activity = context as FragmentActivity

    var mensajeError by remember {
        mutableStateOf<String?>(null)
    }

    var mostrandoBiometria by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {

        mostrandoBiometria = true

        autenticarConBiometria(

            activity = activity,

            onSuccess = {

                mostrandoBiometria = false

                onBiometricSuccess()
            },

            onError = { error ->

                mostrandoBiometria = false

                mensajeError = error
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),

        horizontalAlignment = Alignment.CenterHorizontally,

        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Verificación de identidad",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text = "Confirma tu identidad utilizando tu biometría."
        )

        Spacer(
            modifier = Modifier.height(32.dp)
        )

        if (mostrandoBiometria) {

            CircularProgressIndicator()

        }

        mensajeError?.let { error ->

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Button(
                onClick = {

                    mensajeError = null

                    autenticarConBiometria(

                        activity = activity,

                        onSuccess = {
                            onBiometricSuccess()
                        },

                        onError = { nuevoError ->
                            mensajeError = nuevoError
                        }
                    )
                }
            ) {

                Text(
                    text = "Intentar nuevamente"
                )
            }
        }
    }
}