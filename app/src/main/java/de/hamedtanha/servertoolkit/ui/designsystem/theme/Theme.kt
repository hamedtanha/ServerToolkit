package de.hamedtanha.servertoolkit.ui.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext

@Composable
fun ServerToolkitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    visualProfile: ServerToolkitVisualProfile =
        DefaultServerToolkitVisualProfile,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        visualProfile.dynamicColorPolicy ==
            DynamicColorPolicy.AllowedWhenSupported &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }

        darkTheme -> visualProfile.darkColorScheme
        else -> visualProfile.lightColorScheme
    }

    CompositionLocalProvider(
        LocalServerToolkitSpacing provides visualProfile.spacing,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = visualProfile.typography,
            shapes = visualProfile.shapes,
            content = content,
        )
    }
}
