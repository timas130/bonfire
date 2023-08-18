package com.sayzen.campfiresdk.screens.post.create

import android.graphics.Bitmap
import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.activities.UserActivity
import com.dzen.campfire.api.models.fandoms.Rubric
import com.dzen.campfire.api.models.publications.post.*
import com.dzen.campfire.api.requests.fandoms.RFandomsGet
import com.dzen.campfire.api.requests.post.*
import com.sayzen.campfiresdk.models.AttacheAgent
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.support.adapters.XFandom
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sayzen.campfiresdk.controllers.ControllerStoryQuest
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.publications.EventPostChanged
import com.sayzen.campfiresdk.models.events.publications.EventPostDraftCreated
import com.sayzen.campfiresdk.models.events.publications.EventPostStatusChange
import com.sayzen.campfiresdk.models.events.publications.EventPublicationRemove
import com.sayzen.campfiresdk.screens.post.view.SPost
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.splash.Splash
import com.sup.dev.android.views.splash.SplashAlert
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsThreads

class SPostCreate constructor(
        val fandomId: Long,
        val languageId: Long,
        val fandomName: String,
        val fandomImageId: Long,
        changePost: PublicationPost?,
        private val postParams: PostParams,
        showMenu: Boolean
) : Screen(R.layout.screen_post_create), AttacheAgent {

    companion object {

        fun instance(publicationId: Long, action: NavigationAction, onOpen: (SPostCreate) -> Unit = {}) {
            ApiRequestsSupporter.executeInterstitial(action, RPostGetDraft(publicationId)) { r ->
                val screen = SPostCreate(r.publication.fandom.id, r.publication.fandom.languageId, r.publication.fandom.name, r.publication.fandom.imageId, r.publication, PostParams().setTags(Array(r.tags.size) { r.tags[it].id }), true)
                onOpen.invoke(screen)
                screen
            }
        }

        fun instance(fandomId: Long, languageId: Long, fandomName: String, fandomImageId: Long, postParams: PostParams, action: NavigationAction) {
            Navigator.action(action, SPostCreate(fandomId, languageId, fandomName, fandomImageId, null, postParams, true))
        }

        fun instance(fandomId: Long, languageId: Long, postParams: PostParams, onOpen: (SPostCreate) -> Unit = {}, action: NavigationAction) {
            ApiRequestsSupporter.executeInterstitial(action, RFandomsGet(fandomId, languageId, ControllerApi.getLanguageId())) { r ->
                val screen = SPostCreate(fandomId, languageId, r.fandom.name, r.fandom.imageId, null, postParams, true)
                onOpen.invoke(screen)
                screen
            }
        }


    }

    constructor(fandomId: Long, languageId: Long, fandomName: String, fandomImageId: Long) : this(fandomId, languageId, fandomName, fandomImageId, null, PostParams(), true)

    private val vRecycler: RecyclerView = findViewById(R.id.vRecycler)
    private val vAdd: FloatingActionButton = findViewById(R.id.vAdd)
    private val vFinish: FloatingActionButton = findViewById(R.id.vFinish)
    private val vAvatarTitle: ViewAvatarTitle = findViewById(R.id.vAvatarTitle)
    private val xPostCreator = PostCreator(changePost?.pages ?: emptyArray(), vRecycler, vAdd, vFinish, { backIfEmptyAndNewerAdd() }, requestPutPage(), requestRemovePage(), requestChangePage(), requestMovePage())
    private val xFandom = XFandom()
            .setId(fandomId)
            .setLanguageId(languageId)
            .setName(fandomName)
            .setImageId(fandomImageId)
            .setOnChanged { updateTitle() }

    private var publicationId = 0L
    private var publicationTag3 = 0L

    init {
        setTitle(t(API_TRANSLATE.post_draft))
        disableShadows()
        isSingleInstanceInBackStack = true

        this.publicationId = changePost?.id ?: 0
        this.publicationTag3 = changePost?.tag_3 ?: 0
        postParams.closed = changePost?.closed ?: false

        vFinish.setOnClickListener {
            if (changePost == null || changePost.isDraft) SPostCreationTags.instance(publicationId, postParams, publicationTag3, true, fandomId, languageId, false, Navigator.TO)
            else Navigator.back()
        }
        vFinish.setOnLongClickListener {
            SPostCreationTags.create(publicationId, postParams) {
                SPost.instance(publicationId, 0, NavigationAction.replace())
                EventBus.post(EventPostStatusChange(publicationId, API.STATUS_PUBLIC))
            }
            true
        }
        if (changePost != null && !changePost.isDraft) vFinish.setImageResource(R.drawable.ic_done_white_24dp)

        if (changePost != null && changePost.status == API.STATUS_PUBLIC && !SplashAlert.check("ALERT_CHANGE_POSTS"))
            ToolsThreads.main(true) {
                SplashAlert()
                        .setTopTitleText(t(API_TRANSLATE.app_attention))
                        .setCancelable(false)
                        .setTitleImageBackgroundRes(R.color.blue_700)
                        .setText(t(API_TRANSLATE.post_change_alert))
                        .setChecker("ALERT_CHANGE_POSTS")
                        .setOnEnter(t(API_TRANSLATE.app_got_it))
                        .asSheetShow()
            }

        if (changePost == null && showMenu) ToolsThreads.main(true) { vAdd.performClick() }

        updateTitle()

        ToolsView.disableScrollViewJump(vRecycler)
    }

    override fun onResume() {
        super.onResume()
    }

    private fun updateTitle() {
        xFandom.setView(vAvatarTitle)
    }

    fun backIfEmptyAndNewerAdd() {
        if (xPostCreator.pages.isEmpty() && xPostCreator.isNewerAdd()) Navigator.back()
    }

    fun setPublicationId(publicationId: Long) {
        this.publicationId = publicationId
        EventBus.post(EventPostDraftCreated(publicationId))
    }

    //
    //  Requests
    //

    private fun requestPutPage(): (Splash?, Array<Page>, (Array<Page>) -> Unit, () -> Unit) -> Unit = { widget, pages, onCreate, onFinish ->
        ApiRequestsSupporter.executeEnabled(widget, RPostPutPage(publicationId, pages, fandomId, languageId, ControllerCampfireSDK.ROOT_PROJECT_KEY, ControllerCampfireSDK.ROOT_PROJECT_SUB_KEY)) { r ->
            if (this.publicationId == 0L) setPublicationId(r.publicationId)
            onCreate.invoke(r.pages)
            EventBus.post(EventPostChanged(publicationId, xPostCreator.pages))
            ControllerStoryQuest.incrQuest(API.QUEST_STORY_DRAFT)
        }.onFinish {
            onFinish.invoke()
        }
    }

    private fun requestRemovePage(): (Array<Int>, () -> Unit) -> Unit = { pages, onFinish ->
        ApiRequestsSupporter.executeEnabledConfirm(t(API_TRANSLATE.post_page_remove_confirm), t(API_TRANSLATE.app_remove), RPostRemovePage(publicationId, pages)) {
            onFinish.invoke()
            EventBus.post(EventPostChanged(publicationId, xPostCreator.pages))
            if (xPostCreator.pages.isEmpty()) {
                EventBus.post(EventPublicationRemove(publicationId))
                publicationId = 0
            }
        }
    }

    private fun requestChangePage(): (Splash?, Page, Int, (Page) -> Unit) -> Unit = { widget, page, index, onFinish ->
        ApiRequestsSupporter.executeEnabled(widget, RPostChangePage(publicationId, page, index)) { r ->
            onFinish.invoke(r.page!!)
            EventBus.post(EventPostChanged(publicationId, xPostCreator.pages))
        }
    }

    private fun requestMovePage(): (Int, Int, () -> Unit) -> Unit = { currentIndex, targetIndex, onFinish ->
        ApiRequestsSupporter.executeProgressDialog(RPostMovePage(publicationId, currentIndex, targetIndex)) { _ ->
            onFinish.invoke()
            EventBus.post(EventPostChanged(publicationId, xPostCreator.pages))
        }
    }

    //
    //  Share
    //

    override fun attacheText(text: String, postAfterAdd: Boolean) {
        xPostCreator.addText(text) {
            if (postAfterAdd) {
                SPostCreationTags.create(publicationId, postParams) { SPost.instance(publicationId, 0, Navigator.REPLACE) }
                EventBus.post(EventPostStatusChange(publicationId, API.STATUS_PUBLIC))
            }
        }
    }

    override fun attacheImage(image: Uri, postAfterAdd: Boolean) {
        xPostCreator.addImage(image) {
            if (postAfterAdd) {
                SPostCreationTags.create(publicationId, postParams) { SPost.instance(publicationId, 0, Navigator.REPLACE) }
                EventBus.post(EventPostStatusChange(publicationId, API.STATUS_PUBLIC))
            }
        }
    }

    override fun attacheImage(image: Bitmap, postAfterAdd: Boolean) {
        xPostCreator.addImage(image) {
            if (postAfterAdd) {
                SPostCreationTags.create(publicationId, postParams) { SPost.instance(publicationId, 0, Navigator.REPLACE) }
                EventBus.post(EventPostStatusChange(publicationId, API.STATUS_PUBLIC))
            }
        }
    }

    override fun attacheAgentIsActive() = Navigator.getCurrent() == this

    class PostParams {

        var tags: Array<Long> = emptyArray()
        var activity: UserActivity? = null
        var nextUserId = 0L
        var rubric: Rubric? = null
        var notifyFollowers = false
        var pendingTime = 0L
        var closed = false
        var multilingual = false

        fun setTags(presetTags: Array<Long>): PostParams {
            this.tags = presetTags;return this
        }

        fun setActivity(presetActivity: UserActivity): PostParams {
            this.activity = presetActivity;return this
        }

        fun setNextRelayRaceUserId(nextUserId: Long): PostParams {
            this.nextUserId = nextUserId;return this
        }

        fun setRubric(rubric: Rubric): PostParams {
            this.rubric = rubric;return this
        }

        fun setNotifyFollowers(notifyFollowers: Boolean): PostParams {
            this.notifyFollowers = notifyFollowers;return this
        }

        fun setPendingTime(pendingTime: Long): PostParams {
            this.pendingTime = pendingTime;return this
        }

        fun setClosed(closed: Boolean): PostParams {
            this.closed = closed;return this
        }

        fun setMultilingual(multilingual: Boolean): PostParams {
            this.multilingual = multilingual;return this
        }

    }

}