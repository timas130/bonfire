package sh.sit.bonfire.formatting.compose.blocks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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

    Row(
        Modifier
            .fillMaxWidth()
            .padding(contentPadding)
    ) {
        if (block.ordered) {
            Text(
                text = "${block.startNumber}. ",
                modifier = Modifier
                    .widthIn(min = 24.dp)
                    .alpha(0.7f),
                textAlign = TextAlign.End,
            )
        } else {
            Box(
                Modifier
                    .width(24.dp)
                    .height(with(LocalDensity.current) { LocalTextStyle.current.lineHeight.toDp() })
                    .alpha(0.7f)
            ) {
                Box(
                    Modifier
                        .align(Alignment.Center)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(LocalContentColor.current)
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            for (innerBlock in innerBlocks) {
                BonfireMarkdownBlock(
                    block = innerBlock,
                    fullInline = fullInline,
                    allBlocks = innerItems,
                    lists = lists,
                    contentPadding = contentPadding.copy(start = 0.dp),
                )
            }
        }
    }
}

@Composable
fun PaddingValues.copy(
    start: Dp? = null,
    top: Dp? = null,
    end: Dp? = null,
    bottom: Dp? = null
): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current
    return PaddingValues(
        start = start ?: this.calculateStartPadding(layoutDirection),
        top = top ?: this.calculateTopPadding(),
        end = end ?: this.calculateEndPadding(layoutDirection),
        bottom = bottom ?: this.calculateBottomPadding(),
    )
}

