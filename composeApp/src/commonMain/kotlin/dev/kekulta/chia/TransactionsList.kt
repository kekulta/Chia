package dev.kekulta.chia

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color


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
        item {
            Spacer(
                modifier = Modifier
                    .background(Color.Cyan)
                    .height(statusBarHeight())
            )
        }
    }
}
