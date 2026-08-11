package de.hamedtanha.servertoolkit.ui.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

enum class DynamicColorPolicy {
    Disabled,
    AllowedWhenSupported,
}

data class ServerToolkitVisualProfile(
    val lightColorScheme: ColorScheme,
    val darkColorScheme: ColorScheme,
    val typography: Typography,
    val shapes: Shapes,
    val spacing: ServerToolkitSpacing,
    val dynamicColorPolicy: DynamicColorPolicy,
)

private val DefaultDarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
)

private val DefaultLightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
)

internal val DefaultServerToolkitVisualProfile =
    ServerToolkitVisualProfile(
        lightColorScheme = DefaultLightColorScheme,
        darkColorScheme = DefaultDarkColorScheme,
        typography = ServerToolkitTypography,
        shapes = ServerToolkitShapes,
        spacing = DefaultServerToolkitSpacing,
        dynamicColorPolicy = DynamicColorPolicy.AllowedWhenSupported,
    )
