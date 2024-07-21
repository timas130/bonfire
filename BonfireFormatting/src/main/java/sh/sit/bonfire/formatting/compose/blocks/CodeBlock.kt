package sh.sit.bonfire.formatting.compose.blocks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import io.github.kbiakov.codeview.highlight.ColorTheme
import io.github.kbiakov.codeview.highlight.ColorThemeData
import io.github.kbiakov.codeview.highlight.prettify.PrettifyParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sh.sit.bonfire.formatting.core.model.spans.CodeBlock

private fun ColorThemeData.buildColorsMap(): HashMap<String, Int> {
    return hashMapOf(
        "typ" to syntaxColors.type,
        "kwd" to syntaxColors.keyword,
        "lit" to syntaxColors.literal,
        "com" to syntaxColors.comment,
        "str" to syntaxColors.string,
        "pun" to syntaxColors.punctuation,
        "pln" to syntaxColors.plain,
        "tag" to syntaxColors.tag,
        "dec" to syntaxColors.declaration,
        "src" to syntaxColors.plain,
        "atn" to syntaxColors.attrName,
        "atv" to syntaxColors.attrValue,
        "nocode" to syntaxColors.plain
    )
}

val theme = ColorTheme.MONOKAI.theme()

private fun CodeBlock.buildAnnotatedString(content: String): AnnotatedString {
    val builder = AnnotatedString.Builder(content)
    val parsed = PrettifyParser().parse(language, content)

    val themeMap = theme.buildColorsMap()

    for (span in parsed) {
        for (styleKey in span.styleKeys) {
            builder.addStyle(
                style = SpanStyle(
                    color = Color(
                        (themeMap[styleKey] ?: theme.syntaxColors.plain) or 0xFF000000.toInt()
                    )
                ),
                start = span.offset,
                end = span.offset + span.length,
            )
        }
    }

    return builder.toAnnotatedString()
}

@Composable
fun CodeBlock(
    block: CodeBlock,
    blockText: AnnotatedString,
    modifier: Modifier = Modifier,
) {
    var annotatedString by remember { mutableStateOf(blockText) }

    LaunchedEffect(blockText) {
        annotatedString = withContext(Dispatchers.IO) {
            block.buildAnnotatedString(blockText.toString())
        }
    }

    Text(
        text = annotatedString,
        style = TextStyle.Default.copy(fontFamily = FontFamily.Monospace),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(theme.bgContent or 0xFF000000.toInt()))
            .padding(8.dp)
    )
}
