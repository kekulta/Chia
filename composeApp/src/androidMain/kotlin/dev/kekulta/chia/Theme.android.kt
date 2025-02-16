package dev.kekulta.chia

import androidx.compose.material3.ColorScheme

actual fun tryGetPlatformPalette(darkTheme: Boolean): ColorScheme? =
    ChiaApp.tryGetDynamicPalette(darkTheme)
