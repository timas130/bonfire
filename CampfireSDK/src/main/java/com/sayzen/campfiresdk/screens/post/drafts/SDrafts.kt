package com.sayzen.campfiresdk.screens.post.drafts

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.Publication
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.requests.fandoms.RFandomsGet
import com.dzen.campfire.api.requests.publications.RPublicationsDraftsGetAll
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sayzen.campfiresdk.models.cards.CardPost
import com.sayzen.campfiresdk.screens.fandoms.search.SFandomsSearch
import com.sayzen.campfiresdk.screens.post.create.SPostCreate
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.publications.EventPostDraftCreated
import com.sayzen.campfiresdk.models.events.publications.EventPostStatusChange
import com.sayzen.campfiresdk.screens.post.pending.SPending
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.splash.SplashMenu
import com.sup.dev.java.libs.eventBus.EventBus

class SDrafts constructor(
        val onSelect: ((Publication) -> Unit)? = null
) : SLoadingRecycler<CardPost, Publication>() {

    private val eventBus = EventBus
            .subscribe(EventPostStatusChange::class) {this.onEventPostStatusChange(it) }
            .subscribe(EventPostDraftCreated::class) {this.onEventPostDraftCreated(it) }

    init {
        setScreenColorBackground()
        setTitle(t(API_TRANSLATE.app_drafts))
        setTextEmpty(t(API_TRANSLATE.post_drafts_empty_text))
        setTextProgress(t(API_TRANSLATE.post_drafts_loading))
        setBackgroundImage(API_RESOURCES.IMAGE_BACKGROUND_2)

        addToolbarIcon(R.drawable.ic_more_vert_white_24dp) { view ->
            SplashMenu()
                    .add(t(API_TRANSLATE.app_pending)) { Navigator.to(SPending()) }
                    .asPopupShow(view)
        }

        (vFab as View).visibility = View.VISIBLE
        vFab.setImageResource(R.drawable.ic_add_white_24dp)
        vFab.setOnClickListener {
            if(ControllerCampfireSDK.ROOT_FANDOM_ID > 0){
                val languageId = ControllerApi.getLanguageId()
                ApiRequestsSupporter.executeProgressDialog(RFandomsGet(ControllerCampfireSDK.ROOT_FANDOM_ID, languageId, languageId)){ r->
                    SPostCreate.instance(ControllerCampfireSDK.ROOT_FANDOM_ID, r.fandom.languageId, r.fandom.name, r.fandom.imageId, SPostCreate.PostParams(), Navigator.TO)
                }
            }else {
                SFandomsSearch.instance(Navigator.TO, true) { fandom ->
                    SPostCreate.instance(fandom.id, fandom.languageId, fandom.name, fandom.imageId, SPostCreate.PostParams(), Navigator.TO)
                }
            }
        }

        adapter.setBottomLoader { onLoad, cards ->
            val r = RPublicationsDraftsGetAll(ControllerCampfireSDK.ROOT_FANDOM_ID, ControllerCampfireSDK.ROOT_PROJECT_KEY, ControllerCampfireSDK.ROOT_PROJECT_SUB_KEY, cards.size.toLong())
                    .onComplete { r -> onLoad.invoke(r.publications) }
                    .onNetworkError { onLoad.invoke(null) }
            r.tokenRequired = true
            r.send(api)
        }
    }

    override fun classOfCard() = CardPost::class

    override fun map(item: Publication): CardPost {
        val card = CardPost(vRecycler, item as PublicationPost)
        if (onSelect != null) card.onClick = {
            Navigator.remove(this)
            onSelect.invoke(item)
        }
        card.showFandom = true
        return card
    }

    //
    //  EventBus
    //

    private fun onEventPostStatusChange(e: EventPostStatusChange) {
        if (e.status != API.STATUS_PUBLIC) adapter.reloadBottom()
    }

    private fun onEventPostDraftCreated(e: EventPostDraftCreated) {
        for (c in adapter.get(CardPost::class)) if (c.xPublication.publication.id == e.publicationId) return
        adapter.reloadBottom()
    }

}
