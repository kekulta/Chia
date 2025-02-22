package dev.kekulta.chia

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
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.roundToInt

@Composable
fun Tag(
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .height(44.dp)
            .clip(CircleShape)
            .clickable { onClick() }
            .then(modifier)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = value,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
            )
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


        val focusRequester = remember { FocusRequester() }
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
                    editorContent = { conn ->
                        Column(
                            modifier = Modifier.matchParentSize(),
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("FIRST")
                            EditorTextField(
                                textState = textState,
                                focusRequester = focusRequester,
                            )
                            LazyRow(
                                modifier = Modifier.nestedScroll(conn),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                            ) {
                                items(7) {
                                    Tag("El #$it")
                                }
                                item {
                                    Spacer(modifier = Modifier.width(24.dp))
                                }
                                item {
                                    Tag("New")
                                }
                            }
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
                            onKeyPressed = { focusRequester.requestFocus() },
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
