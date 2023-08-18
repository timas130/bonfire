package com.sayzen.campfiresdk.screens.account.stickers

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.stickers.PublicationSticker
import com.dzen.campfire.api.models.publications.stickers.PublicationStickersPack
import com.dzen.campfire.api.requests.stickers.RStickersAdd
import com.dzen.campfire.api.requests.stickers.RStickersGetAllByPackId
import com.dzen.campfire.api.requests.stickers.RStickersPacksGetInfo
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerStickers
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.cards.stickers.CardSticker
import com.sayzen.campfiresdk.models.events.publications.EventPublicationRemove
import com.sayzen.campfiresdk.models.events.stickers.EventStickerCreate
import com.sayzen.campfiresdk.models.events.stickers.EventStickersPackChanged
import com.sayzen.campfiresdk.screens.account.profile.SProfile
import com.sayzen.campfiresdk.screens.comments.SComments
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sayzen.campfiresdk.support.adapters.XComments
import com.sayzen.campfiresdk.support.adapters.XKarma
import com.sayzen.campfiresdk.views.ViewKarma
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.*
import com.sup.dev.android.views.screens.SCrop
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.splash.SplashChooseImage
import com.sup.dev.android.views.splash.SplashProgressTransparent
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsBytes
import com.sup.dev.java.tools.ToolsThreads

class SStickersView(
        val stickersPack: PublicationStickersPack,
        val stickerId: Long
) : SLoadingRecycler<CardSticker, PublicationSticker>(R.layout.screen_stickers_view) {

    companion object {

        fun instanceBySticker(stickerId: Long, action: NavigationAction) {
            instance(0, stickerId, action)
        }

        fun instance(packId: Long, action: NavigationAction) {
            instance(packId, 0, action)
        }

        fun instance(packId: Long, stickerId: Long, action: NavigationAction) {
            ApiRequestsSupporter.executeInterstitial(action, RStickersPacksGetInfo(packId, stickerId)) { r ->
                SStickersView(r.stickersPack, stickerId)
            }
        }

    }

    private val eventBus = EventBus
            .subscribe(EventStickerCreate::class) { onEventStickerCreate(it) }
            .subscribe(EventStickersPackChanged::class) { onEventStickersPackChanged(it) }
            .subscribe(EventPublicationRemove::class) { if (it.publicationId == stickersPack.id) Navigator.remove(this) }

    private val vAvatarTitle: ViewAvatarTitle = findViewById(R.id.vAvatarTitle)
    private val vCommentsCount: TextView = findViewById(R.id.vCommentsCount)
    private val vKarma: ViewKarma = findViewById(R.id.vKarma)
    private val vCommentsContainer: View = findViewById(R.id.vCommentsContainer)
    private var loaded = false
    private val xKarma = XKarma(stickersPack) { updateKarma() }
    private val xComments = XComments(stickersPack) { updateComments() }

    init {
        disableNavigation()
        disableShadows()
        setTextEmpty(t(API_TRANSLATE.stickers_pack_view_empty))
        setBackgroundImage(API_RESOURCES.IMAGE_BACKGROUND_4)

        val spanCount = if (ToolsAndroid.isScreenPortrait()) 3 else 6
        vRecycler.layoutManager = GridLayoutManager(context, spanCount)
        ToolsView.setRecyclerAnimation(vRecycler)
        addToolbarIcon(R.drawable.ic_more_vert_white_24dp) {
            ControllerStickers.showStickerPackPopup(it, 0f, 0f, stickersPack)
        }

        vFab.setImageResource(R.drawable.ic_add_white_24dp)
        vFab.setOnClickListener { chooseImage() }
        if (stickersPack.creator.id == ControllerApi.account.getId() && ControllerApi.can(API.LVL_CREATE_STICKERS)) (vFab as View).visibility = View.VISIBLE



        vAvatarTitle.setOnClickListener {
            SProfile.instance(stickersPack.creator, Navigator.TO)
        }

        vCommentsContainer.setOnClickListener {
            Navigator.to(SComments(stickersPack.id, 0))
        }

        updateTitle()
        updateKarma()
        updateComments()

        adapter.setShowLoadingCardBottom(false)
        adapter.setBottomLoader { onLoad, _ ->
            subscription = RStickersGetAllByPackId(stickersPack.id)
                    .onComplete { r ->
                        if (loaded) {
                            onLoad.invoke(emptyArray())
                        } else {
                            loaded = true
                            onLoad.invoke(r.stickers)
                        }
                    }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }
    }

    override fun classOfCard() = CardSticker::class

    override fun map(item: PublicationSticker): CardSticker {
        val card = CardSticker(item)
        if (item.id == stickerId) card.flash()
        return card
    }

    private fun updateComments() {
        xComments.setView(vCommentsCount)
    }

    private fun updateKarma() {
        xKarma.setView(vKarma)
    }

    private fun updateTitle() {
        vAvatarTitle.setTitle(stickersPack.name)
        vAvatarTitle.setSubtitle(stickersPack.creator.name)
        ImageLoader.load(stickersPack.imageId).into(vAvatarTitle.vAvatar.vImageView)

    }

    private fun chooseImage() {


        if (adapter.size(CardSticker::class) >= API.STICKERS_MAX_COUNT_IN_PACK) {
            ToolsToast.show(t(API_TRANSLATE.stickers_message_too_many))
            return
        }

        SplashChooseImage()
                .setOnSelected { _, bytes, _ ->

                    ToolsThreads.thread {

                        val bitmap = ToolsBitmap.decode(bytes)
                        if (bitmap == null) {
                            ToolsToast.show(t(API_TRANSLATE.error_cant_load_image))
                            return@thread
                        }

                        ToolsThreads.main {

                            val isGif = ToolsBytes.isGif(bytes)
                            val cropSize = if (isGif) API.STICKERS_IMAGE_SIDE_GIF else API.STICKERS_IMAGE_SIDE

                            Navigator.to(SCrop(bitmap, cropSize, cropSize) { _, b2, x, y, w, h ->
                                if (isGif) {

                                    val d = ToolsView.showProgressDialog()
                                    ToolsThreads.thread {
                                        val bytesGif = ToolsGif.resize(bytes, API.STICKERS_IMAGE_SIDE_GIF, API.STICKERS_IMAGE_SIDE_GIF, x, y, w, h, true)
                                        ControllerApi.toBytes(b2, API.STICKERS_IMAGE_WEIGHT, API.STICKERS_IMAGE_SIDE_GIF, API.STICKERS_IMAGE_SIDE_GIF, true) {
                                            if (it == null) d.hide()
                                            else {
                                                ToolsThreads.main {
                                                    if (bytesGif.size > API.STICKERS_IMAGE_WEIGHT_GIF) {
                                                        d.hide()
                                                        ToolsToast.show(t(API_TRANSLATE.error_too_long_file))
                                                    } else {
                                                        changeAvatarNow(d, it, bytesGif)
                                                    }
                                                }
                                            }
                                        }
                                    }

                                } else {
                                    val d = ToolsView.showProgressDialog()
                                    ControllerApi.toBytes(b2, API.STICKERS_IMAGE_WEIGHT, API.STICKERS_IMAGE_SIDE, API.STICKERS_IMAGE_SIDE, true) {
                                        if (it == null) d.hide()
                                        else changeAvatarNow(d, it, null)
                                    }
                                }
                            })

                        }


                    }


                }
                .asSheetShow()

    }

    private fun changeAvatarNow(dialog: SplashProgressTransparent, bytes: ByteArray, gifBytes: ByteArray?) {
        ApiRequestsSupporter.executeProgressDialog(dialog, RStickersAdd(stickersPack.id, bytes, gifBytes)) { r ->
            ToolsToast.show(t(API_TRANSLATE.app_done))
            EventBus.post(EventStickerCreate(r.sticker))
        }
    }

    private fun onEventStickerCreate(e: EventStickerCreate) {
        if (e.sticker.tag_1 == stickersPack.id) {
            val card = CardSticker(e.sticker)
            adapter.add(card)
            card.flash()
            setState(State.NONE)
        }
    }

    private fun onEventStickersPackChanged(e: EventStickersPackChanged) {
        if (e.stickersPack.id == stickerId) {
            stickersPack.name = e.stickersPack.name
            stickersPack.imageId = e.stickersPack.imageId
            updateTitle()
        }
    }


}