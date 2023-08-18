package com.sayzen.campfiresdk.models.cards.stickers

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.stickers.PublicationStickersPack
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerStickers
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.controllers.tCap
import com.sayzen.campfiresdk.models.cards.CardPublication
import com.sayzen.campfiresdk.models.events.stickers.EventStickersPackChanged
import com.sayzen.campfiresdk.models.splashs.SplashComment
import com.sayzen.campfiresdk.screens.comments.SComments
import com.sayzen.campfiresdk.screens.account.stickers.SStickersView
import com.sayzen.campfiresdk.screens.reports.SReports
import com.sayzen.campfiresdk.views.ViewKarma
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.java.libs.eventBus.EventBus

class CardStickersPack(
        publication: PublicationStickersPack,
        val isShowFullInfo: Boolean = false,
        val isShowReports: Boolean = true,
        val isShowKarmaAndMenuAndComments: Boolean = true
) : CardPublication(R.layout.card_stickers_pack, publication) {

    private val eventBus = EventBus
            .subscribe(EventStickersPackChanged::class) {
                if (it.stickersPack.id == publication.id) {
                    publication.imageId = it.stickersPack.imageId
                    publication.name = it.stickersPack.name
                    update()
                }
            }

    var onClick:(PublicationStickersPack)->Unit = {
        Navigator.to(SStickersView(publication, 0))
    }

    override fun bindView(view: View) {
        super.bindView(view)
        val publication = xPublication.publication as PublicationStickersPack

        val vTouch:View = view.findViewById(R.id.vTouch)
        val vAvatar: ViewAvatarTitle = view.findViewById(R.id.vAvatar)
        val vMenu: View = view.findViewById(R.id.vMenu)
        val vTitle: TextView = view.findViewById(R.id.vTitle)
        val vComments: TextView = view.findViewById(R.id.vComments)

        vComments.text = publication.subPublicationsCount.toString() + ""

        vMenu.visibility = if(isShowKarmaAndMenuAndComments) View.VISIBLE else View.GONE
        vComments.visibility = if(isShowKarmaAndMenuAndComments) View.VISIBLE else View.GONE
        vTitle.visibility = if(isShowFullInfo) View.VISIBLE else View.GONE
        vTitle.text = tCap(API_TRANSLATE.sticker_event_create_stickers_pack, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_created), t(API_TRANSLATE.she_created)))

        ImageLoader.load(publication.imageId).into(vAvatar.vAvatar.vImageView)
        vAvatar.setTitle(publication.name)
        vAvatar.setSubtitle(publication.creator.name)

        vComments.setOnClickListener {
            SComments.instance(publication.id, 0, Navigator.TO)
        }
        vComments.setOnLongClickListener {
            SplashComment(publication.id, null, true) { }.asSheetShow()
            true
        }

        vMenu.setOnClickListener { ControllerStickers.showStickerPackPopup(vMenu, 0f, 0f, publication) }

        vTouch.setOnClickListener { onClick.invoke(publication) }
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

    override fun updateReports() {
        if(getView() == null) return
        val vReports:TextView = getView()!!.findViewById(R.id.vReports)?:return
        vReports.setOnClickListener { Navigator.to(SReports(xPublication.publication.id)) }
        xPublication.xReports.setView(vReports)
    }

    override fun updateKarma() {
        if (getView() == null) return
        val vKarma: ViewKarma = getView()!!.findViewById(R.id.vKarma)
        xPublication.xKarma.setView(vKarma)
        vKarma.visibility = if(isShowKarmaAndMenuAndComments) View.VISIBLE else View.GONE
    }

    override fun updateReactions() {
        update()
    }

    override fun notifyItem() {
        val publication = xPublication.publication as PublicationStickersPack
        ImageLoader.load(publication.imageId).intoCash()
    }


}
