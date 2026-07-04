package de.hamedtanha.servertoolkit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import de.hamedtanha.servertoolkit.navigation.AppNavHost
import de.hamedtanha.servertoolkit.ui.theme.ServerToolkitTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ServerToolkitTheme {
                AppNavHost()
            }
        }
    }
}