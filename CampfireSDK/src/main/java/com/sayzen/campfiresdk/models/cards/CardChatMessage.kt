package com.sayzen.campfiresdk.models.cards

import android.util.LongSparseArray
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.models.notifications.chat.NotificationChatMessageChange
import com.dzen.campfire.api.models.notifications.chat.NotificationChatMessageRemove
import com.dzen.campfire.api.models.notifications.publications.NotificationMention
import com.dzen.campfire.api.models.notifications.publications.NotificationPublicationReaction
import com.dzen.campfire.api.models.publications.chat.PublicationChatMessage
import com.dzen.campfire.api.requests.publications.RPublicationsReactionAdd
import com.dzen.campfire.api.requests.publications.RPublicationsReactionRemove
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.*
import com.sayzen.campfiresdk.models.events.account.EventAccountAddToBlackList
import com.sayzen.campfiresdk.models.events.account.EventAccountRemoveFromBlackList
import com.sayzen.campfiresdk.models.events.chat.*
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sayzen.campfiresdk.models.events.publications.EventPublicationBlocked
import com.sayzen.campfiresdk.models.events.publications.EventPublicationDeepBlockRestore
import com.sayzen.campfiresdk.models.events.publications.EventPublicationReactionAdd
import com.sayzen.campfiresdk.models.events.publications.EventPublicationReactionRemove
import com.sayzen.campfiresdk.screens.account.stickers.SStickersView
import com.sayzen.campfiresdk.screens.chat.SChat
import com.sayzen.campfiresdk.screens.post.history.SPublicationHistory
import com.sayzen.campfiresdk.screens.reports.SReports
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.models.EventStyleChanged
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.screens.SImageView
import com.sup.dev.android.views.splash.SplashMenu
import com.sup.dev.android.views.views.*
import com.sup.dev.android.views.views.layouts.LayoutCorned
import com.sup.dev.java.classes.collections.HashList
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.libs.text_format.TextFormatter
import com.sup.dev.java.tools.ToolsColor
import com.sup.dev.java.tools.ToolsDate
import com.sup.dev.java.tools.ToolsText

open class CardChatMessage constructor(
        publication: PublicationChatMessage,
        var onClick: ((PublicationChatMessage) -> Boolean)? = null,
        var onChange: ((PublicationChatMessage) -> Unit)? = null,
        var onQuote: ((PublicationChatMessage) -> Unit)? = null,
        var onGoTo: ((Long) -> Unit)? = null,
        var onBlocked: ((PublicationChatMessage) -> Unit)? = null
) : CardPublication(R.layout.card_chat_message, publication) {

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

        fun instance(publication: PublicationChatMessage,
                     onClick: ((PublicationChatMessage) -> Boolean)? = null,
                     onChange: ((PublicationChatMessage) -> Unit)? = null,
                     onQuote: ((PublicationChatMessage) -> Unit)? = null,
                     onGoTo: ((Long) -> Unit)? = null,
                     onBlocked: ((PublicationChatMessage) -> Unit)? = null
        ): CardChatMessage {
            return CardChatMessage(publication, onClick, onChange, onQuote, onGoTo, onBlocked)
        }

    }

    private val eventBus = EventBus
            .subscribe(EventNotification::class) { onNotification(it) }
            .subscribe(EventChatMessageChanged::class) { onEventChanged(it) }
            .subscribe(EventChatReadDateChanged::class) { onEventChatReadDateChanged(it) }
            .subscribe(EventStyleChanged::class) { update() }
            .subscribe(EventPublicationBlocked::class) { onEventPublicationBlocked(it) }
            .subscribe(EventPublicationDeepBlockRestore::class) { onEventPublicationDeepBlockRestore(it) }
            .subscribe(EventVoiceMessageStateChanged::class) { update() }
            .subscribe(EventVoiceMessageStep::class) { if (it.id == publication.voiceResourceId) updatePlayTime() }
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

    var changeEnabled = true
    var useMessageContainerBackground = true
    var quoteEnabled = true
    var copyEnabled = true
    var wasBlocked = false

    init {
        updateFandomOnBind = false
        useBackgroundToFlash = true
    }

    fun onSameChanged(){
        updateAccount()
        updateSameCards()
        updateLabel()
        updateAlert()
    }

    //
    //  Bind
    //

    override fun bindView(view: View) {
        if (updateBlacklisted(view)) return

        super.bindView(view)

        val publication = xPublication.publication as PublicationChatMessage

        if (SupAndroid.activityIsVisible) {
            ControllerNotifications.removeNotificationFromNew(NotificationMention::class, publication.id)
            ControllerNotifications.removeNotificationFromNew(NotificationPublicationReaction::class, publication.id)
        }

        val vAvatarContainer: View = view.findViewById(R.id.vAvatarContainer)
        val vMessageContainer: View = view.findViewById(R.id.vMessageContainer)
        val vNotRead: View = view.findViewById(R.id.vNotRead)
        val vLabel: View = view.findViewById(R.id.vLabel)
        val vMessageRootContainer: View = view.findViewById(R.id.vMessageRootContainer)
        vAvatarContainer.visibility = View.VISIBLE
        vMessageContainer.visibility = View.VISIBLE
        vNotRead.visibility = View.VISIBLE
        vLabel.visibility = View.VISIBLE
        vMessageRootContainer.visibility = View.VISIBLE

        updateSwipe()
        updateBase()
        updateRead()
        updateQuote()
        updateSameCards()
        updateLabel()
        updateAlert()

        //  Порядок важен!
        updateText()
        updateReactions()
        updateImage()
        updateImages()
        updateVoice()
        updateSticker()
    }

    fun updateAlert() {
        val view = getView() ?: return
        val publication = xPublication.publication as PublicationChatMessage
        val vSwipe: ViewGroup = view.findViewById(R.id.vSwipe)
        var vAlertContainer: View? = vSwipe.findViewById(R.id.vAlertContainer)

        if (publication.type != PublicationChatMessage.TYPE_SYSTEM) {
            if (vAlertContainer != null) {
                vSwipe.removeView(vAlertContainer)
                putViewToCash("alert", vAlertContainer)
            }
            return
        }
        if (vAlertContainer == null) {
            vAlertContainer = getViewFromCash("alert") ?: ToolsView.inflate(vSwipe, R.layout.card_chat_message_view_alert)
            vSwipe.addView(vAlertContainer, 0)
        }

        val vRootContainer: ViewGroup = view.findViewById(R.id.vRootContainer)
        val vAvatarContainer: View = vRootContainer.findViewById(R.id.vAvatarContainer)
        val vMessageContainer: View = vRootContainer.findViewById(R.id.vMessageContainer)
        val vNotRead: View = vRootContainer.findViewById(R.id.vNotRead)
        val vLabel: View = vRootContainer.findViewById(R.id.vLabel)
        val vMessageRootContainer: View = vRootContainer.findViewById(R.id.vMessageRootContainer)
        vAvatarContainer.visibility = View.GONE
        vMessageContainer.visibility = View.GONE
        vNotRead.visibility = View.GONE
        vLabel.visibility = View.GONE
        vMessageRootContainer.visibility = View.GONE

        val vSystemMessage: ViewText = vAlertContainer.findViewById(R.id.vSystemMessage)

        vSystemMessage.setTextColor(ToolsResources.getColorAttr(R.attr.colorOnPrimaryVariant))
        if (publication.systemType == PublicationChatMessage.SYSTEM_TYPE_BLOCK) vSystemMessage.setTextColor(ToolsResources.getColor(R.color.red_600))
        vSystemMessage.text = ControllerChats.getSystemText(publication)
        ControllerLinks.makeLinkable(vSystemMessage)

        val vTouchModeration: ViewGroup = view.findViewById(R.id.vTouchModeration) ?: return

        vTouchModeration.setOnClickListener { ControllerCampfireSDK.onToModerationClicked(publication.blockModerationEventId, 0, Navigator.TO) }
        vTouchModeration.isClickable = publication.blockModerationEventId != 0L

    }

    fun updateLabel() {
        val view = getView() ?: return
        val publication = xPublication.publication as PublicationChatMessage
        val vLabel: ViewText = view.findViewById(R.id.vLabel) ?: return

        if (ControllerApi.isCurrentAccount(publication.creator.id)) {
            if (vLabel.layoutParams is FrameLayout.LayoutParams) (vLabel.layoutParams as FrameLayout.LayoutParams).gravity = Gravity.RIGHT or Gravity.BOTTOM
            if (vLabel.layoutParams is LinearLayout.LayoutParams) (vLabel.layoutParams as LinearLayout.LayoutParams).gravity = Gravity.RIGHT
            vLabel.text = ToolsDate.dateToString(publication.dateCreate) + (if (publication.changed) " " + t(API_TRANSLATE.app_edited) else "")
        } else {
            if (vLabel.layoutParams is FrameLayout.LayoutParams) (vLabel.layoutParams as FrameLayout.LayoutParams).gravity = Gravity.LEFT or Gravity.BOTTOM
            if (vLabel.layoutParams is LinearLayout.LayoutParams) (vLabel.layoutParams as LinearLayout.LayoutParams).gravity = Gravity.LEFT
            if (publication.chatTag().chatType == API.CHAT_TYPE_PRIVATE)
                vLabel.text = ToolsDate.dateToString(publication.dateCreate) + (if (publication.changed) " " + t(API_TRANSLATE.app_edited) else "")
            else {
                val color = xPublication.xAccount.getLevelColorHex()
                vLabel.text =  "{$color ${xPublication.xAccount.getName()}}  " + ToolsDate.dateToString(publication.dateCreate) + (if (publication.changed) " " + t(API_TRANSLATE.app_edited) else "")
                ControllerLinks.makeLinkable(vLabel)
            }

        }

        vLabel.gravity = if(ControllerApi.isCurrentAccount(publication.creator.id)) Gravity.RIGHT else Gravity.LEFT

        vLabel.visibility = if (willHideLabel()) View.GONE else View.VISIBLE
    }

    fun updateVoice() {
        val view = getView() ?: return
        val vText: ViewText = view.findViewById(R.id.vText) ?: return
        val publication = xPublication.publication as PublicationChatMessage
        val vContentContainer: ViewGroup = vText.parent as ViewGroup
        var vVoiceContainer: View? = vContentContainer.findViewById(R.id.vVoiceContainer)

        if (publication.voiceResourceId < 1) {
            if (vVoiceContainer != null) {
                vContentContainer.removeView(vVoiceContainer)
                putViewToCash("voice", vVoiceContainer)
            }
            return
        }
        if (vVoiceContainer == null) {
            vVoiceContainer = getViewFromCash("voice") ?: ToolsView.inflate(vContentContainer, R.layout.card_chat_message_view_voice)
            vContentContainer.addView(vVoiceContainer, vContentContainer.indexOfChild(vText) + 1)
        }

        val vPlay: ViewIcon = vVoiceContainer.findViewById(R.id.vPlay)
        val vSoundLine: ViewSoundLine = vVoiceContainer.findViewById(R.id.vSoundLine)

        vSoundLine.setSoundMask(publication.voiceMask)


        if (ControllerVoiceMessages.isLoading(publication.voiceResourceId)) {
            vPlay.isEnabled = false
            vPlay.setImageResource(R.drawable.ic_play_arrow_white_24dp)
        } else if (ControllerVoiceMessages.isPlay(publication.voiceResourceId)) {
            vPlay.isEnabled = true
            vPlay.setImageResource(R.drawable.ic_pause_white_24dp)
        } else {
            vPlay.isEnabled = true
            vPlay.setImageResource(R.drawable.ic_play_arrow_white_24dp)
        }

        vPlay.setOnClickListener {
            if (ControllerVoiceMessages.isPlay(publication.voiceResourceId))
                ControllerVoiceMessages.pause(publication.voiceResourceId)
            else
                ControllerVoiceMessages.play(publication.voiceResourceId)
        }

        updatePlayTime()


    }

    fun updateSticker() {
        val view = getView() ?: return
        val vText: ViewText = view.findViewById(R.id.vText) ?: return
        val publication = xPublication.publication as PublicationChatMessage
        val vContentContainer: ViewGroup = vText.parent as ViewGroup
        var vStickerContainer: View? = vContentContainer.findViewById(R.id.vStickerContainer)

        if (publication.stickerGifId < 1 && publication.stickerImageId < 1) {
            if (vStickerContainer != null) {
                vContentContainer.removeView(vStickerContainer)
                putViewToCash("sticker", vStickerContainer)
            }
            return
        }
        if (vStickerContainer == null) {
            vStickerContainer = getViewFromCash("sticker") ?: ToolsView.inflate(vContentContainer, R.layout.card_chat_message_view_sticker)
            vContentContainer.addView(vStickerContainer, vContentContainer.indexOfChild(vText) + 1)
        }


        val vImage: ImageView = vStickerContainer.findViewById(R.id.vImage)
        val vGifProgressBar: View = vStickerContainer.findViewById(R.id.vGifProgressBar)

        ToolsView.setOnLongClickCoordinates(vImage) { v, x, y -> showMenu(v,x,y) }

        vImage.setOnClickListener { SStickersView.instanceBySticker(publication.stickerId, Navigator.TO) }
        vImage.setOnLongClickListener{ Navigator.to(SImageView(ImageLoader.load(if (publication.stickerGifId == 0L) publication.stickerImageId else publication.stickerGifId))); true }

        ImageLoader.loadGif(publication.stickerImageId, publication.stickerGifId, vImage, vGifProgressBar) {
            it.crop(ToolsView.dpToPx(156).toInt())
            it.size((publication.imageW * 1.7f).toInt(), (publication.imageH * 1.7f).toInt())
        }
    }

    fun updateImages() {
        val view = getView() ?: return
        val vText: ViewText = view.findViewById(R.id.vText) ?: return
        val publication = xPublication.publication as PublicationChatMessage
        val vContentContainer: ViewGroup = vText.parent as ViewGroup
        var vImagesContainer: View? = vContentContainer.findViewById(R.id.vImagesContainer)

        if (publication.imageIdArray.isEmpty()) {
            if (vImagesContainer != null) {
                vContentContainer.removeView(vImagesContainer)
                val vImages: ViewImagesContainer = vImagesContainer.findViewById(R.id.vImages)
                vImages.clear()
                putViewToCash("images", vImagesContainer)
            }
            return
        }
        if (vImagesContainer == null) {
            vImagesContainer = getViewFromCash("images") ?: ToolsView.inflate(vContentContainer, R.layout.card_chat_message_view_images)
            vContentContainer.addView(vImagesContainer, vContentContainer.indexOfChild(vText) + 1)
        }

        val vImages: ViewImagesContainer = vImagesContainer.findViewById(R.id.vImages)

        ToolsView.setOnLongClickCoordinates(vImages) { v, x, y -> showMenu(v,x,y) }
        vImages.setOnClickListener { Navigator.to(SImageView(ImageLoader.load(publication.resourceId, publication.imagePwd))) }
        vImages.clear()

        for (i in publication.imageIdArray.indices) {
            vImages.add(
                    ImageLoader.load(publication.imageIdArray[i], publication.imagePwdArray.getOrNull(i) ?: "")
                            .size(publication.imageWArray[i], publication.imageHArray[i]),
                    null,
                    { showMenu(it.view, it.x, it.y) })
        }
    }

    fun updateImage() {
        val view = getView() ?: return
        val vText: ViewText = view.findViewById(R.id.vText) ?: return
        val publication = xPublication.publication as PublicationChatMessage
        val vContentContainer: ViewGroup = vText.parent as ViewGroup
        var vImageContainer: View? = vContentContainer.findViewById(R.id.vImageContainer)

        if (publication.resourceId < 1 && publication.gifId < 1) {
            if (vImageContainer != null) {
                vContentContainer.removeView(vImageContainer)
                putViewToCash("image", vImageContainer)
            }
            return
        }
        if (vImageContainer == null) {
            vImageContainer = getViewFromCash("image") ?: ToolsView.inflate(vContentContainer, R.layout.card_chat_message_view_image)
            vContentContainer.addView(vImageContainer, vContentContainer.indexOfChild(vText) + 1)
        }

        val vImage: ImageView = vImageContainer.findViewById(R.id.vImage)
        val vGifProgressBar: View = vImageContainer.findViewById(R.id.vGifProgressBar)
        val vLabelRemoved: TextView = vImageContainer.findViewById(R.id.vImageRemoved)

        vLabelRemoved.setText(t(API_TRANSLATE.message_removed_by_server))

        ToolsView.setOnLongClickCoordinates(vImage) { v, x, y -> showMenu(v,x,y) }

        vImage.setOnClickListener(null)

        vLabelRemoved.tag = publication
        vLabelRemoved.visibility = View.GONE

        ImageLoader.loadGif(publication.resourceId, publication.gifId, publication.imagePwd, vImage, vGifProgressBar) {
            it.maxSize(ToolsView.dpToPx(612).toInt())
            it.minSize(ToolsView.dpToPx(128).toInt())
            it.size((publication.imageW * 1.7f).toInt(), (publication.imageH * 1.7f).toInt())
        }

        vImage.setOnClickListener { Navigator.to(SImageView(ImageLoader.load(if (publication.gifId == 0L) publication.resourceId else publication.gifId, publication.imagePwd))) }
    }

    fun updateText() {
        val view = getView() ?: return
        val vText: ViewText = view.findViewById(R.id.vText) ?: return
        val publication = xPublication.publication as PublicationChatMessage

        vText.text = publication.text
        vText.visibility = if (publication.text.isEmpty()) View.GONE else View.VISIBLE
        (vText.layoutParams as ViewGroup.MarginLayoutParams).topMargin = if(publication.quoteId > 0) 0 else ToolsView.dpToPx(8).toInt()

        ControllerLinks.makeLinkable(vText) {
            val myName = ControllerApi.account.getName() + ","
            if (publication.text.startsWith(myName)) {
                vText.text = "{ff6d00 $myName}" + "${vText.text}".substring(myName.length)
            } else {
                if (publication.answerName.isNotEmpty()) {
                    val otherName = publication.answerName + ","
                    if (publication.text.startsWith(otherName)) {
                        vText.text = "{90A4AE $otherName}" + "${vText.text}".substring(otherName.length)
                    }
                }
            }
        }

    }

    fun updateBase() {
        val view = getView() ?: return
        val publication = xPublication.publication as PublicationChatMessage
        val vRootContainer: ViewGroup? = view.findViewById(R.id.vRootContainer)
        val vMessageContainer: LayoutCorned? = view.findViewById(R.id.vMessageContainer)

        if (vRootContainer != null) {
            (vRootContainer.layoutParams as LinearLayout.LayoutParams).gravity = if (ControllerApi.isCurrentAccount(publication.creator.id)) Gravity.RIGHT or Gravity.TOP else Gravity.LEFT or Gravity.TOP
        }

        if (vMessageContainer != null) {
            vMessageContainer.setCornedSizePx(ToolsView.dpToPx(ControllerSettings.styleChatRounding).toInt())
        }

        if (ControllerApi.isCurrentAccount(publication.creator.id)) {
            if (vMessageContainer != null) {
                (vMessageContainer.layoutParams as ViewGroup.MarginLayoutParams).rightMargin =0
                (vMessageContainer.layoutParams as ViewGroup.MarginLayoutParams).leftMargin =0
                if (useMessageContainerBackground) {
                    if (ToolsColor.red(ToolsResources.getColorAttr(R.attr.colorSurface)) < 0x60)
                        vMessageContainer.setBackgroundColor(ToolsColor.add(ToolsResources.getColorAttr(R.attr.colorSurface), 0xFF202020.toInt()))
                    else
                        vMessageContainer.setBackgroundColor(ToolsColor.remove(ToolsResources.getColorAttr(R.attr.colorSurface), 0xFF202020.toInt()))
                } else {
                    vMessageContainer.setBackgroundColor(0x00000000)
                }
            }
            if (vRootContainer != null) {
                (vRootContainer.layoutParams as ViewGroup.MarginLayoutParams).rightMargin = ToolsView.dpToPx(12).toInt()
                (vRootContainer.layoutParams as ViewGroup.MarginLayoutParams).leftMargin = ToolsView.dpToPx(48).toInt()
            }
        } else {
            if (vMessageContainer != null) {
                (vMessageContainer.layoutParams as ViewGroup.MarginLayoutParams).rightMargin = ToolsView.dpToPx(48).toInt()
                (vMessageContainer.layoutParams as ViewGroup.MarginLayoutParams).leftMargin = ToolsView.dpToPx(12).toInt()
                if (useMessageContainerBackground) vMessageContainer.setBackgroundColor(ToolsResources.getColorAttr(R.attr.colorSurface))
                else vMessageContainer.setBackgroundColor(0x00000000)
            }
            if (vRootContainer != null) {
                (vRootContainer.layoutParams as ViewGroup.MarginLayoutParams).rightMargin = 0
                (vRootContainer.layoutParams as ViewGroup.MarginLayoutParams).leftMargin = 0
            }
        }
    }

    fun updateQuote() {
        val view = getView() ?: return
        val vText: ViewText = view.findViewById(R.id.vText) ?: return
        val publication = xPublication.publication as PublicationChatMessage
        var vQuoteContainer: View? = view.findViewById(R.id.vQuoteContainer)
        val vContentContainer: ViewGroup = vText.parent as ViewGroup

        if (publication.quoteText.isEmpty() && publication.quoteImages.isEmpty()) {
            if (vQuoteContainer != null) {
                vContentContainer.removeView(vQuoteContainer)
                val vQuoteImage: ViewImagesContainer = vQuoteContainer.findViewById(R.id.vQuoteImage)
                vQuoteImage.clear()
                putViewToCash("quote", vQuoteContainer)
            }
            return
        }
        if (vQuoteContainer == null) {
            vQuoteContainer = getViewFromCash("quote") ?: ToolsView.inflate(vContentContainer, R.layout.card_chat_message_view_quote)
            vContentContainer.addView(vQuoteContainer, vContentContainer.indexOfChild(vText))
        }

        val vQuoteText: ViewText = vQuoteContainer.findViewById(R.id.vQuoteText)
        val vQuoteImage: ViewImagesContainer = vQuoteContainer.findViewById(R.id.vQuoteImage)

        vQuoteContainer.setOnClickListener { if (onGoTo != null) onGoTo!!.invoke(publication.quoteId) }
        vQuoteContainer.setOnLongClickListener { showMenu(vQuoteContainer, it.x, it.y);true }

        val quoteTextRaw = TextFormatter(publication.quoteText).parseNoTags()
        val quoteText = if (quoteTextRaw.length > 100) "${quoteTextRaw.substring(0, 100)}..."
        else if (publication.type == PublicationChatMessage.TYPE_VOICE) t(API_TRANSLATE.app_voice_message)
        else quoteTextRaw
        vQuoteText.text = quoteText
        if (publication.quoteCreatorName.isNotEmpty()) {
            val otherName = publication.quoteCreatorName + ":"
            if (quoteText.startsWith(otherName)) {
                val color = if(publication.quoteCreatorName == ControllerApi.account.getName()) "FF6D00" else "90A4AE"
                vQuoteText.text = "{$color $otherName}" + quoteText.substring(otherName.length)
            }
        }
        ControllerLinks.makeLinkable(vQuoteText)

        vQuoteImage.clear()
        vQuoteImage.visibility = View.VISIBLE
        if (publication.quoteStickerId > 0) {
            vQuoteImage.add(ImageLoader.load(publication.quoteStickerImageId)
                    .crop(ToolsView.dpToPx(100).toInt()).cropSquare(),
                    onClick = { SStickersView.instanceBySticker(publication.quoteStickerId, Navigator.TO) })
        } else if (publication.quoteImages.isNotEmpty()) {
            for (i in publication.quoteImages.indices) vQuoteImage.add(ImageLoader.load(publication.quoteImages[i], publication.quoteImagesPwd.getOrNull(i) ?: "")
                    .crop(ToolsView.dpToPx(100).toInt()).cropSquare())
        } else {
            vQuoteImage.visibility = View.GONE
        }

    }

    fun updateSwipe() {
        val view = getView() ?: return
        val publication = xPublication.publication as PublicationChatMessage
        val vSwipe: ViewSwipe? = view.findViewById(R.id.vSwipe)

        if (vSwipe != null) {
            vSwipe.onClick = {
                if (ControllerApi.isCurrentAccount(publication.creator.id) && onChange != null) {
                    onChange?.invoke(publication)
                } else if (!onClick()) {
                    showMenu(vSwipe, it.x, it.y)
                }
            }
            vSwipe.onLongClick = { showMenu(vSwipe, it.x, it.y) }
            vSwipe.swipeEnabled = quoteEnabled && onQuote != null


            if (onQuote != null) {
                vSwipe.onSwipe = { onQuote?.invoke(publication) }
            }
        }

    }

    fun updateSameCards() {
        val view = getView() ?: return

        val vPaddingContainer: ViewGroup = view.findViewById(R.id.vSwipe) ?: return
        val vMessageContainer: LayoutCorned = view.findViewById(R.id.vMessageContainer)

        val willHideLabel = willHideLabel()
        val bottomIsWillHideLabel = getBottomCard()?.willHideLabel()?:false

        if (ControllerApi.isCurrentAccount(xPublication.publication.creator.id)) {
            vMessageContainer.setCornedTL(true)
            vMessageContainer.setCornedBL(true)
            vMessageContainer.setCornedTR(!willHideLabel)
            vMessageContainer.setCornedBR(!bottomIsWillHideLabel)
        } else {
            vMessageContainer.setCornedTL(!willHideLabel)
            vMessageContainer.setCornedBL(!bottomIsWillHideLabel)
            vMessageContainer.setCornedTR(true)
            vMessageContainer.setCornedBR(true)
        }

        vPaddingContainer.setPadding(0, 0, 0, if (bottomIsWillHideLabel && getBottomCard()?.willHideLabel() == true) ToolsView.dpToPx(1).toInt() else ToolsView.dpToPx(16).toInt())

    }

    fun updateRead() {
        val publication = xPublication.publication as PublicationChatMessage
        if (getView() == null) return
        val vNotRead: View? = getView()!!.findViewById(R.id.vNotRead)
        if (vNotRead != null) {

            if (!ControllerApi.isCurrentAccount(publication.creator.id) || publication.chatType != API.CHAT_TYPE_PRIVATE)
                vNotRead.visibility = View.GONE
            else if (ControllerChats.isRead(publication.chatTag(), publication.dateCreate))
                vNotRead.visibility = View.INVISIBLE
            else
                vNotRead.visibility = View.VISIBLE

        }

    }

    override fun updateReactions() {
        val view = getView() ?: return
        var vReactions: ViewGroup? = view.findViewById(R.id.vReactions)
        val vText: ViewText = view.findViewById(R.id.vText) ?: return
        val vContentContainer: ViewGroup = vText.parent as ViewGroup
        xPublication.publication as PublicationChatMessage

        if (xPublication.publication.reactions.isEmpty()) {
            if (vReactions != null) {
                vContentContainer.removeView(vReactions)
                putViewToCash("reactions", vReactions)
            }
            return
        }

        if (vReactions == null) {
            vReactions = (getViewFromCash("reactions") ?: ToolsView.inflate(vContentContainer, R.layout.card_chat_message_view_reactions)) as ViewGroup
            vContentContainer.addView(vReactions, vContentContainer.indexOfChild(vText) + 1)
        }

        val dp = ToolsView.dpToPx(8)


        val map = LongSparseArray<ViewChip>()
        for (i in xPublication.publication.reactions) {
            var v: ViewChip? = map.get(i.reactionIndex)
            if (v == null) {
                v = ToolsView.inflate(R.layout.z_chip)
                v.setOnClickListener { sendReaction(i.reactionIndex) }
                v.tag = 0
                v.iconStartPadding = dp / 2
                v.iconEndPadding = dp / 2
                v.setTextPaddings(0f, dp)
                map.put(i.reactionIndex, v)
            }

            v.tag = (v.tag as Int) + 1
            if (i.accountId == ControllerApi.account.getId()) {
                v.setChipBackgroundColorResource(R.color.blue_700)
                v.setOnClickListener { removeReaction(i.reactionIndex) }
            }
            v.setOnLongClickListener { ControllerReactions.showAccounts(xPublication.publication.id, i.reactionIndex, v);true }

            val index = if (i.reactionIndex > -1 && i.reactionIndex < API.REACTIONS.size) i.reactionIndex.toInt() else 0
            v.setIcon(R.color.focus)
            ImageLoader.load(API.REACTIONS[index]).intoBitmap { v.setIcon(it) }
        }

        (vReactions.layoutParams as ViewGroup.MarginLayoutParams).topMargin = if( xPublication.publication.text.isEmpty()) ToolsView.dpToPx(8).toInt() else 0

        vReactions.removeAllViews()
        for (i in 0 until map.size()) {
            val v = map.valueAt(i)
            v.text = "${v.tag}"
            vReactions.addView(v)
            v.setChipIconSizePadding(dp * 1.5f)
        }
    }

    override fun updateAccount() {
        val view = getView() ?: return
        val publication = xPublication.publication as PublicationChatMessage
        val vAvatarContainer: ViewGroup = view.findViewById(R.id.vAvatarContainer) ?: return
        (vAvatarContainer.layoutParams as ViewGroup.MarginLayoutParams).leftMargin = 0

        var vAvatar: ViewAvatar? = vAvatarContainer.findViewById(R.id.vAvatar)

        if (ControllerApi.isCurrentAccount(publication.creator.id) || isTopSameUserAndFandom() || publication.chatTag().chatType == API.CHAT_TYPE_PRIVATE) {
            if (vAvatar != null) {
                vAvatarContainer.removeView(vAvatar)
                putViewToCash("avatar", vAvatar)
            }
            if (!ControllerApi.isCurrentAccount(publication.creator.id) && publication.chatTag().chatType != API.CHAT_TYPE_PRIVATE && isTopSameUserAndFandom())
                (vAvatarContainer.layoutParams as ViewGroup.MarginLayoutParams).leftMargin = ToolsView.dpToPx(48).toInt()
            return
        }
        if (vAvatar == null) {
            vAvatar = (getViewFromCash("avatar") ?: ToolsView.inflate(vAvatarContainer, R.layout.card_chat_message_view_avatar)) as ViewAvatar
            vAvatarContainer.addView(vAvatar, 0)
        }

        if (!showFandom) {
            xPublication.xAccount.setView(vAvatar)
            vAvatar.vChip.visibility = View.GONE
        }
        else {
            xPublication.xFandom.setView(vAvatar)
            vAvatar.vChip.visibility = View.VISIBLE
        }

    }

    override fun updateComments() {
        update()
    }

    override fun updateFandom() {
        updateAccount()
    }

    override fun updateKarma() {
        update()
    }

    override fun updateReports() {
        if (getView() == null) return
        val vReports: TextView = getView()!!.findViewById(R.id.vReports)?:return
        vReports.setOnClickListener { Navigator.to(SReports(xPublication.publication.id)) }
        xPublication.xReports.setView(vReports)
    }

    //
    //  Other
    //

    private fun updatePlayTime() {
        val view = getView() ?: return
        val publication = xPublication.publication as PublicationChatMessage
        val vVoiceContainer: View = view.findViewById(R.id.vVoiceContainer) ?: return

        val vTimeLabel: TextView = vVoiceContainer.findViewById(R.id.vTimeLabel)
        val vSoundLine: ViewSoundLine = vVoiceContainer.findViewById(R.id.vSoundLine)

        val time = ControllerVoiceMessages.getPlayTimeMs(publication.voiceResourceId)
        if (time < publication.voiceMs && ControllerVoiceMessages.isPlay(publication.voiceResourceId) || ControllerVoiceMessages.isPause(publication.voiceResourceId)) {
            vTimeLabel.text = ToolsText.toTime(publication.voiceMs - time)
            vSoundLine.setProgress(time.toFloat(), publication.voiceMs.toFloat())
        } else {
            vTimeLabel.text = ToolsText.toTime(publication.voiceMs)
            vSoundLine.setProgress(0f, publication.voiceMs.toFloat())
        }
    }

    fun isTopSameUserAndFandom(): Boolean {
        var myIndex = adapter.indexOf(this)
        myIndex--
        if (myIndex > -1) {
            val card = adapter.get(myIndex)
            if (card is CardChatMessage) return card.xPublication.publication.creator.id == xPublication.publication.creator.id && card.xPublication.publication.fandom.id == xPublication.publication.fandom.id && card.xPublication.publication.fandom.languageId == xPublication.publication.fandom.languageId
        }
        return false
    }

    fun isBottomsSameUser(): Boolean {
        val u = getBottomPublication()
        return u != null && u.creator.id == xPublication.publication.creator.id && u.fandom.id == xPublication.publication.fandom.id && u.fandom.languageId == u.fandom.languageId
    }

    fun getBottomCard(): CardChatMessage? {
        var myIndex = adapter.indexOf(this)
        myIndex++
        if (myIndex < adapter.size()) {
            val card = adapter.get(myIndex)
            if (card is CardChatMessage) return card
        }
        return null
    }

    fun getBottomPublication() = getBottomCard()?.xPublication?.publication as PublicationChatMessage?

    fun getTopCard(): CardChatMessage? {
        var myIndex = adapter.indexOf(this)
        myIndex--
        if (myIndex > -1) {
            val card = adapter.get(myIndex)
            if (card is CardChatMessage) return card
        }
        return null
    }

    fun getTopPublication(): PublicationChatMessage? {
        return getTopCard()?.xPublication?.publication as PublicationChatMessage?
    }

    fun showMenu(targetView: View, x: Float, y: Float) {
        val view = getView() ?: return
        val publication = xPublication.publication as PublicationChatMessage

        val vMenuReactions = FrameLayout(view.context)
        val vMenuReactionsLinear = LinearLayout(view.context)
        vMenuReactionsLinear.orientation = LinearLayout.HORIZONTAL
        vMenuReactions.addView(vMenuReactionsLinear)
        (vMenuReactionsLinear.layoutParams as FrameLayout.LayoutParams).gravity = Gravity.CENTER
        (vMenuReactionsLinear.layoutParams as FrameLayout.LayoutParams).topMargin = ToolsView.dpToPx(8).toInt()
        vMenuReactionsLinear.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        vMenuReactionsLinear.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT


        val w = SplashMenu()
                .addTitleView(vMenuReactions)
                .groupCondition(ControllerApi.isCurrentAccount(publication.creator.id))
                .add(t(API_TRANSLATE.app_remove)) {
                    val topCard = getTopCard()
                    val bottomCard = getBottomCard()
                    ControllerApi.removePublication(publication.id, t(API_TRANSLATE.chat_remove_confirm), t(API_TRANSLATE.chat_error_gone)) {
                        onRemoved(topCard, bottomCard)
                    }
                }
                .clearGroupCondition()
                .add(t(API_TRANSLATE.app_copy)) {
                    ToolsAndroid.setToClipboard(publication.text)
                    ToolsToast.show(t(API_TRANSLATE.app_copied))
                }.condition(copyEnabled)
                .add(t(API_TRANSLATE.app_history)) {  Navigator.to(SPublicationHistory(publication.id)) }.condition(ControllerPost.ENABLED_HISTORY)
                .groupCondition(!ControllerApi.isCurrentAccount(publication.creator.id))
                .add(t(API_TRANSLATE.app_report)) {  ControllerApi.reportPublication(publication.id, t(API_TRANSLATE.chat_report_confirm), t(API_TRANSLATE.chat_error_gone)) }.condition(publication.chatType == API.CHAT_TYPE_FANDOM_ROOT)
                .spoiler(t(API_TRANSLATE.app_moderator))
                .add(t(API_TRANSLATE.app_clear_reports)) {  ControllerApi.clearReportsPublication(publication.id, publication.publicationType) }.backgroundRes(R.color.blue_700).textColorRes(R.color.white).condition(publication.chatType == API.CHAT_TYPE_FANDOM_ROOT && ControllerApi.can(publication.fandom.id, publication.fandom.languageId, API.LVL_MODERATOR_BLOCK) && publication.reportsCount > 0)
                .add(t(API_TRANSLATE.app_block)) {
                    val topCard = getTopCard()
                    val bottomCard = getBottomCard()
                    ControllerPublications.block(publication) {
                        onRemoved(topCard, bottomCard)
                    }
                }.backgroundRes(R.color.blue_700).textColorRes(R.color.white).condition(publication.chatType == API.CHAT_TYPE_FANDOM_ROOT && ControllerApi.can(publication.fandom.id, publication.fandom.languageId, API.LVL_MODERATOR_BLOCK))
                .clearGroupCondition()
                .spoiler(t(API_TRANSLATE.app_protoadmin))
                .add("Востановить") {  ControllerPublications.restoreDeepBlock(publication.id) }.backgroundRes(R.color.orange_700).textColorRes(R.color.white).condition(ControllerApi.can(API.LVL_PROTOADMIN) && publication.status == API.STATUS_DEEP_BLOCKED)
                .asPopupShow(targetView, x, y)

        val p = ToolsView.dpToPx(4).toInt()
        for (i in API.REACTIONS.indices) {
            val v: ViewIcon = ToolsView.inflate(vMenuReactionsLinear, R.layout.z_icon_18)
            v.setPadding(p, p, p, p)
            v.setOnClickListener { sendReaction(i.toLong()); w?.hide(); }
            vMenuReactionsLinear.addView(v)
            ImageLoader.load(API.REACTIONS[i]).into(v)
        }
    }

    private fun onRemoved(topCard:CardChatMessage?, bottomCard:CardChatMessage?){
        val publication = xPublication.publication as PublicationChatMessage
        topCard?.update()
        bottomCard?.update()
        val p = topCard?.xPublication?.publication as PublicationChatMessage?
        if(p != null){
            EventBus.post(EventChatNewBottomMessage(p))
        }else{
            val n = PublicationChatMessage()
            n.chatType = publication.chatType
            n.fandom = publication.fandom
            EventBus.post(EventChatNewBottomMessage(n))
        }
    }

    private fun sendReaction(reactionIndex: Long) {
        ApiRequestsSupporter.executeProgressDialog(RPublicationsReactionAdd(xPublication.publication.id, reactionIndex)) { _ ->
            ToolsToast.show(t(API_TRANSLATE.app_done))
            EventBus.post(EventPublicationReactionAdd(xPublication.publication.id, reactionIndex))
        }
                .onApiError(API.ERROR_ALREADY) { ToolsToast.show(t(API_TRANSLATE.app_done)) }
                .onApiError(API.ERROR_GONE) { ToolsToast.show(t(API_TRANSLATE.comment_error_gone)) }
    }

    private fun removeReaction(reactionIndex: Long) {
        ApiRequestsSupporter.executeProgressDialog(RPublicationsReactionRemove(xPublication.publication.id, reactionIndex)) { _ ->
            ToolsToast.show(t(API_TRANSLATE.app_done))
            EventBus.post(EventPublicationReactionRemove(xPublication.publication.id, reactionIndex))
        }
                .onApiError(API.ERROR_GONE) { ToolsToast.show(t(API_TRANSLATE.comment_error_gone)) }
    }


    fun onClick(): Boolean {
        val publication = xPublication.publication as PublicationChatMessage
        if (publication.type == PublicationChatMessage.TYPE_SYSTEM && publication.systemType == PublicationChatMessage.SYSTEM_TYPE_BLOCK) {
            ControllerCampfireSDK.onToModerationClicked(publication.blockModerationEventId, 0, Navigator.TO)
            return true
        }

        if (onClick == null) {
            SChat.instance(ChatTag(publication.chatType, publication.fandom.id, publication.fandom.languageId), publication.id, false, Navigator.TO)
            return true
        } else {
            return onClick!!.invoke(publication)
        }
    }

    override fun notifyItem() {
        val publication = xPublication.publication as PublicationChatMessage
        ImageLoader.load(publication.creator.imageId).intoCash()
    }

    //
    //  Event Bus
    //

    private fun onEventPublicationDeepBlockRestore(e: EventPublicationDeepBlockRestore) {
        if (e.publicationId == xPublication.publication.id && xPublication.publication.status == API.STATUS_DEEP_BLOCKED) {
            adapter.remove(this)
        }
    }

    private fun onNotification(e: EventNotification) {
        val publication = xPublication.publication as PublicationChatMessage
        if (e.notification is NotificationChatMessageChange) {
            val n = e.notification
            if (n.publicationId == publication.id) {
                publication.text = n.text
                publication.changed = true
                update()
            }
        } else if (e.notification is NotificationChatMessageRemove) {
            if (e.notification.publicationId == publication.id) adapter.remove(this)

        }
    }

    private fun onEventChanged(e: EventChatMessageChanged) {
        val publication = xPublication.publication as PublicationChatMessage
        if (e.publicationId == publication.id) {
            publication.text = e.text
            publication.quoteId = e.quoteId
            publication.quoteText = e.quoteText
            publication.changed = true
            flash()
            update()
        }
    }

    private fun willHideLabel():Boolean{
        val topPublication = getTopPublication()
        return topPublication != null &&
                topPublication.creator.id == xPublication.publication.creator.id &&
                topPublication.dateCreate > xPublication.publication.dateCreate - 1000L * 60L * 5
    }

    private fun onEventChatReadDateChanged(e: EventChatReadDateChanged) {
        val publication = xPublication.publication as PublicationChatMessage
        if (e.tag == publication.chatTag()) {
            updateRead()
        }
    }

    private fun onEventPublicationBlocked(e: EventPublicationBlocked) {
        val publication = xPublication.publication as PublicationChatMessage
        if (!wasBlocked && e.firstBlockPublicationId == publication.id) {
            wasBlocked = true
            if (onBlocked != null && e.publicationChatMessage != null) onBlocked!!.invoke(e.publicationChatMessage)
        }
    }

    override fun equals(other: Any?): Boolean {
        val publication = xPublication.publication as PublicationChatMessage
        return if (other is CardChatMessage) publication.id == other.xPublication.publication.id
        else super.equals(other)
    }

}
