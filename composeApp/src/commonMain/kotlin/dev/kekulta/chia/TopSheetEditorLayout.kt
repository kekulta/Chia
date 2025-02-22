package dev.kekulta.chia

import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.abs

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
        val density = LocalDensity.current
        val halfExpandedPos = topSheetState.anchors.positionOf(TopSheetState.HalfExpanded)
            .takeIf { !it.isNaN() } ?: 0f
        val expandedPos = topSheetState.anchors.positionOf(TopSheetState.Expanded)
            .takeIf { !it.isNaN() } ?: 0f

        val statusBarHeight = statusBarHeight()
        val navigationBarHeight = navigationBarHeight()

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

                    val height = remember(topSheetState.anchors) {
                        with(density) {
                            topSheetState.anchors.positionOf(TopSheetState.HalfExpanded)
                                .takeIf { !it.isNaN() }
                                ?.toDp()
                                ?.let { it - 38.dp }
                                ?.let { it - statusBarHeight }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .run { height?.let { height(it) } ?: matchParentSize() }
                            .fillMaxWidth()
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

        Spacer(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .background(colorEditor.copy(alpha = 0.7f))
                .fillMaxWidth()
                .height(statusBarHeight())
        )
    }
}

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
