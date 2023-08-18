package com.sayzen.campfiresdk.screens.other.minigame

import android.widget.TextView
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.requests.project.RProjectMiniGameAddScore
import com.dzen.campfire.api.requests.project.RProjectMiniGameGetinfo
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.tools.ToolsResources

class SMinigame(
        var miniGameScoreHumans: Long,
        var miniGameScoreRobots: Long
) : Screen(R.layout.screen_minigame) {

    companion object {

        fun instance(action: NavigationAction) {
            ApiRequestsSupporter.executeInterstitial(action, RProjectMiniGameGetinfo()) { r ->
                SMinigame(r.miniGameScoreHumans, r.miniGameScoreRobots)
            }
        }

    }

    private val vMiniGame: ViewMiniGame = findViewById(R.id.vMiniGame)
    private val vScoreRobots: TextView = findViewById(R.id.vScoreRobots)
    private val vScoreHumans: TextView = findViewById(R.id.vScoreHumans)

    init {
        activityRootBackground = ToolsResources.getColorAttr(R.attr.colorPrimary)
        disableNavigation()
        isBackStackAllowed = false

        vScoreRobots.text = t(API_TRANSLATE.into_mini_game_robots, miniGameScoreRobots)
        vScoreHumans.text = t(API_TRANSLATE.into_mini_game_humans, miniGameScoreHumans)

        vMiniGame.onWinHuman = {
            miniGameScoreHumans++
            vScoreHumans.text = t(API_TRANSLATE.into_mini_game_humans, miniGameScoreHumans)
            RProjectMiniGameAddScore(1, 0).send(api)
        }
        vMiniGame.onWinRobot = {
            miniGameScoreRobots++
            vScoreRobots.text = t(API_TRANSLATE.into_mini_game_robots, miniGameScoreRobots)
            RProjectMiniGameAddScore(0, 1).send(api)
        }
    }



}