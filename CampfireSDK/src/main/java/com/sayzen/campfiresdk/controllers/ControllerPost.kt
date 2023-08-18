package com.sayzen.campfiresdk.controllers

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.PagesContainer
import com.dzen.campfire.api.models.publications.Publication
import com.dzen.campfire.api.models.publications.post.*
import com.dzen.campfire.api.requests.fandoms.RFandomsAdminMakeModerator
import com.dzen.campfire.api.requests.fandoms.RFandomsModerationImportant
import com.dzen.campfire.api.requests.fandoms.RFandomsModerationToDrafts
import com.dzen.campfire.api.requests.post.*
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.cards.post_pages.CardPage
import com.sayzen.campfiresdk.models.cards.post_pages.CardPageSpoiler
import com.sayzen.campfiresdk.models.events.publications.*
import com.sayzen.campfiresdk.screens.account.profile.SProfile
import com.sayzen.campfiresdk.screens.fandoms.rubrics.SRubricsList
import com.sayzen.campfiresdk.screens.post.create.SPostCreationTags
import com.sayzen.campfiresdk.screens.post.history.SPublicationHistory
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.screens.SImageView
import com.sup.dev.android.views.splash.SplashField
import com.sup.dev.android.views.splash.SplashMenu
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapter
import com.sup.dev.java.libs.eventBus.EventBus

object ControllerPost {

    var ON_PRE_SHOW_MENU: (Publication, SplashMenu) -> Unit = { _, _ -> }


    var ENABLED_BOOKMARK = false
    var ENABLED_WATCH = false
    var ENABLED_SHARE = false
    var ENABLED_COPY_LINK = false
    var ENABLED_NOTIFY_FOLLOWERS = false
    var ENABLED_CHANGE = false
    var ENABLED_CHANGE_TAGS = false
    var ENABLED_REMOVE = true
    var ENABLED_TO_DRAFTS = false
    var ENABLED_CHANGE_FANDOM = false
    var ENABLED_REPORT = true
    var ENABLED_CLEAR_REPORTS = false
    var ENABLED_BLOCK = false
    var ENABLED_MODER_TO_DRAFT = false
    var ENABLED_MODER_CHANGE_TAGS = false
    var ENABLED_INPORTANT = false
    var ENABLED_MAKE_MODER = false
    var ENABLED_MODER_CHANGE_FANDOM = false
    var ENABLED_PIN_PROFILE = false
    var ENABLED_PIN_FANDOM = false
    var ENABLED_MAKE_MULTILINGUAL = false
    var ENABLED_HISTORY = false
    var ENABLED_CLOSE = false

    fun showPostMenu(v: View, post: PublicationPost) {

        val w = SplashMenu()
                .add(t(API_TRANSLATE.app_change)) { ControllerCampfireSDK.onToDraftClicked(post.id, Navigator.TO) }.condition(ENABLED_CHANGE && (post.isPublic || post.status == API.STATUS_PENDING) && ControllerApi.isCurrentAccount(post.creator.id))
                .add(t(API_TRANSLATE.post_menu_change_tags)) { changeTags(post) }.condition(ENABLED_CHANGE_TAGS && (post.isPublic || post.status == API.STATUS_PENDING) && post.fandom.languageId != -1L && ControllerApi.isCurrentAccount(post.creator.id))
                .add(t(API_TRANSLATE.app_remove)) { remove(post) }.condition(ENABLED_REMOVE && ControllerApi.isCurrentAccount(post.creator.id))
                .add(t(API_TRANSLATE.app_duplicate)) { duplicateDraft(post) }.condition(post.isDraft)
                .add(t(API_TRANSLATE.app_to_drafts)) { toDrafts(post) }.condition(ENABLED_TO_DRAFTS && (post.isPublic || post.status == API.STATUS_PENDING) && ControllerApi.isCurrentAccount(post.creator.id))
                .add(t(API_TRANSLATE.app_publish)) { publishPending(post) }.condition(post.status == API.STATUS_PENDING && ControllerApi.isCurrentAccount(post.creator.id))
                .add(t(API_TRANSLATE.app_copy_link)) { copyLink(post) }.condition(ENABLED_COPY_LINK && post.isPublic)
                .add(t(API_TRANSLATE.app_report)) { ControllerPublications.report(post) }.condition(ENABLED_REPORT && !ControllerApi.isCurrentAccount(post.creator.id))
                .spoiler(t(API_TRANSLATE.app_additional)) { w, card ->
                    card.setProgress(true)
                    ApiRequestsSupporter.execute(RPostMenuInfoGet(post.id, Array(ControllerSettings.bookmarksFolders.size) { ControllerSettings.bookmarksFolders[it].id })) { r ->
                        card.setProgress(false)
                        w.spoiler(card)
                                .add(if (!r.bookmark) t(API_TRANSLATE.bookmarks_add) else if(ControllerSettings.bookmarksFolders.isEmpty())t(API_TRANSLATE.bookmarks_remove) else t(API_TRANSLATE.bookmarks_remove_or_change)) { ControllerPublications.changeBookmark(post.id, r.bookmark, r.folderId) }.backgroundRes(R.color.focus).condition(ENABLED_BOOKMARK && post.isPublic)
                                .add(if (r.follow) t(API_TRANSLATE.publication_menu_comments_watch_no) else t(API_TRANSLATE.publication_menu_comments_watch)) { ControllerPublications.changeWatchComments(post.id) }.backgroundRes(R.color.focus).condition(ENABLED_WATCH && post.isPublic)
                                .add(t(API_TRANSLATE.app_share)) { ControllerApi.sharePost(post.id) }.backgroundRes(R.color.focus).condition(ENABLED_SHARE && post.isPublic)
                                .add(t(API_TRANSLATE.app_history)) { Navigator.to(SPublicationHistory(post.id)) }.backgroundRes(R.color.focus).condition(ENABLED_HISTORY)
                                .add(t(API_TRANSLATE.post_create_notify_followers)) { notifyFollowers(post.id) }.backgroundRes(R.color.focus).condition(ENABLED_NOTIFY_FOLLOWERS && post.isPublic && post.tag_3 == 0L && ControllerApi.isCurrentAccount(post.creator.id))
                                .add(t(API_TRANSLATE.publication_menu_change_fandom)) { changeFandom(post.id) }.backgroundRes(R.color.focus).condition(ENABLED_CHANGE_FANDOM && post.fandom.languageId != -1L && (post.status == API.STATUS_PUBLIC || post.status == API.STATUS_DRAFT) && ControllerApi.isCurrentAccount(post.creator.id))
                                .add(t(API_TRANSLATE.publication_menu_pin_in_profile)) { pinInProfile(post) }.backgroundRes(R.color.focus).condition(ENABLED_PIN_PROFILE && ControllerApi.can(API.LVL_CAN_PIN_POST) && post.isPublic && !post.isPined && ControllerApi.isCurrentAccount(post.creator.id))
                                .add(t(API_TRANSLATE.publication_menu_unpin_in_profile)) { unpinInProfile(post) }.backgroundRes(R.color.focus).condition(ENABLED_PIN_PROFILE && post.isPined && ControllerApi.isCurrentAccount(post.creator.id))
                                .add(t(API_TRANSLATE.publication_menu_multilingual)) { multilingual(post) }.backgroundRes(R.color.focus).condition(ENABLED_MAKE_MULTILINGUAL && post.fandom.languageId != -1L && post.status == API.STATUS_PUBLIC && ControllerApi.isCurrentAccount(post.creator.id))
                                .add(t(API_TRANSLATE.publication_menu_multilingual_not)) { multilingualNot(post) }.backgroundRes(R.color.focus).condition(ENABLED_MAKE_MULTILINGUAL && post.fandom.languageId == -1L && post.status == API.STATUS_PUBLIC && ControllerApi.isCurrentAccount(post.creator.id))
                                .add(t(API_TRANSLATE.app_close)) { close(post) }.backgroundRes(R.color.focus).condition(!post.closed && ControllerApi.isCurrentAccount(post.creator.id))
                                .add(t(API_TRANSLATE.app_open)) { open(post) }.backgroundRes(R.color.focus).condition(post.closed && ControllerApi.isCurrentAccount(post.creator.id))
                                .add(t(API_TRANSLATE.post_change_rubric)) { changeRubric(post) }.backgroundRes(R.color.focus).condition(ControllerApi.isCurrentAccount(post.creator.id) && post.dateCreate < System.currentTimeMillis() - 1000 * 3600 * 24 * 7)
                                .finishItemBuilding()
                    }
                }
                .groupCondition(post.isPublic)
                .spoiler(t(API_TRANSLATE.app_moderator))
                .add(t(API_TRANSLATE.app_clear_reports)) { ControllerPublications.clearReports(post) }.backgroundRes(R.color.blue_700).textColorRes(R.color.white).condition(ENABLED_CLEAR_REPORTS && ControllerApi.can(post.fandom.id, post.fandom.languageId, API.LVL_MODERATOR_BLOCK) && post.reportsCount > 0 && !ControllerApi.isCurrentAccount(post.creator.id))
                .add(t(API_TRANSLATE.app_block)) { ControllerPublications.block(post) }.backgroundRes(R.color.blue_700).textColorRes(R.color.white).condition(ENABLED_BLOCK && ControllerApi.can(post.fandom.id, post.fandom.languageId, API.LVL_MODERATOR_BLOCK) && !ControllerApi.isCurrentAccount(post.creator.id))
                .add(t(API_TRANSLATE.publication_menu_moderator_to_drafts)) { moderatorToDrafts(post.id) }.backgroundRes(R.color.blue_700).textColorRes(R.color.white).condition(ENABLED_MODER_TO_DRAFT && ControllerApi.can(post.fandom.id, post.fandom.languageId, API.LVL_MODERATOR_TO_DRAFTS) && !ControllerApi.isCurrentAccount(post.creator.id))
                .add(t(API_TRANSLATE.publication_menu_multilingual_not)) { moderatorMakeMultilingualNot(post) }.backgroundRes(R.color.blue_700).condition(ENABLED_MAKE_MULTILINGUAL && ControllerApi.can(post.fandom.id, post.fandom.languageId, API.LVL_MODERATOR_TO_DRAFTS) && post.fandom.languageId == -1L && !ControllerApi.isCurrentAccount(post.creator.id))
                .add(t(API_TRANSLATE.post_menu_change_tags)) { changeTagsModer(post) }.backgroundRes(R.color.blue_700).textColorRes(R.color.white).condition(ENABLED_MODER_CHANGE_TAGS && ControllerApi.can(post.fandom.id, post.fandom.languageId, API.LVL_MODERATOR_POST_TAGS) && post.fandom.languageId != -1L && !ControllerApi.isCurrentAccount(post.creator.id))
                .add(t(API_TRANSLATE.publication_menu_pin_in_fandom)) { pinInFandom(post) }.backgroundRes(R.color.blue_700).textColorRes(R.color.white).condition(ENABLED_PIN_FANDOM && ControllerApi.can(post.fandom.id, post.fandom.languageId, API.LVL_MODERATOR_PIN_POST) && post.isPublic && !post.isPined && Navigator.getCurrent() !is SProfile)
                .add(t(API_TRANSLATE.publication_menu_unpin_in_fandom)) { unpinInFandom(post) }.backgroundRes(R.color.blue_700).textColorRes(R.color.white).condition(ENABLED_PIN_FANDOM && ControllerApi.can(post.fandom.id, post.fandom.languageId, API.LVL_MODERATOR_PIN_POST) && post.isPined && Navigator.getCurrent() !is SProfile)
                .add(t(API_TRANSLATE.app_close)) { closeAdmin(post) }.backgroundRes(R.color.blue_700).textColorRes(R.color.white).condition(ENABLED_CLOSE && ControllerApi.can(post.fandom.id, post.fandom.languageId, API.LVL_MODERATOR_CLOSE_POST) && !post.closed && !ControllerApi.isCurrentAccount(post.creator.id))
                .add(t(API_TRANSLATE.app_open)) { openAdmin(post) }.backgroundRes(R.color.blue_700).textColorRes(R.color.white).condition(ENABLED_CLOSE && ControllerApi.can(post.fandom.id, post.fandom.languageId, API.LVL_MODERATOR_CLOSE_POST) && post.closed && !ControllerApi.isCurrentAccount(post.creator.id))
                .add(if (post.important == API.PUBLICATION_IMPORTANT_IMPORTANT) t(API_TRANSLATE.publication_menu_important_unmark) else t(API_TRANSLATE.publication_menu_important_mark)) { markAsImportant(post.id, !(post.important == API.PUBLICATION_IMPORTANT_IMPORTANT)) }.backgroundRes(R.color.blue_700).textColorRes(R.color.white).condition(ENABLED_INPORTANT && ControllerApi.can(post.fandom.id, post.fandom.languageId, API.LVL_MODERATOR_IMPORTANT) && post.isPublic && post.fandom.languageId != -1L)
                .spoiler(t(API_TRANSLATE.app_admin))
                .add(t(API_TRANSLATE.admin_make_moder)) { makeModerator(post) }.backgroundRes(R.color.red_700).textColorRes(R.color.white).condition(ENABLED_MAKE_MODER && ControllerApi.can(API.LVL_ADMIN_MAKE_MODERATOR) && post.fandom.languageId != -1L && !ControllerApi.isCurrentAccount(post.creator.id))
                .add(t(API_TRANSLATE.publication_menu_remove_media)) { removeMedia(post) }.backgroundRes(R.color.red_700).textColorRes(R.color.white).condition(ControllerApi.can(API.LVL_ADMIN_REMOVE_MEDIA) && post.fandom.languageId != -1L)
                .add(t(API_TRANSLATE.publication_menu_change_fandom)) { changeFandomAdmin(post.id) }.backgroundRes(R.color.red_700).textColorRes(R.color.white).condition(ENABLED_MODER_CHANGE_FANDOM && ControllerApi.can(API.LVL_ADMIN_POST_CHANGE_FANDOM) && post.fandom.languageId != -1L && !ControllerApi.isCurrentAccount(post.creator.id))
                .groupCondition(ControllerApi.can(API.LVL_PROTOADMIN))
                .spoiler(t(API_TRANSLATE.app_protoadmin))
                .add("Востановить") { ControllerPublications.restoreDeepBlock(post.id) }.backgroundRes(R.color.orange_700).textColorRes(R.color.white).condition(post.status == API.STATUS_DEEP_BLOCKED)

        ON_PRE_SHOW_MENU.invoke(post, w)
        w.asPopupShow(v)
    }

    fun close(publications: PublicationPost) {
        ApiRequestsSupporter.executeEnabledConfirm(
                t(API_TRANSLATE.post_close_confirm),
                t(API_TRANSLATE.app_close),
                RPostClose(publications.id)
        ) {
            EventBus.post(EventPostCloseChange(publications.id, true))
            ToolsToast.show(t(API_TRANSLATE.app_done))
        }
    }

    fun open(publications: PublicationPost) {
        ApiRequestsSupporter.executeEnabledConfirm(
                t(API_TRANSLATE.post_open_confirm),
                t(API_TRANSLATE.app_open),
                RPostCloseNo(publications.id)
        ) {
            EventBus.post(EventPostCloseChange(publications.id, false))
            ToolsToast.show(t(API_TRANSLATE.app_done))
        }
    }

    fun changeRubric(post: PublicationPost) {
        Navigator.to(SRubricsList(
            fandomId = post.fandom.id,
            languageId = post.fandom.languageId,
            ownerId = ControllerApi.account.getId(),
            canCreatePost = false,
        ) { rubric ->
            ApiRequestsSupporter.executeProgressDialog(RPostMoveRubric(post.id, rubric.id)) { _ ->
                post.rubricId = rubric.id
                post.rubricName = rubric.name

                EventBus.post(EventPostRubricChange(post.id, rubric))
                ToolsToast.show(t(API_TRANSLATE.app_done))
            }
        })
    }

    fun closeAdmin(publications: PublicationPost) {
        SplashField()
                .setTitle(t(API_TRANSLATE.post_close_confirm))
                .setHint(t(API_TRANSLATE.comments_hint))
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(t(API_TRANSLATE.app_close)) { w, comment ->
                    ApiRequestsSupporter.executeEnabled(w, RPostCloseModerator(publications.id, comment)) {
                        EventBus.post(EventPostCloseChange(publications.id, true))
                        ToolsToast.show(t(API_TRANSLATE.app_done))
                    }
                }
                .asSheetShow()
    }

    fun openAdmin(publications: PublicationPost) {
        SplashField()
                .setTitle(t(API_TRANSLATE.post_open_confirm))
                .setHint(t(API_TRANSLATE.comments_hint))
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(t(API_TRANSLATE.app_open)) { w, comment ->
                    ApiRequestsSupporter.executeEnabled(w, RPostCloseNoModerator(publications.id, comment)) {
                        EventBus.post(EventPostCloseChange(publications.id, false))
                        ToolsToast.show(t(API_TRANSLATE.app_done))
                    }
                }
                .asSheetShow()
    }

    fun publishPending(publications: PublicationPost) {
        ApiRequestsSupporter.executeEnabledConfirm(
                t(API_TRANSLATE.post_pending_publish),
                t(API_TRANSLATE.app_publish),
                RPostPendingPublish(publications.id)
        ) {
            EventBus.post(EventPostStatusChange(publications.id, API.STATUS_PUBLIC))
            ToolsToast.show(t(API_TRANSLATE.app_done))
        }
    }

    fun multilingual(publications: PublicationPost) {
        ApiRequestsSupporter.executeEnabledConfirm(
                t(API_TRANSLATE.publication_menu_multilingual_confirm),
                t(API_TRANSLATE.app_continue),
                RPostMakeMultilingual(publications.id)
        ) {
            EventBus.post(EventPostMultilingualChange(publications.id, -1L, publications.fandom.languageId))
            ToolsToast.show(t(API_TRANSLATE.app_done))
        }
    }

    fun multilingualNot(publications: PublicationPost) {
        ApiRequestsSupporter.executeEnabledConfirm(
                t(API_TRANSLATE.publication_menu_multilingual_not),
                t(API_TRANSLATE.app_continue),
                RPostMakeMultilingualNot(publications.id)
        ) {
            EventBus.post(EventPostMultilingualChange(publications.id, publications.tag_5, -1L))
            ToolsToast.show(t(API_TRANSLATE.app_done))
        }
    }

    fun pinInFandom(publications: PublicationPost) {
        SplashField()
                .setTitle(t(API_TRANSLATE.publication_menu_pin_in_fandom))
                .setHint(t(API_TRANSLATE.comments_hint))
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(t(API_TRANSLATE.app_pin)) { w, comment ->
                    ApiRequestsSupporter.executeEnabled(w, RPostPinFandom(publications.id, publications.fandom.id, publications.fandom.languageId, comment)) {
                        EventBus.post(EventPostPinedFandom(publications.fandom.id, publications.fandom.languageId, publications))
                        ToolsToast.show(t(API_TRANSLATE.app_done))
                    }
                }
                .asSheetShow()
    }

    fun unpinInFandom(publications: PublicationPost) {
        SplashField()
                .setTitle(t(API_TRANSLATE.publication_menu_unpin_in_fandom))
                .setHint(t(API_TRANSLATE.comments_hint))
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(t(API_TRANSLATE.app_unpin)) { w, comment ->
                    ApiRequestsSupporter.executeEnabled(w, RPostPinFandom(0, publications.fandom.id, publications.fandom.languageId, comment)) {
                        EventBus.post(EventPostPinedFandom(publications.fandom.id, publications.fandom.languageId, null))
                        ToolsToast.show(t(API_TRANSLATE.app_done))
                    }
                }
                .asSheetShow()
    }

    fun pinInProfile(publications: PublicationPost) {
        ApiRequestsSupporter.executeEnabledConfirm(
                t(API_TRANSLATE.publication_menu_pin_profile_confirm),
                t(API_TRANSLATE.app_pin),
                RPostPinAccount(publications.id)
        ) {
            EventBus.post(EventPostPinedProfile(publications.creator.id, publications))
            ToolsToast.show(t(API_TRANSLATE.app_done))
        }
    }

    fun unpinInProfile(publication: PublicationPost) {
        ApiRequestsSupporter.executeEnabledConfirm(
                t(API_TRANSLATE.publication_menu_unpin_profile_confirm),
                t(API_TRANSLATE.app_unpin),
                RPostPinAccount(0)
        ) {
            EventBus.post(EventPostPinedProfile(publication.creator.id, null))
            ToolsToast.show(t(API_TRANSLATE.app_done))
        }
    }

    fun notifyFollowers(publicationId: Long) {
        ApiRequestsSupporter.executeEnabledConfirm(
                t(API_TRANSLATE.post_create_notify_followers),
                t(API_TRANSLATE.app_notify),
                RPostNotifyFollowers(publicationId)
        ) {
            EventBus.post(EventPostNotifyFollowers(publicationId))
            ToolsToast.show(t(API_TRANSLATE.app_done))
        }
    }

    fun getPagesGroup(adapter: RecyclerCardAdapter): ArrayList<CardPage> {
        var b = false
        val list = ArrayList<CardPage>()
        for (i in 0 until adapter.size()) {
            if (adapter[i] !is CardPage) {
                if (b)
                    break
                else
                    continue
            }
            b = true
            list.add(adapter[i] as CardPage)
        }
        return list
    }

    fun openAllSpoilers(adapter: RecyclerCardAdapter) {
        val list = getPagesGroup(adapter)
        for (card in list)
            if (card is CardPageSpoiler) {
                (card.page as PageSpoiler).isOpen = true
                card.setHidedX(false)
            }
        updateSpoilers(list)
    }

    fun updateSpoilers(adapter: RecyclerCardAdapter) {
        updateSpoilers(getPagesGroup(adapter))
    }

    fun updateSpoilers(listMine: ArrayList<CardPage>) {
        val list = ArrayList<CardPage>()
        for (c in listMine) if (c.isSpoilerAvalible) list.add(c)
        while (list.isNotEmpty()) {
            val card = list.removeAt(0)
            if (card is CardPageSpoiler) parseSpoiler(
                    list,
                    (card.page as PageSpoiler).count,
                    !(card.page as PageSpoiler).isOpen
            )
        }
    }

    private fun parseSpoiler(list: ArrayList<CardPage>, maxCount: Int, hide: Boolean) {
        var parsedPages = 0
        while (list.isNotEmpty()) {
            val card = list.removeAt(0)
            parsedPages++
            card.setHidedX(hide)
            if (card is CardPageSpoiler) parseSpoiler(
                    list,
                    (card.page as PageSpoiler).count,
                    !(card.page as PageSpoiler).isOpen || hide
            )
            if (parsedPages == maxCount) return
        }
    }

    fun changeFandom(publicationId: Long) {
        ControllerCampfireSDK.SEARCH_FANDOM.invoke { fandom ->
            ApiRequestsSupporter.executeEnabledConfirm(
                    t(API_TRANSLATE.publication_menu_change_fandom_confirm),
                    t(API_TRANSLATE.app_change),
                    RPostChangeFandom(publicationId, fandom.id, fandom.languageId, "")
            ) {
                ToolsToast.show(t(API_TRANSLATE.app_done))
                EventBus.post(EventPublicationFandomChanged(publicationId, fandom.id, fandom.languageId, fandom.name, fandom.imageId))
            }
                    .onApiError(RPostChangeFandom.E_SAME_FANDOM) { ToolsToast.show(t(API_TRANSLATE.error_same_fandom)) }
        }
    }

    fun changeFandomAdmin(publicationId: Long) {
        ControllerCampfireSDK.SEARCH_FANDOM.invoke { fandom ->
            SplashField()
                    .setTitle(t(API_TRANSLATE.publication_menu_change_fandom_confirm))
                    .setHint(t(API_TRANSLATE.moderation_widget_comment))
                    .setOnCancel(t(API_TRANSLATE.app_cancel))
                    .setMin(API.MODERATION_COMMENT_MIN_L)
                    .setMax(API.MODERATION_COMMENT_MAX_L)
                    .setOnEnter(t(API_TRANSLATE.app_change)) { w, comment ->
                        ApiRequestsSupporter.executeEnabled(
                                w,
                                RPostChangeFandom(publicationId, fandom.id, fandom.languageId, comment)
                        ) {
                            ToolsToast.show(t(API_TRANSLATE.app_done))
                            EventBus.post(
                                    EventPublicationFandomChanged(
                                            publicationId,
                                            fandom.id,
                                            fandom.languageId,
                                            fandom.name,
                                            fandom.imageId
                                    )
                            )
                        }
                                .onApiError(RPostChangeFandom.E_SAME_FANDOM) { ToolsToast.show(t(API_TRANSLATE.error_same_fandom)) }
                    }
                    .asSheetShow()
        }
    }

    fun markAsImportant(publicationId: Long, important: Boolean) {
        SplashField()
                .setTitle(if (!important) t(API_TRANSLATE.publication_menu_important_unmark) else t(API_TRANSLATE.publication_menu_important_mark))
                .setHint(t(API_TRANSLATE.comments_hint))
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(if (!important) t(API_TRANSLATE.app_do_unmark) else t(API_TRANSLATE.app_do_mark)) { _, comment ->
                    ApiRequestsSupporter.executeEnabledConfirm(
                            if (!important) t(API_TRANSLATE.publication_menu_important_unmark_confirm) else t(API_TRANSLATE.publication_menu_important_mark_confirm),
                            if (!important) t(API_TRANSLATE.app_do_unmark) else t(API_TRANSLATE.app_do_mark),
                            RFandomsModerationImportant(publicationId, important, comment)
                    ) {
                        ToolsToast.show(t(API_TRANSLATE.app_done))
                        EventBus.post(
                                EventPublicationImportantChange(
                                        publicationId,
                                        if (important) API.PUBLICATION_IMPORTANT_IMPORTANT else API.PUBLICATION_IMPORTANT_DEFAULT
                                )
                        )
                    }
                }
                .asSheetShow()
    }

    fun moderatorToDrafts(publicationId: Long) {
        SplashField()
                .setTitle(t(API_TRANSLATE.publication_menu_moderator_to_drafts))
                .setHint(t(API_TRANSLATE.moderation_widget_comment))
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(t(API_TRANSLATE.app_to_return)) { w, comment ->
                    ApiRequestsSupporter.executeEnabled(w, RFandomsModerationToDrafts(publicationId, comment)) {
                        ToolsToast.show(t(API_TRANSLATE.app_done))
                        EventBus.post(EventPublicationRemove(publicationId))
                    }
                            .onApiError(RFandomsModerationToDrafts.E_ALREADY) {
                                ToolsToast.show(t(API_TRANSLATE.error_already_returned_to_drafts))
                                EventBus.post(EventPublicationRemove(publicationId))
                            }
                            .onApiError(RFandomsModerationToDrafts.E_BLOCKED) {
                                ToolsToast.show(t(API_TRANSLATE.error_already_blocked))
                                EventBus.post(EventPublicationRemove(publicationId))
                            }
                            .onApiError(RFandomsModerationToDrafts.E_LOW_KARMA_FORCE) { ToolsToast.show(t(API_TRANSLATE.moderation_low_karma)) }
                }
                .asSheetShow()
    }

    fun moderatorMakeMultilingualNot(publication: Publication) {
        SplashField()
                .setTitle(t(API_TRANSLATE.publication_menu_multilingual_not))
                .setHint(t(API_TRANSLATE.moderation_widget_comment))
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(t(API_TRANSLATE.app_make)) { w, comment ->
                    ApiRequestsSupporter.executeEnabled(w, RPostMakeMultilingualModeratorNot(publication.id, comment)) {
                        ToolsToast.show(t(API_TRANSLATE.app_done))
                        EventBus.post(EventPostMultilingualChange(publication.id, publication.tag_5, -1L))
                    }
                            .onApiError(RPostMakeMultilingualModeratorNot.E_LOW_KARMA_FORCE) { ToolsToast.show(t(API_TRANSLATE.moderation_low_karma)) }
                }
                .asSheetShow()
    }

    fun makeModerator(publication: Publication) {
        SplashField()
                .setTitle(t(API_TRANSLATE.admin_make_moder))
                .setHint(t(API_TRANSLATE.moderation_widget_comment))
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .setAutoHideOnEnter(false)
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(t(API_TRANSLATE.app_make)) { w, comment ->
                    ApiRequestsSupporter.executeEnabled(w, RFandomsAdminMakeModerator(publication.id, comment)) { r ->
                        ToolsToast.show(t(API_TRANSLATE.app_done))
                    }
                            .onApiError(RFandomsAdminMakeModerator.E_ALREADY) { ToolsToast.show(t(API_TRANSLATE.error_moderator_already)) }
                            .onApiError(RFandomsAdminMakeModerator.E_TOO_MANY) { ToolsToast.show(t(API_TRANSLATE.error_moderator_too_many)) }
                            .onApiError(RFandomsAdminMakeModerator.E_FANDOM_HAVE_MODERATORS) { ToolsToast.show(t(API_TRANSLATE.error_moderator_moderators_exist)) }
                            .onApiError(RFandomsAdminMakeModerator.E_LOW_LVL) { ToolsToast.show(t(API_TRANSLATE.error_moderator_low_lvl)) }
                }
                .asSheetShow()

    }

    fun removeMedia(publication: Publication) {
        ControllerApi.moderation(t(API_TRANSLATE.publication_menu_remove_media), t(API_TRANSLATE.app_remove),
                {comment ->  RPostAdminRemoveMedia(publication.id, comment)}
                ){
            ToolsToast.show(t(API_TRANSLATE.app_done))
        }
    }

    private fun copyLink(publication: Publication) {
        ToolsAndroid.setToClipboard(ControllerLinks.linkToPost(publication.id))
        ToolsToast.show(t(API_TRANSLATE.app_copied))
    }

    private fun changeTags(publication: Publication) {
        SPostCreationTags.instance(publication.id, true, true, Navigator.TO)
    }

    private fun changeTagsModer(publication: Publication) {
        SPostCreationTags.instance(publication.id, false, true, Navigator.TO)
    }

    private fun remove(publication: Publication) {
        ControllerApi.removePublication(
                publication.id,
                t(API_TRANSLATE.post_remove_confirm),
                t(API_TRANSLATE.post_error_gone)
        )
    }

    private fun duplicateDraft(publication: PublicationPost) {
        ApiRequestsSupporter.executeProgressDialog(RPostDuplicateDraft(
            postId = publication.id,
        )) { resp ->
            EventBus.post(EventPostDraftCreated(resp.unitId))
        }
    }

    private fun toDrafts(publication: Publication) {
        ControllerPublications.toDrafts(publication.id) {
            ControllerCampfireSDK.onToDraftsClicked(
                    Navigator.REPLACE
            )
        }
    }

    fun toImagesScreen(pagesContainer: PagesContainer, imageId: Long) {
        val list = ArrayList<Long>()
        var index = 0

        for (p in pagesContainer.getPagesArray()) {
            when (p) {
                is PageImage -> {
                    if (p.imageId == imageId) index = list.size
                    list.add(p.getMainImageId())
                }
                is PageImages -> {
                    for (subImageId in p.imagesIds) {
                        if (subImageId == imageId) index = list.size
                        list.add(subImageId)
                    }
                }
                is PageTable -> {
                    for (cell in p.cells) {
                        if (cell.imageId < 1) continue
                        if (cell.imageId == imageId) index = list.size
                        list.add(cell.imageId)
                    }
                }
            }
        }

        Navigator.to(SImageView(index, Array(list.size) { ImageLoader.load(list[it]) }))
    }

}
