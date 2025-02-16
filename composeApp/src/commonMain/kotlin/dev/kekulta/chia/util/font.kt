package dev.kekulta.chia.util

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Stable
fun min(a: TextUnit, b: TextUnit): TextUnit = kotlin.math.min(a.value, b.value).sp

@Stable
fun max(a: TextUnit, b: TextUnit): TextUnit = kotlin.math.max(a.value, b.value).sp

@Composable
fun calcMaxFont(
    height: Float,
    text: String = "SAMPLE 1234567890",
    style: TextStyle = MaterialTheme.typography.displayLarge,
): TextUnit {
    val measureFontSize = 100.sp

    val paragraph = measureText(text, style.copy(fontSize = measureFontSize))

    return with(LocalDensity.current) {
        ((measureFontSize.toPx() / paragraph.height) * height).toSp()
    }
}

@Composable
fun measureText(text: String, style: TextStyle): IntSize {
    val textMeasurer = rememberTextMeasurer()
    return textMeasurer.measure(text, style).size
}

@Composable
fun calcAdaptiveFont(
    height: Float,
    width: Float,
    minFontSize: TextUnit,
    maxFontSize: TextUnit,
    text: String = "SAMPLE 1234567890",
    style: TextStyle = MaterialTheme.typography.displayLarge,
): TextUnit {
    var measureFontSize = calcMaxFont(height = height, text = text, style = style)
    val measureTextSize = measureText(text, style.copy(fontSize = measureFontSize))

    if (measureTextSize.width > width) {
        measureFontSize *= width / measureTextSize.width
    }

    return min(max(minFontSize, measureFontSize), maxFontSize)
}
