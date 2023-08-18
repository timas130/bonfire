package com.sayzen.campfiresdk.screens.achievements

import androidx.appcompat.widget.Toolbar
import android.view.View
import androidx.viewpager.widget.ViewPager
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE

import com.dzen.campfire.api.requests.achievements.RAchievementsInfo
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.support.adapters.XAccount
import com.sayzen.campfiresdk.screens.achievements.achievements.PageAchievements
import com.sayzen.campfiresdk.screens.achievements.lvl.PageLvl
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerStoryQuest
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.devsupandroidgoogle.ControllerFirebaseAnalytics
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.support.adapters.pager.PagerCardAdapter
import com.sup.dev.android.views.views.pager.ViewPagerIndicatorTitles

class SAchievements private constructor(
        accountId: Long,
        accountName: String,
        scrollToId: Long,
        toPrev: Boolean,
        r: RAchievementsInfo.Response
) : Screen(R.layout.screen_achievements) {

    companion object {

        fun instance(toPrev: Boolean = false, action: NavigationAction) {
            instance(ControllerApi.account.getId(), ControllerApi.account.getName(), 0, toPrev, action)
        }

        fun instance(accountId: Long, accountName: String, scrollToId: Long, toPrev: Boolean = false, action: NavigationAction) {
            ApiRequestsSupporter.executeInterstitial(if (Navigator.getCurrent() is SAchievements) Navigator.REPLACE else action, RAchievementsInfo(accountId)) { r ->
                SAchievements(accountId, accountName, scrollToId, toPrev, r)
            }
        }
    }

    private val xAccount = XAccount()
            .setId(accountId)
            .setName(accountName)
            .setLevel(r.karmaForce)
            .setOnChanged { update() }

    init {
        disableShadows()
        disableNavigation()
        setTitle(t(API_TRANSLATE.app_achievements))

        if (ControllerApi.isCurrentAccount(accountId) && ControllerApi.account.getLevel() < r.karmaForce)
            ControllerApi.account.setLevel(r.karmaForce)

        val vPager: ViewPager = findViewById(R.id.vPager)
        val vTitles: ViewPagerIndicatorTitles = findViewById(R.id.viIndicator)

        vTitles.setTitles(t(API_TRANSLATE.app_achievements), t(API_TRANSLATE.app_privilege))
        val pagerCardAdapter = PagerCardAdapter()
        vPager.adapter = pagerCardAdapter

        pagerCardAdapter.add(PageAchievements(accountId, scrollToId, r))
        pagerCardAdapter.add(PageLvl(accountId, r.karmaForce, r.karma30))

        if (toPrev) {
            vPager.setCurrentItem(1, false)
        }

        vPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                if (position == 1) {
                    ControllerFirebaseAnalytics.post("Screen_Achievements", "To Privilege")
                }
            }
        })

        update()

        ControllerStoryQuest.incrQuest(API.QUEST_STORY_ACHI_SCREEN)
    }

    private fun update() {
        (findViewById<View>(R.id.vToolbar) as Toolbar).title = if (ControllerApi.isCurrentAccount(xAccount.getId())) t(API_TRANSLATE.app_achievements) else xAccount.getName()
    }

}
