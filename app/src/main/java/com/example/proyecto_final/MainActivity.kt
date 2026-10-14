package com.example.proyecto_final

import android.os.Bundle
import androidx.activity.ComponentActivity
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
//FragmentActivity() {
class MainActivity : FragmentActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        //FirebaseApp.initializeApp(this)
        setContent {
            Column (
                modifier = Modifier.fillMaxSize().statusBarsPadding()
            ) {

                ReproductorAudio(
                    modifier = Modifier.fillMaxWidth()
                )

                // Navegación debajo del reproductor
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    AppNavigation()
                }
            }
        }
    }
}