package com.sayzen.campfiresdk.screens.activities.administration.block

import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.sayzen.campfiresdk.R
import com.dzen.campfire.api.models.publications.PublicationBlocked
import com.dzen.campfire.api.requests.publications.RPublicationsBlockGetAll
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.cards.CardPublication
import com.sayzen.campfiresdk.models.events.publications.EventPublicationBlockedRemove
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsThreads

class SAdministrationBlock : SLoadingRecycler<CardPublication, PublicationBlocked>() {

    private val eventBus = EventBus.subscribe(EventPublicationBlockedRemove::class) {
        var i = 0
        while (i < adapter.size()) {
            if (adapter[i] is CardPublicationBlock) {
                if ((adapter[i] as CardPublicationBlock).publication.moderationId == it.moderationId) {
                    adapter.remove(i--)
                    adapter.remove(i--)
                }
            }
            i++
        }
    }

    init {
        disableNavigation()

        vScreenRoot!!.setBackgroundColor(ToolsResources.getBackgroundColor(context))
        setTitle(t(API_TRANSLATE.app_block_title))
        setTextEmpty(t(API_TRANSLATE.app_empty))
        setBackgroundImage(API_RESOURCES.IMAGE_BACKGROUND_27)

        adapter.setBottomLoader { onLoad, cards ->
            RPublicationsBlockGetAll(cards.size.toLong())
                    .onComplete { r ->
                        onLoad.invoke(r.publications)
                        ToolsThreads.main { afterPackLoaded() }
                    }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }
    }

    override fun classOfCard() = CardPublication::class

    override fun map(item: PublicationBlocked): CardPublication {
        val c = CardPublication.instance(item.publication, null, false, false)
        c.tag = item
        return c
    }

    override fun reload() {
        super.reload()
        adapter.clear()
    }

    private fun afterPackLoaded() {
        var i = 0
        while (i < adapter.size()) {
            if (adapter[i] is CardPublication) {
                if (i != adapter.size() - 1) {
                    if (adapter[i + 1] is CardPublication) {
                        adapter.add(i + 1, CardPublicationBlock(adapter[i].tag as PublicationBlocked))
                        i++
                    }
                } else {
                    adapter.add(i + 1, CardPublicationBlock((adapter[i].tag as PublicationBlocked)))
                    i++
                }
            }
            i++
        }
    }


}