package com.sofa.nerdrunning.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = DarkBlue,
    primaryVariant = Purple700,
    secondary = Teal200,
    background = GrayC8,
    onBackground = DarkBlue,
    onSurface = DarkBlue,
    onPrimary = Color.White,
)

private val LightColorPalette = lightColors(
    primary = DarkBlue,
    primaryVariant = Purple700,
    secondary = Teal200,
    background = GrayC8,
    onBackground = DarkBlue,
    onSurface = DarkBlue,

    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)

@Composable
fun NerdRunningTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}