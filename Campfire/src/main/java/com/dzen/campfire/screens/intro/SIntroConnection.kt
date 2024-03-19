package com.dzen.campfire.screens.intro

import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.WorkerThread
import androidx.core.view.WindowCompat
import com.dzen.campfire.R
import com.dzen.campfire.api.API
import com.dzen.campfire.api.ApiResources
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.requests.accounts.RAccountsAddNotificationsToken
import com.dzen.campfire.api.requests.accounts.RAccountsGetInfo
import com.dzen.campfire.api.requests.accounts.RAccountsLogin
import com.dzen.campfire.api.requests.project.RProjectGetLoadingPictures
import com.dzen.campfire.app.App
import com.dzen.campfire.screens.hello.SCampfireHello
import com.sayzen.campfiresdk.compose.auth.AuthStartScreen
import com.sayzen.campfiresdk.compose.auth.VerifyEmailScreen
import com.sayzen.campfiresdk.controllers.*
import com.sayzen.campfiresdk.models.objects.MChatMessagesPool
import com.sayzen.campfiresdk.support.load
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsStorage
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.java.libs.debug.info
import com.sup.dev.java.tools.ToolsThreads
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import sh.sit.bonfire.auth.AuthController

class SIntroConnection : Screen(R.layout.screen_intro_connection) {
    enum class State {
        PROGRESS, ERROR
    }

    private var loadedBackground = false

    private val vProgress: View = findViewById(R.id.vProgress)
    private val vBackground: ImageView = findViewById(R.id.vBackground)
    private val vEmptyImage: ImageView = findViewById(R.id.vEmptyImage)
    private val vMessage: TextView = findViewById(R.id.vMessage)
    private val vRetry: TextView = findViewById(R.id.vRetry)
    private val vChangeAccount: TextView = findViewById(R.id.vChangeAccount)
    private val vBackgroundInfo: LinearLayout = findViewById(R.id.vBackgroundInfo)
    private val vTitle: TextView = findViewById(R.id.vTitle)
    private val vSubtitle: TextView = findViewById(R.id.vSubtitle)
    private var feedCategories: Array<Long> =
        emptyArray()  //  Костыль. Загрузка настроек может перезаписать выбранные пользователем фильтры

    init {
        activityRootBackground = ToolsResources.getColorAttr(R.attr.colorPrimary)
        disableNavigation()
        isBackStackAllowed = false

        vMessage.visibility = View.INVISIBLE
        vRetry.visibility = View.INVISIBLE
        vChangeAccount.visibility = View.INVISIBLE
        vRetry.text = ToolsResources.s(R.string.retry)
        vChangeAccount.text = ToolsResources.s(R.string.app_change_account)
        vRetry.setOnClickListener { sendLoginRequest() }
        vChangeAccount.setOnClickListener {
            ToolsThreads.thread {
                runBlocking(Dispatchers.IO) { AuthController.logout() }
            }
            Navigator.set(AuthStartScreen(onLogin = {
                Navigator.set(SIntroConnection())
            }))
        }
        vMessage.text = ToolsResources.s(R.string.connection_error)

        App.activity().updateMessagesCount()
        ControllerActivities.clear()
        App.activity().updateNotificationsCount()
        App.activity().resetStacks()
        ControllerHoliday.onAppStart()

        // for some ungodly reason, DecorFitsSystemWindowEffect doesn't dispose sometimes
        WindowCompat.setDecorFitsSystemWindows(getActivity().window, true)

        loadBackgroundImageData()
        sendLoginRequest()
    }

    override fun onBackPressed(): Boolean {
        App.activity().finish()
        return true
    }

    private fun setError(message: String) {
        ToolsThreads.main {
            setState(State.ERROR)
            vMessage.text = message
        }
    }

    private fun sendLoginRequest() {
        setState(State.PROGRESS)
        ToolsThreads.thread {
            val canLogin = runBlocking(Dispatchers.IO) { AuthController.getCanLogin() }
            Log.d("AuthController", "getCanLogin(): $canLogin")
            when (canLogin) {
                AuthController.CanLoginResult.NotLoggedIn, AuthController.CanLoginResult.InvalidLogin -> {
                    ToolsThreads.main {
                        Navigator.replace(AuthStartScreen(onLogin = {
                            Navigator.replace(SIntroConnection())
                        }))
                    }
                }

                AuthController.CanLoginResult.NoNetwork -> {
                    setError(ToolsResources.s(R.string.error_network_error))
                }

                AuthController.CanLoginResult.HardBanned -> {
                    setError(ToolsResources.s(R.string.error_hard_banned))
                }

                AuthController.CanLoginResult.NotVerified -> {
                    ToolsThreads.main {
                        Navigator.replace(VerifyEmailScreen(onBack = {
                            Navigator.replace(SIntroConnection())
                        }))
                    }
                }

                AuthController.CanLoginResult.UnknownError -> {
                    setError(ToolsResources.s(R.string.error_unknown))
                }

                AuthController.CanLoginResult.Success -> {
                    continueLegacyLogin()
                }
            }
        }
    }

    @WorkerThread
    private fun continueLegacyLogin() {
        val account = ControllerApi.getLastAccount()
        if (account.id == 0L) {
            ControllerChats.clearMessagesCount()
            sendLoginRequestNow(false)

            // if the fcm token is not ready yet, try to refresh
            // it every second for a minute
            if (ControllerNotifications.token.isEmpty()) {
                ToolsThreads.timerMain(1000, 1000 * 60L, {
                    if (ControllerNotifications.token.isNotEmpty()) {
                        it.unsubscribe()
                        RAccountsAddNotificationsToken(ControllerNotifications.token)
                            .send(api)
                    }
                })
            }
        } else {
            ControllerApi.setCurrentAccount(
                account,
                ControllerApi.getLastHasSubscribes(),
                ControllerApi.getLastProtadmins()
            )
            ToolsThreads.main(3000) { sendLoginRequestNow(true) }
            toFeed()
        }
    }

    private fun sendLoginRequestNow(background: Boolean) {
        setState(State.PROGRESS)
        val languageId = ControllerApi.getLanguageId()
        RAccountsLogin(
            ControllerNotifications.token, languageId,
            ControllerTranslate.getSavedHash(languageId),
            ControllerTranslate.getSavedHash(API.LANGUAGE_EN)
        ).onComplete { r ->
            ControllerABParams.set(r.ABParams)
            ControllerApi.setVersion(r.version, r.supported)
            ControllerApi.setCurrentAccount(r.account ?: Account(), r.hasSubscribes, r.protoadmins)

            if (r.account == null) {
                ToolsToast.show(ToolsResources.s(R.string.error_unknown))
                Navigator.set(AuthStartScreen(onLogin = {
                    Navigator.set(SIntroConnection())
                }))
            } else {
                ControllerTranslate.addMap(r.translate_language_id, r.translate_map, r.translateMapHash)
                ControllerTranslate.addMap(API.LANGUAGE_EN, r.translate_map_eng, r.translateMapHashEng)
                ControllerSettings.setSettings(r.account!!.id, r.settings)
                ControllerSettings.feedCategories = feedCategories
                ControllerApi.setServerTime(r.serverTime)
                ToolsThreads.main(1000) { loadInfo() }
                if (!background) {
                    Log.d("SIntroConnection", "navigating to feed after full load")
                    toFeed()
                }
            }
        }.onError {
            setState(State.ERROR)
        }.send(api)
    }


    private fun toFeed() {
        ToolsThreads.main(true) {
            if (!App.activity().parseStartAction()) {
                SCampfireHello.showIfNeed {
                    feedCategories = ControllerSettings.feedCategories
                    App.activity().toMainScreen()
                }
            }
        }
    }

    private fun setState(state: State) {
        vRetry.visibility = if (state == State.ERROR) View.VISIBLE else View.GONE
        vChangeAccount.visibility = if (state == State.ERROR) View.VISIBLE else View.GONE
        vMessage.visibility = if (state == State.ERROR) View.VISIBLE else View.GONE

        vBackground.visibility = if (state == State.ERROR) View.GONE else View.VISIBLE
        vBackgroundInfo.visibility = if (state == State.ERROR) View.GONE else View.VISIBLE

        if (state == State.ERROR) {
            vProgress.visibility = View.GONE
            vEmptyImage.visibility = View.VISIBLE
            ImageLoader.load(ApiResources.IMAGE_BACKGROUND_20).noHolder().into(vEmptyImage)
        }

        if (state == State.PROGRESS && !loadedBackground) {
            vProgress.visibility = View.GONE
        }
    }

    private fun loadInfo(tryCount: Int = 3) {
        RAccountsGetInfo(ControllerApi.getLanguageId())
            .onComplete { r ->
                ControllerApi.setApiInfo(r.apiInfo)
                ControllerApi.setFandomsKarma(r.fandomsIds, r.languagesIds, r.karmaCounts)
                ControllerApi.setFandomsViceroy(r.viceroyFandomsIds, r.viceroyLanguagesIds)
                ControllerActivities.setRelayRacesCount(r.activitiesCount)
                ControllerNotifications.setNewNotifications(r.notifications)
                ControllerChats.clearMessagesCount()
                for (i in r.chatMessagesCountTags) ControllerChats.setMessages(MChatMessagesPool(i, true).setCount(1))
            }
            .onError {
                if (tryCount > 0)
                    ToolsThreads.main(3000) { loadInfo(tryCount - 1) }
            }
            .send(api)
    }

    companion object {
        private const val LAST_UPDATE_TIME = "SIntroConnection.bg.update"
        private const val LAST_UPDATE_DATA = "SIntroConnection.bg.data"
    }

    private fun loadBackgroundImageData() {
        val lastUpdate = ToolsStorage.getLong(LAST_UPDATE_TIME, 0L)
        // if the last update is more than 1 day old
        if (lastUpdate <= System.currentTimeMillis() - 1000 * 3600) {
            // refresh in the background
            RProjectGetLoadingPictures()
                .onComplete { r ->
                    info("refreshed loading background image info")
                    ToolsStorage.put(LAST_UPDATE_TIME, System.currentTimeMillis())
                    ToolsStorage.put(LAST_UPDATE_DATA, r.pictures)
                }
                .onError { err ->
                    err.printStackTrace()
                }
                .send(api)
        }

        val data = ToolsStorage.getJsonParsables(LAST_UPDATE_DATA, RProjectGetLoadingPictures.LoadingPicture::class)
            ?: arrayOf()
        val activeBackground = data.find { it.isActive() } ?: return

        val title = ControllerTranslate.getMyMap()?.get(activeBackground.titleTranslation)?.text
            ?: activeBackground.titleTranslation
        val subtitle = ControllerTranslate.getMyMap()?.get(activeBackground.subtitleTranslation)?.text
            ?: activeBackground.subtitleTranslation

        val imageLink = ImageLoader.load(activeBackground.image)
        ImageLoader.load(
            link = imageLink,
            vImage = vBackground,
            onLoadedBitmap = {
                vBackgroundInfo.visibility = View.VISIBLE
                vTitle.text = title
                vSubtitle.text = subtitle
                vEmptyImage.visibility = View.GONE
                vProgress.visibility = View.GONE
                loadedBackground = true
            }
        )
    }
}
