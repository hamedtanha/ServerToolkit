package de.hamedtanha.servertoolkit.ui.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class ServerToolkitSpacing(
    val extraSmall: Dp,
    val small: Dp,
    val medium: Dp,
    val large: Dp,
    val extraLarge: Dp,
)

internal val DefaultServerToolkitSpacing = ServerToolkitSpacing(
    extraSmall = 4.dp,
    small = 8.dp,
    medium = 12.dp,
    large = 16.dp,
    extraLarge = 24.dp,
)

internal val LocalServerToolkitSpacing =
    staticCompositionLocalOf {
        DefaultServerToolkitSpacing
    }

object ServerToolkitDesignSystem {

    val spacing: ServerToolkitSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalServerToolkitSpacing.current
}
