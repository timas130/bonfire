package com.sayzen.campfiresdk.compose.publication.post.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dzen.campfire.api.models.publications.post.PageQuote
import sh.sit.bonfire.formatting.compose.BonfireMarkdown
import sh.sit.bonfire.formatting.core.BonfireFormatter

@Composable
internal fun PageQuoteRenderer(page: PageQuote) {
    val formattedAuthor = remember { BonfireFormatter.parse(page.author + ":", inlineOnly = true) }
    val formattedText = remember { BonfireFormatter.parse(page.text) }

    val borderColor = MaterialTheme.colorScheme.onSurface
    val borderWidth = with(LocalDensity.current) { 4.dp.toPx() }

    Card(
        Modifier
            .padding(horizontal = 12.dp)
            .fillMaxWidth()
    ) {
        Column(
            Modifier
                .drawWithContent {
                    drawContent()
                    drawRect(
                        borderColor,
                        Offset.Zero,
                        Size(borderWidth, size.height)
                    )
                }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            if (page.author.isNotBlank()) {
                CompositionLocalProvider(
                    LocalTextStyle provides MaterialTheme.typography.labelMedium
                ) {
                    BonfireMarkdown(
                        text = formattedAuthor,
                        selectable = false,
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                            .alpha(0.6f)
                    )
                }
            }
            BonfireMarkdown(text = formattedText)
        }
    }
}

@Preview
@Composable
private fun QuotePreview() {
    Surface {
        PageQuoteRenderer(page = PageQuote().apply {
            author = "Shakespeare"
            text = """
                This is a quote from Shakespeare
                # With headings
            """.trimIndent()
        })
    }
}
