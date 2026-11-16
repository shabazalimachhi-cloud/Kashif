package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = MandiGreenPrimary,
    onPrimary = MandiGreenOnPrimary,
    primaryContainer = MandiGreenPrimaryContainer,
    onPrimaryContainer = MandiGreenOnPrimaryContainer,
    secondary = MandiLeafSecondary,
    onSecondary = MandiLeafOnSecondary,
    secondaryContainer = MandiLeafSecondaryContainer,
    onSecondaryContainer = MandiLeafOnSecondaryContainer,
    tertiary = MandiAmberTertiary,
    onTertiary = MandiAmberOnTertiary,
    tertiaryContainer = MandiAmberTertiaryContainer,
    onTertiaryContainer = MandiAmberOnTertiaryContainer,
    background = MandiBackgroundLight,
    onBackground = MandiOnBackgroundLight,
    surface = MandiSurfaceLight,
    onSurface = MandiOnSurfaceLight,
    surfaceVariant = MandiSurfaceVariantLight,
    onSurfaceVariant = MandiOnSurfaceVariantLight,
    outline = MandiOutlineLight,
    error = MandiErrorLight,
    errorContainer = MandiErrorContainerLight,
    onErrorContainer = MandiOnErrorContainerLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = MandiGreenPrimaryDark,
    onPrimary = MandiGreenOnPrimaryDark,
    primaryContainer = MandiGreenPrimaryContainerDark,
    onPrimaryContainer = MandiGreenOnPrimaryContainerDark,
    secondary = MandiLeafSecondaryDark,
    onSecondary = MandiLeafOnSecondaryDark,
    secondaryContainer = MandiLeafSecondaryContainerDark,
    onSecondaryContainer = MandiLeafOnSecondaryContainerDark,
    tertiary = MandiAmberTertiaryDark,
    onTertiary = MandiAmberOnTertiaryDark,
    tertiaryContainer = MandiAmberTertiaryContainerDark,
    onTertiaryContainer = MandiAmberOnTertiaryContainerDark,
    background = MandiBackgroundDark,
    onBackground = MandiOnBackgroundDark,
    surface = MandiSurfaceDark,
    onSurface = MandiOnSurfaceDark,
    surfaceVariant = MandiSurfaceVariantDark,
    onSurfaceVariant = MandiOnSurfaceVariantDark,
    outline = MandiOutlineDark,
    error = MandiErrorDark,
    errorContainer = MandiErrorContainerDark,
    onErrorContainer = MandiOnErrorContainerDark,
)

private val MandiShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
)

@Composable
fun SabziMandiKhataTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = MandiShapes,
        content = content,
    )
}
