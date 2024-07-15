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
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        BonfireMarkdownContent(text = text)
    }
}

@Composable
fun BonfireMarkdownContent(
    text: FormattedText,
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
        SelectionContainer {
            Box {
                BonfireMarkdownBlock(
                    block = block,
                    fullInline = fullInline,
                    allBlocks = allBlocks,
                    lists = lists,
                    contentPadding = contentPadding,
                )
            }
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding)
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
