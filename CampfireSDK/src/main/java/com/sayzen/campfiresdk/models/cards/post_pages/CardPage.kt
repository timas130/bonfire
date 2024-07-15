package com.sayzen.campfiresdk.models.cards.post_pages

import android.view.View
import androidx.annotation.CallSuper
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.PagesContainer
import com.dzen.campfire.api.models.publications.post.*
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.compose.publication.post.pages.ComposeCardPage
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.splash.SplashMenu
import com.sup.dev.android.views.support.adapters.NotifyItem
import com.sup.dev.android.views.views.ViewIcon

abstract class CardPage(
        layout: Int,
        val pagesContainer: PagesContainer?,
        page: Page
) : Card(layout), NotifyItem {

    companion object {

        //
        //  Static
        //

        fun instance(pagesContainer: PagesContainer?, page: Page, composeMode: Boolean = false): CardPage {
            if (composeMode) {
                return if (page is PageSpoiler) {
                    CardPageSpoiler(pagesContainer, page)
                } else {
                    ComposeCardPage(pagesContainer, page)
                }
            }

            return when (page) {
                is PageText -> CardPageText(pagesContainer, page)
                is PageImage -> CardPageImage(pagesContainer, page)
                is PageImages -> CardPageImages(pagesContainer, page)
                is PageLink -> CardPageLink(pagesContainer, page)
                is PageLinkImage -> CardPageLinkImage(pagesContainer, page)
                is PageQuote -> CardPageQuote(pagesContainer, page)
                is PageSpoiler -> CardPageSpoiler(pagesContainer, page)
                is PagePolling -> CardPagePolling(pagesContainer, page)
                is PageVideo -> CardPageVideo(pagesContainer, page)
                is PageTable -> CardPageTable(pagesContainer, page)
                is PageDownload -> CardPageDownload(pagesContainer, page)
                is PageCampfireObject -> CardPageCampfireObject(pagesContainer, page)
                is PageUserActivity -> CardPageUserActivity(pagesContainer, page)
                is PageCode -> CardPageCode(pagesContainer, page)
                else -> CardPageUnknown(pagesContainer, page)
            }
        }
    }

    open var page = page
    var hided = false
    var editMode: Boolean = false
    var postIsDraft: Boolean = false
    var clickable = true
    var widgetMenu: SplashMenu
    var isSpoilerAvalible = true
    private var onMoveClicked: ((CardPage) -> Unit)? = null
    private var onChangeClicked: ((CardPage) -> Unit)? = null
    private var onRemoveClicked: ((CardPage) -> Unit)? = null

    init {
        widgetMenu = SplashMenu()
            .add(getChangeMenuItemText()) { if (onChangeClicked != null) onChangeClicked!!.invoke(this) }
            .add(t(API_TRANSLATE.app_remove)) { if (onRemoveClicked != null) onRemoveClicked!!.invoke(this) }
            .add(t(API_TRANSLATE.app_move)) { if (onMoveClicked != null) onMoveClicked!!.invoke(this) }
    }

    @CallSuper
    override fun bindView(view: View) {
        super.bindView(view)
        view.visibility = if (hided) View.GONE else View.VISIBLE
        val vMore:ViewIcon? = view.findViewById(R.id.vMore)

        if(vMore != null) {
            vMore.visibility = if (editMode) View.VISIBLE else View.GONE
            if (editMode) vMore.setOnClickListener { widgetMenu.asPopupShow(vMore) }
        }
    }

    fun setEditMod(
        editMode: Boolean,
        onMoveClicked: (CardPage) -> Unit,
        onChangeClicked: (CardPage) -> Unit,
        onRemoveClicked: (CardPage) -> Unit
    ): CardPage {
        this.editMode = editMode
        this.postIsDraft = this.postIsDraft || editMode
        this.onMoveClicked = onMoveClicked
        this.onChangeClicked = onChangeClicked
        this.onRemoveClicked = onRemoveClicked
        update()
        return this
    }

    fun setHidedX(hided: Boolean) {
        this.hided = hided
        update()
    }

    open fun getChangeMenuItemText() = t(API_TRANSLATE.app_change)


}
