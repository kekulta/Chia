package dev.kekulta.chia

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.insert
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.InterceptPlatformTextInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.awaitCancellation

/**
 * Looks horrible, please rewrite that later.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EditorTextField(
    textState: TextFieldState,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null
) {
    fun produceItems(scale: Float) = buildList<@Composable (() -> Unit)> {
        add @Composable {
            Text(
                modifier = Modifier
                    .padding(start = 24.dp)
                    .padding(end = 24.dp * scale),
                text = "RUB",
                style = MaterialTheme.typography.displayLarge,
                fontSize = MaterialTheme.typography.displayLarge.fontSize * scale,
                color = colorOnEditor,
            )
        }

        add @Composable {
            BasicTextField(
                outputTransformation = DecimalSeparatedTransformation,
                modifier = Modifier
                    .run { focusRequester?.let { focusRequester(it) } ?: this }
                    .width(IntrinsicSize.Min),
                lineLimits = TextFieldLineLimits.SingleLine,
                state = textState,
                textStyle = MaterialTheme.typography.displayLarge.copy(
                    color = colorOnEditor,
                    textAlign = TextAlign.End,
                    fontSize = 100.sp * scale
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            )
        }
    }

    BoxWithConstraints(
        modifier = modifier,
    ) {
        InterceptPlatformTextInput(interceptor = { _, _ -> awaitCancellation() }) {
            val itemsToMeasure = remember(textState) { produceItems(1f) }

            MeasureViewSize(
                viewToMeasure = {
                    Row {
                        Spacer(modifier = Modifier.width(24.dp))
                        itemsToMeasure.forEach { it() }
                        Spacer(modifier = Modifier.width(24.dp))
                    }
                },
                measureConstraints = Constraints(),
            ) { size ->
                val maxWidth = with(LocalDensity.current) { constraints.maxWidth.toDp() }
                val scale =
                    (if (maxWidth >= size.width) 1f else maxWidth / size.width).coerceAtLeast(0.45f)

                val itemsToDraw = remember(textState, scale) { produceItems(scale) }

                Row(modifier = Modifier.fillMaxWidth()) {
                    LazyRow(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                    ) {
                        if (textState.text.isNotEmpty()) {
                            itemsToDraw.forEach { item { it() } }
                        }
                    }

                    if (scale > 0.45f) Spacer(modifier = Modifier.width(24.dp))
                }
            }
        }
    }
}

val DecimalSeparatedTransformation = OutputTransformation {
    val wholePart =
        originalText.takeWhile { it != '.' }.toString()

    val count = wholePart.length / 3
    val offset = (wholePart.length % 3)

    if (offset == 0) {
        repeat(count - 1) {
            insert(3 * (it + 1) + it, ",")
        }
    } else {
        repeat(count) {
            insert(offset + 3 * it + it, ",")
        }
    }
}
