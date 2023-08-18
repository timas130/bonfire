package com.sayzen.campfiresdk.screens.activities.administration.api_statistic

import com.dzen.campfire.api.API_TRANSLATE
import com.sayzen.campfiresdk.R
import com.dzen.campfire.api.models.project.StatisticRequest
import com.dzen.campfire.api.requests.project.RProjectStatisticRequests
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsStorage
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.splash.SplashMenu

class SAdministrationRequests private constructor(

) : SLoadingRecycler<CardRequest, StatisticRequest>() {

    companion object {

        private val KEY = "SAdministrationRequests_Sort"

        fun instance(action: NavigationAction) {
            Navigator.action(action, SAdministrationRequests())
        }
    }

    init {
        setTitle(t(API_TRANSLATE.administration_requests))

        addToolbarIcon(R.drawable.ic_tune_white_24dp) {
            SplashMenu()
                    .add(t(API_TRANSLATE.administration_requests_f_middle)) {
                        ToolsStorage.put(KEY, RProjectStatisticRequests.SORT_AVG)
                        reload()
                    }
                    .add(t(API_TRANSLATE.administration_requests_f_total)) {
                        ToolsStorage.put(KEY, RProjectStatisticRequests.SORT_TOTAL)
                        reload()
                    }
                    .add(t(API_TRANSLATE.administration_requests_f_count)) {
                        ToolsStorage.put(KEY, RProjectStatisticRequests.SORT_COUNT)
                        reload()
                    }
                    .add(t(API_TRANSLATE.administration_requests_f_max)) {
                        ToolsStorage.put(KEY, RProjectStatisticRequests.SORT_MAX)
                        reload()
                    }
                    .asSheetShow()
        }

        adapter.setBottomLoader { onLoad, cards ->
            RProjectStatisticRequests(ToolsStorage.getLong(KEY, RProjectStatisticRequests.SORT_AVG), cards.size.toLong())
                    .onComplete { r -> onLoad.invoke(r.statistic) }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }
    }

    override fun classOfCard() = CardRequest::class

    override fun map(item: StatisticRequest) = CardRequest(item)

}
