package com.sayzen.campfiresdk.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.requests.accounts.RAccountsBioSetSex
import com.dzen.campfire.api.requests.accounts.RAccountsBlackListAdd
import com.dzen.campfire.api.requests.accounts.RAccountsBlackListRemove
import com.dzen.campfire.api.requests.accounts.RAccountsChangeName
import com.dzen.campfire.api.requests.achievements.RAchievementsOnFinish
import com.dzen.campfire.api.requests.fandoms.RFandomsBlackListAdd
import com.dzen.campfire.api.requests.fandoms.RFandomsBlackListContains
import com.dzen.campfire.api.requests.fandoms.RFandomsBlackListRemove
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.events.account.EventAccountAddToBlackList
import com.sayzen.campfiresdk.models.events.account.EventAccountBioChangedSex
import com.sayzen.campfiresdk.models.events.account.EventAccountChanged
import com.sayzen.campfiresdk.models.events.account.EventAccountRemoveFromBlackList
import com.sayzen.campfiresdk.models.events.fandom.EventFandomBlackListChange
import com.sayzen.campfiresdk.models.events.translate.EventTranslateChanged
import com.sayzen.campfiresdk.screens.achievements.SAchievements
import com.sayzen.campfiresdk.screens.fandoms.moderation.view.SModerationView
import com.sayzen.campfiresdk.screens.post.create.SPostCreate
import com.sayzen.campfiresdk.screens.post.view.SPost
import com.sayzen.devsupandroidgoogle.ControllerFirebaseAnalytics
import com.sayzen.devsupandroidgoogle.ControllerGoogleAuth
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.tools.ToolsIntent
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.screens.SAlert
import com.sup.dev.android.views.splash.SplashAlert
import com.sup.dev.android.views.splash.SplashCheckBoxes
import com.sup.dev.android.views.splash.SplashField
import com.sup.dev.android.views.splash.SplashMenu
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsColor
import com.sup.dev.java.tools.ToolsText
import com.sup.dev.java.tools.ToolsThreads

object ControllerCampfireSDK {

    var ROOT_FANDOM_ID = 0L
    var ROOT_PROJECT_KEY: String = ""
    var ROOT_PROJECT_SUB_KEY: String = ""

    var IS_DEBUG = false

    var ENABLE_CLOSE_FANDOM_ALERT = false

    var ON_TO_DRAFTS_CLICKED: (action: NavigationAction) -> Unit = { }
    var ON_SCREEN_CHAT_START: () -> Unit = { }

    var SEARCH_FANDOM: (callback: (Fandom) -> Unit) -> Unit = { }

    var projectKey = ""
    var onLoginFailed: () -> Unit = {}

    val eventBus = EventBus.subscribe(EventTranslateChanged::class){ onTranslateChanged()}

    fun init(
            projectKey: String,
            logoColored: Int,
            logoWhite: Int,
            onLoginFailed: () -> Unit
    ) {
        this.projectKey = projectKey
        this.onLoginFailed = onLoginFailed
        ControllerSettings.init()
        ControllerApi.init()
        ControllerActivities.init()
        ControllerChats.init()
        ControllerNotifications.init(logoColored, logoWhite)
        ControllerFirebaseAnalytics.init()
        ControllerAlive.init()
        ControllerGoogleAuth.init("276237287601-6e9aoah4uivbjh6lnn1l9hna6taljd9u.apps.googleusercontent.com", onLoginFailed)

        SAlert.GLOBAL_SHOW_WHOOPS = false

        onTranslateChanged()
    }

    fun onTranslateChanged(){
        SupAndroid.TEXT_APP_NAME = ToolsResources.s("app_name")
        SupAndroid.TEXT_APP_CANCEL = t(API_TRANSLATE.app_cancel)
        SupAndroid.TEXT_APP_WHOOPS = t(API_TRANSLATE.app_whoops)
        SupAndroid.TEXT_APP_RETRY = t(API_TRANSLATE.app_retry)
        SupAndroid.TEXT_APP_BACK = t(API_TRANSLATE.app_back)
        SupAndroid.TEXT_APP_DOWNLOADING = t(API_TRANSLATE.app_downloading)
        SupAndroid.TEXT_APP_SHARE = t(API_TRANSLATE.app_share)
        SupAndroid.TEXT_APP_SHARE_MESSAGE_HINT = t(API_TRANSLATE.app_share_message_hint)
        SupAndroid.TEXT_APP_DOWNLOADED = t(API_TRANSLATE.app_downloaded)
        SupAndroid.TEXT_APP_DONT_SHOW_AGAIN = t(API_TRANSLATE.app_dont_show_again)
        SupAndroid.TEXT_APP_LINK = t(API_TRANSLATE.app_link)
        SupAndroid.TEXT_APP_CHOOSE = t(API_TRANSLATE.app_choose)
        SupAndroid.TEXT_APP_LOADING = t(API_TRANSLATE.app_loading)
        SupAndroid.TEXT_ERROR_NETWORK = t(API_TRANSLATE.error_network)
        SupAndroid.TEXT_ERROR_ACCOUNT_BANED = t(API_TRANSLATE.error_account_baned)
        SupAndroid.TEXT_ERROR_GONE = t(API_TRANSLATE.error_gone)
        SupAndroid.TEXT_ERROR_CANT_LOAD_IMAGE = t(API_TRANSLATE.error_cant_load_image)
        SupAndroid.TEXT_ERROR_PERMISSION_FILES = t(API_TRANSLATE.error_permission_files)
        SupAndroid.TEXT_ERROR_PERMISSION_MIC = t(API_TRANSLATE.error_permission_mic)
        SupAndroid.TEXT_ERROR_APP_NOT_FOUND = t(API_TRANSLATE.error_app_not_found)
        SupAndroid.TEXT_ERROR_CANT_FIND_IMAGES = t(API_TRANSLATE.error_cant_find_images)
        SupAndroid.TEXT_ERROR_MAX_ITEMS_COUNT = t(API_TRANSLATE.error_max_items_count)
    }

    fun onToModerationClicked(moderationId: Long, commentId: Long, action: NavigationAction) {
        SModerationView.instance(moderationId, commentId, action)
    }

    fun onToPostClicked(postId: Long, commentId: Long, action: NavigationAction) {
        SPost.instance(postId, commentId, action)
    }

    fun onToDraftClicked(postId: Long, action: NavigationAction) {
        SPostCreate.instance(postId, action)
    }

    fun onToDraftsClicked(action: NavigationAction) {
        ON_TO_DRAFTS_CLICKED.invoke(action)
    }

    /*fun onToPostTagsClicked(postId: Long, isMyPublication: Boolean, action: NavigationAction) {
        SPostCreationTags.instance(postId, isMyPublication, action)
    }*/


    fun onToAchievementClicked(accountId: Long, accountName: String, achievementIndex: Long, toPrev: Boolean, action: NavigationAction) {
        SAchievements.instance(accountId, accountName, achievementIndex, toPrev, action)
    }

    //
    //  Actions
    //

    fun changeLogin() {
        SplashField()
                .setTitle(t(API_TRANSLATE.profile_change_name))
                .addChecker(t(API_TRANSLATE.profile_change_name_error)) { s -> ToolsText.checkStringChars(s, API.ACCOUNT_LOGIN_CHARS) }
                .setMin(API.ACCOUNT_NAME_L_MIN)
                .setMax(API.ACCOUNT_NAME_L_MAX)
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .setOnEnter(t(API_TRANSLATE.app_change)) { dialog, name ->
                    changeLoginNow(name, true) {}
                }
                .asSheetShow()
    }

    fun changeLoginNow(name: String, achievementNotificationEnabled: Boolean, onComplete: () -> Unit) {
        ApiRequestsSupporter.executeProgressDialog(RAccountsChangeName(name, achievementNotificationEnabled)) { r ->
            EventBus.post(EventAccountChanged(ControllerApi.account.getId(), ControllerApi.account.getName()))
            onComplete.invoke()
        }.onApiError(RAccountsChangeName.E_LOGIN_NOT_ENABLED) {
            ToolsToast.show(t(API_TRANSLATE.error_login_taken))
        }.onApiError(RAccountsChangeName.E_LOGIN_IS_NOT_DEFAULT) {
            ToolsToast.show(t(API_TRANSLATE.error_login_cant_change))
            onComplete.invoke()
        }.onApiError(API.ERROR_ACCOUNT_IS_BANED) {
            ToolsToast.show(t(API_TRANSLATE.error_login_cant_change))
            onComplete.invoke()
        }
    }

    fun setSex(sex: Long, onComplete: () -> Unit) {
        ApiRequestsSupporter.executeProgressDialog(RAccountsBioSetSex(sex)) { r ->
            EventBus.post(EventAccountBioChangedSex(ControllerApi.account.getId(), sex))
            onComplete.invoke()
        }
    }

    fun switchToBlackListFandom(fandomId: Long) {
        ApiRequestsSupporter.executeProgressDialog(RFandomsBlackListContains(fandomId)) { r ->
            if (r.contains) removeFromBlackListFandom(fandomId)
            else addToBlackListFandom(fandomId)
        }
    }

    fun addToBlackListFandom(fandomId: Long) {
        ApiRequestsSupporter.executeEnabledConfirm(t(API_TRANSLATE.fandoms_menu_black_list_add_confirm), t(API_TRANSLATE.app_add), RFandomsBlackListAdd(fandomId)) {
            EventBus.post(EventFandomBlackListChange(fandomId, true))
            ToolsToast.show(t(API_TRANSLATE.app_done))
        }
    }

    fun removeFromBlackListFandom(fandomId: Long) {
        ApiRequestsSupporter.executeEnabledConfirm(t(API_TRANSLATE.fandoms_menu_black_list_remove_confirm), t(API_TRANSLATE.app_remove), RFandomsBlackListRemove(fandomId)) {
            EventBus.post(EventFandomBlackListChange(fandomId, false))
            ToolsToast.show(t(API_TRANSLATE.app_done))
        }
    }

    fun addToBlackListUser(accountId: Long) {
        ApiRequestsSupporter.executeEnabledConfirm(t(API_TRANSLATE.profile_black_list_add_confirm), t(API_TRANSLATE.app_add), RAccountsBlackListAdd(accountId)) {
            EventBus.post(EventAccountAddToBlackList(accountId))
            ToolsToast.show(t(API_TRANSLATE.app_done))
        }
    }

    fun removeFromBlackListUser(accountId: Long) {
        ApiRequestsSupporter.executeEnabledConfirm(t(API_TRANSLATE.profile_black_list_remove_confirm), t(API_TRANSLATE.app_remove), RAccountsBlackListRemove(accountId)) {
            EventBus.post(EventAccountRemoveFromBlackList(accountId))
            ToolsToast.show(t(API_TRANSLATE.app_done))
        }
    }

    fun shareCampfireApp() {
        SplashField()
                .setHint(t(API_TRANSLATE.app_message))
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .setOnEnter(t(API_TRANSLATE.app_share)) { _, text ->
                    ToolsIntent.shareText("$text\n\rhttps://play.google.com/store/apps/details?id=com.dzen.campfire")
                    ToolsThreads.main(10000) { RAchievementsOnFinish(API.ACHI_APP_SHARE.index).send(api) }
                }
                .asSheetShow()
    }

    fun createLanguageMenu(selectedId: Long, exclude: Array<Long> = emptyArray(), onClick: (Long) -> Unit): SplashMenu {
        val w = SplashMenu()
        val code = ControllerApi.getLanguageCode()

        for (i in API.LANGUAGES)
            if (i.code == code || i.code == "en")
                if (!exclude.contains(i.id))
                    w.add(i.name) { onClick.invoke(i.id) }.backgroundRes(R.color.focus) { i.id == selectedId }
        w.group(" ")

        for (i in API.LANGUAGES)
            if (i.code != code && i.code != "en")
                if (!exclude.contains(i.id))
                    w.add(i.name) { onClick.invoke(i.id) }.backgroundRes(R.color.focus) { i.id == selectedId }

        return w
    }


    fun createLanguageCheckMenu(languages: ArrayList<Long>): SplashCheckBoxes {
        val w = SplashCheckBoxes()
        val code = ControllerApi.getLanguageCode()
        for (i in API.LANGUAGES) {
            if (i.code == code || i.code == "en")
                w.add(i.name).checked(languages.contains(i.id)).onChange {
                    if (it.isChecked) {
                        if (!languages.contains(i.id)) languages.add(i.id)
                    } else {
                        languages.remove(i.id)
                    }
                }
        }
        w.group(" ")
        for (i in API.LANGUAGES) {
            if (i.code != code && i.code != "en")
                w.add(i.name).checked(languages.contains(i.id)).onChange {
                    if (it.isChecked) {
                        if (!languages.contains(i.id)) languages.add(i.id)
                    } else {
                        languages.remove(i.id)
                    }
                }
        }
        return w
    }

    fun logoutWithAlert() {
        SplashAlert()
                .setText(t(API_TRANSLATE.settings_exit_confirm))
                .setOnEnter(t(API_TRANSLATE.app_exit)) { logoutNow() }
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .asSheetShow()
    }

    fun logoutNow() {
        val d = ToolsView.showProgressDialog()
        ControllerApi.logout {
            d.hide()
            onLoginFailed.invoke()
        }
    }

    fun showCampfireAlertProfile(){
        SplashAlert()
                .setTitleImage(R.drawable.logo_campfire_128x128)
                .setImageBackground(ToolsColor.argb(255, 49, 49, 49))
                .setText(t(API_TRANSLATE.message_alert_campfire_profile))
                .setOnEnter("Google Play"){
                    ToolsIntent.openLink("https://play.google.com/store/apps/details?id=com.dzen.campfire")
                }
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .asSheetShow()
    }

}
