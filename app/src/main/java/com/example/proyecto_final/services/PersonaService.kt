
package com.example.proyecto_final.services

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

class PersonaService {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    suspend fun registrarPersonaDesdeGoogle(): Result<Unit> {
        Log.d("PERSONA_FIRESTORE", "INICIO DEL REGISTRO")

        return try {
            val user = auth.currentUser
                ?: throw Exception("No existe una sesión activa en Firebase")

            val uid = user.uid

            Log.d("PERSONA_FIRESTORE", "UID: $uid")
            Log.d("PERSONA_FIRESTORE", "Correo: ${user.email}")

            val referencia = firestore
                .collection("persona")
                .document(uid)

            Log.d("PERSONA_FIRESTORE", "ANTES DE CONSULTAR DOCUMENTO")

            val documento = referencia.get().await()

            Log.d(
                "PERSONA_FIRESTORE",
                "CONSULTA COMPLETADA. Existe: ${documento.exists()}"
            )

            val datos = hashMapOf<String, Any>(
                "uid" to uid,
                "nombre" to (user.displayName ?: ""),
                "email" to (user.email ?: ""),
                "fotoUrl" to (documento.getString("fotoUrl") ?: ""),
                "fechaRegistro" to (
                        documento.getLong("fechaRegistro")
                            ?: System.currentTimeMillis()
                        )
            )

            Log.d("PERSONA_FIRESTORE", "ANTES DE GUARDAR DOCUMENTO")

            referencia.set(datos).await()

            Log.d(
                "PERSONA_FIRESTORE",
                "DOCUMENTO GUARDADO: persona/$uid"
            )

            val fotoGoogle = user.photoUrl

            if (fotoGoogle != null) {
                try {
                    Log.d("PERSONA_FIRESTORE", "INICIO DE SUBIDA DE FOTO")

                    val bytes = withContext(Dispatchers.IO) {
                        URL(fotoGoogle.toString())
                            .openStream()
                            .use { it.readBytes() }
                    }

                    val archivo = storage.reference
                        .child("imagenes/persona/$uid/perfil.jpg")

                    archivo.putBytes(bytes).await()

                    val fotoUrl = archivo.downloadUrl.await().toString()

                    referencia.update("fotoUrl", fotoUrl).await()

                    Log.d("PERSONA_FIRESTORE", "FOTO GUARDADA CORRECTAMENTE")
                } catch (e: Exception) {
                    Log.e(
                        "PERSONA_FIRESTORE",
                        "ERROR SUBIENDO FOTO",
                        e
                    )
                }
            } else {
                Log.w("PERSONA_FIRESTORE", "GOOGLE NO PROPORCIONÓ FOTO")
            }

            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(
                "PERSONA_FIRESTORE",
                "ERROR GUARDANDO PERSONA",
                e
            )

            Result.failure(e)
        }
    }
}
    /*suspend fun registrarPersonaDesdeGoogle(): Result<Unit> {
        Log.d(
            "PERSONA_FIRESTORE",
            "Entró a registrarPersonaDesdeGoogle()"
        )
        return try {
            val user = auth.currentUser?: throw Exception(
                    "No existe una sesión activa en Firebase"
                )

            val uid = user.uid

            Log.i("PERSONA_FIRESTORE", "UID: $uid")
            Log.i("PERSONA_FIRESTORE", "Correo: ${user.email}")

            val referencia = firestore.collection("persona").document(uid)
            Log.d("PERSONA_FIRESTORE", "ANTES DE CONSULTAR DOCUMENTO")
            val documento = referencia.get().await()
            Log.d("PERSONA_FIRESTORE", "DOCUMENTO CONSULTADO")
            val datos = hashMapOf<String, Any>(
                "uid" to uid,
                "nombre" to (user.displayName ?: ""),
                "email" to (user.email ?: ""),
                "fotoUrl" to (documento.getString("fotoUrl") ?: ""),
                "fechaRegistro" to (
                        documento.getLong("fechaRegistro")
                            ?: System.currentTimeMillis()
                        )
            )
            Log.d("PERSONA_FIRESTORE", "ANTES DE GUARDAR DOCUMENTO")
            //Registra en Base de Datos (FireStorage)
            referencia.set(datos).await()

            Log.d(
                "PERSONA_FIRESTORE",
                "DOCUMENTO GUARDADO: persona/$uid"
            )

            // 2. Subir la foto por separado.
            val fotoGoogle = user.photoUrl

            if (fotoGoogle != null) {
                try {
                    val bytes = withContext(Dispatchers.IO) {
                        URL(fotoGoogle.toString())
                            .openStream()
                            .use { it.readBytes() }
                    }

                    val archivo = storage.reference
                        .child("imagenes/persona/$uid/perfil.jpg")

                    //Conversión y guardado de foto
                    archivo.putBytes(bytes).await()

                    val fotoUrl = archivo.downloadUrl
                        .await()
                        .toString()
                    // Guardar URL creada para mi foto en FireStorage
                    referencia.update(
                        "fotoUrl", fotoUrl
                    ).await()

                    Log.i(
                        "PERSONA_FIRESTORE",
                        "FOTO SUBIDA Y URL ACTUALIZADA"
                    )
                } catch (e: Exception) {
                    Log.e(
                        "PERSONA_FIRESTORE",
                        "La persona se guardó, pero falló la foto",
                        e
                    )
                }
            } else {
                Log.w(
                    "PERSONA_FIRESTORE",
                    "Google no proporcionó una fotografía"
                )
            }

            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(
                "PERSONA_FIRESTORE",
                "ERROR GUARDANDO PERSONA",
                e
            )

            Result.failure(e)
        }
    }
}*/
