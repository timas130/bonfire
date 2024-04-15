package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.models.account.AccountSettings
import com.dzen.campfire.api.requests.accounts.RAccountsLogin
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.rust.RustDailyTask
import com.dzen.campfire.server.rust.RustNotifications
import com.dzen.campfire.server.type.NotificationTokenType
import com.sup.dev.java.libs.json.Json

class EAccountsLogin : RAccountsLogin("", 0, 0, 0) {

    private var account: Account? = null


    override fun check() {

    }

    override fun execute(): Response {

        loadAccount()
        ControllerOptimizer.insertEnter(apiAccount.id)

        updateEnter()
        if (tokenNotification.isNotEmpty()) {
            RustNotifications.setToken(accessToken!!, NotificationTokenType.FCM, tokenNotification)
        }
        ControllerSubThread.inSub("EAccountsLogin") {
            if (languageId > 0) ControllerProjects.initAccountForProject(apiAccount, languageId, requestProjectKey)
        }

        val accountSettings = AccountSettings()
        try {
            accountSettings.json(false, Json(apiAccount.settings))
        } catch (e: Exception) {
        }

        val serverTranslateHash = ControllerServerTranslates.getHash(languageId)
        val translateMap = if (serverTranslateHash == translateMapHash && translateMapHash != 0) {
            hashMapOf()
        } else {
            ControllerServerTranslates.getMap(languageId)
        }

        val serverTranslateHashEng = ControllerServerTranslates.getHash(API.LANGUAGE_EN)
        val translateMapEng = if (serverTranslateHashEng == translateMapHashEng && translateMapHashEng != 0) {
            hashMapOf()
        } else {
            ControllerServerTranslates.getMap(API.LANGUAGE_EN)
        }

        RustDailyTask.checkIn(apiAccount.id)

        return Response(
                API.VERSION, API.SUPPORTED_VERSION,
                arrayOf(),
                API.PROTOADMINS, account, accountSettings, apiAccount.tag_s_1.isNotEmpty(),
                languageId, translateMap, translateMapEng, serverTranslateHash,
                serverTranslateHashEng
        )
    }

    private fun loadAccount() {
        if (apiAccount.id < 1) return
        account = ControllerAccounts.instance(
                apiAccount.id, apiAccount.accessTag, System.currentTimeMillis(),
                apiAccount.name, apiAccount.imageId, apiAccount.sex, apiAccount.accessTagSub,
                apiAccount.dateCreate,
        )
    }

    private fun updateEnter() {
        val last = ControllerOptimizer.getCollisionNullable(apiAccount.id, API.COLLISION_ACCOUNT_LAST_DAILY_ENTER_DATE) ?: 0
        if (last + 1000L * 60 * 60 * 24 < System.currentTimeMillis()) {
            ControllerOptimizer.updateOrCreateCollision(apiAccount.id, API.COLLISION_ACCOUNT_LAST_DAILY_ENTER_DATE, System.currentTimeMillis())
            ControllerAccounts.updateEnters(apiAccount.id, 1)
            ControllerAchievements.addAchievementWithCheck(apiAccount.id, API.ACHI_ENTERS)
        }
    }


}
