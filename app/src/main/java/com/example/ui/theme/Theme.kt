package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val DarkColorScheme =
  darkColorScheme(
    primary = CoralLight,
    onPrimary = Color(0xFF3E0A00),
    primaryContainer = Color(0xFF6B2212),
    onPrimaryContainer = Color(0xFFFFDAD2),
    secondary = Color(0xFFAEC6FF),
    onSecondary = Color(0xFF0A1D42),
    secondaryContainer = DeepNavyVariant,
    onSecondaryContainer = Color(0xFFD8E3FF),
    tertiary = MintLight,
    onTertiary = Color(0xFF003732),
    tertiaryContainer = Color(0xFF0B4F48),
    onTertiaryContainer = Color(0xFFB5F5EC),
    background = DeepNavyBg,
    onBackground = Color(0xFFF3F4F6),
    surface = DeepNavySurface,
    onSurface = Color(0xFFF3F4F6),
    surfaceVariant = DeepNavyVariant,
    onSurfaceVariant = Color(0xFFB6C2D9),
    outline = DeepNavyOutline,
    error = ErrorRedLight,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = CoralPrimary,
    onPrimary = Color.White,
    primaryContainer = CoralContainerLight,
    onPrimaryContainer = OnCoralContainerLight,
    secondary = IndigoInk,
    onSecondary = Color.White,
    secondaryContainer = IndigoContainerLight,
    onSecondaryContainer = IndigoInk,
    tertiary = MintAccent,
    onTertiary = Color.White,
    tertiaryContainer = MintContainerLight,
    onTertiaryContainer = OnMintContainerLight,
    background = WarmIvory,
    onBackground = IndigoInk,
    surface = WarmSurface,
    onSurface = IndigoInk,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = IndigoMuted,
    outline = WarmOutline,
    error = ErrorRed,
  )

val AddUpShapes =
  Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
  )

@Composable
fun AddUpTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = AddUpTypography,
    shapes = AddUpShapes,
    content = content,
  )
}
