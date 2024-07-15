package sh.sit.bonfire.formatting.compose.blocks

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import sh.sit.bonfire.formatting.compose.BonfireMarkdownBlock
import sh.sit.bonfire.formatting.compose.filterNonOverlapping
import sh.sit.bonfire.formatting.core.model.spans.ListBlock
import sh.sit.bonfire.formatting.core.model.spans.QuoteBlock
import sh.sit.bonfire.formatting.core.model.spans.Span

@Composable
internal fun ContainerBlock(
    allBlocks: List<Span>,
    block: Span,
    fullInline: AnnotatedString,
    lists: List<ListBlock>,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val theme = MaterialTheme.colorScheme

    val innerItems = allBlocks.filter {
        it.start >= block.start && block.end >= it.end && it !== block
    }
    val listItems = innerItems.filterNonOverlapping()

    Column(
        verticalArrangement = if (block is QuoteBlock) {
            Arrangement.spacedBy(8.dp)
        } else {
            Arrangement.spacedBy(4.dp)
        },
        modifier = if (block is QuoteBlock) {
            val strokeWidth = with(LocalDensity.current) { 2.dp.toPx() }

            Modifier
                .padding(contentPadding)
                .fillMaxWidth()
                .drawBehind {
                    drawRect(color = theme.surfaceContainerHigh)
                    drawRect(
                        color = theme.onSurface,
                        size = Size(strokeWidth, size.height)
                    )
                }
                .padding(contentPadding)
        } else {
            Modifier.fillMaxWidth()
        }
    ) {
        for (item in listItems) {
            BonfireMarkdownBlock(
                block = item,
                fullInline = fullInline,
                allBlocks = innerItems,
                lists = lists,
                contentPadding = contentPadding,
            )
        }
    }
}
