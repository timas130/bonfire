package com.sayzen.campfiresdk.controllers


import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.BookmarksFolder
import com.dzen.campfire.api.models.account.AccountSettings
import com.dzen.campfire.api.requests.accounts.RAccountSetSettings
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.models.EventStyleChanged
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsStorage
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.ViewCircleImage
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.tools.ToolsThreads

object ControllerSettings {

    var accountSettings = AccountSettings()
    var fastUpdateLimit = 5 // Возможность быстро обновить настройки при запуске приложения. Это важно если пользователь зашел включить режим не беспокоить и сразу закрыл приложение.
    private var updateSettingsTime = 0L
    private var updateSettingsStarted = false

    fun init() {
        accountSettings.json(false, ToolsStorage.getJson("ControllerSettings_accountSettings") ?: Json())
    }

    fun clear() {
        val settings = AccountSettings()
        settings.isStub = true
        setSettings(0, settings)
    }

    fun onSettingsUpdated() {
        updateSettingsTime = System.currentTimeMillis() + 1000
        EventBus.post(EventStyleChanged())
        if (!updateSettingsStarted) {
            updateSettingsStarted = true
            ToolsThreads.thread {
                if (fastUpdateLimit < 1) {
                    while (System.currentTimeMillis() < updateSettingsTime) {
                        val x = updateSettingsTime - System.currentTimeMillis()
                        if (x > 0) ToolsThreads.sleep(x)
                    }
                }
                fastUpdateLimit--
                if (!updateSettingsStarted) return@thread
                setSettingsNow()
            }
        }
    }

    fun setSettingsNow(onFinish: () -> Unit = {}, onError: () -> Unit = {}) {
        updateSettingsStarted = false
        ToolsStorage.put("ControllerSettings_accountSettings", accountSettings.json(true, Json()))
        if (accountSettings.isStub) return
        ApiRequestsSupporter.execute(RAccountSetSettings(accountSettings)) {
            onFinish.invoke()
        }.onError { onError.invoke() }
    }

    var notifyDateChecked: Long
        get() = ToolsStorage.getLong("KEY_NOTIFY_DATE_CHECKED", 0)
        set(date) = ToolsStorage.put("KEY_NOTIFY_DATE_CHECKED", date)

    fun setSettings(accountId: Long, accountSettings: AccountSettings) {
        ToolsStorage.put("ControllerSettings_accountId", accountId)
        this.accountSettings = accountSettings
        ToolsStorage.put("ControllerSettings_accountSettings", accountSettings.json(true, Json()))
        ViewCircleImage.SQUARE_GLOBAL_MODE = styleAvatarsSquare
        ViewCircleImage.SQUARE_GLOBAL_CORNED = ToolsView.dpToPx(styleAvatarsRounding)
        EventBus.post(EventStyleChanged())

    }

    fun getLastSettingsAccountId() = ToolsStorage.getLong("ControllerSettings_accountId", 0L)

    //
    //  Style
    //

    var styleNewYearAvatars: Boolean
        get() = accountSettings.styleNewYearAvatars
        set(b) {
            accountSettings.styleNewYearAvatars = b
            onSettingsUpdated()
        }
    var styleNewYearProfileAnimation: Boolean
        get() = accountSettings.styleNewYearProfileAnimation
        set(b) {
            accountSettings.styleNewYearProfileAnimation = b
            onSettingsUpdated()
        }
    var styleNewYearSnow: Int
        get() = accountSettings.styleNewYearSnow
        set(b) {
            accountSettings.styleNewYearSnow = b
            onSettingsUpdated()
        }
    var styleAvatarsSquare: Boolean
        get() = accountSettings.styleSquare
        set(b) {
            accountSettings.styleSquare = b
            ViewCircleImage.SQUARE_GLOBAL_MODE = styleAvatarsSquare
            onSettingsUpdated()
        }

    var styleAvatarsRounding: Int
        get() = accountSettings.styleCorned
        set(b) {
            accountSettings.styleCorned = b
            ViewCircleImage.SQUARE_GLOBAL_CORNED = ToolsView.dpToPx(styleAvatarsRounding)
            onSettingsUpdated()
        }

    var fandomBackground: Boolean
        get() = accountSettings.fandomBackground
        set(b) {
            accountSettings.fandomBackground = b
            onSettingsUpdated()
        }

    var styleChatRounding: Int
        get() = accountSettings.styleChatCorned
        set(b) {
            accountSettings.styleChatCorned = b
            onSettingsUpdated()
        }


    var styleTheme: Int
        get() = accountSettings.theme
        set(b) {
            accountSettings.theme = b
            onSettingsUpdated()
        }

    var interfaceType: Int
        get() = accountSettings.interfaceType
        set(b) {
            accountSettings.interfaceType = b
            onSettingsUpdated()
        }

    var fullscreen: Boolean
        get() = accountSettings.fullscreen
        set(b) {
            accountSettings.fullscreen = b
            onSettingsUpdated()
        }

    var isProfileListStyle: Boolean
        get() = accountSettings.isProfileListStyle
        set(b) {
            accountSettings.isProfileListStyle = b
            onSettingsUpdated()
        }

    //
    //  App
    //

    var appLanguage: String
        get() = if (accountSettings.appLanguage.isEmpty()) ToolsAndroid.getLanguageCode().toLowerCase() else accountSettings.appLanguage
        set(b) {
            accountSettings.appLanguage = b
            onSettingsUpdated()
        }

    var watchPost: Boolean
        get() = accountSettings.watchPost
        set(b) {
            accountSettings.watchPost = b
            onSettingsUpdated()
        }

    var longPlusFandomId: Long
        get() = accountSettings.longPlusFandomId
        set(b) {
            accountSettings.longPlusFandomId = b
            onSettingsUpdated()
        }

    var longPlusFandomLanguageId: Long
        get() = if (accountSettings.longPlusFandomLanguageId == 0L) ControllerApi.getLanguage(appLanguage).id else accountSettings.longPlusFandomLanguageId
        set(b) {
            accountSettings.longPlusFandomLanguageId = b
            onSettingsUpdated()
        }

    var longPlusFandomName: String
        get() = accountSettings.longPlusFandomName
        set(b) {
            accountSettings.longPlusFandomName = b
            onSettingsUpdated()
        }

    var fastPublicationFandomId: Long
        get() = accountSettings.fastPublicationFandomId
        set(b) {
            accountSettings.fastPublicationFandomId = b
            onSettingsUpdated()
        }

    var fastPublicationFandomLanguageId: Long
        get() = if (accountSettings.fastPublicationFandomLanguageId == 0L) ControllerApi.getLanguage(appLanguage).id else accountSettings.fastPublicationFandomLanguageId
        set(b) {
            accountSettings.fastPublicationFandomLanguageId = b
            onSettingsUpdated()
        }

    var fastPublicationFandomName: String
        get() = accountSettings.fastPublicationFandomName
        set(b) {
            accountSettings.fastPublicationFandomName = b
            onSettingsUpdated()
        }

    var voiceMessagesAutoLock: Boolean
        get() = accountSettings.voiceMessagesAutoLock
        set(b) {
            accountSettings.voiceMessagesAutoLock = b
            onSettingsUpdated()
        }

    var voiceMessagesAutoSend: Boolean
        get() = accountSettings.voiceMessagesAutoSend
        set(b) {
            accountSettings.voiceMessagesAutoSend = b
            onSettingsUpdated()
        }

    var voiceMessagesIgnore: Boolean
        get() = accountSettings.voiceMessagesIgnore
        set(b) {
            accountSettings.voiceMessagesIgnore = b
            onSettingsUpdated()
        }

    var helloIsShowed: Boolean
        get() = accountSettings.helloIsShowed
        set(b) {
            accountSettings.helloIsShowed = b
            onSettingsUpdated()
        }

    var helloShortIsShowed: Boolean
        get() = accountSettings.helloShortIsShowed
        set(b) {
            accountSettings.helloShortIsShowed = b
            onSettingsUpdated()
        }

    var rulesIsShowed: Boolean
        get() = accountSettings.rulesIsShowed
        set(b) {
            if (b != accountSettings.rulesIsShowed) onSettingsUpdated()
            accountSettings.rulesIsShowed = b
        }

    var anonRates: Boolean
        get() = accountSettings.anonRates
        set(b) {
            if (b != accountSettings.anonRates) onSettingsUpdated()
            accountSettings.anonRates = b
        }

    var viewedChats: Array<Long>
        get() = accountSettings.viewedChats
        set(b) {
            accountSettings.viewedChats = b
            onSettingsUpdated()
        }

    var storyQuestIndex: Long
        get() = accountSettings.storyQuestIndex
        set(b) {
            accountSettings.storyQuestIndex = b
            onSettingsUpdated()
        }

    var storyQuestProgress: Long
        get() = accountSettings.storyQuestProgress
        set(b) {
            accountSettings.storyQuestProgress = b
            onSettingsUpdated()
        }

    //
    //  Notifications Settings
    //

    var notifications: Boolean
        get() = accountSettings.notifications
        set(b) {
            accountSettings.notifications = b
            onSettingsUpdated()
        }

    var autoReadNotifications: Boolean
        get() = accountSettings.autoReadNotifications
        set(b) {
            accountSettings.autoReadNotifications = b
            onSettingsUpdated()
        }

    var salientTime: Long
        get() = accountSettings.salientTime
        set(b) {
            accountSettings.salientTime = b
            onSettingsUpdated()
        }

    var notificationsComments: Boolean
        get() = accountSettings.notificationsComments
        set(b) {
            accountSettings.notificationsComments = b
            onSettingsUpdated()
        }

    var notificationsCommentsAnswers: Boolean
        get() = accountSettings.notificationsCommentsAnswers
        set(b) {
            accountSettings.notificationsCommentsAnswers = b
            onSettingsUpdated()
        }

    var notificationsKarma: Boolean
        get() = accountSettings.notificationsKarma
        set(b) {
            accountSettings.notificationsKarma = b
            onSettingsUpdated()
        }

    var notificationsOther: Boolean
        get() = accountSettings.notificationsOther
        set(b) {
            accountSettings.notificationsOther = b
            onSettingsUpdated()
        }

    var notificationsFollows: Boolean
        get() = accountSettings.notificationsFollows
        set(b) {
            accountSettings.notificationsFollows = b
            onSettingsUpdated()
        }

    var notificationsImportant: Boolean
        get() = accountSettings.notificationsImportant
        set(b) {
            accountSettings.notificationsImportant = b
            onSettingsUpdated()
        }

    var notificationsFollowsPosts: Boolean
        get() = accountSettings.notificationsFollowsPosts
        set(b) {
            accountSettings.notificationsFollowsPosts = b
            onSettingsUpdated()
        }

    var notificationsAchievements: Boolean
        get() = accountSettings.notificationsAchievements
        set(b) {
            accountSettings.notificationsAchievements = b
            onSettingsUpdated()
        }

    var notificationsChatMessages: Boolean
        get() = accountSettings.notificationsChatMessages
        set(b) {
            accountSettings.notificationsChatMessages = b
            onSettingsUpdated()
        }

    var notificationsChatAnswers: Boolean
        get() = accountSettings.notificationsChatAnswers
        set(b) {
            accountSettings.notificationsChatAnswers = b
            onSettingsUpdated()
        }

    var notificationsPM: Boolean
        get() = accountSettings.notificationsPM
        set(b) {
            accountSettings.notificationsPM = b
            onSettingsUpdated()
        }

    var notificationsSalientOnTimeStartH: Int
        get() = accountSettings.notificationsSalientOnTimeStartH
        set(b) {
            accountSettings.notificationsSalientOnTimeStartH = b
            onSettingsUpdated()
        }

    var notificationsSalientOnTimeStartM: Int
        get() = accountSettings.notificationsSalientOnTimeStartM
        set(b) {
            accountSettings.notificationsSalientOnTimeStartM = b
            onSettingsUpdated()
        }

    var notificationsSalientOnTimeEndH: Int
        get() = accountSettings.notificationsSalientOnTimeEndH
        set(b) {
            accountSettings.notificationsSalientOnTimeEndH = b
            onSettingsUpdated()
        }

    var notificationsSalientOnTimeEndM: Int
        get() = accountSettings.notificationsSalientOnTimeEndM
        set(b) {
            accountSettings.notificationsSalientOnTimeEndM = b
            onSettingsUpdated()
        }

    var notificationsSalientAll: Boolean
        get() = accountSettings.notificationsSalientAll
        set(b) {
            accountSettings.notificationsSalientAll = b
            onSettingsUpdated()
        }
    var notificationsSalientOnTimeEnabled: Boolean
        get() = accountSettings.notificationsSalientOnTimeEnabled
        set(b) {
            accountSettings.notificationsSalientOnTimeEnabled = b
            onSettingsUpdated()
        }

    var userActivitiesAutoSubscribe: Boolean
        get() = accountSettings.userActivitiesAutoSubscribe
        set(b) {
            accountSettings.userActivitiesAutoSubscribe = b
            onSettingsUpdated()
        }

    var userActivitiesAllowed_all: Boolean
        get() = accountSettings.userActivitiesAllowed_all
        set(b) {
            accountSettings.userActivitiesAllowed_all = b
            onSettingsUpdated()
        }

    var userActivitiesAllowed_followedFandoms: Boolean
        get() = accountSettings.userActivitiesAllowed_followedFandoms
        set(b) {
            accountSettings.userActivitiesAllowed_followedFandoms = b
            onSettingsUpdated()
        }

    var userActivitiesAllowed_followedUsers: Boolean
        get() = accountSettings.userActivitiesAllowed_followedUsers
        set(b) {
            accountSettings.userActivitiesAllowed_followedUsers = b
            onSettingsUpdated()
        }

    //
    //  Feed
    //

    var feedLanguages: Array<Long>
        get() = if (accountSettings.feedLanguages.isEmpty()) arrayOf(ControllerApi.getLanguageId()) else accountSettings.feedLanguages
        set(b) {
            accountSettings.feedLanguages = b
            onSettingsUpdated()
        }

    var adminReportsLanguages: Array<Long>
        get() = if (accountSettings.adminReportsLanguages.isEmpty()) arrayOf(ControllerApi.getLanguageId()) else accountSettings.adminReportsLanguages
        set(b) {
            accountSettings.adminReportsLanguages = b
            onSettingsUpdated()
        }

    var feedCategories: Array<Long>
        get() = if (accountSettings.feedCategories.isEmpty()) Array(CampfireConstants.CATEGORIES.size) { CampfireConstants.CATEGORIES[it].index } else accountSettings.feedCategories
        set(b) {
            accountSettings.feedCategories = b
            onSettingsUpdated()
        }

    var feedImportant: Boolean
        get() = accountSettings.feedImportant
        set(b) {
            accountSettings.feedImportant = b
            onSettingsUpdated()
        }

    var feedOrder: Array<Long>
        get() = if (accountSettings.feedOrder.isEmpty()) arrayOf(1L, 2L) else accountSettings.feedOrder
        set(b) {
            accountSettings.feedOrder = b
            onSettingsUpdated()
        }

    //
    //  Account
    //

    var profileFilterEvents: Boolean
        get() = accountSettings.profileFilterEvents
        set(b) {
            accountSettings.profileFilterEvents = b
            onSettingsUpdated()
        }
    var profileFilterPosts: Boolean
        get() = accountSettings.profileFilterPosts
        set(b) {
            accountSettings.profileFilterPosts = b
            onSettingsUpdated()
        }
    var profileFilterComments: Boolean
        get() = accountSettings.profileFilterComments
        set(b) {
            accountSettings.profileFilterComments = b
            onSettingsUpdated()
        }
    var profileFilterModerations: Boolean
        get() = accountSettings.profileFilterModerations
        set(b) {
            accountSettings.profileFilterModerations = b
            onSettingsUpdated()
        }
    var profileFilterChatMessages: Boolean
        get() = accountSettings.profileFilterChatMessages
        set(b) {
            accountSettings.profileFilterChatMessages = b
            onSettingsUpdated()
        }

    var profileFilterStickers: Boolean
        get() = accountSettings.profileFilterStickers
        set(b) {
            accountSettings.profileFilterStickers = b
            onSettingsUpdated()
        }

    var lvlDialogLvl: Long
        get() = accountSettings.lvlDialogLvl
        set(b) {
            accountSettings.lvlDialogLvl = b
            onSettingsUpdated()
        }


    fun getProfileFilters(): Array<Long> {
        val list = ArrayList<Long>()

        if (profileFilterComments) list.add(API.PUBLICATION_TYPE_COMMENT)
        if (profileFilterPosts) list.add(API.PUBLICATION_TYPE_POST)
        if (profileFilterModerations) list.add(API.PUBLICATION_TYPE_MODERATION)
        if (profileFilterEvents) {
            list.add(API.PUBLICATION_TYPE_EVENT_USER)
            list.add(API.PUBLICATION_TYPE_EVENT_MODER)
            list.add(API.PUBLICATION_TYPE_EVENT_ADMIN)
        }
        if (profileFilterChatMessages) list.add(API.PUBLICATION_TYPE_CHAT_MESSAGE)
        if (profileFilterStickers) {
            list.add(API.PUBLICATION_TYPE_STICKER)
            list.add(API.PUBLICATION_TYPE_STICKERS_PACK)
        }

        return list.toTypedArray()
    }

    //
    //  Notifications
    //

    var filterFollowsPublications: Boolean
        get() = accountSettings.notificationsFilterFollowsPublications
        set(b) {
            accountSettings.notificationsFilterFollowsPublications = b
            onSettingsUpdated()
        }
    var filterFollows: Boolean
        get() = accountSettings.notificationsFilterFollows
        set(b) {
            accountSettings.notificationsFilterFollows = b
            onSettingsUpdated()
        }
    var filterAchievements: Boolean
        get() = accountSettings.notificationsFilterAchievements
        set(b) {
            accountSettings.notificationsFilterAchievements = b
            onSettingsUpdated()
        }
    var filterComments: Boolean
        get() = accountSettings.notificationsFilterComments
        set(b) {
            accountSettings.notificationsFilterComments = b
            onSettingsUpdated()
        }
    var filterKarma: Boolean
        get() = accountSettings.notificationsFilterKarma
        set(b) {
            accountSettings.notificationsFilterKarma = b
            onSettingsUpdated()
        }
    var filterAnswers: Boolean
        get() = accountSettings.notificationsFilterAnswers
        set(b) {
            accountSettings.notificationsFilterAnswers = b
            onSettingsUpdated()
        }
    var filterImportant: Boolean
        get() = accountSettings.notificationsFilterImportant
        set(b) {
            accountSettings.notificationsFilterImportant = b
            onSettingsUpdated()
        }
    var filterOther: Boolean
        get() = accountSettings.notificationsFilterOther
        set(b) {
            accountSettings.notificationsFilterOther = b
            onSettingsUpdated()
        }

    fun notificationsFilterEnabled(type: Long): Boolean {
        return when (type) {
            API.NOTIF_FOLLOWS_PUBLICATION -> filterFollowsPublications
            API.NOTIF_ACCOUNT_FOLLOWS_ADD -> filterFollows
            API.NOTIF_ACHI -> filterAchievements
            API.NOTIF_COMMENT -> filterComments
            API.NOTIF_COMMENT_ANSWER -> filterAnswers
            API.NOTIF_KARMA_ADD -> filterKarma
            API.NOTIF_PUBLICATION_IMPORTANT -> filterImportant
            else -> filterOther
        }
    }

    //
    //  Fandom
    //

    var fandomFilterModerationsPosts: Boolean
        get() = accountSettings.fandomFilterModerationsPosts
        set(b) {
            accountSettings.fandomFilterModerationsPosts = b
            onSettingsUpdated()
        }

    var fandomFilterOnlyImportant: Boolean
        get() = accountSettings.fandomFilterOnlyImportant
        set(b) {
            accountSettings.fandomFilterOnlyImportant = b
            onSettingsUpdated()
        }

    var fandomFilterAdministrations: Boolean
        get() = accountSettings.fandomFilterAdministrations
        set(b) {
            accountSettings.fandomFilterAdministrations = b
            onSettingsUpdated()
        }

    var fandomFilterModerations: Boolean
        get() = accountSettings.fandomFilterModerations
        set(b) {
            accountSettings.fandomFilterModerations = b
            onSettingsUpdated()
        }

    var fandomFilterModerationsBlocks: Boolean
        get() = accountSettings.fandomFilterModerationsBlocks
        set(b) {
            accountSettings.fandomFilterModerationsBlocks = b
            onSettingsUpdated()
        }

    var fandomNSFW: Array<Long>
        get() = accountSettings.fandomNSFW
        set(b) {
            accountSettings.fandomNSFW = b
            onSettingsUpdated()
        }

    fun getFandomFilters(): Array<Long> {
        val list = ArrayList<Long>()

        if (fandomFilterModerationsPosts || fandomFilterOnlyImportant) list.add(API.PUBLICATION_TYPE_POST)
        if (fandomFilterAdministrations) list.add(API.PUBLICATION_TYPE_EVENT_FANDOM)
        if (fandomFilterModerations || fandomFilterModerationsBlocks) list.add(API.PUBLICATION_TYPE_MODERATION)

        return list.toTypedArray()
    }

    //
    //  Bookmarks
    //

    var bookmarksFolders: Array<BookmarksFolder>
        get() = accountSettings.bookmarksFolders
        set(b) {
            accountSettings.bookmarksFolders = b
            onSettingsUpdated()
        }


    var allowAddingToConferences: Boolean
        get() = accountSettings.allowAddingToConferences
        set(b) {
            accountSettings.allowAddingToConferences = b
            onSettingsUpdated()
        }
}