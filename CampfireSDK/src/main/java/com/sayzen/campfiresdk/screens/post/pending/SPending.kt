package com.sayzen.campfiresdk.screens.post.pending

import android.view.View
import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.requests.post.RPostPendingGetAll
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.cards.CardPost
import com.sayzen.campfiresdk.screens.fandoms.search.SFandomsSearch
import com.sayzen.campfiresdk.screens.post.create.SPostCreate
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import kotlin.reflect.KClass

class SPending : SLoadingRecycler<CardPost, PublicationPost>() {

    init {
        setScreenColorBackground()
        setTitle(t(API_TRANSLATE.app_pending))
        setTextEmpty(t(API_TRANSLATE.post_drafts_empty_text))
        setTextProgress(t(API_TRANSLATE.post_drafts_loading))
        setBackgroundImage(API_RESOURCES.IMAGE_BACKGROUND_2)

        (vFab as View).visibility = View.VISIBLE
        vFab.setImageResource(R.drawable.ic_add_white_24dp)
        vFab.setOnClickListener {
            SFandomsSearch.instance(Navigator.TO, true) { fandom ->
                SPostCreate.instance(fandom.id, fandom.languageId, fandom.name, fandom.imageId, SPostCreate.PostParams(), Navigator.TO)
            }
        }

        adapter.setBottomLoader { onLoad, cards ->
            val r = RPostPendingGetAll(cards.size.toLong())
                    .onComplete { r -> onLoad.invoke(r.publications) }
                    .onNetworkError { onLoad.invoke(null) }
            r.tokenRequired = true
            r.send(api)
        }
    }

    override fun classOfCard() = CardPost::class

    override fun map(item: PublicationPost): CardPost {
        val card = CardPost(vRecycler, item)
        card.showFandom = true
        return card
    }

}
