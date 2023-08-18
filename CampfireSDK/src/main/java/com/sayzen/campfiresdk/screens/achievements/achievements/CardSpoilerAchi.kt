package com.sayzen.campfiresdk.screens.achievements.achievements

import androidx.annotation.StringRes
import com.dzen.campfire.api.API
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.cards.CardLoading
import com.sup.dev.android.views.cards.CardSpoiler
import com.sup.dev.java.tools.ToolsText
import com.sup.dev.java.tools.ToolsThreads

class CardSpoilerAchi(
        val pageAchievements: PageAchievements,
        val packIndex: Int
) : CardSpoiler() {

    private var finCount = 0
    val cardLoading = CardLoading()
    var isFirstExpand = true
    var karmaCount = 0.0
    var scrollToIndex = 0L
    val pack = when (packIndex) {
        1 -> API.ACHI_PACK_1
        2 -> API.ACHI_PACK_2
        3 -> API.ACHI_PACK_3
        4 -> API.ACHI_PACK_4
        5 -> API.ACHI_PACK_5
        else -> API.ACHI_PACK_6
    }


    init {
        super.add(cardLoading)
        setDividerTopVisible(false)
        setDividerVisible(false)

        for (i in pack) {
            karmaCount += (CampfireConstants.getAchievement(i.index)).info.getForce() * pageAchievements.achiLvl(i.index)
            if (pageAchievements.achiLvl(i.index) == (CampfireConstants.getAchievement(i.index)).info.maxLvl.toLong()) finCount++
        }

        if (karmaCount > 0)
            setRightText(finCount.toString() + " / " + pack.size + " (${ToolsText.numToStringRoundAndTrim(karmaCount, 2)})")
        else
            setRightText(finCount.toString() + " / " + pack.size)
        setRightTextColor(if (finCount > 0 && finCount == pack.size) ToolsResources.getColor(R.color.green_700) else 0)
    }

    override fun onExpandedClicked(expanded: Boolean) {
        if (!expanded) return
        if (!isFirstExpand) return
        isFirstExpand = false
        pageAchievements.loadPack(packIndex, this)
    }

    fun addAchi(card: CardAchievement): CardSpoilerAchi {
        if (card.achievement == API.ACHI_POST_KARMA
                || card.achievement == API.ACHI_COMMENTS_KARMA
                || card.achievement == API.ACHI_KARMA_COUNT
                || card.achievement == API.ACHI_KARMA_30
                || card.achievement == API.ACHI_MODERATOR_ACTION_KARMA
                || card.achievement == API.ACHI_STICKERS_KARMA
                || card.achievement == API.ACHI_VICEROY_KARMA_COUNT
                || card.achievement == API.ACHI_QUEST_KARMA
        ) card.setValueMultiplier(0.01)
        super.add(card)
        if (isExpanded() && card.achievement.index == scrollToIndex) {
            scrollToIndex = 0
            ToolsThreads.main(500) {
                pageAchievements.scrollToCard(card)
                ToolsThreads.main(500) {
                    card.flash()
                }
            }
        }

        return this
    }

    override fun setTitle(@StringRes title: Int): CardSpoilerAchi {
        return super.setTitle(title) as CardSpoilerAchi
    }

    fun onLoaded() {
        remove(cardLoading)
        for (i in pack.indices) {
            val card = CardAchievement(pageAchievements, pack[i])
            addAchi(card)
        }
    }

}
