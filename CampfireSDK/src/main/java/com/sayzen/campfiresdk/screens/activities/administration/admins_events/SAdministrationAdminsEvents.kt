package com.sayzen.campfiresdk.screens.activities.administration.admins_events

import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.Publication
import com.dzen.campfire.api.models.publications.events_admins.PublicationEventAdmin
import com.dzen.campfire.api.requests.publications.RPublicationsGetAll
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.cards.events.CardPublicationEventAdmin
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import kotlin.reflect.KClass

class SAdministrationAdminsEvents : SLoadingRecycler<CardPublicationEventAdmin, Publication>() {

    init {
        disableNavigation()

        vScreenRoot!!.setBackgroundColor(ToolsResources.getBackgroundColor(context))
        setTitle(t(API_TRANSLATE.administration_admins_events))

        adapter.setBottomLoader { onLoad, cards ->
            RPublicationsGetAll()
                    .setOffset(cards.size)
                    .setPublicationTypes(API.PUBLICATION_TYPE_EVENT_ADMIN)
                    .setOrder(RPublicationsGetAll.ORDER_NEW)
                    .setIncludeZeroLanguages(true)
                    .setIncludeMultilingual(true)
                    .onComplete { rr -> onLoad.invoke(rr.publications) }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }
        adapter.setRetryMessage(t(API_TRANSLATE.error_network), t(API_TRANSLATE.app_retry))
        adapter.setEmptyMessage(t(API_TRANSLATE.app_empty))
        adapter.setNotifyCount(5)
    }

    override fun classOfCard() = CardPublicationEventAdmin::class

    override fun map(item: Publication) = CardPublicationEventAdmin(item as PublicationEventAdmin)


}
