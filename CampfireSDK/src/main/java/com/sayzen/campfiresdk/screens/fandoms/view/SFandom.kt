package com.sayzen.campfiresdk.screens.fandoms.view

import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.models.publications.Publication
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.requests.fandoms.RFandomsGet
import com.dzen.campfire.api.requests.fandoms.RFandomsGetProfile
import com.dzen.campfire.api.requests.publications.RPublicationsGetAll
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.*
import com.sayzen.campfiresdk.models.PostList
import com.sayzen.campfiresdk.models.cards.CardPost
import com.sayzen.campfiresdk.models.cards.CardPublication
import com.sayzen.campfiresdk.models.events.fandom.EventFandomChanged
import com.sayzen.campfiresdk.models.events.fandom.EventFandomRemove
import com.sayzen.campfiresdk.models.events.publications.EventPostPinedFandom
import com.sayzen.campfiresdk.models.events.publications.EventPostStatusChange
import com.sayzen.campfiresdk.screens.post.create.SPostCreate
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sayzen.campfiresdk.support.adapters.XFandom
import com.sup.dev.android.libs.image_loader.ImageLoaderId
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.screens.SImageView
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsThreads

class SFandom private constructor(
        fandom: Fandom
) : Screen(R.layout.screen_fandom), PostList {

    companion object {
        fun instance(fandom: Fandom, action: NavigationAction) {
            if (fandom.languageId < 1L) fandom.languageId = ControllerApi.getLanguageId()
            Navigator.action(action, SFandom(fandom))
        }

        fun instance(fandomId: Long, action: NavigationAction) {
            instance(fandomId, ControllerApi.getLanguageId(), action)
        }

        fun instance(fandomId: Long, languageId: Long, action: NavigationAction) {
            var languageIdV = languageId
            if (languageIdV < 1L) languageIdV = ControllerApi.getLanguageId()
            ApiRequestsSupporter.executeInterstitial(action, RFandomsGet(fandomId, languageIdV, ControllerApi.getLanguageId())) { r -> SFandom(r.fandom) }
        }
    }

    private val eventBus = EventBus
            .subscribe(EventPostStatusChange::class) { onEventPostStatusChange(it) }
            .subscribe(EventFandomRemove::class) { Navigator.remove(this) }
            .subscribe(EventPostPinedFandom::class) { if (it.fandomId == xFandom.getId() && it.languageId == xFandom.getLanguageId()) setPinnedPost(it.post) }

    private val vToolbarCollapsingShadow: View = findViewById(R.id.vToolbarCollapsingShadow)
    private val vTitle: TextView = findViewById(R.id.vToolbarTitle)
    private val vRecycler: RecyclerView = findViewById(R.id.vRecycler)
    private val vImageTitle: ImageView = findViewById(R.id.vImageTitle)
    private val vFab: View = findViewById(R.id.vFab)
    private val vMore: ViewIcon = findViewById(R.id.vMore)
    private val vLanguage: ViewIcon = findViewById(R.id.vLanguage)

    private val adapter: RecyclerCardAdapterLoading<CardPublication, Publication>
    private val xFandom = XFandom().setFandom(fandom).setOnChanged { update() }
    private val cardFilters: CardFilters = CardFilters {
        if (cardPinnedPost != null) setPinnedPost(cardPinnedPost!!.xPublication.publication as PublicationPost)
        reload()
    }
    private val cardTitle = CardTitle(xFandom, fandom.category)
    private val cardButtons = CardButtons(xFandom)
    private val cardViceroy = CardViceroy(xFandom.getId(), xFandom.getLanguageId())
    private val cardFandomInfo = CardFandomInfo(xFandom, fandom.karmaCof)
    private var cardPinnedPost: CardPost? = null

    init {
        vToolbarCollapsingShadow.background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(0x60000000, 0x00000000))

        adapter = RecyclerCardAdapterLoading<CardPublication, Publication>(CardPublication::class) { publication -> CardPublication.instance(publication, vRecycler, false, false, true) }
                .setBottomLoader { onLoad, cards ->
                    RPublicationsGetAll()
                            .setOffset(cards.size)
                            .setPublicationTypes(ControllerSettings.getFandomFilters())
                            .setOrder(RPublicationsGetAll.ORDER_NEW)
                            .setFandomId(xFandom.getId())
                            .setLanguageId(xFandom.getLanguageId())
                            .setImportant(if (ControllerSettings.fandomFilterOnlyImportant) API.PUBLICATION_IMPORTANT_IMPORTANT else API.PUBLICATION_IMPORTANT_NONE)
                            .setIncludeZeroLanguages(true)
                            .setIncludeMultilingual(true)
                            .setIncludeModerationsBlocks(ControllerSettings.fandomFilterModerationsBlocks)
                            .setIncludeModerationsOther(ControllerSettings.fandomFilterModerations)
                            .setAccountId(cardFilters.account?.id ?: 0)
                            .onComplete { rr ->
                                onLoad.invoke(rr.publications)
                                afterPackLoaded()
                            }
                            .onNetworkError { onLoad.invoke(null) }
                            .send(api)
                }
                .setRetryMessage(t(API_TRANSLATE.error_network), t(API_TRANSLATE.app_retry))
                .setEmptyMessage(t(API_TRANSLATE.fandom_posts_empty))
                .setNotifyCount(5)

        adapter.add(cardTitle)
        adapter.add(cardButtons)
        adapter.add(cardViceroy)
        adapter.add(cardFandomInfo)
        adapter.add(cardFilters)

        reload()

        vRecycler.layoutManager = LinearLayoutManager(context)
        vRecycler.adapter = adapter

        vFab.setOnClickListener { Navigator.to(SPostCreate(xFandom.getId(), xFandom.getLanguageId(), xFandom.getName(), xFandom.getImageId())) }

        vMore.setOnClickListener { ControllerFandoms.showPopupMenu(xFandom, vMore) }

        if (fandom.closed) ToolsThreads.main(true) { ControllerFandoms.showAlertIfNeed(this, xFandom.getId(), false) }

        ApiRequestsSupporter.executeWithRetry(RFandomsGetProfile(fandom.id, fandom.languageId)) { r ->
            setPinnedPost(r.pinnedPost)
            xFandom.setImageTitleId(r.imageTitleId)
            xFandom.setImageTitleGifId(r.imageTitleGifId)
            cardTitle.setParams(r.subscriptionType)
            cardViceroy.setParams(r.viceroyAccount, r.viceroyDate)
        }.onFinish{
            updateBackground()
        }

        ImageLoaderId(fandom.imageId).noLoadFromCash().intoBitmap { EventBus.post(EventFandomChanged(xFandom.getId(), xFandom.getName())) }

        ControllerApi.getIconForLanguage(fandom.languageId).into(vLanguage)
        vLanguage.setOnClickListener {
            ControllerCampfireSDK.createLanguageMenu(xFandom.getLanguageId()) { languageId ->
                instance(xFandom.getId(), languageId, Navigator.REPLACE)
            }.asSheetShow()
        }

        update()
    }

    private fun reload() {
        adapter.reloadBottom()
    }

    private fun afterPackLoaded() {
        if (cardPinnedPost != null && ControllerSettings.getFandomFilters().contains(API.PUBLICATION_TYPE_POST))
            for (c in adapter.get(CardPost::class))
                if (c.xPublication.publication.id == cardPinnedPost!!.xPublication.publication.id && !(c.xPublication.publication as PublicationPost).isPined)
                    adapter.remove(c)
    }

    private fun setPinnedPost(post: PublicationPost?) {
        if (cardPinnedPost != null) adapter.remove(cardPinnedPost!!)
        if (post == null) {
            cardPinnedPost = null
        } else {
            for (c in adapter.get(CardPost::class)) if (c.xPublication.publication.id == post.id) adapter.remove(c)
            post.isPined = true
            cardPinnedPost = CardPost(vRecycler, post)
            if (ControllerSettings.getFandomFilters().contains(API.PUBLICATION_TYPE_POST)) {
                adapter.add(adapter.indexOf(cardFilters) + 1, cardPinnedPost!!)
            }
        }
    }

    override fun contains(card: CardPost) = adapter.contains(card)


    private fun updateBackground() {
        xFandom.setViewBig(vImageTitle)
        vImageTitle.setOnClickListener { Navigator.to(SImageView(ImageLoaderId(if (xFandom.getImageTitleGifId() > 0) xFandom.getImageTitleGifId() else xFandom.getImageTitleId()))) }
    }

    private fun update() {
        xFandom.setView(vTitle)
        cardTitle.updateAvatar()
        cardTitle.update()
    }

    //
    //  EventBus
    //

    private fun onEventPostStatusChange(e: EventPostStatusChange) {
        if (e.status == API.STATUS_PUBLIC) reload()
    }


}

