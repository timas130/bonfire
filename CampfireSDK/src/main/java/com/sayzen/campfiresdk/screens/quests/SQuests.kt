package com.sayzen.campfiresdk.screens.quests

import androidx.viewpager.widget.ViewPager
import com.dzen.campfire.api.API_TRANSLATE
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.screens.quests.feed.QuestPage
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.support.adapters.pager.PagerCardAdapter
import com.sup.dev.android.views.views.pager.ViewPagerIndicatorTitles

class SQuests : Screen(R.layout.screen_quests) {
    private val vPager: ViewPager = findViewById(R.id.vPager)
    private val vTitles: ViewPagerIndicatorTitles = findViewById(R.id.viIndicator)
    private val vAddFab: FloatingActionButton = findViewById(R.id.vAddFab)
    private val pagerCardAdapter = PagerCardAdapter()

    init {
        setTitle(t(API_TRANSLATE.quests))

        vPager.adapter = pagerCardAdapter

        vTitles.setTitles(t(API_TRANSLATE.quests_cat_new))
        pagerCardAdapter.add(QuestPage(this))

        vAddFab.setOnClickListener {
            Navigator.action(Navigator.TO, SQuestDrafts())
        }
    }
}
