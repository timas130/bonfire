package com.sayzen.campfiresdk.support.adapters

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.notifications.comments.NotificationComment
import com.dzen.campfire.api.models.notifications.comments.NotificationCommentAnswer
import com.dzen.campfire.api.models.publications.PublicationComment
import com.dzen.campfire.api.requests.comments.RCommentGet
import com.dzen.campfire.api.requests.comments.RCommentsGetAll
import com.dzen.campfire.api.tools.client.ApiClient
import com.posthog.PostHog
import com.sayzen.campfiresdk.compose.publication.comment.CardCommentProxy
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sayzen.campfiresdk.models.events.publications.EventCommentsCountChanged
import com.sayzen.campfiresdk.models.splashs.SplashComment
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.CardSpace
import com.sup.dev.android.views.splash.SplashAlert
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsThreads

class AdapterComments(
        private val publicationId: Long,
        private var scrollToCommentId: Long,
        private val vRecycler: RecyclerView,
        private val startFromBottom: Boolean = false
) : RecyclerCardAdapterLoading<CardCommentProxy, PublicationComment>(CardCommentProxy::class, null) {


    private val eventBus = EventBus
            .subscribe(EventNotification::class) { this.onNotification(it) }
            .subscribe(EventCommentsCountChanged::class) { this.onEventCommentsCountChanged(it) }

    private var needScrollToBottom = false
    private var scrollToCommentWasLoaded = false

    init {
        setBottomLoader { onLoad, cards ->
            RCommentsGetAll(publicationId, if (cards.isEmpty()) 0 else cards.get(cards.size - 1).xPublication.publication.dateCreate, false, startFromBottom)
                    .onComplete { r ->
                        remove(CardSpace::class)
                        onLoad.invoke(r.publications)
                        add(CardSpace(124))
                    }
                    .onError { onLoad.invoke(null) }
                    .send(api)
        }
        setAddToSameCards(true)
        setMapper { instanceCard(it) }
        setShowLoadingCardBottom(false)
        setShowLoadingCardTop(true)
        setRemoveSame(true)
        addOnLoadedPack { onCommentsPackLoaded() }
        addOnFinish {
            if (scrollToCommentId > 0 && scrollToCommentWasLoaded) {
                scrollToCommentId = 0
                scrollToCommentWasLoaded = false
                SplashAlert().setText(t(API_TRANSLATE.comment_error_gone)).setOnEnter(t(API_TRANSLATE.app_ok)).asSheetShow()
            }
        }
        setRetryMessage(t(API_TRANSLATE.error_network), t(API_TRANSLATE.app_retry))
        setEmptyMessage(t(API_TRANSLATE.comments_empty), t(API_TRANSLATE.app_comment)) {
            PostHog.capture("open_comment_editor", properties = mapOf("from" to "empty"))
            showCommentDialog()
        }
        setNotifyCount(5)
        ToolsThreads.main(true) { this.loadBottom() }
    }

    fun setCommentButton(view: View) {
        view.setOnClickListener {
            PostHog.capture("open_comment_editor", properties = mapOf("from" to "comment_btn"))
            showCommentDialog()
        }
    }

    fun showCommentDialog(comment: PublicationComment? = null, changeComment: PublicationComment? = null, quoteId: Long = 0, quoteText: String = "") {
        SplashComment(publicationId, comment, changeComment, quoteId, quoteText, false) {
            val card = addComment(it)
            scrollToCard(card)
        }.asSheetShow()
    }

    fun enableTopLoader() {
        setTopLoader { onLoad, cards ->
            RCommentsGetAll(publicationId, if (cards.isEmpty()) 0 else cards.get(0).xPublication.publication.dateCreate, true, startFromBottom)
                    .onComplete { r ->
                        onLoad.invoke(r.publications)
                    }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }
    }

    private fun scrollToCard(card: CardCommentProxy, owerscroll:Int=1) {
        ToolsThreads.main(500) {
            ToolsView.scrollRecycler(vRecycler, indexOf(card) + owerscroll)
            ToolsThreads.main(200) { card.flash() }
        }
    }

    private fun onCommentsPackLoaded() {
        if (scrollToCommentId == -1L) {
            scrollToCommentId = 0
            scrollToCommentWasLoaded = false
            val v = get(CardCommentProxy::class)
            val index = if (v.isNotEmpty()) indexOf(v.get(0)) else size()
            vRecycler.scrollToPosition(index)
        } else if (scrollToCommentId != 0L) {
            for (c in get(CardCommentProxy::class)) {
                if (c.xPublication.publication.id == scrollToCommentId) {
                    scrollToCommentId = 0
                    scrollToCommentWasLoaded = false
                    scrollToCard(c)
                }
            }
            if (scrollToCommentId != 0L) {
                if (scrollToCommentWasLoaded) {
                    loadBottom()
                } else {
                    RCommentGet(publicationId, scrollToCommentId)
                            .onComplete {
                                loadBottom()
                                scrollToCommentWasLoaded = true
                            }
                            .onApiError(ApiClient.ERROR_GONE) {
                                if (it.messageError == RCommentGet.GONE_BLOCKED) ControllerApi.showBlockedDialog(it, t(API_TRANSLATE.comment_error_gone_block))
                                else if (it.messageError == RCommentGet.GONE_REMOVE) SplashAlert().setText(t(API_TRANSLATE.comment_error_gone_remove)).setOnEnter(t(API_TRANSLATE.app_ok)).asSheetShow()
                                else SplashAlert().setText(t(API_TRANSLATE.comment_error_gone)).setOnEnter(t(API_TRANSLATE.app_ok)).asSheetShow()
                                scrollToCommentWasLoaded = false
                                scrollToCommentId = 0
                            }
                            .send(api)

                }
            }
        }

        if (needScrollToBottom) {
            needScrollToBottom = false
        }

    }

    fun addComment(publicationComment: PublicationComment): CardCommentProxy {
        val card = instanceCard(publicationComment)
        remove(CardSpace::class)
        addWithHashBottom(card)
        add(CardSpace(124))
        scrollToCard(card)
        return card
    }

    fun loadAndScrollTo(scrollToCommentId: Long) {
        this.scrollToCommentId = scrollToCommentId
        loadBottom()
    }

    private fun instanceMapper():((PublicationComment)->CardCommentProxy) = {instanceCard(it)}

    private fun instanceCard(publication: PublicationComment): CardCommentProxy {
        return CardCommentProxy(
            publication = publication,
            dividers = true,
            miniSize = false,
            allowEditing = true,
            allowSwipeReply = true,
            onGoTo = { id ->
                for (i in get(CardCommentProxy::class)) {
                    if (i.xPublication.publication.id == id) {
                        scrollToCard(i, 0)
                        break
                    }
                }
            },
            onCreated = {
                addComment(it)
            }
        )
    }

    //
    //  EventBus
    //

    private fun onEventCommentsCountChanged(e: EventCommentsCountChanged) {
        if (e.publicationId == publicationId && e.change < 0) {
            if (get(CardCommentProxy::class).size == 0) {
                reloadBottom()
            }
        }
    }

    private fun onNotification(e: EventNotification) {
        if (e.notification is NotificationComment)
            if (e.notification.publicationId == publicationId) {
                needScrollToBottom = (vRecycler.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition() == itemCount - 1
                loadBottom()
            }

        if (e.notification is NotificationCommentAnswer)
            if (e.notification.publicationId == publicationId) {
                needScrollToBottom = (vRecycler.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition() == itemCount - 1
                loadBottom()
            }

    }

}
