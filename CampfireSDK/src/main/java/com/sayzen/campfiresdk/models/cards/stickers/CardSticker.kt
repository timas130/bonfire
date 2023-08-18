package com.sayzen.campfiresdk.models.cards.stickers

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.stickers.PublicationSticker
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerStickers
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.controllers.tCap
import com.sayzen.campfiresdk.models.cards.CardPublication
import com.sayzen.campfiresdk.screens.account.stickers.SStickersView
import com.sayzen.campfiresdk.screens.reports.SReports
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.screens.SImageView

class CardSticker(
        publication: PublicationSticker,
        val isShowFullInfo: Boolean = false,
        val isShowReports: Boolean = false
) : CardPublication(if (isShowFullInfo) R.layout.card_sticker_info else R.layout.card_sticker, publication) {

    var onClick: (PublicationSticker) -> Unit = { Navigator.to(SImageView(ImageLoader.load(if (publication.gifId == 0L) publication.imageId else publication.gifId))) }
    var onLongClick: ((PublicationSticker) -> Unit)? = null

    override fun bindView(view: View) {
        super.bindView(view)
        val publication = xPublication.publication as PublicationSticker

        val vImage: ImageView = view.findViewById(R.id.vImage)
        val vProgress: View = view.findViewById(R.id.vProgress)
        val vRootContainer: View = view.findViewById(R.id.vRootContainer)
        val vTitle: TextView = view.findViewById(R.id.vTitle)
        val vMenu: View = view.findViewById(R.id.vMenu)

        vTitle.visibility = if (isShowFullInfo) View.VISIBLE else View.GONE
        vMenu.visibility = if (isShowFullInfo || isShowReports) View.VISIBLE else View.GONE
        vTitle.text = tCap(API_TRANSLATE.sticker_event_create_sticker, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_add), t(API_TRANSLATE.she_add)))

        vMenu.setOnClickListener { ControllerStickers.showStickerPopup(vMenu, 0f, 0f, publication) }

        if (isShowFullInfo) {
            ToolsView.setOnLongClickCoordinates(vRootContainer) { _, _, _ ->
                if (onLongClick != null) onLongClick?.invoke(publication)
            }
            vRootContainer.setOnClickListener { SStickersView.instanceBySticker(publication.id, Navigator.TO) }
        } else {
            ToolsView.setOnLongClickCoordinates(vRootContainer) { _, x, y ->
                if (onLongClick != null) onLongClick?.invoke(publication)
                else ControllerStickers.showStickerPopup(vRootContainer, x, y, publication)
            }
            view.setOnClickListener { onClick.invoke(publication) }
            vRootContainer.setBackgroundColor(0x00000000)
        }

        ImageLoader.loadGif(publication.imageId, publication.gifId, vImage, vProgress)
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
        val publication = xPublication.publication as PublicationSticker
        ImageLoader.load(publication.imageId).intoCash()
    }

}
