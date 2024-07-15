package sh.sit.bonfire.formatting.compose.blocks

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sh.sit.bonfire.formatting.compose.BonfireMarkdownBlock
import sh.sit.bonfire.formatting.compose.filterNonOverlapping
import sh.sit.bonfire.formatting.core.model.spans.ListBlock
import sh.sit.bonfire.formatting.core.model.spans.ListItemBlock
import sh.sit.bonfire.formatting.core.model.spans.Span

@Composable
internal fun ListItemBlock(
    allBlocks: List<Span>,
    block: ListItemBlock,
    fullInline: AnnotatedString,
    lists: List<ListBlock>,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val innerItems = allBlocks.filter {
        it.start >= block.start && block.end >= it.end && it !== block
    }
    val innerBlocks = innerItems.filterNonOverlapping()

    val markerWidth = if (block.ordered) {
        24.sp
    } else {
        12.sp
    }

    Row(Modifier.fillMaxWidth().padding(contentPadding)) {
        Text(
            text = if (block.ordered) {
                "${block.startNumber}. "
            } else {
                "\u2022 "
            },
            modifier = Modifier
                .widthIn(min = with(LocalDensity.current) { markerWidth.toDp() })
                .alpha(0.7f),
            textAlign = TextAlign.End,
        )

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            for (innerBlock in innerBlocks) {
                BonfireMarkdownBlock(
                    block = innerBlock,
                    fullInline = fullInline,
                    allBlocks = innerItems,
                    lists = lists,
                    contentPadding = contentPadding,
                )
            }
        }
    }
}
