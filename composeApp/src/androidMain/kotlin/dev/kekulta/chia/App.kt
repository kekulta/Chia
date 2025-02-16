package dev.kekulta.chia

import android.app.Application
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme

class ChiaApp : Application() {
    override fun onCreate() {
        super.onCreate()

        instance = this
    }

    fun tryGetDynamicPalette(darkTheme: Boolean): ColorScheme? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (darkTheme) dynamicDarkColorScheme(this) else dynamicLightColorScheme(this)
        } else null

    companion object {
        private lateinit var instance: ChiaApp
        fun tryGetDynamicPalette(darkTheme: Boolean) = instance.tryGetDynamicPalette(darkTheme)
    }
}