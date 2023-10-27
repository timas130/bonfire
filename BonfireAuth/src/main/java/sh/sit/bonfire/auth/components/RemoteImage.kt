package sh.sit.bonfire.auth.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.sup.dev.android.libs.image_loader.ImageLink
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Composable
fun RemoteImage(
    link: ImageLink,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
) {
    var image by remember {
        mutableStateOf<ImageBitmap?>(null)
    }

    LaunchedEffect(link) {
        try {
            image = suspendCoroutine { continuation ->
                link.setOnError {
                    continuation.resumeWithException(RuntimeException("failed to load image"))
                }.intoBitmap {
                    continuation.resume(it?.asImageBitmap())
                }
            }
        } catch (_: Exception) {
        }
    }

    val currentImage = image
    if (currentImage != null) {
        Image(currentImage, contentDescription, modifier, alignment, contentScale, alpha, colorFilter)
    } else {
        Box(modifier)
    }
}
