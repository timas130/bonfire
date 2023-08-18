package com.sayzen.campfiresdk.screens.reports

import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.PublicationReport
import com.dzen.campfire.api.requests.accounts.RAccountsReportsGetAllForAccount
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.views.screens.SLoadingRecycler

class SReportsAccount(
        val accountId: Long
) : SLoadingRecycler<CardReport, PublicationReport>() {

    init {
        disableShadows()
        disableNavigation()
        setTitle(t(API_TRANSLATE.app_reports))
        setTextEmpty(t(API_TRANSLATE.app_empty))

        adapter.setBottomLoader { onLoad, cards ->
            subscription = RAccountsReportsGetAllForAccount(accountId, cards.size.toLong())
                    .onComplete { r -> onLoad.invoke(r.reports) }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }
    }

    override fun classOfCard() = CardReport::class

    override fun map(item: PublicationReport) = CardReport(item)

}
