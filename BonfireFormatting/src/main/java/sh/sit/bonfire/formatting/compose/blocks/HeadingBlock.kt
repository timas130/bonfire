package sh.sit.bonfire.formatting.compose.blocks

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.sp
import sh.sit.bonfire.formatting.compose.LinksClickableText
import sh.sit.bonfire.formatting.core.model.spans.HeadingBlock

@Composable
internal fun HeadingBlock(
    block: HeadingBlock,
    blockText: AnnotatedString,
    modifier: Modifier = Modifier,
) {
    val typography = MaterialTheme.typography

    LinksClickableText(
        text = blockText,
        style = when (block.level) {
            1 -> typography.headlineLarge
            2 -> typography.headlineMedium
            3 -> typography.headlineSmall
            4 -> typography.titleLarge
            5 -> typography.titleMedium.copy(fontSize = 19.sp)
            6 -> typography.titleSmall.copy(fontSize = 11.sp)
            else -> typography.titleLarge
        }.merge(
            textAlign = LocalTextStyle.current.textAlign,
        ),
        modifier = modifier
    )
}
