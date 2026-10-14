package com.example.proyecto_final.services
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import com.example.proyecto_final.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.firebase.auth.FirebaseUser

class GoogleAuthHelper(private val context: Context) {

    private val auth = FirebaseAuth.getInstance()

    private val credentialManager = CredentialManager.create(context)

    suspend fun signInWithGoogle(): Result<FirebaseUser> {
        return try {

            // 1. Configuramos Google
            val googleOption = GetSignInWithGoogleOption.Builder(
                context.getString( R.string.default_web_client_id )
            ).build()

            // 2. Creamos la solicitud
            val request = GetCredentialRequest.Builder().addCredentialOption(googleOption).build()

            // 3. Mostramos el selector de cuentas
            val result = credentialManager.getCredential(
                context,
                request
            )

            // 4. Obtenemos la credencial
            val credential = result.credential

            // 5. Verificamos que sea Google
            if (
                credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {

                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                // 6. Convertimos el token de Google
                //    en una credencial de Firebase
                val firebaseCredential = GoogleAuthProvider.getCredential(
                        idToken,
                        null
                    )

                // 7. Iniciamos sesión en Firebase
                val authResult = auth.signInWithCredential(firebaseCredential).await()

                val user = authResult.user?: throw Exception(
                        "Firebase no devolvió un usuario autenticado"
                    )
                Result.success(user)

            } else {
                throw Exception(
                     "La credencial  no corresponde a Google"
                )

            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }
}