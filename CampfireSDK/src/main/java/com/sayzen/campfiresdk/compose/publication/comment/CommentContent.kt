package com.sayzen.campfiresdk.compose.publication.comment

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dzen.campfire.api.models.images.ImageRef
import com.dzen.campfire.api.models.publications.PublicationComment
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.compose.publication.post.pages.FixedGridPageImagesVariant
import com.sayzen.campfiresdk.compose.publication.post.pages.LegacyFormattedText
import com.sayzen.campfiresdk.compose.util.RemoteImageShimmer
import com.sayzen.campfiresdk.screens.account.stickers.SStickersView
import com.sayzen.campfiresdk.support.load
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.screens.SImageView
import sh.sit.bonfire.formatting.compose.BonfireMarkdown
import sh.sit.bonfire.formatting.core.BonfireFormatter
import sh.sit.bonfire.images.RemoteImage

@Composable
internal fun CommentQuote(
    comment: PublicationComment,
    modifier: Modifier = Modifier,
    scrollToComment: (id: PublicationComment) -> Unit = DefaultScrollToComment,
) {
    if (comment.quoteId == 0L) return // no quote

    val colors = MaterialTheme.colorScheme
    val borderWidth = with(LocalDensity.current) { 2.dp.toPx() }

    val quotedComment = PublicationComment().apply {
        id = comment.quoteId
        parentPublicationId = comment.parentPublicationId
        parentPublicationType = comment.parentPublicationType

        text = comment.quoteText.removePrefix("${comment.quoteCreatorName}: ")
        newFormatting = true

        type = when {
            comment.quoteStickerId != 0L -> PublicationComment.TYPE_STICKER
            comment.quoteImages.size == 1 -> PublicationComment.TYPE_IMAGE
            comment.quoteImages.size > 1 -> PublicationComment.TYPE_IMAGES
            else -> PublicationComment.TYPE_TEXT
        }
        images = comment.quoteImages
        image = comment.quoteImages.singleOrNull() ?: ImageRef()
        stickerId = comment.quoteStickerId
        stickerImage = comment.quoteStickerImage
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .nestedClickable {
                scrollToComment(quotedComment)
            }
            .drawBehind {
                drawRect(
                    color = colors.primaryContainer,
                    size = Size(borderWidth, size.height)
                )
            }
            .padding(start = 8.dp, top = 2.dp, bottom = 2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = comment.quoteCreatorName,
            color = colors.primary,
            style = MaterialTheme.typography.labelSmall,
        )

        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodySmall) {
            CommentText(
                comment = quotedComment,
                maxLines = 2,
                modifier = Modifier.alpha(0.6f),
            )
        }

        CommentImage(
            comment = quotedComment,
            modifier = Modifier.heightIn(max = 50.dp)
        )
    }
}

@Composable
private fun CommentText(
    text: String,
    newFormatting: Boolean,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
) {
    if (newFormatting) {
        val formattedText = remember(text) {
            BonfireFormatter.parse(text, inlineOnly = true)
        }

        BonfireMarkdown(
            text = formattedText,
            selectable = false,
            maxLines = maxLines,
            modifier = modifier
        )
    } else {
        LegacyFormattedText(
            text = text,
            maxLines = maxLines,
            modifier = modifier
        )
    }
}

@Composable
internal fun CommentText(
    comment: PublicationComment,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE
) {
    CommentText(
        text = comment.text,
        newFormatting = comment.newFormatting,
        modifier = modifier,
        maxLines = maxLines,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun CommentImage(
    comment: PublicationComment,
    modifier: Modifier = Modifier
) {
    when (comment.type) {
        PublicationComment.TYPE_TEXT -> {}
        PublicationComment.TYPE_IMAGE, PublicationComment.TYPE_GIF -> {
            // what the fuck...
            RemoteImage(
                link = comment.image,
                contentDescription = "",
                gifLink = comment.gif,
                loader = { RemoteImageShimmer() },
                matchHeightConstraintsFirst = true,
                modifier = modifier
                    .heightIn(max = 100.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .nestedClickable {
                        val image = ImageLoader.load(comment.gif.takeIf { it.isNotEmpty() } ?: comment.image)
                        Navigator.to(SImageView(image))
                    },
            )
        }
        PublicationComment.TYPE_IMAGES -> {
            FixedGridPageImagesVariant(
                images = comment.images,
                modifier = modifier
            )
        }
        PublicationComment.TYPE_STICKER -> {
            RemoteImage(
                link = comment.stickerImage,
                contentDescription = stringResource(R.string.comment_sticker_alt),
                gifLink = comment.stickerGif,
                loader = { RemoteImageShimmer() },
                modifier = modifier
                    .height(150.dp)
                    .aspectRatio(1f, matchHeightConstraintsFirst = true)
                    .clip(RoundedCornerShape(8.dp))
                    .nestedClickable {
                        SStickersView.instanceBySticker(comment.stickerId, Navigator.TO)
                    }
            )
        }
    }
}
