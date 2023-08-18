package com.sayzen.campfiresdk.screens.post.history

import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.history.HistoryPublication
import com.dzen.campfire.api.requests.publications.RPublicationsHistoryGetAll
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.cards.history.CardHistoryPublication
import com.sup.dev.android.views.screens.SLoadingRecycler

class SPublicationHistory(
        val publicationId: Long
) : SLoadingRecycler<CardHistoryPublication, HistoryPublication>() {

    init {
        disableShadows()
        disableNavigation()

        setTitle(t(API_TRANSLATE.app_history))
        setTextEmpty(t(API_TRANSLATE.app_empty))
        setBackgroundImage(API_RESOURCES.IMAGE_BACKGROUND_4)

        adapter.setBottomLoader { onLoad, cards ->
            RPublicationsHistoryGetAll(publicationId, cards.size.toLong())
                    .onComplete { r ->
                        onLoad.invoke(r.history)
                    }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }
    }

    override fun classOfCard() = CardHistoryPublication::class

    override fun map(item: HistoryPublication) = CardHistoryPublication(item)

}
