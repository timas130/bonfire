package sh.sit.bonfire.formatting.compose

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import sh.sit.bonfire.formatting.R

@Composable
fun LinksClickableText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    onClick: ((Int) -> Boolean)? = null,
    style: TextStyle = LocalTextStyle.current,
) {
    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
    val realOnClick = onClick ?: createTextOnClick(text, LocalContext.current)
    val clickModifier = Modifier.pointerInput(realOnClick) {
        awaitEachGesture {
            val down = awaitFirstDown()
            val up = withTimeout(viewConfiguration.longPressTimeoutMillis) {
                waitForUpOrCancellation()
            }

            if (up == null) {
                return@awaitEachGesture
            }

            layoutResult.value?.let {
                val consume = realOnClick(it.getOffsetForPosition(up.position))
                if (consume) {
                    down.consume()
                    up.consume()
                }
            }
        }
    }

    Text(
        text = text,
        modifier = modifier then clickModifier,
        onTextLayout = { layoutResult.value = it },
        style = style,
    )
}

@OptIn(ExperimentalTextApi::class)
internal fun createTextOnClick(
    blockText: AnnotatedString,
    context: Context,
) = { pos: Int ->
    val span = blockText.getUrlAnnotations(pos, pos + 1).firstOrNull()
    val url = span?.item?.url
    if (url != null) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                context,
                context.getString(R.string.link_open_fail),
                Toast.LENGTH_LONG
            ).show()
        }
        true
    } else {
        false
    }
}
