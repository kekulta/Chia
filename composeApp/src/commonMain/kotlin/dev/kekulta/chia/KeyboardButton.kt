package dev.kekulta.chia

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import dev.kekulta.chia.util.calcMaxFont
import kotlin.math.min
import dev.kekulta.chia.util.min

enum class KeyboardButtonType { DEFAULT, PRIMARY, SECONDARY, TERTIARY, DELETE }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KeyboardButton(
    modifier: Modifier = Modifier,
    type: KeyboardButtonType,
    text: String? = null,
    icon: Painter? = null,
    onClick: (() -> Unit) = {},
    onLongClick: (() -> Unit) = {},
) {
    val localDensity = LocalDensity.current
    var minSize by remember { mutableStateOf(Int.MAX_VALUE.dp) }
    var minSizeFloat by remember { mutableStateOf(Int.MAX_VALUE.toFloat()) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState()
    val radius = animateDpAsState(targetValue = if (isPressed.value) 20.dp else minSize / 2)

    val color = when (type) {
        KeyboardButtonType.DEFAULT -> colorButton
        KeyboardButtonType.PRIMARY -> MaterialTheme.colorScheme.primaryContainer
        KeyboardButtonType.SECONDARY -> MaterialTheme.colorScheme.secondaryContainer
        KeyboardButtonType.TERTIARY -> MaterialTheme.colorScheme.tertiaryContainer
        KeyboardButtonType.DELETE -> MaterialTheme.colorScheme.errorContainer
    }

    val contentColor = when (type) {
        KeyboardButtonType.DEFAULT -> colorOnButton
        KeyboardButtonType.PRIMARY -> MaterialTheme.colorScheme.onPrimaryContainer
        KeyboardButtonType.SECONDARY -> MaterialTheme.colorScheme.onSecondaryContainer
        KeyboardButtonType.TERTIARY -> MaterialTheme.colorScheme.onTertiaryContainer
        KeyboardButtonType.DELETE -> MaterialTheme.colorScheme.onErrorContainer
    }

    Surface(
        tonalElevation = 10.dp,
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned {
                minSize = with(localDensity) { min(it.size.height, it.size.width).toDp() }
                minSizeFloat = min(it.size.height, it.size.width).toFloat()
            }
            .clip(RoundedCornerShape(radius.value))
    ) {
        Box(
            modifier = Modifier
                .background(color = color)
                .fillMaxSize()
                .clip(RoundedCornerShape(radius.value))
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = ripple(),
                    onClick = { onClick.invoke() },
                    onLongClick = { onLongClick.invoke() },
                ),
            contentAlignment = Alignment.Center
        ) {
            if (text !== null) {
                val fontSize: TextUnit = min(
                    calcMaxFont(minSizeFloat),
                    46.sp,
                )

                Text(
                    text = text,
                    color = contentColor,
                    style = MaterialTheme.typography.displaySmall,
                    fontSize = fontSize,
                )
            }
            if (icon !== null) {
                Icon(
                    painter = icon,
                    tint = contentColor,
                    modifier = Modifier.size(min(minSize * 0.34f, 154.dp)),
                    contentDescription = null,
                )
            }
        }
    }
}
