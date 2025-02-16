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
//
//@Composable
//fun calcFontHeight(
//    text: String = "SAMPLE 1234567890",
//    style: TextStyle = MaterialTheme.typography.displayLarge,
//): Dp {
//    val intrinsics = ParagraphIntrinsics(
//        text = text,
//        style = style,
//        density = LocalDensity.current,
//        fontFamilyResolver = createFontFamilyResolver(LocalContext.current)
//    )
//
//    val paragraph = Paragraph(
//        paragraphIntrinsics = intrinsics,
//        constraints = Constraints(maxWidth = ceil(1000f).toInt()),
//        maxLines = 1,
//        ellipsis = false
//    )
//
//    return with(LocalDensity.current) {
//        paragraph.height.toDp()
//    }
//}
//
//@Composable
//fun calcAdaptiveFont(
//    height: Float,
//    width: Float,
//    minFontSize: TextUnit,
//    maxFontSize: TextUnit,
//    text: String = "SAMPLE 1234567890",
//    style: TextStyle = MaterialTheme.typography.displayLarge,
//): TextUnit {
//    var measureFontSize = calcMaxFont(height = height, text = text, style = style)
//
//    var intrinsics = ParagraphIntrinsics(
//        text = text,
//        style = style.copy(fontSize = measureFontSize),
//        density = LocalDensity.current,
//        fontFamilyResolver = createFontFamilyResolver(LocalContext.current)
//    )
//
//    while (intrinsics.maxIntrinsicWidth > width && measureFontSize > minFontSize) {
//        measureFontSize *= 0.9f
//        intrinsics = ParagraphIntrinsics(
//            text = text,
//            style = style.copy(fontSize = measureFontSize),
//            density = LocalDensity.current,
//            fontFamilyResolver = createFontFamilyResolver(LocalContext.current)
//        )
//    }
//
//    return min(max(minFontSize, measureFontSize), maxFontSize)
//}
