package sh.sit.bonfire.formatting.compose

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.UrlAnnotation
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import sh.sit.bonfire.formatting.core.model.FormattedText
import sh.sit.bonfire.formatting.core.model.spans.*

@OptIn(ExperimentalTextApi::class)
fun FormattedText.buildInlineAnnotatedString(theme: ColorScheme): AnnotatedString {
    val builder = AnnotatedString.Builder(text)

    for (span in spans) {
        // only inline spans here!
        when (span) {
            is BoldSpan -> builder.addStyle(
                style = SpanStyle(fontWeight = FontWeight.Bold),
                start = span.start,
                end = span.end
            )
            is CodeSpan -> builder.addStyle(
                style = SpanStyle(
                    fontFamily = FontFamily.Monospace,
                    background = theme.surfaceContainerHigh
                ),
                start = span.start,
                end = span.end
            )
            is ColorSpan -> {
                builder.addStyle(
                    style = if (span.colors.size > 1) {
                        SpanStyle(brush = Brush.linearGradient(span.colors.map { Color(it) }))
                    } else {
                        SpanStyle(color = Color(span.colors.single()))
                    },
                    start = span.start,
                    end = span.end,
                )
            }
            is ItalicSpan -> builder.addStyle(
                style = SpanStyle(fontStyle = FontStyle.Italic),
                start = span.start,
                end = span.end,
            )
            is LinkSpan -> {
                builder.addStyle(
                    style = SpanStyle(color = theme.primary, textDecoration = TextDecoration.Underline),
                    start = span.start,
                    end = span.end,
                )
                builder.addUrlAnnotation(
                    urlAnnotation = UrlAnnotation(span.link),
                    start = span.start,
                    end = span.end,
                )
            }
            is UnderlineSpan -> builder.addStyle(
                style = SpanStyle(textDecoration = TextDecoration.Underline),
                start = span.start,
                end = span.end,
            )
            is StrikethroughSpan -> builder.addStyle(
                style = SpanStyle(textDecoration = TextDecoration.LineThrough),
                start = span.start,
                end = span.end,
            )
            is SubscriptSpan -> builder.addStyle(
                style = SpanStyle(baselineShift = BaselineShift.Subscript),
                start = span.start,
                end = span.end,
            )
            is SuperscriptSpan -> builder.addStyle(
                style = SpanStyle(baselineShift = BaselineShift.Superscript),
                start = span.start,
                end = span.end,
            )
            is MarkedSpan -> builder.addStyle(
                style = SpanStyle(background = theme.primary.copy(alpha = 0.25f)),
                start = span.start,
                end = span.end,
            )
            else -> {}
        }
    }

    return builder.toAnnotatedString()
}
