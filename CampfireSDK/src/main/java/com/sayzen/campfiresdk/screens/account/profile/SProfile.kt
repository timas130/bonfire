package com.sayzen.campfiresdk.screens.account.profile

import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.ApiResources
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.models.publications.Publication
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.requests.accounts.*
import com.dzen.campfire.api.requests.publications.RPublicationsGetAll
import com.posthog.PostHog
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.compose.profile.badges.shelf.BadgeShelfCard
import com.sayzen.campfiresdk.compose.publication.post.CardPostProxy
import com.sayzen.campfiresdk.controllers.*
import com.sayzen.campfiresdk.models.PostList
import com.sayzen.campfiresdk.models.cards.CardPublication
import com.sayzen.campfiresdk.models.events.account.EventAccountChanged
import com.sayzen.campfiresdk.models.events.account.EventAccountStatusChanged
import com.sayzen.campfiresdk.models.events.publications.EventPostPinedProfile
import com.sayzen.campfiresdk.models.splashs.SplashAdminAccountRemove
import com.sayzen.campfiresdk.models.splashs.SplashAdminBlock
import com.sayzen.campfiresdk.screens.administation.SAdministrationDeepBlocked
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sayzen.campfiresdk.support.adapters.XAccount
import com.sayzen.campfiresdk.support.clear
import com.sayzen.campfiresdk.support.load
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.*
import com.sup.dev.android.views.screens.SAlert
import com.sup.dev.android.views.screens.SCrop
import com.sup.dev.android.views.screens.SImageView
import com.sup.dev.android.views.splash.*
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsBytes
import com.sup.dev.java.tools.ToolsText
import com.sup.dev.java.tools.ToolsThreads

class SProfile private constructor(
    account: Account,
) : Screen(R.layout.screen_profile), PostList {
    companion object {
        fun instance(account: Account, action: NavigationAction) {
            if (account.id == 0L) {
                Navigator.to(instaneAnonimys())
            } else {
                if (account.karma30 > 0) {
                    Navigator.action(action, SProfile(account))
                } else {
                    instance(account.id, Navigator.TO)
                }
            }
        }

        fun instance(name: String, action: NavigationAction) {
            ApiRequestsSupporter.executeInterstitial(action, RAccountsGet(0, name),
                    { r ->
                        SProfile(r.account)
                    },
                    goneText = t(API_TRANSLATE.profile_error_gone))
        }

        fun instance(accountId: Long, action: NavigationAction) {
            if (accountId == 0L) {

                Navigator.to(instaneAnonimys())
            } else ApiRequestsSupporter.executeInterstitial(action, RAccountsGet(accountId, "")) { r ->
                SProfile(r.account)
            }
        }

        fun instaneAnonimys(): Screen {
            val screen = SAlert(
                    null,
                    t(API_TRANSLATE.profile_anonymous_text),
                    t(API_TRANSLATE.app_back), null)
            screen.setImage(ImageLoader.load(ApiResources.IMAGE_BACKGROUND_23).noHolder().noFade())
            screen.onAction = { Navigator.remove(screen) }
            screen.isNavigationAllowed = false
            screen.isNavigationAnimation = false
            return screen
        }
    }

    private val eventBus = EventBus
            .subscribe(EventPostPinedProfile::class) { if (it.accountId == xAccount.getId()) setPinnedPost(it.post) }

    val xAccount = XAccount().setAccount(account)
            .setOnChanged { update() }

    private val vToolbarCollapsingShadow: View = findViewById(R.id.vToolbarCollapsingShadow)
    private val vTitle: TextView = findViewById(R.id.vToolbarTitle)
    private val vRecycler: RecyclerView = findViewById(R.id.vRecycler)
    private val vMore: View = findViewById(R.id.vMore)
    private val vImage: ImageView = findViewById(R.id.vImage)
    private val vImageHoliday: ImageView = findViewById(R.id.vImageHoliday)
    private val adapter: RecyclerCardAdapterLoading<CardPublication, Publication>
    private val cardTitle = if (ControllerSettings.isProfileListStyle) CardTitleNew(xAccount) else CardTitleOld(xAccount)
    val cardStatus = CardStatus(xAccount)
    private val cardButtonsMain = CardButtonsMain(xAccount)
    private val cardButtons = if (ControllerSettings.isProfileListStyle) CardButtonsInfoNew(xAccount) else CardButtonsInfoOld(xAccount)
    private val cardBadges = BadgeShelfCard(account.id.toString())
    private val cardBio = CardBio(xAccount)
    private var cardPinnedPost: CardPostProxy? = null
    private val cardFilters = CardFilters {
        if (cardPinnedPost != null) setPinnedPost(cardPinnedPost!!.xPublication.publication as PublicationPost)
        getAdapter().reloadBottom()
    }

    init {

        if (xAccount.isCurrentAccount()) ControllerStoryQuest.incrQuest(API.QUEST_STORY_PROFILE)

        vToolbarCollapsingShadow.background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(0x60000000, 0x00000000))

        vMore.setOnClickListener { showDialog() }
        vRecycler.layoutManager = LinearLayoutManager(context)
        (vRecycler.layoutParams as MarginLayoutParams).topMargin = -1 * ToolsView.dpToPx(if (ControllerSettings.isProfileListStyle) 24 else 72).toInt()

        vImage.setOnClickListener {
            if (ControllerApi.isCurrentAccount(xAccount.getId()) && ControllerApi.can(API.LVL_CAN_CHANGE_PROFILE_IMAGE)) onChangeTitleImageClicked()
            else if (xAccount.getTitleImageGif().isNotEmpty()) Navigator.to(SImageView(ImageLoader.load(xAccount.getTitleImageGif())))
            else Navigator.to(SImageView(ImageLoader.load(xAccount.getTitleImage())))
        }
        vImage.setOnLongClickListener {
            if (xAccount.getTitleImageGif().isNotEmpty()) Navigator.to(SImageView(ImageLoader.load(xAccount.getTitleImageGif())))
            else Navigator.to(SImageView(ImageLoader.load(xAccount.getTitleImage())))
            true
        }

        if (ControllerSettings.styleNewYearProfileAnimation) {
            val holidayImage = ControllerHoliday.getProfileBackgroundImage()
            if (holidayImage != null) {
                ImageLoader.load(holidayImage).into(vImageHoliday)
            }
        }


        vTitle.text = xAccount.getName()

        load()

        adapter = RecyclerCardAdapterLoading<CardPublication, Publication>(CardPublication::class) { publication -> CardPublication.instance(publication, vRecycler, true, isShowFullInfo = true) }
                .setBottomLoader { onLoad, cards ->
                    val req = RPublicationsGetAll()
                            .setAccountId(xAccount.getId())
                            .setOffset(cards.size)
                    if (cardFilters.fandomId > 0) {
                        req.setPublicationTypes(API.PUBLICATION_TYPE_POST)
                        req.setFandomId(cardFilters.fandomId)
                    } else {
                        req.setPublicationTypes(ControllerSettings.getProfileFilters())
                    }
                    req.tokenRequired = true
                    req.onComplete { resp ->
                        onLoad.invoke(resp.publications)
                        afterPackLoaded()
                    }
                    req.onNetworkError { onLoad.invoke(null) }
                    req.send(api)
                }
                .setRetryMessage(t(API_TRANSLATE.error_network), t(API_TRANSLATE.app_retry))
                .setNotifyCount(5)
                .setEmptyMessage(if (xAccount.getId() == ControllerApi.account.getId()) t(API_TRANSLATE.profile_empty_my) else t(API_TRANSLATE.profile_empty_other))

        vRecycler.adapter = adapter

        adapter.add(cardTitle)
        adapter.add(cardStatus)
        if (ControllerSettings.isProfileListStyle) {
            if (!ControllerApi.isCurrentAccount(xAccount.getId())) adapter.add(cardButtonsMain)
            adapter.add(cardButtons)
        } else {
            adapter.add(cardButtons)
            if (!ControllerApi.isCurrentAccount(xAccount.getId())) adapter.add(cardButtonsMain)
        }
        if (PostHog.isFeatureEnabled("badges_profile")) {
            adapter.add(cardBadges)
        }
        adapter.add(cardBio)
        adapter.add(cardFilters)
        adapter.loadBottom()

        ImageLoader.load(xAccount.getImage()).noLoadFromCash().intoBitmap {
            EventBus.post(EventAccountChanged(xAccount.getId(), xAccount.getName(), xAccount.getImage()))
        }

        update()
    }

    private fun getAdapter() = adapter

    override fun onResume() {
        super.onResume()
        if (xAccount.getId() == 0L) {
            Navigator.replace(instaneAnonimys())
        }
    }

    private fun load() {
        ApiRequestsSupporter.executeWithRetry(RAccountsGetProfile(xAccount.getId(), "")) { r ->
            setPinnedPost(r.pinnedPost)
            xAccount.setTitleImage(r.titleImage)
            xAccount.setTitleImageGif(r.titleImageGif)
            xAccount.setDateAccountCreated(r.dateCreate)
            updateTitleImage()

            cardTitle.updateDateCreate()
            cardStatus.setInfo(r.status, r.note, r.banDate)
            cardButtonsMain.setIsFollow(r.isFollow)
            cardButtonsMain.setFollowsYou(r.followsYou)

            cardButtons.setInfo(r)

            cardBio.setInfo(r.age, r.description, r.links)
        }
    }

    private fun afterPackLoaded() {
        if (cardPinnedPost != null && ControllerSettings.getProfileFilters().contains(API.PUBLICATION_TYPE_POST))
            for (c in adapter.get(CardPostProxy::class))
                if (c.xPublication.publication.id == cardPinnedPost!!.xPublication.publication.id && !(c.xPublication.publication as PublicationPost).isPined)
                    adapter.remove(c)
    }

    private fun setPinnedPost(post: PublicationPost?) {
        if (cardPinnedPost != null) adapter.remove(cardPinnedPost!!)
        if (post == null) {
            cardPinnedPost = null
        } else {
            for (c in adapter.get(CardPostProxy::class)) {
                if (c.xPublication.publication.id == post.id) {
                    adapter.remove(c)
                }
            }
            post.isPined = true
            cardPinnedPost = CardPostProxy(vRecycler, post)
            cardPinnedPost?.showFandom = true
            if (ControllerSettings.getProfileFilters().contains(API.PUBLICATION_TYPE_POST)) {
                adapter.add(adapter.indexOf(cardFilters) + 1, cardPinnedPost!!)
            }
        }
    }

    override fun contains(card: CardPostProxy) = adapter.contains(card)

    private fun updateTitleImage() {
        xAccount.setViewBig(vImage)
    }

    private fun update() {
        xAccount.setView(vTitle)
        cardTitle.update()
        cardButtons.update()
        updateTitleImage()

        adapter.remove(CardEffect::class)
        for (i in xAccount.getAccount().accountEffects) {
            if (i.dateEnd > System.currentTimeMillis()) {
                adapter.add(adapter.indexOf(cardBio), CardEffect(i))
            }
        }
    }

    private fun showDialog() {
        val w = SplashMenu()
                .add(t(API_TRANSLATE.app_copy_link)) { ToolsAndroid.setToClipboard(ControllerLinks.linkToAccount(xAccount.getName()));ToolsToast.show(t(API_TRANSLATE.app_copied)) }
                .add(t(API_TRANSLATE.app_report)) { onReportClicked() }.condition(!ControllerApi.isCurrentAccount(xAccount.getId()))
                .add(t(API_TRANSLATE.app_note)) { SplashNote(this) }.condition(cardStatus.getNote() != null)
                .add(t(API_TRANSLATE.settings_black_list)) { blackList() }.condition(!ControllerApi.isCurrentAccount(xAccount.getId()))
                .groupCondition(ControllerApi.account.getId() == xAccount.getId())
                .add(t(API_TRANSLATE.profile_change_avatar)) { cardTitle.onChangeAvatarClicked() }
                .add(t(API_TRANSLATE.profile_change_name)) { ControllerCampfireSDK.changeLogin() }.condition(ControllerApi.account.getName().contains("#"))
                .add(t(API_TRANSLATE.profile_change_title_image)) { onChangeTitleImageClicked() }.condition(ControllerApi.can(API.LVL_CAN_CHANGE_PROFILE_IMAGE))
                .spoiler(t(API_TRANSLATE.app_admin))
                .reverseGroupCondition()
                .add(t(API_TRANSLATE.profile_remove_avatar)) { onAdminRemoveAvatarClicked() }.backgroundRes(R.color.red_700).textColorRes(R.color.white).condition(ControllerApi.can(API.LVL_ADMIN_USER_REMOVE_IMAGE))
                .add(t(API_TRANSLATE.profile_remove_name)) { onAdminRemoveNameClicked() }.backgroundRes(R.color.red_700).textColorRes(R.color.white).condition(ControllerApi.can(API.LVL_ADMIN_USER_REMOVE_NAME))
                .add(t(API_TRANSLATE.app_clear_reports)) { ControllerApi.clearUserReports(xAccount.getId()) }.backgroundRes(R.color.red_700).textColorRes(R.color.white).condition(ControllerApi.can(API.LVL_ADMIN_BAN))
                .add(t(API_TRANSLATE.app_punish)) { onAdminPunishClicked() }.backgroundRes(R.color.red_700).textColorRes(R.color.white).condition(ControllerApi.can(API.LVL_ADMIN_BAN))
                .add(t(API_TRANSLATE.admin_change_name)) { adminChangeName() }.backgroundRes(R.color.red_700).textColorRes(R.color.white).condition(ControllerApi.can(API.LVL_ADMIN_USER_CHANGE_NAME))
                .add(t(API_TRANSLATE.profile_remove_title_image)) { onAdminRemoveTitleImageClicked() }.backgroundRes(R.color.red_700).textColorRes(R.color.white).condition(ControllerApi.can(API.LVL_ADMIN_USER_REMOVE_IMAGE))
                .add(t(API_TRANSLATE.profile_remove_status)) { removeStatus() }.backgroundRes(R.color.red_700).textColorRes(R.color.white).condition(ControllerApi.can(API.LVL_ADMIN_USER_REMOVE_STATUS))
                .add(t(API_TRANSLATE.profile_recount_level)) { protoadminAchievementsRecount() }.backgroundRes(R.color.red_700).textColorRes(R.color.white).condition(ControllerApi.can(API.LVL_ADMIN_DEBUG_RECOUNT_LEVEL_AND_KARMA))
                .add(t(API_TRANSLATE.profile_recount_karma)) { protoadminKarmaRecount() }.backgroundRes(R.color.red_700).textColorRes(R.color.white).condition(ControllerApi.can(API.LVL_ADMIN_DEBUG_RECOUNT_LEVEL_AND_KARMA))
                .add(t(API_TRANSLATE.profile_add_effect)) { ControllerEffects.addEffect(xAccount.getId()) }.backgroundRes(R.color.red_700).textColorRes(R.color.white).condition(ControllerApi.can(API.LVL_ADMIN_FANDOM_EFFECTS))
                .spoiler(t(API_TRANSLATE.app_protoadmin))
                .groupCondition(ControllerApi.can(API.LVL_PROTOADMIN))
                .add(t(API_TRANSLATE.protoadin_profile_blocked)) { Navigator.to(SAdministrationDeepBlocked(xAccount.getId())) }.backgroundRes(R.color.orange_700).textColorRes(R.color.white)
                .add(t(API_TRANSLATE.protoadin_profile_autch)) { protoadminAutorization() }.backgroundRes(R.color.orange_700).textColorRes(R.color.white)
                .add(t(API_TRANSLATE.app_remove)) { onProtoadminRemoveClicked() }.backgroundRes(R.color.orange_700).textColorRes(R.color.white).condition(!ControllerApi.isCurrentAccount(xAccount.getId()))

        w.asPopupShow(vMore)
    }

    private fun blackList() {
        ApiRequestsSupporter.executeProgressDialog(RAccountsBlackListCheck(xAccount.getId())) { r ->
            if (r.isInBlackList) ControllerCampfireSDK.removeFromBlackListUser(xAccount.getId()) else ControllerCampfireSDK.addToBlackListUser(xAccount.getId())
        }
    }

    fun onReportClicked() {
        SplashField()
                .setTitle(t(API_TRANSLATE.profile_report_confirm))
                .setHint(t(API_TRANSLATE.app_report_hint))
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .setMin(0)
                .setMax(API.REPORT_COMMENT_L)
                .setOnEnter(t(API_TRANSLATE.app_report)) { _, comment ->
                    ApiRequestsSupporter.executeProgressDialog(RAccountsReport(xAccount.getId(), comment)) { _ ->
                        ToolsToast.show(t(API_TRANSLATE.profile_report_reported))
                    }
                            .onApiError(RAccountsReport.E_EXIST) { ToolsToast.show(t(API_TRANSLATE.profile_report_already_exist)) }
                }
                .asSheetShow()
    }

    private fun onChangeTitleImageClicked() {
        SplashChooseImage()
                .setOnSelected { _, bytes, _ ->

                    ToolsThreads.thread {

                        val bitmap = ToolsBitmap.decode(bytes)
                        if (bitmap == null) {
                            ToolsToast.show(t(API_TRANSLATE.error_cant_load_image))
                            return@thread
                        }

                        ToolsThreads.main {


                            val isGif = ControllerApi.can(API.LVL_CAN_CHANGE_AVATAR_GIF) && ToolsBytes.isGif(bytes)
                            val cropSizeW = if (isGif) API.ACCOUNT_TITLE_IMG_GIF_W else API.ACCOUNT_TITLE_IMG_W
                            val cropSizeH = if (isGif) API.ACCOUNT_TITLE_IMG_GIF_H else API.ACCOUNT_TITLE_IMG_H

                            Navigator.to(SCrop(bitmap, cropSizeW, cropSizeH) { _, b2, x, y, w, h ->
                                if (isGif) {

                                    val d = ToolsView.showProgressDialog()
                                    ToolsThreads.thread {
                                        val bytesSized = ToolsGif.resize(bytes, API.ACCOUNT_TITLE_IMG_GIF_W, API.ACCOUNT_TITLE_IMG_GIF_H, x, y, w, h, true)

                                        ToolsThreads.main {
                                            if (bytesSized.size > API.ACCOUNT_TITLE_IMG_GIF_WEIGHT) {
                                                d.hide()
                                                ToolsToast.show(t(API_TRANSLATE.error_too_long_file))
                                            } else {
                                                ControllerApi.toBytes(b2, API.ACCOUNT_TITLE_IMG_WEIGHT, API.ACCOUNT_TITLE_IMG_GIF_W, API.ACCOUNT_TITLE_IMG_GIF_H, true) {
                                                    if (it == null) d.hide()
                                                    else changeTitleImageNow(d, it, bytesSized)
                                                }
                                            }
                                        }
                                    }

                                } else {
                                    val d = ToolsView.showProgressDialog()
                                    ControllerApi.toBytes(b2, API.ACCOUNT_TITLE_IMG_WEIGHT, API.ACCOUNT_TITLE_IMG_W, API.ACCOUNT_TITLE_IMG_H, true) {
                                        if (it == null) d.hide()
                                        else changeTitleImageNow(d, it, null)
                                    }
                                }
                            })
                        }
                    }
                }
                .asSheetShow()
    }

    private fun changeTitleImageNow(d: SplashProgressTransparent, bytes: ByteArray, bytesGif: ByteArray?) {
        ApiRequestsSupporter.executeProgressDialog(d, RAccountsChangeTitleImage(bytes, bytesGif)) { r ->
            ImageLoader.clear(xAccount.getTitleImage())
            ImageLoader.clear(xAccount.getTitleImageGif())
            EventBus.post(EventAccountChanged(xAccount.getId(), xAccount.getName(), xAccount.getImage(), r.image, r.imageGif))
        }
    }

    fun onAdminRemoveAvatarClicked() {
        SplashField()
                .setHint(t(API_TRANSLATE.profile_remove_avatar))
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(t(API_TRANSLATE.app_remove)) { w, comment ->
                    ApiRequestsSupporter.executeEnabled(w, RAccountsRemoveAvatar(xAccount.getId(), comment)) {
                        ToolsToast.show(t(API_TRANSLATE.app_done))
                        ImageLoader.clear(xAccount.getImage())
                        EventBus.post(EventAccountChanged(xAccount.getId(), xAccount.getName(), xAccount.getImage()))
                    }
                }
                .asSheetShow()
    }

    fun onAdminRemoveNameClicked() {
        SplashField()
                .setHint(t(API_TRANSLATE.profile_remove_name))
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(t(API_TRANSLATE.app_remove)) { w, comment ->
                    ApiRequestsSupporter.executeEnabled(w, RAccountsRemoveName(xAccount.getId(), comment)) {
                        ToolsToast.show(t(API_TRANSLATE.app_done))
                        EventBus.post(EventAccountChanged(xAccount.getId(), "User#" + xAccount.getId(), xAccount.getImage()))
                    }
                }
                .asSheetShow()

    }

    fun adminChangeName() {
        SplashFieldTwo()
                .setTitle(t(API_TRANSLATE.profile_change_name))
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .setText_1(xAccount.getName())
                .setMin_2(API.MODERATION_COMMENT_MIN_L)
                .setMax_2(API.MODERATION_COMMENT_MAX_L)
                .setHint_1(t(API_TRANSLATE.app_name_s))
                .setLinesCount_1(1)
                .addChecker_1(t(API_TRANSLATE.profile_change_name_error)) { s -> ToolsText.isValidUsername(s) }
                .setHint_2(t(API_TRANSLATE.comments_hint))
                .setOnEnter(t(API_TRANSLATE.app_change)) { dialog, name, comment ->
                    ApiRequestsSupporter.executeEnabled(dialog, RAccountsAdminChangeName(xAccount.getId(), name, comment)) {
                        ToolsToast.show(t(API_TRANSLATE.app_done))
                        EventBus.post(EventAccountChanged(xAccount.getId(), name))
                    }.onApiError(RAccountsAdminChangeName.E_LOGIN_NOT_ENABLED) {
                        ToolsToast.show(t(API_TRANSLATE.error_login_taken))
                    }
                }
                .asSheetShow()
    }

    fun onAdminPunishClicked() {
        SplashAdminBlock.show(xAccount.getId(), xAccount.getName())
    }

    fun onAdminRemoveTitleImageClicked() {
        SplashField()
                .setTitle(t(API_TRANSLATE.profile_remove_title_image))
                .setHint(t(API_TRANSLATE.comments_hint))
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(t(API_TRANSLATE.app_remove)) { w, comment ->
                    ApiRequestsSupporter.executeEnabled(w, RAccountsRemoveTitleImage(xAccount.getId(), comment)) {
                        ToolsToast.show(t(API_TRANSLATE.app_done))
                        EventBus.post(EventAccountChanged(xAccount.getId(), xAccount.getName(), xAccount.getImage()))
                    }
                }
                .asSheetShow()


    }

    fun onProtoadminRemoveClicked() {
        SplashAdminAccountRemove(xAccount.getId(), xAccount.getName()).asSheetShow()
    }

    private fun removeStatus() {
        SplashField()
                .setTitle(t(API_TRANSLATE.profile_remove_status))
                .setHint(t(API_TRANSLATE.comments_hint))
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(t(API_TRANSLATE.app_remove)) { w, comment ->
                    ApiRequestsSupporter.executeEnabled(w, RAccountsAdminStatusRemove(xAccount.getId(), comment)) {
                        ToolsToast.show(t(API_TRANSLATE.app_done))
                        EventBus.post(EventAccountStatusChanged(xAccount.getId(), ""))
                    }
                }
                .asSheetShow()
    }

    private fun protoadminAutorization() {
        ApiRequestsSupporter.executeEnabledConfirm(t(API_TRANSLATE.protoadin_profile_autch_title), t(API_TRANSLATE.protoadin_profile_autch_action), RAccountsProtoadminAutorization(xAccount.getId())) {
            ToolsToast.show(t(API_TRANSLATE.app_done))
            ControllerCampfireSDK.logoutNow(announceLogout = false)
        }
    }

    private fun protoadminAchievementsRecount() {
        ControllerApi.moderation(t(API_TRANSLATE.profile_recount_level_title), t(API_TRANSLATE.profile_recount_level_action), { RAccountsAchievementsRecount(xAccount.getId(), it) }) {
            ToolsToast.show(t(API_TRANSLATE.app_done))
        }
    }

    private fun protoadminKarmaRecount() {
        ControllerApi.moderation(t(API_TRANSLATE.profile_recount_karma_title), t(API_TRANSLATE.profile_recount_karma_action), { RAccountsKarmaRecount(xAccount.getId(), it) }) {
            ToolsToast.show(t(API_TRANSLATE.app_done))
        }
    }
}
