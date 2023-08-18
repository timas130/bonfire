package com.sayzen.campfiresdk.screens.fandoms.moderation.moderators.reports

import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.Publication
import com.dzen.campfire.api.requests.publications.RPublicationsReportedGetAll
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.cards.CardPublication
import com.sayzen.campfiresdk.models.events.publications.EventPublicationReportsClear
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.java.libs.eventBus.EventBus
import kotlin.reflect.KClass

class SReports(
        private val fandomId: Long,
        private val languageId: Long
) : SLoadingRecycler<CardPublication, Publication>() {

    private val eventBus = EventBus.subscribe(EventPublicationReportsClear::class) {
      for (c in adapter.get(CardPublication::class)) if (c.xPublication.publication.id == it.publicationId) adapter.remove(c)
    }

    init {
        disableShadows()
        disableNavigation()
        setBackgroundImage(API_RESOURCES.IMAGE_BACKGROUND_15)
        setTitle(t(API_TRANSLATE.moderation_screen_reports))
        setTextEmpty(t(API_TRANSLATE.moderation_screen_reports_empty))

        adapter.setBottomLoader { onLoad, cards ->
            RPublicationsReportedGetAll(fandomId, arrayOf(languageId), cards.size.toLong())
                    .onComplete { r -> onLoad.invoke(r.publications) }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }
    }

    override fun classOfCard() = CardPublication::class

    override fun map(item: Publication) = CardPublication.instance(item, null, false, false)

}