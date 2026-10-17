package com.example.proyecto_final

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.fragment.app.FragmentActivity
import com.example.proyecto_final.navigation.AppNavigation
import androidx.compose.ui.Modifier
import com.example.proyecto_final.interfaz.audio.ReproductorAudio
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

//FragmentActivity() {
class MainActivity : FragmentActivity(){
    private val permisoNotificaciones = registerForActivityResult( androidx.activity.result.contract.ActivityResultContracts .RequestPermission() ) { concedido ->
            android.util.Log.d( "FCM", "Permiso de notificaciones concedido: $concedido" )
        }
    override fun onCreate(savedInstanceState: Bundle?) {

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                Log.d("FCM", "Token obtenido correctamente")
                Log.d("FCM_TOKEN", token)
            }
            .addOnFailureListener { error ->
                Log.e("FCM", "No se pudo obtener el token", error)
            }
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        //FirebaseApp.initializeApp(this)
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            if (
                androidx.core.conteFCMnt.ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                permisoNotificaciones.launch(
                    android.Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }
        setContent {
            Column (
                modifier = Modifier.fillMaxSize().statusBarsPadding()
            ) {
                ReproductorAudio(modifier = Modifier.fillMaxWidth())
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    AppNavigation()
                }
            }
        }
    }
}