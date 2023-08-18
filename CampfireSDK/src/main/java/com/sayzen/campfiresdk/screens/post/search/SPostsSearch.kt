package com.sayzen.campfiresdk.screens.post.search

import android.view.View
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.requests.post.RPostGetAllByTag
import com.sayzen.campfiresdk.models.cards.CardPublication
import com.dzen.campfire.api.models.publications.Publication
import com.dzen.campfire.api.models.publications.tags.PublicationTag
import com.dzen.campfire.api.requests.tags.RTagsGet
import com.sayzen.campfiresdk.screens.post.create.SPostCreate
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.screens.fandoms.STags
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.screens.SLoadingRecycler

class SPostsSearch(val tag: PublicationTag) : SLoadingRecycler<CardPublication, Publication>() {

    companion object {

        fun instance(tag: PublicationTag, action: NavigationAction) {
            Navigator.action(action, SPostsSearch(tag))
        }

        fun instance(tagId: Long, action: NavigationAction) {
            ApiRequestsSupporter.executeInterstitial(action,
                    RTagsGet(tagId)
            ) { r -> SPostsSearch(r.tag) }
        }
    }

    init {
        vScreenRoot!!.setBackgroundColor(ToolsResources.getBackgroundColor(context))
        setTitle(tag.name)
        setTextEmpty(t(API_TRANSLATE.post_search_empty))
        setAction(t(API_TRANSLATE.post_search_action)) {
            if (Navigator.hasPrevious() && Navigator.getPrevious() is STags)
                Navigator.back()
            else
                STags.instance(tag.fandom.id, tag.fandom.languageId, Navigator.REPLACE)
        }
        (vFab as View).visibility = View.VISIBLE
        vFab.setImageResource(R.drawable.ic_add_white_24dp)
        vFab.setOnClickListener {
            SPostCreate.instance(tag.fandom.id, tag.fandom.languageId, tag.fandom.name, tag.fandom.imageId, SPostCreate.PostParams().setTags(arrayOf(tag.id)), Navigator.TO)
        }

        adapter.setBottomLoader { onLoad, cards ->
            RPostGetAllByTag(tag.id, cards.size.toLong())
                    .onComplete { r -> onLoad.invoke(r.publications) }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }
    }

    override fun classOfCard() = CardPublication::class

    override fun map(item: Publication) = CardPublication.instance(item, vRecycler)

}
