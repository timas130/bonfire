package com.sayzen.campfiresdk.screens.administation

import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.Publication
import com.dzen.campfire.api.requests.publications.RPublicationsGetAllDeepBlocked
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.cards.CardPublication
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import kotlin.reflect.KClass

class SAdministrationDeepBlocked(val accountId: Long) : SLoadingRecycler<CardPublication, Publication>() {

    init {
        disableNavigation()

        vScreenRoot!!.setBackgroundColor(ToolsResources.getBackgroundColor(context))
        setTitle(t(API_TRANSLATE.protoadin_bloc_title))
        setTextEmpty(t(API_TRANSLATE.protoadin_bloc_empty))

        adapter.setBottomLoader { onLoad, cards ->
            RPublicationsGetAllDeepBlocked(accountId, cards.size.toLong())
                    .onComplete { r -> onLoad.invoke(r.publications) }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }
    }

    override fun classOfCard() = CardPublication::class

    override fun map(item: Publication) = CardPublication.instance(item, vRecycler, true, false, true, true)

}
