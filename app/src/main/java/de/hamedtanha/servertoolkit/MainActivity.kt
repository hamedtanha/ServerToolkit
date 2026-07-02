package de.hamedtanha.servertoolkit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import de.hamedtanha.servertoolkit.feature.dashboard.presentation.screen.DashboardScreen
import de.hamedtanha.servertoolkit.ui.theme.ServerToolkitTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ServerToolkitTheme {
                DashboardScreen()
            }
        }
    }
}