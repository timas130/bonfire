package com.sayzen.campfiresdk.compose.publication.post.pages

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.dzen.campfire.api.models.publications.post.PageCode
import sh.sit.bonfire.formatting.compose.blocks.CodeBlock
import sh.sit.bonfire.formatting.core.model.spans.CodeBlock

@Composable
internal fun PageCodeRenderer(page: PageCode) {
    CodeBlock(
        block = CodeBlock(page.language),
        blockText = AnnotatedString(page.code),
        modifier = Modifier
            .padding(horizontal = 12.dp)
    )
}
