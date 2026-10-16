
package com.example.proyecto_final.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.proyecto_final.MainActivity
import com.example.proyecto_final.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

class FirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "PUSH_NOTI"
        private const val CHANNEL_ID = "notificaciones_generales"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.d(TAG, "Nuevo token FCM recibido")

        // Asociar el token al usuario.
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid != null) {
            FirebaseFirestore.getInstance().collection("persona").document(uid).update("fcmToken", token).addOnSuccessListener {
                Log.d(TAG, "Token FCM actualizado en Firestore")
            }.addOnFailureListener { error ->
                Log.e(TAG, "No se pudo guardar el token", error)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "Mensaje FCM recibido")
        val titulo = message.notification?.title?: message.data["titulo"]?: "Mi Aplicación"
        val cuerpo = message.notification?.body?: message.data["mensaje"]?: "Tienes una nueva notificación"
        mostrarNotificacion(titulo, cuerpo)
    }

    private fun mostrarNotificacion(titulo: String,cuerpo: String) {
        crearCanalNotificaciones()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notificacion = NotificationCompat.Builder(
            this,
            CHANNEL_ID
        ).setSmallIcon(R.mipmap.ic_launcher).setContentTitle(titulo).setContentText(cuerpo).setStyle(
                NotificationCompat.BigTextStyle().bigText(cuerpo)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(this)
                .notify(
                    System.currentTimeMillis().toInt(),
                    notificacion
                )
        } catch (error: SecurityException) {
            Log.e(TAG, "No hay permiso para mostrar notificaciones", error)
        }
    }

    private fun crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CHANNEL_ID,
                "Notificaciones generales",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Avisos de Mi Aplicación"
            }

            val manager = getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

            manager.createNotificationChannel(canal)
        }
    }
}
