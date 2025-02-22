package dev.kekulta.chia

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.delete
import androidx.compose.foundation.text.input.insert
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


sealed interface KeyType {
    data class Character(val value: Char) : KeyType
    data object Backspace : KeyType
    data object Enter : KeyType
}

@Composable
fun Keyboard(
    modifier: Modifier = Modifier,
    onKeyPressed: ((KeyType) -> Unit)? = null,
    textState: TextFieldState? = null,
) {
    val pad = 6.dp

    fun add(num: String) {
        textState?.edit {
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
        textState?.edit {
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
                                onClick = {
                                    onKeyPressed?.invoke(KeyType.Character(num.first()))
                                    add(num)
                                },
                            )
                        }
                    }
                }

                Row(modifier = Modifier.weight(1f)) {
                    KeyboardButton(
                        modifier = Modifier.padding(pad).weight(2f),
                        type = KeyboardButtonType.DEFAULT,
                        text = "0",
                        onClick = {
                            onKeyPressed?.invoke(KeyType.Character('0'))
                            add("0")
                        },
                    )

                    KeyboardButton(
                        modifier = Modifier.padding(pad).weight(1f),
                        type = KeyboardButtonType.DEFAULT,
                        text = ".",
                        onClick = {
                            onKeyPressed?.invoke(KeyType.Character('.'))
                            add(".")
                        },
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                KeyboardButton(
                    modifier = Modifier.padding(pad).weight(1f),
                    type = KeyboardButtonType.DELETE,
                    onClick = {
                        onKeyPressed?.invoke(KeyType.Backspace)
                        remove()
                    }
                )
                KeyboardButton(
                    modifier = Modifier.padding(pad).weight(3f),
                    type = KeyboardButtonType.PRIMARY,
                    onClick = { onKeyPressed?.invoke(KeyType.Enter) }
                )
            }
        }
    }
}

