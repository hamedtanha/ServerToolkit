package de.hamedtanha.servertoolkit.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import de.hamedtanha.servertoolkit.ui.screens.HomeScreen
import de.hamedtanha.servertoolkit.ui.theme.ServerToolkitTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ServerToolkitTheme {
                HomeScreen()
            }
        }
    }
}