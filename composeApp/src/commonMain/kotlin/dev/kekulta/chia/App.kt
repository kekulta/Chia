package dev.kekulta.chia


import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.InputTransformation.Companion.transformInput
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.delete
import androidx.compose.foundation.text.input.insert
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.InterceptPlatformTextInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.kekulta.chia.util.calcAdaptiveFont
import kotlinx.coroutines.awaitCancellation
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
@Preview
fun App() {
    ChiaTheme {
        val windowInsets = WindowInsets
            .systemBars
            .asPaddingValues()

        val textState = rememberTextFieldState()
        val focusRequester = remember { FocusRequester() }

        CompositionLocalProvider(LocalWindowInsets provides windowInsets) {
            Box(modifier = Modifier.fillMaxSize().background(colorBackground)) {
                val topSheetState = rememberTopSheetState()
                val listState = rememberLazyListState()

                TopSheetEditorLayout(
                    modifier = Modifier.align(Alignment.TopCenter),
                    topSheetState = topSheetState,
                    transactionsState = listState,
                    transactionsContent = {
                        items(50) {
                            Text(
                                "El: $it",
                                modifier = Modifier.clickable { println("El: $it") })
                        }
                    },
                    editorContent = {

                        BoxWithConstraints(
                            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp)
                        ) {
                            InterceptPlatformTextInput(
                                interceptor = { _, _ -> awaitCancellation() },
                                content = {
                                    val style = MaterialTheme.typography.displayLarge.copy(
                                        color = colorOnEditor,
                                        textAlign = TextAlign.End,
                                        fontSize = calcAdaptiveFont(
                                            height = constraints.maxHeight.toFloat(),
                                            width = constraints.maxWidth.toFloat(),
                                            minFontSize = MaterialTheme.typography.displayLarge.fontSize,
                                            maxFontSize = 100.sp,
                                            text = textState.text.toString()
                                        )
                                    )

                                    BasicTextField(
                                        outputTransformation = {
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


                                            if (originalText.lastOrNull() == '.') {
                                                append(buildAnnotatedString {
                                                    withStyle(
                                                        SpanStyle(
                                                            color = Color.Red
                                                        )
                                                    ) {
                                                        append("0")
                                                    }
                                                })
                                            }
                                        },
                                        modifier = Modifier.focusRequester(focusRequester),
                                        lineLimits = TextFieldLineLimits.SingleLine,
                                        state = textState,
                                        textStyle = style,
                                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    )
                                },
                            )
                        }

                        LaunchedEffect(Unit) {
                            focusRequester.requestFocus()
                        }
                    },
                    keyboardContent = {
                        Keyboard(
                            modifier = Modifier.align(Alignment.Center)
                                .padding(bottom = LocalWindowInsets.current.calculateBottomPadding())
                                .padding(top = 6.dp)
                                .padding(bottom = 4.dp)
                                .padding(horizontal = 6.dp)
                                .aspectRatio(1f).fillMaxSize(),
                            textState = textState,
                        )
                    })
            }
        }
    }
}

@Composable
fun Keyboard(modifier: Modifier = Modifier, textState: TextFieldState) {
    val pad = 6.dp

    fun add(num: String) {
        textState.edit {
            if (num == ".") {
                val index = textState.text.indexOf('.')
                if (index != -1 && !selection.contains(index)) {
                    return
                }
            }

            if (hasSelection) {
                replace(selection.start, selection.end, num)
                placeCursorBeforeCharAt(selection.end)
            } else {
                insert(selection.start, num)
            }
        }
    }

    fun remove() {
        textState.edit {
            if (hasSelection) {
                delete(selection.start, selection.end)
            } else if (selection.start > 0) {
                delete(selection.start - 1, selection.end)
            }
        }
    }

    Box(
        modifier = modifier
    ) {
        Row {
            Column(modifier = Modifier.weight(3f)) {
                repeat(3) { row ->
                    Row(Modifier.weight(1f)) {
                        repeat(3) { col ->
                            val num = (10 - ((col + 1) + row * 3)).toString()
                            KeyboardButton(
                                modifier = Modifier.padding(pad).weight(1f),
                                type = KeyboardButtonType.DEFAULT,
                                text = num,
                                onClick = { add(num) }
                            )
                        }
                    }
                }

                Row(modifier = Modifier.weight(1f)) {
                    KeyboardButton(
                        modifier = Modifier.padding(pad).weight(2f),
                        type = KeyboardButtonType.DEFAULT,
                        text = "0"
                    )

                    KeyboardButton(
                        modifier = Modifier.padding(pad).weight(1f),
                        type = KeyboardButtonType.DEFAULT,
                        text = ".",
                        onClick = { add(".") }
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                KeyboardButton(
                    modifier = Modifier.padding(pad).weight(1f),
                    type = KeyboardButtonType.DELETE,
                    onClick = { remove() }
                )
                KeyboardButton(
                    modifier = Modifier.padding(pad).weight(3f),
                    type = KeyboardButtonType.PRIMARY
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TopSheetEditorLayout(
    topSheetState: AnchoredDraggableState<TopSheetState>,
    transactionsState: LazyListState,
    modifier: Modifier = Modifier,
    transactionsContent: LazyListScope.() -> Unit,
    editorContent: @Composable BoxScope.(NestedScrollConnection) -> Unit,
    keyboardContent: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        val halfExpandedPos = topSheetState.anchors.positionOf(TopSheetState.HalfExpanded)
            .takeIf { !it.isNaN() } ?: 0f
        val expandedPos = topSheetState.anchors.positionOf(TopSheetState.Expanded)
            .takeIf { !it.isNaN() } ?: 0f

        val navigationBarHeight = LocalWindowInsets.current.calculateBottomPadding()
            .coerceAtLeast(16.dp)

        val keyboardHeight =
            with(LocalDensity.current) { abs(expandedPos - halfExpandedPos + navigationBarHeight.toPx() + 16.dp.toPx()).toDp() }

        Box(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(keyboardHeight)
        ) {
            keyboardContent()
        }

        TopSheet(topSheetState) { conn, progress ->
            val otherVisibility = 1 - (progress / 0.5f).coerceIn(0f, 1f)
            val listVisibility = ((progress - 0.5f) / 0.5f).coerceIn(0f, 1f)

            LaunchedEffect(topSheetState.currentValue) {
                if (topSheetState.currentValue == TopSheetState.HalfExpanded) {
                    transactionsState.scrollToItem(0)
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    LazyColumn(
                        state = transactionsState,
                        reverseLayout = true,
                        modifier = Modifier.fillMaxWidth().nestedScroll(conn)
                            .graphicsLayer(alpha = listVisibility),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        transactionsContent()
                    }

                    if (listVisibility == 0f) {
                        Box(modifier = Modifier.matchParentSize().nestedScroll(conn))
                    }

                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .nestedScroll(conn)
                            .align(Alignment.BottomCenter)
                            .graphicsLayer(alpha = otherVisibility)
                    ) {
                        editorContent(conn)
                    }
                }

                Box(
                    modifier = Modifier
                        .padding(bottom = 10.dp, top = 20.dp)
                        .size(width = 36.dp, height = 8.dp)
                        .background(colorOnEditor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                )
            }
        }
    }
}

val LocalWindowInsets = compositionLocalOf { PaddingValues(0.dp) }

enum class TopSheetState { Expanded, HalfExpanded }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun rememberTopSheetState() = remember {
    AnchoredDraggableState(
        initialValue = TopSheetState.HalfExpanded,
        anchors = DraggableAnchors { },
        positionalThreshold = { it * .1f },
        snapAnimationSpec = spring(),
        velocityThreshold = { 0f },
        decayAnimationSpec = exponentialDecay(frictionMultiplier = 3f)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TopSheet(
    state: AnchoredDraggableState<TopSheetState>,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.(nestedScroll: NestedScrollConnection, progress: Float) -> Unit
) {
    val localDensity = LocalDensity.current

    val navigationBarHeightDp =
        LocalWindowInsets.current.calculateBottomPadding().coerceAtLeast(16.dp)

    var isFlinging by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter,
    ) {
        val fullHeightPx = constraints.maxHeight.toFloat()
        val halfHeightPx = fullHeightPx / 1.85f

        val expandHeightPx =
            with(localDensity) { (fullHeightPx - navigationBarHeightDp.toPx() - 16.dp.toPx()) }
        val expandHeightDp =
            with(localDensity) { expandHeightPx.toDp() }

        val anchors = remember(halfHeightPx, expandHeightPx) {
            DraggableAnchors {
                TopSheetState.HalfExpanded at halfHeightPx
                TopSheetState.Expanded at expandHeightPx
            }
        }

        state.updateAnchors(anchors)

        val conn = remember(anchors) {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    val shouldScroll = anchors.positionOf(state.currentValue) != state.offset
                    if (shouldScroll) {
                        return state.dispatchRawDelta(available.y).let { available.copy(x = 0f) }
                    }

                    return when (state.currentValue) {
                        TopSheetState.Expanded -> super.onPreScroll(available, source)
                        TopSheetState.HalfExpanded -> state.dispatchRawDelta(available.y)
                            .let { available.copy(x = 0f) }
                    }
                }

                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource
                ): Offset {
                    return when (state.currentValue) {
                        TopSheetState.Expanded -> {
                            if (!isFlinging) {
                                state.dispatchRawDelta(available.y)
                                available.copy(x = 0f)
                            } else {
                                Offset.Zero
                            }
                        }

                        TopSheetState.HalfExpanded -> super.onPostScroll(
                            consumed,
                            available,
                            source
                        )
                    }
                }

                override suspend fun onPreFling(available: Velocity): Velocity {
                    val shouldFling = anchors.positionOf(state.currentValue) != state.offset

                    if (shouldFling) {
                        return state.settle(available.y.coerceIn(-200f, 200f))
                            .let { available.copy(x = 0f) }
                    }

                    isFlinging = true
                    return super.onPreFling(available)
                }

                override suspend fun onPostFling(
                    consumed: Velocity,
                    available: Velocity
                ): Velocity {
                    isFlinging = false

                    return super.onPostFling(consumed, available)
                }
            }
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(x = 0, y = -(expandHeightPx - state.offset).roundToInt()) }
                .background(
                    colorEditor,
                    RoundedCornerShape(bottomStart = 48.dp, bottomEnd = 48.dp)
                )
                .fillMaxWidth()
                .anchoredDraggable(state, orientation = Orientation.Vertical)
                .height(expandHeightDp),

            content = {
                content(
                    conn,
                    state.progress(TopSheetState.HalfExpanded, TopSheetState.Expanded)
                )
            }
        )
    }
}
