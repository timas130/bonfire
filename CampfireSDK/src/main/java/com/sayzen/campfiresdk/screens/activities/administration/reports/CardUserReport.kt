package com.sayzen.campfiresdk.screens.activities.administration.reports

import android.view.View
import android.widget.Button
import android.widget.TextView
import com.dzen.campfire.api.API_TRANSLATE
import com.sayzen.campfiresdk.R
import com.dzen.campfire.api.models.account.AccountReports
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.cards.CardAccount
import com.sayzen.campfiresdk.models.events.account.EventAccountReportsCleared
import com.sayzen.campfiresdk.screens.reports.SReports
import com.sayzen.campfiresdk.screens.reports.SReportsAccount
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.java.libs.eventBus.EventBus

class CardUserReport(
        val report: AccountReports
) : CardAccount(report.account, R.layout.screen_administration_user_reports_card) {

    private val eventBus = EventBus.subscribe(EventAccountReportsCleared::class){if(report.account.id == it.accountId) remove() }

    init {
        setOnClick {
            Navigator.to(SReportsAccount(report.account.id))
        }
    }

    override fun bindView(view: View) {
        super.bindView(view)

        val vReports: TextView = view.findViewById(R.id.vReports)
        val vClearReports: Button = view.findViewById(R.id.vClearReports)

        vReports.text = "${report.reportsCount}"
        vClearReports.text = t(API_TRANSLATE.app_clear_reports)

        vClearReports.setOnClickListener {
            ControllerApi.clearUserReports(report.account.id)
        }

    }

}