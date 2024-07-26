package com.sayzen.campfiresdk.compose.publication.post.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.dzen.campfire.api.models.publications.post.PageText
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sup.dev.java.libs.text_format.TextFormatter
import sh.sit.bonfire.formatting.compose.BonfireMarkdownContent
import sh.sit.bonfire.formatting.compose.LinksClickableText

@Composable
internal fun PageTextRenderer(page: PageText) {
    if (page.icon > 0 && page.icon < CampfireConstants.TEXT_ICONS.size) {
        val icon = painterResource(CampfireConstants.TEXT_ICONS[page.icon])

        Row(
            modifier = Modifier.padding(start = 12.dp),
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.padding(top = 4.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PageTextContent(page = page)
            }
        }
    } else {
        PageTextContent(page)
    }
}

@Composable
private fun PageTextContent(page: PageText) {
    val textAlign = when (page.align) {
        PageText.ALIGN_LEFT -> TextAlign.Start
        PageText.ALIGN_RIGHT -> TextAlign.End
        PageText.ALIGN_CENTER -> TextAlign.Center
        else -> TextAlign.Unspecified
    }

    if (page.newFormatting) {
        val textStyle = LocalTextStyle.current.merge(textAlign = textAlign)
        CompositionLocalProvider(LocalTextStyle provides textStyle) {
            BonfireMarkdownContent(
                text = page.formattedText,
                contentPadding = PaddingValues(horizontal = 12.dp),
            )
        }
    } else {
        val textStyle = if (page.size == PageText.SIZE_1) {
            MaterialTheme.typography.headlineMedium
        } else {
            LocalTextStyle.current
        }.merge(
            textAlign = textAlign
        )
        CompositionLocalProvider(LocalTextStyle provides textStyle) {
            LegacyFormattedText(
                text = page.text,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}

@Composable
fun LegacyFormattedText(
    text: String,
    maxLines: Int = Int.MAX_VALUE,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val annotatedString = remember(text) {
        val filtered = text.replace("<", "&#60;")
        val html = TextFormatter(filtered).parseHtml()
            .replace("\n", "<br />")

        val linkStyle = SpanStyle(color = colors.primary, textDecoration = TextDecoration.Underline)
        val focusLinkStyle = linkStyle.copy(background = colors.primaryContainer)

        AnnotatedString.fromHtml(
            htmlString = html,
            linkStyles = TextLinkStyles(
                style = linkStyle,
                focusedStyle = focusLinkStyle,
                hoveredStyle = focusLinkStyle,
                pressedStyle = focusLinkStyle,
            ),
        )
    }

    LinksClickableText(
        text = annotatedString,
        maxLines = maxLines,
        modifier = modifier
    )
}
