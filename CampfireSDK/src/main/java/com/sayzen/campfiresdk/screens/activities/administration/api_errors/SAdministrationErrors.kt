package com.sayzen.campfiresdk.screens.activities.administration.api_errors

import com.dzen.campfire.api.API_TRANSLATE
import com.sayzen.campfiresdk.R
import com.dzen.campfire.api.models.project.StatisticError
import com.dzen.campfire.api.requests.project.RProjectStatisticErrors
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import kotlin.reflect.KClass

class SAdministrationErrors private constructor(

) : SLoadingRecycler<CardError, StatisticError>() {

    companion object {

        fun instance(action: NavigationAction) {
            Navigator.action(action, SAdministrationErrors())
        }
    }

    init {
        disableNavigation()
        disableShadows()

        setTitle(t(API_TRANSLATE.administration_errors))
        setTextEmpty(t(API_TRANSLATE.administration_errors_empty))

        adapter.setBottomLoader { onLoad, cards ->
            RProjectStatisticErrors(cards.size.toLong())
                    .onComplete { r -> onLoad.invoke(r.errors) }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }
    }

    override fun classOfCard() = CardError::class

    override fun map(item: StatisticError)= CardError(item)

}
