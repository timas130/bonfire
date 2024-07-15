package sh.sit.bonfire.images

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.dzen.campfire.api.models.images.ImageRef
import sh.sit.bonfire.auth.R
import sh.sit.schema.fragment.Ui

@Composable
fun BoxScope.RemoteImageLoader() {
    Box(
        Modifier
            .matchParentSize()
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        CircularProgressIndicator(Modifier.align(Alignment.Center))
    }
}

@Composable
fun RemoteImage(
    link: ImageRef,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    gifLink: ImageRef? = null,
    loader: @Composable BoxScope.() -> Unit = {},
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    matchHeightConstraintsFirst: Boolean = false,
) {
    var retryHash by remember { mutableIntStateOf(0) }
    var activeImage by remember { mutableStateOf(link) }

    val context = LocalContext.current

    LaunchedEffect(link, gifLink) {
        activeImage = link
        retryHash = 0
    }

    SubcomposeAsyncImage(
        model = remember(activeImage, retryHash) {
            ImageRequest.Builder(context)
                .data(activeImage.url)
                .diskCacheKey(ImageRefKeyer.key(activeImage))
                .memoryCacheKey(ImageRefKeyer.key(activeImage))
                .setParameter("retry_hash", retryHash)
                .build()
        },
        contentDescription = contentDescription,
        modifier = modifier
            .then(if (activeImage.width > 0 && activeImage.height > 0) {
                Modifier.aspectRatio(
                    ratio = activeImage.width / activeImage.height.toFloat(),
                    matchHeightConstraintsFirst = matchHeightConstraintsFirst
                )
            } else {
                Modifier
            }),
        loading = {
            loader()
        },
        error = {
            IconButton(onClick = { retryHash++ }) {
                Icon(Icons.Default.Warning, stringResource(R.string.error_image_alt))
            }
        },
        onSuccess = {
            if (gifLink == null || gifLink.isEmpty()) return@SubcomposeAsyncImage
            if (activeImage == gifLink) return@SubcomposeAsyncImage

            activeImage = gifLink
        },
        onError = {
            it.result.throwable.printStackTrace()
            if (retryHash < 3) retryHash++
        },
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter,
    )
}

fun Ui.toRef(): ImageRef = ImageRef(i.toLong(), u)
