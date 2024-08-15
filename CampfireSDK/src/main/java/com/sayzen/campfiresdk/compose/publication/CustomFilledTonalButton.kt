package com.sayzen.campfiresdk.compose.publication

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.sayzen.campfiresdk.compose.publication.comment.nestedClickableRoot

@Composable
internal fun CustomFilledTonalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: () -> Unit = {},
    longClickEnabledFlag: MutableState<Boolean> = remember { mutableStateOf(true) },
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .nestedClickableRoot(
                onClick = { onClick() },
                onLongClick = { onLongClick() },
                longClickEnabledFlag = longClickEnabledFlag,
            ),
        color = ButtonDefaults.filledTonalButtonColors().containerColor,
        contentColor = ButtonDefaults.filledTonalButtonColors().contentColor,
        shape = RoundedCornerShape(18.dp)
    ) {
        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.labelLarge) {
            Row(
                Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        }
    }
}
