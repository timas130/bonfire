package com.sayzen.campfiresdk.compose.publication.post

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.posthog.PostHog
import com.sayzen.campfiresdk.models.cards.CardPost
import com.sayzen.campfiresdk.models.cards.CardPublication
import com.sup.dev.android.views.support.adapters.CardAdapter

class CardPostProxy(
    vRecycler: RecyclerView?,
    publication: PublicationPost,
    var onClick: ((PublicationPost) -> Unit)? = null
) : CardPublication(0, publication) {
    private val isComposeEnabled = PostHog.isFeatureEnabled("compose_post")
    private val impl = if (isComposeEnabled) {
        ComposeCardPost(
            proxy = this,
            vRecycler = vRecycler,
            post = publication,
            onClick = onClick,
        )
    } else {
        CardPost(
            proxy = this,
            vRecycler = vRecycler,
            publication = publication,
            onClick = onClick
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

    override fun setCardAdapter(adapter: CardAdapter?) {
        impl.setCardAdapter(adapter)
    }

    override var showFandom: Boolean
        get() = when (impl) {
            is CardPost -> impl.showFandom
            is ComposeCardPost -> impl.showFandom
            else -> throw IllegalStateException()
        }
        set(value) = when (impl) {
            is CardPost -> impl.showFandom = value
            is ComposeCardPost -> impl.showFandom = value
            else -> throw IllegalStateException()
        }

    override fun instanceView(vParent: ViewGroup): View {
        val view = impl.instanceView(vParent)
        impl.setViewOverride(view)
        return view
    }

    override fun bindView(view: View) {
        impl.setViewOverride(view)
        return impl.bindView(view)
    }

    override fun notifyItem() {
        if (impl is CardPost) impl.notifyItem()
    }

    override fun updateAccount() {
        if (impl is CardPost) impl.updateAccount()
    }

    override fun updateFandom() {
        if (impl is CardPost) impl.updateFandom()
    }

    override fun updateKarma() {
        if (impl is CardPost) impl.updateKarma()
    }

    override fun updateComments() {
        if (impl is CardPost) impl.updateComments()
    }

    override fun updateReports() {
        if (impl is CardPost) impl.updateReports()
    }

    override fun updateReactions() {
        if (impl is CardPost) impl.updateReactions()
    }

    override fun onDetachView() {
        impl.onDetachView()
    }
}
