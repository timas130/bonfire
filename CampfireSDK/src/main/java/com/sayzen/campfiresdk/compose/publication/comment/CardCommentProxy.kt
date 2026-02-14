package com.sayzen.campfiresdk.compose.publication.comment

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.dzen.campfire.api.models.publications.PublicationComment
import com.posthog.PostHog
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerPublications
import com.sayzen.campfiresdk.models.cards.CardComment
import com.sayzen.campfiresdk.models.cards.CardPublication
import com.sayzen.campfiresdk.models.splashs.SplashComment
import com.sup.dev.android.views.support.adapters.CardAdapter

class CardCommentProxy(
    publication: PublicationComment,
    dividers: Boolean,
    miniSize: Boolean = false,
    allowEditing: Boolean = false,
    allowSwipeReply: Boolean = false,
    onGoTo: ((Long) -> Unit)? = null,
    onCreated: (PublicationComment) -> Unit = {},
) : CardPublication(0, publication) {
    private val isComposeEnabled = PostHog.isFeatureEnabled("compose_comment", true)
    private val impl = if (isComposeEnabled) {
        ComposeCommentCard(
            initialComment = publication,
            onRemoved = { adapter.remove(this) },
            scrollToComment = onGoTo?.let { goTo -> { goTo(it.id) } } ?: DefaultScrollToComment,
            allowSwipeReply = allowSwipeReply,
            allowEditing = allowEditing,
            withPadding = !dividers, // don't ask me
            onCreated = onCreated,
        )
    } else {
        CardComment(
            proxy = this,
            publication = publication,
            dividers = dividers,
            miniSize = miniSize,
            onClick = { comment ->
                if (allowEditing) {
                    if (!ControllerApi.isCurrentAccount(comment.creator.id)) return@CardComment false
                    SplashComment(comment, true).asSheetShow()
                } else {
                    ControllerPublications.toPublication(
                        publicationType = comment.parentPublicationType,
                        publicationId = comment.parentPublicationId,
                        commentId = comment.id
                    )
                }
                true
            },
            onQuote = { comment ->
                PostHog.capture("open_comment_editor", properties = mapOf("from" to "quote"))
                SplashComment(
                    publicationId = comment.parentPublicationId,
                    answer = comment,
                    changeComment = null,
                    quoteId = comment.id,
                    quoteText = comment.makeQuoteText(),
                    showToast = true,
                    onCreated = onCreated,
                ).asSheetShow()
            },
            onGoTo = onGoTo
        )
    }

    init {
        if (isComposeEnabled) {
            xPublication.eventBus.unsubscribe()
        }
    }

    override fun canCacheView(): Boolean {
        return impl.canCacheView()
    }

    var maxTextSize: Int
        get() = when (impl) {
            is CardComment -> impl.maxTextSize
            is ComposeCommentCard -> impl.maxTextSize
            else -> throw IllegalStateException()
        }
        set(value) = when (impl) {
            is CardComment -> impl.maxTextSize = value
            is ComposeCommentCard -> impl.maxTextSize = value
            else -> throw IllegalStateException()
        }

    override var showFandom: Boolean
        get() = when (impl) {
            is CardComment -> impl.showFandom
            is ComposeCommentCard -> impl.showFandom
            else -> throw IllegalStateException()
        }
        set(value) = when (impl) {
            is CardComment -> impl.showFandom = value
            is ComposeCommentCard -> impl.showFandom = value
            else -> throw IllegalStateException()
        }

    override fun flash() {
        when (impl) {
            is CardComment -> impl.flash()
            is ComposeCommentCard -> impl.flash()
        }
    }

    override fun setCardAdapter(adapter: CardAdapter?) {
        super.setCardAdapter(adapter)
        impl.setCardAdapter(adapter)
    }

    override fun instanceView(vParent: ViewGroup): View {
        val view = impl.instanceView(vParent)
        impl.setViewOverride(view)
        return view
    }

    override fun instanceView(context: Context): View {
        val view = impl.instanceView(context)
        impl.setViewOverride(view)
        return view
    }

    override fun bindView(view: View) {
        impl.setViewOverride(view)
        return impl.bindView(view)
    }

    override fun notifyItem() {
        if (impl is CardComment) impl.notifyItem()
    }

    override fun updateAccount() {
        if (impl is CardComment) impl.updateAccount()
    }

    override fun updateFandom() {
        if (impl is CardComment) impl.updateFandom()
    }

    override fun updateKarma() {
        if (impl is CardComment) impl.updateKarma()
    }

    override fun updateComments() {
        if (impl is CardComment) impl.updateComments()
    }

    override fun updateReports() {
        if (impl is CardComment) impl.updateReports()
    }

    override fun updateReactions() {
        if (impl is CardComment) impl.updateReactions()
    }

    override fun onDetachView() {
        impl.onDetachView()
    }
}
