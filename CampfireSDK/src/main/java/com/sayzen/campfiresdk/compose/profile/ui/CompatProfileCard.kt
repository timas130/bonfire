package com.sayzen.campfiresdk.compose.profile.ui

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sayzen.campfiresdk.R
import com.sup.dev.android.tools.ToolsResources

@Composable
fun CompatProfileCard(content: @Composable ColumnScope.() -> Unit) {
    val context = LocalContext.current

    // we do a little bit of trickery
    Card(
        colors = CardDefaults.cardColors().copy(
            containerColor = Color(remember { ToolsResources.getColorAttr(context, R.attr.colorSurface) }),
            contentColor = Color(remember { ToolsResources.getColorAttr(context, R.attr.colorOnSurface) }),
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .padding(top = 6.dp),
        content = content,
    )
}
