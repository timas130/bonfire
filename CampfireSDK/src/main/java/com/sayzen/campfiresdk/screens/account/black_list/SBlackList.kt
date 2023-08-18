package com.sayzen.campfiresdk.screens.account.black_list

import com.dzen.campfire.api.API_TRANSLATE
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.settings.Settings

class SBlackList(
        val accountId:Long,
        val accountName: String
) : Screen(R.layout.screen_black_list) {

    private val vBlackListUsers: Settings = findViewById(R.id.vBlackListUsers)
    private val vBlackListFandoms: Settings = findViewById(R.id.vBlackListFandoms)

    init {
        disableNavigation()
        disableShadows()
        setTitle(t(API_TRANSLATE.settings_black_list))
        vBlackListUsers.setOnClickListener { Navigator.to(SBlackListUsers(accountId, accountName)) }
        vBlackListFandoms.setOnClickListener { SBlackListFandoms.instance(accountId, accountName, Navigator.TO) }
        vBlackListUsers.setTitle(t(API_TRANSLATE.settings_black_list_users))
        vBlackListFandoms.setTitle(t(API_TRANSLATE.settings_black_list_fandoms))
    }


}
