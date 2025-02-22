package dev.kekulta.chia

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.dp

val LocalWindowInsets = compositionLocalOf { PaddingValues(0.dp) }

@Composable
fun navigationBarHeight() =
    LocalWindowInsets.current
        .calculateBottomPadding()
        .coerceAtLeast(16.dp)

@Composable
fun statusBarHeight() =
    LocalWindowInsets.current
        .calculateTopPadding()
        .coerceAtLeast(16.dp)
