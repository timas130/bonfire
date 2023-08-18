package com.sayzen.campfiresdk.screens.activities.quests

import com.dzen.campfire.api.API_TRANSLATE
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.settings.Settings

class SQuestsList : Screen(R.layout.screen_quests_list) {

    private val vRelayRace: Settings = findViewById(R.id.vRelayRace)
    private val vRubrics: Settings = findViewById(R.id.vRubrics)


    init {
        disableNavigation()
        disableShadows()
        setTitle(t(API_TRANSLATE.app_activities))

        vRelayRace.setTitle("Класический новогодний квест")
        vRelayRace.setOnClickListener { Navigator.to(SQuestNewYear()) }

        vRubrics.setTitle("Однажды зимней ночью")
        vRubrics.setSubtitle("Квест от пользователей Campfire")
        vRubrics.setOnClickListener { Navigator.to(SQuestDanTank()) }


    }

}