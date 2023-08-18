package com.sayzen.campfiresdk.screens.account.rating

import androidx.viewpager.widget.ViewPager

import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.requests.accounts.RAccountsRatingGet
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerStoryQuest
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.views.support.adapters.pager.PagerCardAdapter
import com.sup.dev.android.views.views.pager.ViewPagerIndicatorTitles

class SRating private constructor(r: RAccountsRatingGet.Response) : Screen(R.layout.screen_rating) {

    companion object {

        fun instance(action: NavigationAction) {
            ApiRequestsSupporter.executeInterstitial(action,
                    RAccountsRatingGet()) { r ->
                SRating(r)
            }
        }
    }

    init {
        disableShadows()
        disableNavigation()
        ControllerStoryQuest.incrQuest(API.QUEST_STORY_RATINGS)
        setTitle(t(API_TRANSLATE.app_ratings))

        val vPager = findViewById<ViewPager>(R.id.vPager)
        val vTitles = findViewById<ViewPagerIndicatorTitles>(R.id.viIndicator)
        val pagerAdapter = PagerCardAdapter()

        vTitles.setTitles(t(API_TRANSLATE.app_karma_count_30_days), t(API_TRANSLATE.app_level))

        vPager.adapter = pagerAdapter
        pagerAdapter.add(PageKarma(r))
        pagerAdapter.add(PageLvl(r))
    }


}
