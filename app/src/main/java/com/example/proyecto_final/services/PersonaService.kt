
package com.example.proyecto_final.services

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL

class PersonaService {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    companion object {
        private const val CLOUD_NAME = "dzhwqsqqk"
        private const val UPLOAD_PRESET = "persona_android"
    }

    suspend fun registrarPersonaDesdeGoogle(): Result<Unit> {
        Log.d("PERSONA_FIRESTORE", "Entró a registrarPersonaDesdeGoogle()")

        return try {
            val user = auth.currentUser?: throw Exception("No existe una sesión activa en Firebase")
            val uid = user.uid
            Log.d("PERSONA_FIRESTORE", "UID: $uid")
            Log.d("PERSONA_FIRESTORE", "Correo: ${user.email}")

            val persona = firestore.collection("persona").document(uid)

            Log.d("PERSONA_FIRESTORE", "ANTES DE CONSULTAR DOCUMENTO")
            val documento = persona.get().await()
            Log.d("PERSONA_FIRESTORE", "CONSULTA COMPLETADA. Existe: ${documento.exists()}")

            val datos = hashMapOf<String, Any>(
                "uid" to uid,
                "nombre" to (user.displayName ?: ""),
                "email" to (user.email ?: ""),
                "fotoUrl" to (documento.getString("fotoUrl") ?: ""),
                "fechaRegistro" to (documento.getLong("fechaRegistro")?: System.currentTimeMillis())
            )

            Log.d("PERSONA_FIRESTORE", "ANTES DE GUARDAR DOCUMENTO")

            persona.set(datos).await()

            Log.d("PERSONA_FIRESTORE","DOCUMENTO GUARDADO: persona/$uid")
            val fotoGoogle = user.photoUrl

            if (fotoGoogle != null) {
                try {
                    Log.d("FOTO_CLOUDINARY", "INICIO DE DESCARGA DE FOTO GOOGLE")

                    val bytes = withContext(Dispatchers.IO) {
                        URL(fotoGoogle.toString()).openStream().use { it.readBytes() }
                    }
                    require(bytes.isNotEmpty()) { "La imagen descargada está vacía" }

                    require(bytes.size <= 5 * 1024 * 1024) { "La imagen supera el límite de 5 MB" }
                    Log.d("CLOUDINARY", "SUBIENDO FOTO A CLOUDINARY")
                    val fotoUrl = subirFotoCloudinary(bytes, uid)
                    persona.update("fotoUrl", fotoUrl).await()
                    Log.d("CLOUDINARY", "FOTO GUARDADA EN CLOUDINARY Y URL ACTUALIZADA")
                    /*val archivo = storage.reference.child("imagenes/persona/$uid/perfil.jpg")
                    archivo.putBytes(bytes).await()
                    val fotoUrl = archivo.downloadUrl.await().toString()
                    persona.update("fotoUrl", fotoUrl).await()
                    Log.d("PERSONA_FIRESTORE", "FOTO GUARDADA CORRECTAMENTE")*/
                } catch (e: Exception) {
                    Log.e("FOTO_CLOUDINARY","ERROR SUBIENDO FOTO",e)
                }
            } else {
                Log.w("FOTO_CLOUDINARY", "GOOGLE NO PROPORCIONÓ FOTO")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("PERSONA_FIRESTORE","ERROR GUARDANDO PERSONA",e)
            Result.failure(e)
        }
    }

    private suspend fun subirFotoCloudinary(bytes: ByteArray,uid: String): String = withContext(Dispatchers.IO) {
        require(CLOUD_NAME != CLOUD_NAME && UPLOAD_PRESET != UPLOAD_PRESET) {
            "Configuración OK CLOUD_NAME y UPLOAD_PRESET en PersonaService"
        }
        val boundary = "----AndroidCloudinary${System.currentTimeMillis()}"
        val endpoint = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload"
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 15000
            readTimeout = 30000
            setRequestProperty("Content-Type","multipart/form-data; boundary=$boundary")
        }
        try {
            DataOutputStream(connection.outputStream).use { output ->
                fun campo(nombre: String, valor: String) {
                    output.writeBytes("--$boundary\r\n")
                    output.writeBytes("Content-Disposition: form-data; name=\"$nombre\"\r\n\r\n")
                    output.write(valor.toByteArray(Charsets.UTF_8))
                    output.writeBytes("\r\n")
                }
                campo("upload_preset", UPLOAD_PRESET)
                campo("folder", "personas")
                campo("public_id","${uid}_${System.currentTimeMillis()}")

                output.writeBytes("--$boundary\r\n")
                output.writeBytes("Content-Disposition: form-data; "+"name=\"file\"; filename=\"perfil.jpg\"\r\n")
                output.writeBytes("Content-Type: image/jpeg\r\n\r\n")
                output.write(bytes)
                output.writeBytes("\r\n--$boundary--\r\n")
                output.flush()
            }

            val status = connection.responseCode
            val responseStream = if (status in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val response = responseStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (status !in 200..299) {
                Log.e("CLOUDINARY", "Cloudinary HTTP $status: $response")
                throw Exception("Cloudinary rechazó la imagen (HTTP $status)")
            }

            val json = JSONObject(response)
            val secureUrl = json.optString("secure_url")
            require(secureUrl.startsWith("https://")) { "Cloudinary no devolvió una URL HTTPS válida" }
            secureUrl
        } finally {
            connection.disconnect()
        }
    }
}