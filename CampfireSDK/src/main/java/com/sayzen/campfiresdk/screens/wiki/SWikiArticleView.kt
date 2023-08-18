package com.sayzen.campfiresdk.screens.wiki

import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.PagesContainer
import com.dzen.campfire.api.models.publications.post.Page
import com.dzen.campfire.api.models.wiki.WikiPages
import com.dzen.campfire.api.models.wiki.WikiTitle
import com.dzen.campfire.api.requests.wiki.RWikiGetPages
import com.dzen.campfire.api.requests.wiki.RWikiItemGet
import com.dzen.campfire.api.requests.wiki.RWikiItemHistoryCancel
import com.dzen.campfire.api.requests.wiki.RWikiItemHistoryRestore
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.*
import com.sayzen.campfiresdk.models.cards.post_pages.CardPage
import com.sayzen.campfiresdk.models.events.wiki.EventWikiPagesChanged
import com.sayzen.campfiresdk.models.events.wiki.EventWikiRemove
import com.sayzen.campfiresdk.screens.wiki.history.EventWikiHistoryStatusChanged
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.splash.SplashAlert
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapter
import com.sup.dev.java.libs.eventBus.EventBus

class SWikiArticleView(
        val wikiTitle: WikiTitle,
        var languageId: Long,
        var startPages: WikiPages? = null
) : Screen(R.layout.screen_wiki_article), PagesContainer {

    companion object {

        fun instance(wikiItemId: Long, action: NavigationAction) {
            ApiRequestsSupporter.executeInterstitial(action, RWikiItemGet(wikiItemId)) { r ->
                SWikiArticleView(r.wikiTitle, ControllerApi.getLanguageId())
            }
        }

    }

    private val eventBus = EventBus
            .subscribe(EventWikiPagesChanged::class) { this.onEventWikiPagesChanged(it) }
            .subscribe(EventWikiRemove::class) { if (it.itemId == wikiTitle.itemId) Navigator.remove(this) }
            .subscribe(EventWikiHistoryStatusChanged::class) {
                if (it.itemId == loadedPages?.itemId && it.languageId == loadedPages?.languageId) reload()
                if (it.itemId == startPages?.itemId && it.languageId == startPages?.languageId) Navigator.remove(this)
            }

    private val vToolbarCollapsingShadow: View = findViewById(R.id.vToolbarCollapsingShadow)
    private val vImageTitle: ImageView = findViewById(R.id.vImageTitle)
    private val vToolbarTitle: TextView = findViewById(R.id.vToolbarTitle)
    private val vMore: View = findViewById(R.id.vMore)
    private val vAvatarTouch: View = findViewById(R.id.vAvatarTouch)
    private val vAvatar: ImageView = findViewById(R.id.vAvatar)
    private val vRecycler: RecyclerView = findViewById(R.id.vRecycler)
    private val vMessage: TextView = findViewById(R.id.vMessage)
    private val vProgressLine: View = findViewById(R.id.vProgressLine)
    private val vAction: Button = findViewById(R.id.vAction)
    private val vLanguage: TextView = findViewById(R.id.vLanguage)
    private val vEdit: View = findViewById(R.id.vEdit)

    private val adapter = RecyclerCardAdapter()
    private var loadedPages: WikiPages? = null
    private var pages: Array<Page> = emptyArray()
    private var isLoading = false
    private var error = false
    private var wasSwitchedToEnglish = false

    init {
        disableNavigation()
        disableShadows()

        vToolbarCollapsingShadow.background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(0x60000000, 0x00000000))
        ImageLoader.loadGif(wikiTitle.imageId, 0, vAvatar)
        ImageLoader.loadGif(wikiTitle.imageBigId, 0, vImageTitle)

        vAction.text = t(API_TRANSLATE.app_retry)
        vRecycler.layoutManager = LinearLayoutManager(context)
        vRecycler.adapter = adapter

        vMore.setOnClickListener { ControllerWiki.showMenu(wikiTitle, languageId, null, vMore, 0f, 0f) }

        vLanguage.setOnClickListener {
            ControllerCampfireSDK.createLanguageMenu(languageId) { languageId ->
                this.languageId = languageId
                reload()
            }.asSheetShow()
        }

        vEdit.setOnClickListener { ControllerWiki.toEditArticle(wikiTitle.itemId, languageId) }

        if (startPages != null) {
            vMore.visibility = View.GONE
            vEdit.visibility = View.GONE
            pages = startPages!!.pages
            for (i in pages) adapter.add(CardPage.instance(this, i))
            ControllerPost.updateSpoilers(adapter)
            updateMessage()
            if (ControllerApi.can(wikiTitle.fandomId, languageId, API.LVL_MODERATOR_WIKI_EDIT)) {
                if (startPages!!.wikiStatus == API.STATUS_REMOVED) {
                    vLanguage.text = t(API_TRANSLATE.app_restore)
                    vLanguage.setOnClickListener { restore() }
                } else {
                    vLanguage.text = t(API_TRANSLATE.app_remove)
                    vLanguage.setOnClickListener { cancel() }
                }
            } else {
                vLanguage.isEnabled = false
            }
        } else {
            reload()
        }

    }

    private fun cancel() {
        SplashAlert()
                .setText(t(API_TRANSLATE.wiki_cancel_alert))
                .setOnCancel(t(API_TRANSLATE.app_no))
                .setOnEnter(t(API_TRANSLATE.app_yes)) { w ->
                    ApiRequestsSupporter.executeEnabled(w, RWikiItemHistoryCancel(startPages!!.id)) {
                                EventBus.post(EventWikiHistoryStatusChanged(startPages!!.id, startPages!!.itemId, startPages!!.languageId, API.STATUS_REMOVED))
                                EventBus.post(EventWikiHistoryStatusChanged(it.newPagesId, startPages!!.itemId, startPages!!.languageId, API.STATUS_PUBLIC))
                            }
                            .onApiError(RWikiItemHistoryCancel.E_LAST_ITEM) {
                                ToolsToast.show(t(API_TRANSLATE.wiki_cancel_error))
                            }
                }
                .asSheetShow()
    }

    private fun restore() {
        SplashAlert()
                .setText(t(API_TRANSLATE.wiki_restore_alert))
                .setOnCancel(t(API_TRANSLATE.app_no))
                .setOnEnter(t(API_TRANSLATE.app_yes)) { w ->
                    ApiRequestsSupporter.executeEnabled(w, RWikiItemHistoryRestore(startPages!!.id)) {
                                EventBus.post(EventWikiHistoryStatusChanged(startPages!!.id, startPages!!.itemId, startPages!!.languageId, API.STATUS_DRAFT))
                                EventBus.post(EventWikiHistoryStatusChanged(it.newPagesId, startPages!!.itemId, startPages!!.languageId, API.STATUS_PUBLIC))
                            }
                            .onApiError(RWikiItemHistoryCancel.E_LAST_ITEM) {
                                ToolsToast.show(t(API_TRANSLATE.wiki_cancel_error))
                            }
                }
                .asSheetShow()
    }

    private fun reload() {
        isLoading = true
        error = false
        adapter.clear()
        updateMessage()
        vLanguage.text = ControllerApi.getLanguage(languageId).name
        vToolbarTitle.text = wikiTitle.getName(ControllerApi.getLanguage(languageId).code)
        RWikiGetPages(wikiTitle.itemId, languageId)
                .onComplete {
                    isLoading = false
                    if ((it.wikiPages == null || it.wikiPages!!.pages.isEmpty()) && languageId != 1L && !wasSwitchedToEnglish) {
                        languageId = 1L
                        wasSwitchedToEnglish = true
                        reload()
                    } else {
                        wasSwitchedToEnglish = true
                        loadedPages = it.wikiPages
                        pages = it.wikiPages?.pages ?: emptyArray()
                        adapter.clear()
                        for (i in pages) adapter.add(CardPage.instance(this, i))
                        ControllerPost.updateSpoilers(adapter)
                        updateMessage()
                    }
                }
                .onApiError {
                    isLoading = false
                    if (it.code == API.ERROR_GONE) {
                        if (languageId != 1L) {
                            languageId = 1L
                            reload()
                        } else {
                            updateMessage()
                        }
                    } else {
                        isLoading = false
                        error = true
                        updateMessage()
                    }
                }
                .onNetworkError {
                    isLoading = false
                    error = true
                    updateMessage()
                }
                .send(api)
    }

    private fun updateMessage() {
        vMessage.visibility = View.VISIBLE
        vProgressLine.visibility = View.GONE
        vAction.visibility = View.GONE
        if (isLoading) {
            vProgressLine.visibility = View.VISIBLE
            vMessage.setText(t(API_TRANSLATE.wiki_article_loading))
        } else {
            if (error) {
                vAction.visibility = View.VISIBLE
                vMessage.setText(t(API_TRANSLATE.error_network))
            } else {
                if (pages.isEmpty()) {
                    vMessage.setText(t(API_TRANSLATE.wiki_article_empty))
                } else {
                    vMessage.visibility = View.GONE
                }
            }
        }


    }

    override fun getPagesArray() = pages
    override fun getSourceType() = API.PAGES_SOURCE_TYPE_WIKI
    override fun getSourceId() = wikiTitle.itemId
    override fun getSourceIdSub() = languageId


    //
    //  EventBus
    //

    private fun onEventWikiPagesChanged(e: EventWikiPagesChanged) {
        if (e.itemId == wikiTitle.itemId) {
            if (e.languageId != languageId) {
                languageId = e.languageId
                reload()
            } else {
                adapter.remove(CardPage::class)
                pages = e.pages
                for (i in pages) adapter.add(CardPage.instance(this, i))
                ControllerPost.updateSpoilers(adapter)
                updateMessage()
            }
        }
    }

}
