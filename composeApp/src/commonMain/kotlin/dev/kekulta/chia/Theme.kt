package dev.kekulta.chia

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import colorSeed
import com.materialkolor.dynamicColorScheme

@Composable
fun ChiaTheme(
    content: @Composable () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()

    val palette = remember(isDarkTheme) {
        tryGetPlatformPalette(isDarkTheme)
            ?: dynamicColorScheme(
                seedColor = colorSeed,
                isDark = isDarkTheme,
                isAmoled = false,
            )
    }

    MaterialTheme(colorScheme = palette, content = content)
}

expect fun tryGetPlatformPalette(darkTheme: Boolean): ColorScheme?
