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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.InterceptPlatformTextInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.awaitCancellation
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.abs
import kotlin.math.roundToInt


@Composable
fun TransactionsList(
    modifier: Modifier = Modifier,
    scrollableState: LazyListState = rememberLazyListState()
) {
    LazyColumn(
        state = scrollableState,
        reverseLayout = true,
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        items(50) {
            Text(
                "El: $it",
                modifier = Modifier.clickable { println("El: $it") })
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


@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
@Preview
fun App() {
    ChiaTheme {
        val windowInsets = WindowInsets
            .systemBars
            .asPaddingValues()

        val textState = rememberTextFieldState()

        CompositionLocalProvider(LocalWindowInsets provides windowInsets) {
            Box(modifier = Modifier.fillMaxSize().background(colorBackground)) {
                val topSheetState = rememberTopSheetState()
                val listState = rememberLazyListState()

                TopSheetEditorLayout(
                    modifier = Modifier.align(Alignment.TopCenter),
                    topSheetState = topSheetState,
                    transactionsState = listState,
                    transactionsContent = { conn ->
                        TransactionsList(
                            Modifier
                                .fillMaxWidth()
                                .nestedScroll(conn)
                        )
                    },
                    editorContent = {
                        EditorTextField(
                            textState = textState,
                            modifier = Modifier.align(Alignment.BottomEnd)
                        )
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
fun MeasureViewSize(
    viewToMeasure: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    measureConstraints: Constraints? = null,
    content: @Composable (DpSize) -> Unit,
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val measuredSize = subcompose("viewToMeasure") {
            viewToMeasure()
        }[0].measure(measureConstraints ?: constraints)
            .let {
                DpSize(
                    width = it.width.toDp(),
                    height = it.height.toDp()
                )
            }

        val contentPlaceable = subcompose("content") {
            content(measuredSize)
        }.firstOrNull()?.measure(constraints)

        layout(contentPlaceable?.width ?: 0, contentPlaceable?.height ?: 0) {
            contentPlaceable?.place(0, 0)
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EditorTextField(textState: TextFieldState, modifier: Modifier = Modifier) {
    val focusRequester = remember { FocusRequester() }

    fun produceItems(scale: Float) = buildList<@Composable (() -> Unit)> {
        add @Composable {
            Text(
                modifier = Modifier.padding(start = 24.dp).padding(end = 24.dp * scale),
                text = "RUB",
                style = MaterialTheme.typography.displayLarge,
                fontSize = MaterialTheme.typography.displayLarge.fontSize * scale,
                color = colorOnEditor,
            )
        }

        add @Composable {
            BasicTextField(
                outputTransformation = DecimalSeparatedTransformation,
                modifier = Modifier.focusRequester(focusRequester)
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

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TopSheetEditorLayout(
    topSheetState: AnchoredDraggableState<TopSheetState>,
    transactionsState: LazyListState,
    modifier: Modifier = Modifier,
    transactionsContent: @Composable (NestedScrollConnection) -> Unit,
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
                    Box(modifier = Modifier.alpha(listVisibility)) {
                        transactionsContent(conn)
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
