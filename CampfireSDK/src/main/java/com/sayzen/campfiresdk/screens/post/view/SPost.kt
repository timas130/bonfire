package com.sayzen.campfiresdk.screens.post.view

import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.notifications.publications.NotificationFollowsPublication
import com.dzen.campfire.api.models.notifications.publications.NotificationPublicationImportant
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.models.publications.tags.PublicationTag
import com.dzen.campfire.api.requests.post.RPostGet
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.support.adapters.AdapterComments
import com.sayzen.campfiresdk.support.adapters.XPublication
import com.sayzen.campfiresdk.controllers.*
import com.sayzen.campfiresdk.models.cards.post_pages.CardPage
import com.sayzen.campfiresdk.models.events.publications.*
import com.sup.dev.android.app.SupAndroid
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.screens.SAlert
import com.dzen.campfire.api.tools.client.ApiClient
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.libs.eventBus.EventBusSubscriber
import com.sup.dev.java.tools.ToolsThreads

class SPost constructor(
        publication: PublicationPost,
        tags: Array<PublicationTag>,
        commentId: Long
) : Screen(R.layout.screen_post) {

    companion object {

        fun instance(publicationId: Long, action: NavigationAction) {
            instance(publicationId, 0, action)
        }

        fun instance(publicationId: Long, commentId: Long, action: NavigationAction) {
            ApiRequestsSupporter.executeInterstitial(action,
                    RPostGet(publicationId)) { r ->
                SPost(r.publication, r.tags, commentId)
            }.onApiError(ApiClient.ERROR_GONE) {
                if (it.messageError == RPostGet.GONE_BLOCKED) ControllerApi.showBlockedScreen(it, action, t(API_TRANSLATE.post_error_gone_block))
                else if (it.messageError == RPostGet.GONE_DRAFT)  SAlert.showMessage(t(API_TRANSLATE.post_error_gone_draft), t(API_TRANSLATE.app_back), SupAndroid.imgErrorGone, action)
                else if (it.messageError == RPostGet.GONE_REMOVE) SAlert.showMessage(t(API_TRANSLATE.post_error_gone_remove), t(API_TRANSLATE.app_back), SupAndroid.imgErrorGone, action)
                else SAlert.showMessage(t(API_TRANSLATE.post_error_gone), t(API_TRANSLATE.app_back), SupAndroid.imgErrorGone, action)
            }
        }
    }

    private val eventBus: EventBusSubscriber = EventBus
            .subscribe(EventPostChanged::class) { this.onPostChanged(it) }
            .subscribe(EventPostStatusChange::class) { this.onEventPostStatusChange(it) }

    private val vRecycler: RecyclerView = findViewById(R.id.vRecycler)
    private val vMenu: View = findViewById(R.id.vMenu)
    private val vFab: FloatingActionButton = findViewById(R.id.vFab)
    private val vShare: View = findViewById(R.id.vShare)

    private val adapter: AdapterComments = AdapterComments(publication.id, commentId, vRecycler)
    private val xPublication = XPublication(publication,
            onChangedAccount = { cardInfo.updateAccount() },
            onChangedFandom = { cardInfo.updateFandom() },
            onChangedKarma = { cardInfo.updateKarma() },
            onChangedComments = {
                cardInfo.updateComments()
                adapter.loadBottom()
            },
            onChangedReports = { cardInfo.updateReports() },
            onChangedImportance = {},
            onRemove = { Navigator.remove(this) },
            onChangedReactions = { }
    )
    private val cardInfo: CardInfo = CardInfo(xPublication, tags)

    init {
        setTitle(t(API_TRANSLATE.post))

        vRecycler.layoutManager = LinearLayoutManager(context)
        ToolsView.setRecyclerAnimation(vRecycler)

        for (page in publication.pages) adapter.add(CardPage.instance(publication, page))
        ControllerPost.updateSpoilers(adapter)
        adapter.add(cardInfo)
        adapter.setCommentButton(vFab)

        vShare.setOnClickListener { ControllerApi.sharePost(publication.id) }
        vMenu.setOnClickListener { ControllerPost.showPostMenu(vMenu, publication) }
        vRecycler.adapter = adapter

        ControllerNotifications.removeNotificationFromNew(NotificationFollowsPublication::class, publication.id)
        ControllerNotifications.removeNotificationFromNew(NotificationPublicationImportant::class, publication.id)

        if (publication.fandom.closed) ToolsThreads.main(true) { ControllerFandoms.showAlertIfNeed(this, publication.fandom.id, true) }
    }


    //
    //  EventBus
    //

    private fun onPostChanged(e: EventPostChanged) {
        if (e.publicationId == xPublication.publication.id) {
            adapter.remove(CardPage::class)
            for (i in 0 until e.pages.size) adapter.add(i, CardPage.instance(xPublication.publication as PublicationPost, e.pages[i]))
        }
    }

    private fun onEventPostStatusChange(e: EventPostStatusChange) {
        if (e.publicationId == xPublication.publication.id && e.status != API.STATUS_PUBLIC) Navigator.remove(this)
    }

}
