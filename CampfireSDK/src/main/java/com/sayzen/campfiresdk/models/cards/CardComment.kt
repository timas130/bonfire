package com.sayzen.campfiresdk.models.cards

import android.annotation.SuppressLint
import android.util.LongSparseArray
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.notifications.comments.NotificationComment
import com.dzen.campfire.api.models.notifications.comments.NotificationCommentAnswer
import com.dzen.campfire.api.models.notifications.publications.NotificationMention
import com.dzen.campfire.api.models.notifications.publications.NotificationPublicationReaction
import com.dzen.campfire.api.models.publications.PublicationComment
import com.posthog.PostHog
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.compose.publication.comment.CardCommentProxy
import com.sayzen.campfiresdk.controllers.*
import com.sayzen.campfiresdk.models.events.account.EventAccountAddToBlackList
import com.sayzen.campfiresdk.models.events.account.EventAccountRemoveFromBlackList
import com.sayzen.campfiresdk.models.events.publications.EventCommentChange
import com.sayzen.campfiresdk.models.events.publications.EventPublicationDeepBlockRestore
import com.sayzen.campfiresdk.models.splashs.SplashComment
import com.sayzen.campfiresdk.screens.account.stickers.SStickersView
import com.sayzen.campfiresdk.screens.reports.SReports
import com.sayzen.campfiresdk.support.load
import com.sayzen.campfiresdk.support.loadGif
import com.sayzen.campfiresdk.views.ViewKarma
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.screens.SImageView
import com.sup.dev.android.views.views.*
import com.sup.dev.android.views.views.layouts.LayoutMaxSizes
import com.sup.dev.java.classes.collections.HashList
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.libs.text_format.TextFormatter
import com.sup.dev.java.tools.ToolsDate
import sh.sit.bonfire.formatting.BonfireMarkdown

open class CardComment(
    private val proxy: CardCommentProxy,
    publication: PublicationComment,
    dividers: Boolean,
    private val miniSize: Boolean,
    private val onClick: ((PublicationComment) -> Boolean)? = null,
    private val onQuote: ((PublicationComment) -> Unit)? = null,
    private val onGoTo: ((Long) -> Unit)? = null
) : CardPublication(
    if (miniSize) R.layout.card_comment_mini else if (dividers) R.layout.card_comment else R.layout.card_comment_card,
    publication
) {

    companion object {

        private val viewCash = HashList<String, View>()

        init {
            SupAndroid.addOnLowMemory { viewCash.clear() }
        }

        fun putViewToCash(key: String, view: View) {
            viewCash.add(key, view)
        }

        fun getViewFromCash(key: String): View? {
            return viewCash.removeOne(key)
        }
    }

    private val eventBus = EventBus
        .subscribe(EventCommentChange::class) { e: EventCommentChange -> this.onCommentChange(e) }
        .subscribe(EventPublicationDeepBlockRestore::class) { onEventPublicationDeepBlockRestore(it) }
        .subscribe(EventAccountRemoveFromBlackList::class) {
            if (publication.creator.id == it.accountId) {
                publication.blacklisted = false
                update()
            }
        }
        .subscribe(EventAccountAddToBlackList::class) {
            if (publication.creator.id == it.accountId) {
                publication.blacklisted = true
                update()
            }
        }

    var maxTextSize = Integer.MAX_VALUE
    var quoteEnabled = true

    init {
        flashViewId = R.id.vFlashView
    }

    //
    //  Bind
    //

    @Suppress("DEPRECATION")
    @SuppressLint("SetTextI18n")
    override fun bindView(view: View) {
        if (updateBlacklisted(view)) {
            view.findViewById<ViewSwipe?>(R.id.vSwipe)?.swipeEnabled = false
            return
        } else {
            view.findViewById<ViewSwipe?>(R.id.vSwipe)?.swipeEnabled = true
        }

        super.bindView(view)
        val publication = xPublication.publication as PublicationComment

        if (SupAndroid.activityIsVisible) {
            ControllerNotifications.removeNotificationFromNew(NotificationComment::class, publication.id)
            ControllerNotifications.removeNotificationFromNew(NotificationCommentAnswer::class, publication.id)
            ControllerNotifications.removeNotificationFromNew(NotificationMention::class, publication.id)
            ControllerNotifications.removeNotificationFromNew(NotificationPublicationReaction::class, publication.id)
        }

        updateBaseComponents()
        updateSwipe()
        updateQuote()
        updateReactions()
        updateText()
        updateImage()
        updateSticker()
        updateImages()
    }

    private fun updateBaseComponents() {
        val view = getView() ?: return
        val publication = xPublication.publication as PublicationComment
        val vLabel: TextView? = view.findViewById(R.id.vLabel)
        val vLabelName: TextView? = view.findViewById(R.id.vLabelName)
        val vLabelDate: TextView? = view.findViewById(R.id.vLabelDate)
        val vActiveBadge: ImageView? = view.findViewById(R.id.vActiveBadge)

        vLabelName?.text = publication.creator.name
        vLabelDate?.text = buildString {
            append(ToolsDate.dateToString(publication.dateCreate))
            append(if (publication.changed) " " + t(API_TRANSLATE.app_edited) else "")
        }
        vLabel?.text = buildString {
            append(publication.creator.name)
            append("   ")
            append(ToolsDate.dateToString(publication.dateCreate))

            if (publication.changed) {
                append(" ")
                append(t(API_TRANSLATE.app_edited))
            }
        }

        vActiveBadge?.let { xPublication.xAccount.setActiveBadge(it) }
    }

    fun updateImages() {
        val view = getView() ?: return
        val publication = xPublication.publication as PublicationComment

        val vText: ViewText = view.findViewById(R.id.vText)
        val vRootContainer: ViewGroup = vText.parent as ViewGroup
        var vImagesContainer: View? = vRootContainer.findViewById(R.id.vImagesContainer)

        if (publication.imageIdArray.isEmpty()) {
            if (vImagesContainer != null) {
                vRootContainer.removeView(vImagesContainer)
                val vImages: ViewImagesContainer = vImagesContainer as ViewImagesContainer
                vImages.clear()
                putViewToCash("images", vImagesContainer)
            }
            return
        }
        if (vImagesContainer == null) {
            vImagesContainer = getViewFromCash("images")
                ?: ToolsView.inflate(vRootContainer, R.layout.card_comment_view_images)
            vRootContainer.addView(vImagesContainer, vRootContainer.indexOfChild(vText) + 1)
        }

        val vImages: ViewImagesContainer = vImagesContainer as ViewImagesContainer

        ToolsView.setOnLongClickCoordinates(vImages) { v, x, y -> ControllerComment.showMenu(v, x, y, xPublication.publication) }
        vImages.setOnClickListener { Navigator.to(SImageView(ImageLoader.load(publication.image))) }
        vImages.clear()

        for (image in publication.images) {
            vImages.add(ImageLoader.load(image),
                null,
                { ControllerComment.showMenu(it.view, it.x, it.y, xPublication.publication) }
            )
        }
    }

    fun updateSticker() {
        val view = getView() ?: return
        val publication = xPublication.publication as PublicationComment

        val vText: ViewText = view.findViewById(R.id.vText)
        val vRootContainer: ViewGroup = vText.parent as ViewGroup
        var vStickerContainer: View? = vRootContainer.findViewById(R.id.vStickerContainer)

        if (publication.stickerGifId < 1 && publication.stickerImageId < 1) {
            if (vStickerContainer != null) {
                vRootContainer.removeView(vStickerContainer)
                putViewToCash("sticker", vStickerContainer)
            }
            return
        }
        if (vStickerContainer == null) {
            vStickerContainer = getViewFromCash("sticker")
                ?: ToolsView.inflate(vRootContainer, R.layout.card_comment_view_sticker)
            vRootContainer.addView(vStickerContainer, vRootContainer.indexOfChild(vText) + 1)
        }


        val vImage: ImageView = vStickerContainer.findViewById(R.id.vImage)
        val vGifProgressBar: View = vStickerContainer.findViewById(R.id.vGifProgressBar)

        ToolsView.setOnLongClickCoordinates(vImage) { v, x, y -> ControllerComment.showMenu(v, x, y, xPublication.publication) }

        vImage.setOnClickListener { SStickersView.instanceBySticker(publication.stickerId, Navigator.TO) }
        vImage.setOnLongClickListener { Navigator.to(SImageView(ImageLoader.load(if (publication.stickerGif.isEmpty()) publication.stickerImage else publication.stickerGif))); true }

        ImageLoader.loadGif(publication.stickerImage, publication.stickerGif, vImage, vGifProgressBar) {
            it.crop(ToolsView.dpToPx(156).toInt())
            it.size((publication.stickerImage.width * 1.7f).toInt(), (publication.stickerImage.height * 1.7f).toInt())
        }
    }

    fun updateImage() {
        val view = getView() ?: return
        val publication = xPublication.publication as PublicationComment

        val vText: ViewText = view.findViewById(R.id.vText)
        val vRootContainer: ViewGroup = vText.parent as ViewGroup
        var vImageContainer: View? = vRootContainer.findViewById(R.id.vImageContainer)

        if (publication.imageId < 1 && publication.gifId < 1) {
            if (vImageContainer != null) {
                vRootContainer.removeView(vImageContainer)
                putViewToCash("image", vImageContainer)
            }
            return
        }
        if (vImageContainer == null) {
            vImageContainer = getViewFromCash("image")
                ?: ToolsView.inflate(vRootContainer, R.layout.card_comment_view_image)
            vRootContainer.addView(vImageContainer, vRootContainer.indexOfChild(vText) + 1)
        }

        val vImage: ImageView = vImageContainer.findViewById(R.id.vImage)
        val vGifProgressBar: View = vImageContainer.findViewById(R.id.vGifProgressBar)
        val vMaxSizes: LayoutMaxSizes = vImageContainer.findViewById(R.id.vMaxSizes)

        vMaxSizes.setMaxWidth(if (miniSize) 124 else 200)
        vMaxSizes.setMaxHeight(if (miniSize) 124 else 200)
        ToolsView.setOnLongClickCoordinates(vImage) { v, x, y -> ControllerComment.showMenu(v, x, y, xPublication.publication) }

        vImage.setOnClickListener { Navigator.to(SImageView(ImageLoader.load(if (publication.gif.isEmpty()) publication.image else publication.gif))) }

        ImageLoader.loadGif(publication.image, publication.gif, vImage, vGifProgressBar) {
            it.maxSize(ToolsView.dpToPx(612).toInt())
            it.minSize(ToolsView.dpToPx(128).toInt())
            it.size((publication.image.width * 1.7f).toInt(), (publication.image.height * 1.7f).toInt())
        }
    }

    private fun updateText() {
        val view = getView() ?: return
        val publication = xPublication.publication as PublicationComment
        val vText: ViewText = view.findViewById(R.id.vText)

        var text = ControllerLinks.getAnswerText(publication.answerName, publication.text)
        if (!publication.newFormatting) {
            if (text.length > maxTextSize) {
                val parsedText = TextFormatter(text).parseNoTags()
                if (parsedText.length > maxTextSize) {
                    text = "${parsedText.subSequence(0, maxTextSize)}..."
                }
            }
            vText.text = text
            ControllerLinks.makeLinkable(vText)
        } else {
            BonfireMarkdown.setMarkdownInline(vText, text)
            ControllerLinks.linkifyShort(vText)
            if (vText.text.length > maxTextSize) {
                // no spans lost! zeon is in tears!
                vText.text = vText.text.subSequence(0, maxTextSize)
            }
        }

        (vText.layoutParams as ViewGroup.MarginLayoutParams).topMargin = if (publication.quoteId > 0) {
            0
        } else {
            ToolsView.dpToPx(8).toInt()
        }
    }

    fun updateSwipe() {
        val view = getView() ?: return
        val vSwipe: ViewSwipe? = view.findViewById(R.id.vSwipe)
        val publication = xPublication.publication as PublicationComment
        val vRootContainer: ViewGroup = view.findViewById(R.id.vRootContainer)

        if (vSwipe != null) {
            vSwipe.onClick = { if (onClick()) ControllerComment.showMenu(vSwipe, it.x, it.y, xPublication.publication) }
            vSwipe.onLongClick = { ControllerComment.showMenu(vSwipe, it.x, it.y, xPublication.publication) }
            vSwipe.swipeEnabled = quoteEnabled && onQuote != null
            if (onQuote != null) {
                vSwipe.onClick = {
                    if (ControllerApi.isCurrentAccount(publication.creator.id)) change()
                    else onClick()
                }
                vSwipe.onSwipe = { onQuote.invoke(publication) }
            }
        } else {
            ToolsView.setOnClickAndLongClickCoordinates(vRootContainer,
                { if (onClick()) ControllerComment.showMenu(vRootContainer, it.x, it.y, xPublication.publication) },
                { ControllerComment.showMenu(vRootContainer, it.x, it.y, xPublication.publication) })
        }
    }

    fun updateQuote() {
        val view = getView() ?: return
        val publication = xPublication.publication as PublicationComment
        val vText: ViewText = view.findViewById(R.id.vText)
        val vRootContainer: ViewGroup = vText.parent as ViewGroup
        var vQuoteContainer: View? = vRootContainer.findViewById(R.id.vQuoteContainer)

        if (publication.quoteText.isEmpty() && publication.quoteImageIds.isEmpty()) {
            if (vQuoteContainer != null) {
                vRootContainer.removeView(vQuoteContainer)
                val vQuoteImage: ViewImagesContainer = vQuoteContainer.findViewById(R.id.vQuoteImage)
                vQuoteImage.clear()
                putViewToCash("quote", vQuoteContainer)
            }
            return
        }
        if (vQuoteContainer == null) {
            vQuoteContainer = getViewFromCash("quote")
                ?: ToolsView.inflate(vRootContainer, R.layout.card_comment_view_quote)
            vRootContainer.addView(vQuoteContainer, vRootContainer.indexOfChild(vText))
        }

        val vQuoteText: ViewText = vQuoteContainer.findViewById(R.id.vQuoteText)
        val vQuoteImage: ViewImagesContainer = vQuoteContainer.findViewById(R.id.vQuoteImage)

        vQuoteContainer.setOnClickListener { if (onGoTo != null) onGoTo!!.invoke(publication.quoteId) }
        vQuoteContainer.setOnLongClickListener {
            ControllerComment.showMenu(vQuoteContainer, it.x, it.y, xPublication.publication)
            true
        }

        val quoteText = ControllerLinks.getQuoteText(publication.quoteCreatorName, publication.quoteText)
        BonfireMarkdown.setMarkdownInline(vQuoteText, quoteText)
        ControllerLinks.linkifyShort(vQuoteText)
        vQuoteText.text = vQuoteText.text.subSequence(0, (100).coerceAtMost(vQuoteText.text.length))

        vQuoteImage.clear()
        vQuoteImage.visibility = View.VISIBLE
        if (publication.quoteStickerId != 0L) {
            vQuoteImage.add(
                ImageLoader.load(publication.quoteStickerImage),
                onClick = { SStickersView.instanceBySticker(publication.quoteStickerId, Navigator.TO) })
        } else if (publication.quoteImages.isNotEmpty()) {
            for (i in publication.quoteImages) vQuoteImage.add(ImageLoader.load(i))
        } else {
            vQuoteImage.visibility = View.GONE
        }

    }

    override fun updateReactions() {
        val view = getView() ?: return
        var vReactions: ViewGroup? = view.findViewById(R.id.vReactions)
        val vBottomContainer: ViewGroup = view.findViewById(R.id.vBottomContainer) ?: return
        xPublication.publication as PublicationComment

        if (xPublication.publication.reactions.isEmpty()) {
            if (vReactions != null) {
                vBottomContainer.removeView(vReactions)
                putViewToCash("reactions", vReactions)
            }
            return
        }

        if (vReactions == null) {
            vReactions = (getViewFromCash("reactions")
                ?: ToolsView.inflate(vBottomContainer, R.layout.card_comment_view_reactions)) as ViewGroup
            vBottomContainer.addView(vReactions, 0)
        }

        val dp = ToolsView.dpToPx(8)


        val map = LongSparseArray<ViewChip>()
        for (i in xPublication.publication.reactions) {
            var v: ViewChip? = map.get(i.reactionIndex)
            if (v == null) {
                v = ToolsView.inflate(R.layout.z_chip)
                v.setOnClickListener { ControllerComment.sendReaction(xPublication.publication, i.reactionIndex) }
                v.tag = 0
                v.iconStartPadding = dp / 2
                v.iconEndPadding = dp / 2
                v.setTextPaddings(0f, dp)
                map.put(i.reactionIndex, v)
            }

            v.tag = (v.tag as Int) + 1
            if (i.accountId == ControllerApi.account.getId()) {
                v.setChipBackgroundColorResource(R.color.blue_700)
                v.setOnClickListener { ControllerComment.removeReaction(xPublication.publication, i.reactionIndex) }
            }
            v.setOnLongClickListener {
                ControllerReactions.showAccounts(
                    xPublication.publication.id,
                    i.reactionIndex,
                    v
                );true
            }

            val index = if (i.reactionIndex > -1 && i.reactionIndex < API.REACTIONS.size) i.reactionIndex.toInt() else 0
            v.setIcon(R.color.focus)
            ImageLoader.load(API.REACTIONS[index]).intoBitmap { v.setIcon(it) }
        }

        (vReactions.layoutParams as ViewGroup.MarginLayoutParams).topMargin =
            if (xPublication.publication.text.isEmpty()) ToolsView.dpToPx(8).toInt() else ToolsView.dpToPx(4).toInt()
        vReactions.removeAllViews()
        for (i in 0 until map.size()) {
            val v = map.valueAt(i)
            v.text = "${v.tag}"
            vReactions.addView(v)
            v.setChipIconSizePadding(dp * 1.5f)
        }
    }

    override fun updateKarma() {
        if (getView() == null) return
        val vKarma: ViewKarma? = getView()!!.findViewById(R.id.vKarma)
        if (vKarma != null) xPublication.xKarma.setView(vKarma)
    }

    override fun updateAccount() {
        if (getView() == null) return
        if (showFandom && xPublication.xFandom.getImage().isEmpty()) xPublication.xFandom.setImage(xPublication.xAccount.getImage())
        val vAvatar: ViewAvatar = getView()!!.findViewById(R.id.vAvatar)
        if (!showFandom) xPublication.xAccount.setView(vAvatar)
        else xPublication.xFandom.setView(vAvatar)
    }

    override fun updateReports() {
        if (getView() == null) return
        val vReports: TextView = getView()!!.findViewById(R.id.vReports) ?: return
        vReports.setOnClickListener { Navigator.to(SReports(xPublication.publication.id)) }
        xPublication.xReports.setView(vReports)
    }

    //
    //  Other
    //


    private fun change() {
        PostHog.capture("open_comment_editor", properties = mapOf("from" to "change"))
        SplashComment(xPublication.publication as PublicationComment, false).asSheetShow()
    }


    override fun updateComments() {
        update()
    }

    override fun updateFandom() {
        updateAccount()
    }

    private fun onClick(): Boolean {
        val publication = xPublication.publication as PublicationComment
        if (onClick == null) {
            if (publication.parentPublicationType == 0L) {
                ToolsToast.show(t(API_TRANSLATE.post_error_gone))
            } else {
                ControllerPublications.toPublication(
                    publication.parentPublicationType,
                    publication.parentPublicationId,
                    publication.id
                )
            }
            return false
        } else {
            return !onClick.invoke(publication)
        }
    }

    override fun notifyItem() {
        val publication = xPublication.publication as PublicationComment
        ImageLoader.load(publication.creator.image).intoCash()
    }

    //
    //  Methods
    //

    override fun equals(other: Any?): Boolean {
        val publication = xPublication.publication as PublicationComment
        if (other is CardComment) return other.xPublication.publication.id == publication.id
        return super.equals(other)
    }

    //
    //  Event Bus
    //

    private fun onEventPublicationDeepBlockRestore(e: EventPublicationDeepBlockRestore) {
        if (e.publicationId == xPublication.publication.id && xPublication.publication.status == API.STATUS_DEEP_BLOCKED) {
            adapter.remove(proxy)
        }
    }

    private fun onCommentChange(e: EventCommentChange) {
        val publication = xPublication.publication as PublicationComment
        if (e.publicationId == publication.id) {
            publication.text = e.text
            publication.quoteId = e.quoteId
            publication.quoteText = e.quoteText
            publication.changed = true
            update()
        }
    }

}
