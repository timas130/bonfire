package com.sayzen.campfiresdk.screens.activities.support

import android.widget.Button
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.notifications.project.NotificationDonate
import com.dzen.campfire.api.requests.project.RProjectSupportGetInfo
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerDonates
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.activities.EventVideoAdView
import com.sayzen.campfiresdk.models.events.notifications.EventNotification
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.support.adapters.pager.PagerCardAdapter
import com.sup.dev.android.views.views.ViewProgressLine
import com.sup.dev.android.views.views.pager.ViewPagerIndicatorTitles
import com.sup.dev.java.libs.eventBus.EventBus

class SDonate private constructor(
        var r: RProjectSupportGetInfo.Response,
        val accountId: Long
) : Screen(R.layout.screen_donate) {

    companion object {

        fun instance(action: NavigationAction) {
            instance(0, action)
        }

        fun instance(accountId: Long, action: NavigationAction) {
            if (ControllerApi.account.getLevel() < 200) return
            ApiRequestsSupporter.executeInterstitial(action, RProjectSupportGetInfo()) { r ->
                SDonate(r, accountId)
            }
        }

    }

    private val eventBus = EventBus
            .subscribe(EventVideoAdView::class) { reload() }
            .subscribe(EventNotification::class) { if (it.notification is NotificationDonate) reload() }

    private val vButton: Button = findViewById(R.id.vButton)
    private val vCounter: TextView = findViewById(R.id.vCounter)
    private val vLine: ViewProgressLine = findViewById(R.id.vLine)
    private val vTitles: ViewPagerIndicatorTitles = findViewById(R.id.viIndicator)
    private val vPager: ViewPager = findViewById(R.id.vPager)
    private val vText: TextView = findViewById(R.id.vText)

    val pagerCardAdapter = PagerCardAdapter()
    val pageDonates = PageDonates(this)
    val pageRating30 = PageRating30(this)
    val pageRatingTotal = PageRatingTotal(this)

    init {
        disableNavigation()
        disableShadows()

        vText.text = t(API_TRANSLATE.activities_support_card_title)
        vButton.text = t(API_TRANSLATE.activities_support_card_action_2)
        vPager.adapter = pagerCardAdapter

        pagerCardAdapter.add(pageDonates)
        pagerCardAdapter.add(pageRating30)
        pagerCardAdapter.add(pageRatingTotal)

        if (ControllerApi.isProtoadmin()) {
            pagerCardAdapter.add(PageDrafts(this))
            vTitles.setTitles(t(API_TRANSLATE.activities_support_title_donates), t(API_TRANSLATE.activities_support_title_rating_30), t(API_TRANSLATE.activities_support_title_rating_total), t(API_TRANSLATE.app_drafts))
        } else
            vTitles.setTitles(t(API_TRANSLATE.activities_support_title_donates), t(API_TRANSLATE.activities_support_title_rating_30), t(API_TRANSLATE.activities_support_title_rating_total))

        vButton.setOnClickListener { Navigator.to(SDonateMake()) }

        updateSum()

    }

    override fun onResume() {
        super.onResume()
        if (accountId > 0) {
            vPager.setCurrentItem(1)
            pageRatingTotal.scrollTo(accountId)
        }
    }
    private fun updateSum() {
        ControllerDonates.setupLine(r.totalCount, vLine, vCounter)
    }

    fun reload() {
        RProjectSupportGetInfo()
                .onComplete {
                    r = it
                    updateSum()
                }
                .send(api)
    }

}