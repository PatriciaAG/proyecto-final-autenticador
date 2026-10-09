package com.example.proyecto_final.services
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity

fun autenticarConBiometria(
    activity: FragmentActivity,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {

    val biometricManager = BiometricManager.from(activity)

    val resultado = biometricManager.canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_STRONG
    )

    if (resultado != BiometricManager.BIOMETRIC_SUCCESS) {
        onError("El dispositivo no tiene una biometría disponible.")
        return
    }

    val executor = activity.mainExecutor

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Autenticación")
        .setSubtitle("Confirma tu identidad")
        .setNegativeButtonText("Cancelar")
        .build()

    val biometricPrompt = BiometricPrompt(
        activity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {

            override fun onAuthenticationSucceeded(
                result: BiometricPrompt.AuthenticationResult
            ) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(
                errorCode: Int,
                errString: CharSequence
            ) {
                super.onAuthenticationError(errorCode, errString)
                onError(errString.toString())
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onError("La autenticación no fue reconocida.")
            }
        }
    )

    biometricPrompt.authenticate(promptInfo)
}