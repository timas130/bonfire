package com.sayzen.campfiresdk.screens.post.create.creators

import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.post.*
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.cards.post_pages.*
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.splash.view.SplashViewSheet
import com.sup.dev.android.views.splash.Splash
import com.sup.dev.android.views.splash.SplashAlert
import com.sup.dev.android.views.splash.SplashMenu

class SplashAdd(
        private val requestPutPage: (Page, Screen?, Splash?, (Page) -> CardPage, ((CardPage) -> Unit)) -> Unit,
        private val requestChangePage: (Page, CardPage, Screen?, Splash?, (Page) -> Unit) -> Unit,
        private val onBackEmptyAndNewerAdd: () -> Unit
) : SplashMenu() {

    var wasClicked = false
    var wasShowed = false

    init {
        add(t(API_TRANSLATE.post_page_text)) {
            wasClicked = true
            Navigator.to(SCreatePageText(requestPutPage, requestChangePage, null, null))
        }.icon(R.drawable.ic_text_fields_white_24dp)
        add(t(API_TRANSLATE.post_page_image)) {
            wasClicked = true
            SplashPageImage(requestPutPage, requestChangePage).asSheetShow()
        }.icon(R.drawable.ic_landscape_white_24dp)
        add(t(API_TRANSLATE.post_page_video)) {
            wasClicked = true
            SplashPageVideo(requestPutPage, requestChangePage, null, null).asSheetShow()
        }.icon(R.drawable.ic_play_arrow_white_24dp)
        add(t(API_TRANSLATE.post_page_quote)) {
            wasClicked = true
            SplashPageQuote(requestPutPage, requestChangePage, null, null).asSheetShow()
        }.icon(R.drawable.ic_format_quote_white_24dp)
        add(t(API_TRANSLATE.post_page_link)) {
            wasClicked = true
            SplashPageLink(requestPutPage, requestChangePage, null, null).asSheetShow()
        }.icon(R.drawable.ic_insert_link_white_24dp)
        add(t(API_TRANSLATE.post_page_spoiler)) {
            wasClicked = true
            SplashPageSpoiler(requestPutPage, requestChangePage, null, null).asSheetShow()
        }.icon(R.drawable.eye_off)
        add(t(API_TRANSLATE.post_page_polling)) {
            wasClicked = true
            Navigator.to(SCreatePagePolling(requestPutPage, requestChangePage, null, null))
        }.icon(R.drawable.ic_check_box_white_24dp)
        add(t(API_TRANSLATE.post_page_link_image)) {
            wasClicked = true
            SplashPageLinkImage(requestPutPage, requestChangePage, null, null).asSheetShow()
        }.icon(R.drawable.ic_insert_link_white_24dp)
        add(t(API_TRANSLATE.post_page_table)) {
            wasClicked = true
            Navigator.to(SCreatePageTable(requestPutPage, requestChangePage, null, null))
        }.icon(R.drawable.ic_border_all_white_24dp)
        add(t(API_TRANSLATE.post_page_code)) {
            wasClicked = true
            Navigator.to(SCreatePageCode(requestPutPage, requestChangePage, null, null))
        }.icon(R.drawable.ic_code_white_24dp)
        add(t(API_TRANSLATE.post_page_campfire_object)) {
            wasClicked = true
            SplashPageCampfireObject(requestPutPage, requestChangePage, null, null).asSheetShow()
        }.icon(R.drawable.ic_whatshot_white_24dp)
    }

    fun changePage(c: CardPage) {
        if (c is CardPageText) Navigator.to(SCreatePageText(requestPutPage, requestChangePage, c, c.page as PageText))
        if (c is CardPageImage) SplashPageImage.change(c.page, requestPutPage, requestChangePage, c)
        if (c is CardPageVideo) SplashPageVideo(requestPutPage, requestChangePage, c, c.page as PageVideo).asSheetShow()
        if (c is CardPageQuote) SplashPageQuote(requestPutPage, requestChangePage, c, c.page as PageQuote).asSheetShow()
        if (c is CardPageLink) SplashPageLink(requestPutPage, requestChangePage, c, c.page as PageLink).asSheetShow()
        if (c is CardPageLinkImage) SplashPageLinkImage(requestPutPage, requestChangePage, c, c.page as PageLinkImage).asSheetShow()
        if (c is CardPageSpoiler) SplashPageSpoiler(requestPutPage, requestChangePage, c, c.page as PageSpoiler).asSheetShow()
        if (c is CardPagePolling) Navigator.to(SCreatePagePolling(requestPutPage, requestChangePage, c, c.page as PagePolling))
        if (c is CardPageImages) SplashPageImages(requestChangePage, c, c.page as PageImages).asSheetShow()
        if (c is CardPageTable) Navigator.to(SCreatePageTable(requestPutPage, requestChangePage, c, c.page as PageTable))
        if (c is CardPageCode) Navigator.to(SCreatePageCode(requestPutPage, requestChangePage, c, c.page as PageCode))
        if (c is CardPageCampfireObject) SplashPageCampfireObject(requestPutPage, requestChangePage, c, c.page as PageCampfireObject).asSheetShow()
    }

    override fun asSheetShow(): SplashViewSheet {
        wasShowed = true
        return super.asSheetShow()
    }

    override fun onHide() {
        super.onHide()
        if (wasShowed && !wasClicked) onBackEmptyAndNewerAdd.invoke()    //  Для того чтобы выйти из создания поста на клавишу Back
    }

    companion object {

        fun showConfirmCancelDialog(splash: Splash) {
            splash.setEnabled(false)
            SplashAlert()
                    .setText(t(API_TRANSLATE.post_create_cancel_alert))
                    .setOnEnter(t(API_TRANSLATE.app_yes)) { _ -> splash.hide() }
                    .setOnCancel(t(API_TRANSLATE.app_no))
                    .setOnHide { splash.setEnabled(true) }
                    .asSheetShow()
        }

        fun showConfirmCancelDialog(screen: Screen, onYes: (() -> Unit)? = null) {
            SplashAlert()
                    .setText(t(API_TRANSLATE.post_create_cancel_alert))
                    .setOnEnter(t(API_TRANSLATE.app_yes)) {
                        if (onYes == null) Navigator.remove(screen)
                        else onYes.invoke()
                    }
                    .setOnCancel(t(API_TRANSLATE.app_no))
                    .asSheetShow()
        }
    }
}
