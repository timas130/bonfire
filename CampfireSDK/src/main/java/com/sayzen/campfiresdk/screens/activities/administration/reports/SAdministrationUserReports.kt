package com.sayzen.campfiresdk.screens.activities.administration.reports

import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.sayzen.campfiresdk.R
import com.dzen.campfire.api.models.account.AccountReports
import com.dzen.campfire.api.requests.accounts.RAccountsReportsGetAll
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import kotlin.reflect.KClass

class SAdministrationUserReports(
) : SLoadingRecycler<CardUserReport, AccountReports>() {

    init {
        disableNavigation()
        disableShadows()

        setBackgroundImage(API_RESOURCES.IMAGE_BACKGROUND_15)
        setTitle(t(API_TRANSLATE.administration_user_reports))
        setTextEmpty(t(API_TRANSLATE.administration_user_reports_empty))

        adapter.setBottomLoader { onLoad, cards ->
            RAccountsReportsGetAll(cards.size.toLong())
                    .onComplete { r -> onLoad.invoke(r.accounts) }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }
    }

    override fun classOfCard() = CardUserReport::class

    override fun map(item: AccountReports) = CardUserReport(item)

}