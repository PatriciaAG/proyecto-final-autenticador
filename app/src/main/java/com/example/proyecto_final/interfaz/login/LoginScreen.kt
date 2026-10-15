
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
import com.example.proyecto_final.services.PersonaService
import kotlinx.coroutines.launch
import android.util.Log
@Composable
fun LoginScreen(
    onLoginStarted: () -> Unit,
    onLoginSuccess: () -> Unit,
    onLoginFailed: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val googleAuthHelper = remember { GoogleAuthHelper(context) }
    val personaService = remember { PersonaService() }
    var cargando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<String?>(null) }

    // Se utiliza para iniciar el flujo de Google desde una corrutina
    var iniciarGoogle by remember { mutableIntStateOf(0) }

    LaunchedEffect(iniciarGoogle) {
        if (iniciarGoogle == 0) return@LaunchedEffect

        cargando = true
        mensaje = null
        onLoginStarted()

        try {
            Log.d("FLUJO_LOGIN", "Iniciando autenticación con Google")
            val usuarioGoogle = googleAuthHelper.signInWithGoogle().getOrThrow()
            Log.d( "FLUJO_LOGIN", "Firebase Auth correcto: ${usuarioGoogle.uid}" )
            personaService.registrarPersonaDesdeGoogle().getOrThrow()
            Log.d("FLUJO_LOGIN","Persona guardada correctamente")
            onLoginSuccess()
        } catch (e: Exception) {
            Log.e("LOGIN_FIREBASE","Error durante el inicio de sesión o registro",e)
            mensaje = e.message?: "No se pudo completar el inicio de sesión"
            onLoginFailed()
        } finally {
            cargando = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
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
        // INICIO DE SESIÓN CON GOOGLE
        Button(
            onClick = {
                Log.d("FLUJO_LOGIN","Se pulsó el botón Continuar con Google")
                iniciarGoogle++
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !cargando
        ) {
            Text("Continuar con Google")
        }
        Spacer(modifier = Modifier.height(24.dp))
        // INICIO CON BIOMETRÍA
        Button(
            onClick = {
                mensaje = null
                if (activity == null) {
                    mensaje = "No se pudo obtener la actividad de Android."
                } else {
                    cargando = true
                    autenticarConBiometria(
                        activity = activity,
                        onSuccess = {
                            cargando = false
                            // La huella debe desbloquear una sesión Firebase previamente válida por el inicio de sesión con Google en este caso
                            onLoginSuccess()
                        },
                        onError = { error ->
                            cargando = false
                            mensaje = "Error biométrico: $error"
                            Log.d("BIOMETRIA", "Error biométrico: $error" )
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !cargando
        ) {
            Text("Ingresar con huella / biometría")
        }

        // INDICADOR DE CARGA
        if (cargando) {
            Spacer(modifier = Modifier.height(20.dp))
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(8.dp))
            Text("Autenticando y guardando datos...")
        }

        // MENSAJES DE ERROR
        mensaje?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
