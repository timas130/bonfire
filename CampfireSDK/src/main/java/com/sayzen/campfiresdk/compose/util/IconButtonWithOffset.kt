package com.sayzen.campfiresdk.compose.util

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sayzen.campfiresdk.R

@Composable
fun IconButtonWithOffset(
    onClick: (Offset) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {
        Icon(Icons.Default.MoreVert, stringResource(R.string.post_more))
    }
) {
    val view = LocalView.current
    var moreButtonOffset by remember { mutableStateOf(Offset.Zero) }
    val localButtonCenter = with(LocalDensity.current) { Offset(14.dp.toPx(), 14.dp.toPx()) }

    IconButton(
        onClick = {
            val offset = moreButtonOffset + localButtonCenter

            onClick(offset.relativeToView(view))
        },
        modifier = modifier
            .size(28.dp)
            .onGloballyPositioned {
                moreButtonOffset = it.localToWindow(Offset.Zero)
            }
    ) {
        content()
    }
}
