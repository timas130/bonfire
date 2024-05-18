package com.sayzen.campfiresdk.screens.activities.quests

import com.dzen.campfire.api.API_TRANSLATE
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.compose.publication.post.TestPostScreen
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.settings.Settings

class SQuestsList : Screen(R.layout.screen_quests_list) {
    private val vRelayRace: Settings = findViewById(R.id.vRelayRace)
    private val vRubrics: Settings = findViewById(R.id.vRubrics)
    private val vLogin: Settings = findViewById(R.id.vLogin)

    init {
        disableNavigation()
        disableShadows()
        setTitle(t(API_TRANSLATE.app_activities))

        vRelayRace.setTitle("Клаcсический новогодний квест")
        vRelayRace.setOnClickListener { Navigator.to(SQuestNewYear()) }

        vRubrics.setTitle("Однажды зимней ночью")
        vRubrics.setSubtitle("Квест от пользователей Bonfire")
        vRubrics.setOnClickListener { Navigator.to(SQuestDanTank()) }

        vLogin.setTitle("Хз что это")
        vLogin.setSubtitle("Не нажимай, а то сломаешь приложение")
        vLogin.setOnClickListener {
            Navigator.to(TestPostScreen())
        }
    }
}
