package dev.kekulta.chia


import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        val state = rememberTopSheetState()
        val state2 = rememberLazyListState()

        TopSheet(state) { conn, progress ->
            val otherVisibility = 1 - (progress / 0.5f).coerceIn(0f, 1f)
            val listVisibility = ((progress - 0.5f) / 0.5f).coerceIn(0f, 1f)

            LaunchedEffect(state.currentValue) {
                if (state.currentValue == TopSheetState.HalfExpanded) {
                    state2.scrollToItem(0)
                }
            }

            Column {
                Box(modifier = Modifier.weight(1f)) {
                    Hider(listVisibility) {
                        LazyColumn(
                            state = state2,
                            reverseLayout = true,
                            modifier = Modifier.fillMaxWidth().nestedScroll(conn),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            items(50) {
                                Text(
                                    "El: $it",
                                    modifier = Modifier.clickable { println("El: $it") })
                            }
                        }
                    }

                    Text(
                        "Other content", modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .graphicsLayer(alpha = otherVisibility)
                    )
                }

                Text("Handle")
            }
        }
    }
}

@Composable
fun Hider(alpha: Float, content: @Composable () -> Unit) {
    Box(modifier = Modifier.graphicsLayer(alpha = alpha)) {
        content()
        if (alpha == 0f) {
            Box(modifier = Modifier.matchParentSize().clickable { println("intercept") }) {}
        }
    }
}

val LocalWindowInsets = compositionLocalOf { PaddingValues(0.dp) }

enum class TopSheetState { Expanded, HalfExpanded }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun rememberTopSheetState() = remember {
    AnchoredDraggableState<TopSheetState>(
        initialValue = TopSheetState.HalfExpanded,
        anchors = DraggableAnchors { },
        positionalThreshold = { it * .1f },
        snapAnimationSpec = tween(300),
        velocityThreshold = { 0f },
        decayAnimationSpec = exponentialDecay(frictionMultiplier = 3f)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TopSheet(
    state: AnchoredDraggableState<TopSheetState>,
    content: @Composable BoxScope.(nestedScroll: NestedScrollConnection, progress: Float) -> Unit
) {
    val localDensity = LocalDensity.current

    val navigationBarHeight = LocalWindowInsets.current.calculateBottomPadding()
        .coerceAtLeast(16.dp)

    var isFlinging by remember { mutableStateOf(false) }

    BoxWithConstraints(
        contentAlignment = Alignment.TopCenter,
    ) {
        val fullHeight = constraints.maxHeight.toFloat()
        val halfHeight = fullHeight / 2
        val expandHeight =
            with(localDensity) { (fullHeight - navigationBarHeight.toPx() - 16.dp.toPx()) }

        val anchors = DraggableAnchors {
            TopSheetState.HalfExpanded at halfHeight
            TopSheetState.Expanded at expandHeight
        }

        state.updateAnchors(anchors)

        val height =
            with(localDensity) { expandHeight.toDp() }

        val conn = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    val shouldScroll = anchors.positionOf(state.currentValue) != state.offset
                    if (shouldScroll) {
                        return state.dispatchRawDelta(available.y).let { available }
                    }

                    return when (state.currentValue) {
                        TopSheetState.Expanded -> super.onPreScroll(available, source)
                        TopSheetState.HalfExpanded -> state.dispatchRawDelta(available.y)
                            .let { available }
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
                                available
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
                        return state.settle(available.y.coerceIn(-100f, 100f))
                            .let { available }
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
                .offset { IntOffset(x = 0, y = -(expandHeight - state.offset).roundToInt()) }
                .background(Color.DarkGray)
                .fillMaxWidth()
                .anchoredDraggable(state, orientation = Orientation.Vertical)
                .height(height),
            content = {
                content(
                    conn,
                    state.progress(TopSheetState.HalfExpanded, TopSheetState.Expanded)
                )
            }
        )
    }
}
