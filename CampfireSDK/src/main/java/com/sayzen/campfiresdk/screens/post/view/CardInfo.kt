package com.sayzen.campfiresdk.screens.post.view

import android.view.View
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.models.publications.tags.PublicationTag
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.support.adapters.*
import com.sayzen.campfiresdk.controllers.ControllerPublications
import com.sayzen.campfiresdk.models.events.publications.EventPostChanged
import com.sayzen.campfiresdk.models.events.publications.EventPostStatusChange
import com.sayzen.campfiresdk.models.events.publications.EventPostTagsChanged
import com.sayzen.campfiresdk.screens.fandoms.rubrics.SRubricPosts
import com.sayzen.campfiresdk.screens.post.search.SPostsSearch
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewAvatar
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.views.ViewChip
import com.sup.dev.android.views.views.ViewSpace
import com.sup.dev.android.views.views.layouts.LayoutFlow
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.libs.eventBus.EventBusSubscriber

class CardInfo(
        private val xPublication: XPublication,
        private var tags: Array<PublicationTag>
) : Card(R.layout.screen_post_card_info) {

    private val eventBus: EventBusSubscriber = EventBus
            .subscribe(EventPostTagsChanged::class) { this.onEventPostTagsChanged(it) }

    init {
        xPublication.xFandom.setShowLanguage(false)
    }

    private fun onEventPostTagsChanged(e: EventPostTagsChanged) {
        if (e.publicationId == xPublication.publication.id) {
            tags = e.tags
            update()
        }
    }

    override fun bindView(view: View) {
        super.bindView(view)
        val vFlow: LayoutFlow = view.findViewById(R.id.vFlow)

        val tags = ControllerPublications.parseTags(this.tags)


        if (tags.isEmpty()) {
            vFlow.visibility = View.GONE
        } else {
            vFlow.visibility = View.VISIBLE
        }

        vFlow.removeAllViews()
        for (tagParent in tags) {
            addTag(tagParent.tag, vFlow)
            for (tag in tagParent.tags) addTag(tag, vFlow)
        }

        updateFandom()
        updateAccount()
        updateKarma()
        updateComments()
        updateReports()
    }

    fun updateFandom() {
        if (getView() == null) return
        xPublication.xFandom.setView(getView()!!.findViewById<ViewAvatar>(R.id.vFandom))
    }

    fun updateAccount() {
        if (getView() == null) return
        val vAvatar: ViewAvatarTitle = getView()!!.findViewById(R.id.vAvatar)
        xPublication.xAccount.setView(vAvatar)
        val publication = xPublication.publication as PublicationPost
        vAvatar.vSubtitle.maxLines = 10
        if (publication.rubricId > 0) {
            vAvatar.vSubtitle.text = vAvatar.getSubTitle() + "\n" + publication.rubricName
            ToolsView.addLink(vAvatar.vSubtitle, publication.rubricName) {
                SRubricPosts.instance(publication.rubricId, Navigator.TO)
            }
        }
    }

    fun updateKarma() {
        if (getView() == null) return
        xPublication.xKarma.setView(getView()!!.findViewById(R.id.vKarma))
    }

    fun updateComments() {
        if (getView() == null) return
        xPublication.xComments.setView(getView()!!.findViewById(R.id.vComments))
    }

    fun updateReports() {
        if (getView() == null) return
        xPublication.xReports.setView(getView()!!.findViewById(R.id.vReports))
    }

    private fun addTag(t: PublicationTag, vFlow: LayoutFlow) {
        val vChip = if (t.parentPublicationId == 0L) ViewChip.instance(vFlow.context) else ViewChip.instanceOutline(vFlow.context)
        vChip.text = t.name
        vChip.setOnClickListener { SPostsSearch.instance(t, Navigator.TO) }
        ControllerPublications.createTagMenu(vChip, t, false)
        if (vFlow.childCount != 0 && t.parentPublicationId == 0L) vFlow.addView(ViewSpace(vFlow.context, ToolsView.dpToPx(1).toInt(), 0))
        vFlow.addView(vChip)
        if (t.imageId != 0L) ImageLoader.load(t.imageId).intoBitmap { vChip.setIcon(it)  }
    }


}
