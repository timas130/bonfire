package sh.sit.bonfire.formatting.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import sh.sit.bonfire.formatting.compose.blocks.CodeBlock
import sh.sit.bonfire.formatting.compose.blocks.ContainerBlock
import sh.sit.bonfire.formatting.compose.blocks.HeadingBlock
import sh.sit.bonfire.formatting.compose.blocks.ListItemBlock
import sh.sit.bonfire.formatting.core.model.FormattedText
import sh.sit.bonfire.formatting.core.model.spans.*

@Composable
fun BonfireMarkdown(
    text: FormattedText,
    selectable: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        BonfireMarkdownContent(text, selectable, maxLines)
    }
}

@Composable
fun BonfireMarkdownContent(
    text: FormattedText,
    selectable: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val theme = MaterialTheme.colorScheme

    val fullInline = remember(text, theme) { text.buildInlineAnnotatedString(theme) }
    val allBlocks = remember(text) {
        text.spans.filter { it.isBlockSpan() }
    }
    val blocks = remember(text) {
        allBlocks.filterNonOverlapping()
    }
    val lists = remember(text) {
        text.spans.filterIsInstance<ListBlock>()
    }

    for (block in blocks) {
        val content: @Composable () -> Unit = {
            BonfireMarkdownBlock(
                block = block,
                fullInline = fullInline,
                allBlocks = allBlocks,
                lists = lists,
                contentPadding = contentPadding,
                maxLines = maxLines,
            )
        }

        if (selectable) {
            SelectionContainer {
                Box {
                    content()
                }
            }
        } else {
            content()
        }
    }
}

@Composable
internal fun BonfireMarkdownBlock(
    block: Span,
    fullInline: AnnotatedString,
    allBlocks: List<Span>,
    lists: List<ListBlock>,
    contentPadding: PaddingValues,
    maxLines: Int = Int.MAX_VALUE,
) {
    // this statement is a war zone
    val blockText = fullInline.subSequence(
        block.start.coerceAtMost(fullInline.length),
        block.end.coerceAtLeast(block.start).coerceAtMost(fullInline.length)
    )

    when (block) {
        is ParagraphBlock -> {
            LinksClickableText(
                text = blockText,
                maxLines = maxLines,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding),
            )
        }
        is HeadingBlock -> {
            HeadingBlock(
                block = block,
                blockText = blockText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding)
            )
        }
        is CodeBlock -> {
            CodeBlock(
                block = block,
                blockText = blockText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding)
            )
        }
        is ListBlock, is QuoteBlock -> {
            ContainerBlock(
                allBlocks = allBlocks,
                block = block,
                fullInline = fullInline,
                lists = lists,
                contentPadding = contentPadding,
            )
        }
        is ListItemBlock -> {
            ListItemBlock(
                allBlocks = allBlocks,
                block = block,
                fullInline = fullInline,
                lists = lists,
                contentPadding = contentPadding,
            )
        }
        is ThematicBreakBlock -> {
            HorizontalDivider(modifier = Modifier
                .padding(contentPadding)
                .padding(vertical = 8.dp))
        }
        else -> {}
    }
}
