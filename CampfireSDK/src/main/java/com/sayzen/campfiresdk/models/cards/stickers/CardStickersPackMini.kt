package com.sayzen.campfiresdk.models.cards.stickers

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.stickers.PublicationStickersPack
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerStickers
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.controllers.tCap
import com.sayzen.campfiresdk.models.cards.CardPublication
import com.sayzen.campfiresdk.models.events.stickers.EventStickersPackChanged
import com.sayzen.campfiresdk.screens.account.stickers.SStickersView
import com.sayzen.campfiresdk.screens.reports.SReports
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.java.libs.eventBus.EventBus

class CardStickersPackMini(
        publication: PublicationStickersPack,
        val isShowFullInfo: Boolean = false,
        val isShowReports: Boolean = false
) : CardPublication(R.layout.card_sticker, publication) {

    private val eventBus = EventBus
            .subscribe(EventStickersPackChanged::class) {
                if (it.stickersPack.id == publication.id) {
                    publication.imageId = it.stickersPack.imageId
                    publication.name = it.stickersPack.name
                    update()
                }
            }

    var onClick: (PublicationStickersPack) -> Unit = { Navigator.to(SStickersView(publication, 0)) }
    var onLongClick: ((PublicationStickersPack) -> Unit)? = null

    override fun bindView(view: View) {
        super.bindView(view)
        val publication = xPublication.publication as PublicationStickersPack

        val vImage: ImageView = view.findViewById(R.id.vImage)
        val vProgress: View = view.findViewById(R.id.vProgress)
        val vRootContainer: View = view.findViewById(R.id.vRootContainer)
        val vTitle: TextView = view.findViewById(R.id.vTitle)
        val vMenu: View = view.findViewById(R.id.vMenu)

        vProgress.visibility = View.GONE
        vTitle.visibility = if (isShowFullInfo) View.VISIBLE else View.GONE
        vMenu.visibility = if (isShowFullInfo || isShowReports) View.VISIBLE else View.GONE
        vTitle.text = tCap(API_TRANSLATE.sticker_event_create_sticker, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_add), t(API_TRANSLATE.she_add)))

        vMenu.setOnClickListener { ControllerStickers.showStickerPackPopup(vMenu, 0f, 0f, publication) }

        if (isShowFullInfo) {
            ToolsView.setOnLongClickCoordinates(vRootContainer) { _, _, _ ->
                if (onLongClick != null) onLongClick?.invoke(publication)
            }
            view.setOnClickListener { SStickersView.instanceBySticker(publication.id, Navigator.TO) }
        } else {
            ToolsView.setOnLongClickCoordinates(vRootContainer) { _, x, y ->
                if (onLongClick != null) onLongClick?.invoke(publication)
                else ControllerStickers.showStickerPackPopup(vRootContainer, x, y, publication)
            }
            view.setOnClickListener { onClick.invoke(publication) }
            vRootContainer.setBackgroundColor(0x00000000)
        }

        ImageLoader.load(publication.imageId).into(vImage)
    }

    override fun updateAccount() {
        update()
    }

    override fun updateComments() {
        update()
    }

    override fun updateFandom() {
        update()
    }

    override fun updateKarma() {
        update()
    }

    override fun updateReports() {
        if (getView() == null) return
        val vReports:TextView = getView()!!.findViewById(R.id.vReports)?:return
        vReports.setOnClickListener { Navigator.to(SReports(xPublication.publication.id)) }
        xPublication.xReports.setView(vReports)
    }

    override fun updateReactions() {
        update()
    }

    override fun notifyItem() {
        val publication = xPublication.publication as PublicationStickersPack
        ImageLoader.load(publication.imageId).intoCash()
    }

}