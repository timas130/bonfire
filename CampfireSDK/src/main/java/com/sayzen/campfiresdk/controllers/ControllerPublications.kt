package com.sayzen.campfiresdk.controllers

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.models.publications.Publication
import com.dzen.campfire.api.models.publications.PublicationComment
import com.dzen.campfire.api.models.publications.chat.PublicationChatMessage
import com.dzen.campfire.api.models.publications.moderations.PublicationModeration
import com.dzen.campfire.api.models.publications.moderations.activities.ModerationActivitiesChange
import com.dzen.campfire.api.models.publications.moderations.activities.ModerationActivitiesCreate
import com.dzen.campfire.api.models.publications.moderations.activities.ModerationActivitiesRemove
import com.dzen.campfire.api.models.publications.moderations.chat.ModerationChatChange
import com.dzen.campfire.api.models.publications.moderations.chat.ModerationChatCreate
import com.dzen.campfire.api.models.publications.moderations.chat.ModerationChatRemove
import com.dzen.campfire.api.models.publications.moderations.fandom.*
import com.dzen.campfire.api.models.publications.moderations.posts.*
import com.dzen.campfire.api.models.publications.moderations.publications.ModerationBlock
import com.dzen.campfire.api.models.publications.moderations.publications.ModerationForgive
import com.dzen.campfire.api.models.publications.moderations.rubrics.*
import com.dzen.campfire.api.models.publications.moderations.tags.*
import com.dzen.campfire.api.models.publications.tags.PublicationTag
import com.dzen.campfire.api.requests.bookmarks.RBookmarksAdd
import com.dzen.campfire.api.requests.bookmarks.RBookmarksRemove
import com.dzen.campfire.api.requests.comments.RCommentsWatchChange
import com.dzen.campfire.api.requests.post.RPostToDrafts
import com.dzen.campfire.api.requests.publications.RPublicationsAdminRestoreDeepBlock
import com.dzen.campfire.api.requests.tags.RTagsMove
import com.dzen.campfire.api.requests.tags.RTagsMoveCategory
import com.dzen.campfire.api.requests.tags.RTagsMoveTag
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.models.events.fandom.EventFandomTagMove
import com.sayzen.campfiresdk.models.events.publications.EventPostStatusChange
import com.sayzen.campfiresdk.models.events.publications.EventPublicationBookmarkChange
import com.sayzen.campfiresdk.models.events.publications.EventPublicationCommentWatchChange
import com.sayzen.campfiresdk.models.events.publications.EventPublicationDeepBlockRestore
import com.sayzen.campfiresdk.models.objects.TagParent
import com.sayzen.campfiresdk.models.splashs.SplashCategoryCreate
import com.sayzen.campfiresdk.models.splashs.SplashModerationBlock
import com.sayzen.campfiresdk.screens.account.profile.SProfile
import com.sayzen.campfiresdk.screens.account.stickers.SStickersView
import com.sayzen.campfiresdk.screens.activities.user_activities.relay_race.SRelayRaceInfo
import com.sayzen.campfiresdk.screens.chat.SChat
import com.sayzen.campfiresdk.screens.comments.SComments
import com.sayzen.campfiresdk.screens.fandoms.rubrics.SRubricPosts
import com.sayzen.campfiresdk.screens.fandoms.tags.SplashTagCreate
import com.sayzen.campfiresdk.screens.fandoms.tags.SplashTagRemove
import com.sayzen.campfiresdk.screens.quests.SQuest
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.splash.SplashField
import com.sup.dev.android.views.splash.SplashMenu
import com.sup.dev.android.views.views.ViewText
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsDate
import com.sup.dev.java.tools.ToolsText
import java.util.*

object ControllerPublications {

    fun getMaskFor(publicationType: Long, mask: String, type: Long): String {
        return when (publicationType) {
            API.PUBLICATION_TYPE_POST -> getMaskForPost(mask, type)
            API.PUBLICATION_TYPE_COMMENT -> getMaskForComment(mask, type)
            else -> getMaskForMessage(mask, type)
        }
    }

    fun getMaskForPost(mask: String, type: Long): String {
        if (mask.isNotEmpty()) return " \"${mask}\""
        return getPostPageName(type)
    }

    fun getMaskForComment(mask: String, type: Long): String {
        if (mask.isNotEmpty()) return " \"${mask}\""
        return getCommentName(type)
    }

    fun getMaskForMessage(mask: String, type: Long): String {
        if (mask.isNotEmpty()) return " \"${mask}\""
        return getMessageName(type)
    }

    fun getCommentName(type: Long): String {
        if (type == PublicationComment.TYPE_IMAGE) return " \"${t(API_TRANSLATE.app_image)}\""
        if (type == PublicationComment.TYPE_GIF) return " \"${t(API_TRANSLATE.app_gif)}\""
        if (type == PublicationComment.TYPE_IMAGES) return " \"${t(API_TRANSLATE.app_images)}\""
        if (type == PublicationComment.TYPE_STICKER) return " \"${t(API_TRANSLATE.app_sticker)}\""
        return ""
    }

    fun getMessageName(type: Long): String {
        if (type == PublicationChatMessage.TYPE_IMAGE) return " \"${t(API_TRANSLATE.app_image)}\""
        if (type == PublicationChatMessage.TYPE_GIF) return " \"${t(API_TRANSLATE.app_gif)}\""
        if (type == PublicationChatMessage.TYPE_IMAGES) return " \"${t(API_TRANSLATE.app_images)}\""
        if (type == PublicationChatMessage.TYPE_STICKER) return " \"${t(API_TRANSLATE.app_sticker)}\""
        if (type == PublicationChatMessage.TYPE_VOICE) return " \"${t(API_TRANSLATE.app_voice_message)}\""
        return ""
    }

    fun getPostPageName(type: Long): String {
        if (type == API.PAGE_TYPE_IMAGE) return " \"${t(API_TRANSLATE.post_page_image)}\""
        if (type == API.PAGE_TYPE_IMAGES) return " \"${t(API_TRANSLATE.post_page_images)}\""
        if (type == API.PAGE_TYPE_LINK) return " \"${t(API_TRANSLATE.post_page_link)}\""
        if (type == API.PAGE_TYPE_LINK_IMAGE) return " \"${t(API_TRANSLATE.post_page_link)}\""
        if (type == API.PAGE_TYPE_QUOTE) return " \"${t(API_TRANSLATE.post_page_quote)}\""
        if (type == API.PAGE_TYPE_SPOILER) return " \"${t(API_TRANSLATE.post_page_spoiler)}\""
        if (type == API.PAGE_TYPE_POLLING) return " \"${t(API_TRANSLATE.post_page_polling)}\""
        if (type == API.PAGE_TYPE_VIDEO) return " \"${t(API_TRANSLATE.post_page_video)}\""
        if (type == API.PAGE_TYPE_TABLE) return " \"${t(API_TRANSLATE.post_page_table)}\""
        if (type == API.PAGE_TYPE_CAMPFIRE_OBJECT) return " \"${t(API_TRANSLATE.post_page_campfire_object)}\""
        return ""
    }

    fun getName(publicationType: Long): String {
        return when (publicationType) {
            API.PUBLICATION_TYPE_COMMENT -> t(API_TRANSLATE.app_comment)
            API.PUBLICATION_TYPE_CHAT_MESSAGE -> t(API_TRANSLATE.app_message)
            API.PUBLICATION_TYPE_TAG -> t(API_TRANSLATE.app_tag)
            API.PUBLICATION_TYPE_MODERATION -> t(API_TRANSLATE.app_moderation)
            API.PUBLICATION_TYPE_POST -> t(API_TRANSLATE.app_post)
            API.PUBLICATION_TYPE_STICKERS_PACK -> t(API_TRANSLATE.app_stickers_pack)
            API.PUBLICATION_TYPE_STICKER -> t(API_TRANSLATE.app_sticker)
            else -> "[unknown]"
        }
    }

    fun toPublication(publicationType: Long, publicationId: Long, commentId: Long = 0) {
        if (publicationType == API.PUBLICATION_TYPE_POST) ControllerCampfireSDK.onToPostClicked(publicationId, commentId, Navigator.TO)
        if (publicationType == API.PUBLICATION_TYPE_MODERATION) ControllerCampfireSDK.onToModerationClicked(publicationId, commentId, Navigator.TO)
        if (publicationType == API.PUBLICATION_TYPE_STICKER) SStickersView.instanceBySticker(publicationId, Navigator.TO)
        if (publicationType == API.PUBLICATION_TYPE_CHAT_MESSAGE) SChat.instance(publicationId, true, Navigator.TO)
        if (publicationType == API.PUBLICATION_TYPE_STICKERS_PACK) {
            if (commentId == 0L) SStickersView.instance(publicationId, Navigator.TO)
            else Navigator.to(SComments(publicationId, commentId))
        }
        if (publicationType == API.PUBLICATION_TYPE_QUEST) SQuest.instance(publicationId, commentId, Navigator.TO)
    }

    fun block(publication: Publication, onBlock: () -> Unit = {}) {
        SplashModerationBlock.show(publication, onBlock)
    }

    fun report(publication: Publication) {
        ControllerApi.reportPublication(
                publication.id,
                t(API_TRANSLATE.post_report_confirm),
                t(API_TRANSLATE.post_error_gone)
        )
    }

    fun clearReports(publication: Publication) {
        ControllerApi.clearReportsPublication(publication.id, publication.publicationType)
    }


    //
    //  Tag
    //

    fun createTagMenu(view: View, publication: PublicationTag, moderationAllowed:Boolean, tags: Array<TagParent> = emptyArray()) {

        var parentTags = ArrayList<PublicationTag>()
        if (publication.parentPublicationId > 0) for (t in tags) if (t.tag.id == publication.parentPublicationId) parentTags = t.tags

        val w = SplashMenu()
                .add(t(API_TRANSLATE.app_copy_link)) { ToolsAndroid.setToClipboard(ControllerLinks.linkToTag(publication.id));ToolsToast.show(t(API_TRANSLATE.app_copied)) }
                .spoiler(t(API_TRANSLATE.app_moderator))
                .groupCondition(moderationAllowed && ControllerApi.can(publication.fandom.id, publication.fandom.languageId, API.LVL_MODERATOR_TAGS))
                .add(t(API_TRANSLATE.app_change)) { changeTag(publication) }.backgroundRes(R.color.blue_700).textColorRes(R.color.white)
                .add(t(API_TRANSLATE.app_move)) { moveTag(publication, tags) }.condition(tags.size > 1 && publication.parentPublicationId > 0).backgroundRes(R.color.blue_700).textColorRes(R.color.white)
                .add(t(API_TRANSLATE.app_display_before)) { displayBefore(publication, parentTags) }.condition(parentTags.size > 1 && publication.parentPublicationId > 0).backgroundRes(R.color.blue_700).textColorRes(R.color.white)
                .add(t(API_TRANSLATE.app_display_above)) { displayAbove(publication, tags) }.condition(tags.size > 1 && publication.parentPublicationId == 0L).backgroundRes(R.color.blue_700).textColorRes(R.color.white)
                .add(t(API_TRANSLATE.app_remove)) { SplashTagRemove(publication) }.backgroundRes(R.color.blue_700).textColorRes(R.color.white)

        view.setOnLongClickListener {
            w.asPopupShow(view)
            return@setOnLongClickListener true
        }
    }

    fun changeTag(publication: PublicationTag) {
        if (publication.parentPublicationId == 0L) SplashCategoryCreate(publication)
        else SplashTagCreate(publication)
    }

    fun moveTag(publication: PublicationTag, tags: Array<TagParent>) {
        val menu = SplashMenu()
        for (tag in tags) {
            if (publication.parentPublicationId != tag.tag.id) menu.add(tag.tag.name) {
                SplashField()
                        .setHint(t(API_TRANSLATE.moderation_widget_comment))
                        .setOnCancel(t(API_TRANSLATE.app_cancel))
                        .setMin(API.MODERATION_COMMENT_MIN_L)
                        .setMax(API.MODERATION_COMMENT_MAX_L)
                        .setOnEnter(t(API_TRANSLATE.app_move)) { w, comment ->
                            ApiRequestsSupporter.executeEnabled(w, RTagsMove(publication.id, tag.tag.id, comment)) {
                                EventBus.post(EventFandomTagMove(publication.fandom.id, publication.fandom.languageId, publication.id, publication.parentPublicationId, tag.tag.id))
                                ToolsToast.show(t(API_TRANSLATE.app_done))
                            }
                        }
                        .asSheetShow()
            }
        }
        menu.asSheetShow()
    }

    fun displayBefore(publication: PublicationTag, parentTags: ArrayList<PublicationTag>) {
        val menu = SplashMenu()
        for (t in parentTags) {
            if (t.id != publication.id)
                menu.add(t.name) {
                    ControllerApi.moderation(t(API_TRANSLATE.app_display_before), t(API_TRANSLATE.app_move), {RTagsMoveTag(publication.id, t.id, it)}, {
                        EventBus.post(EventFandomTagMove(publication.fandom.id, publication.fandom.languageId, publication.id, publication.parentPublicationId, t.id))
                        ToolsToast.show(t(API_TRANSLATE.app_done))
                    })
                }
        }
        menu.asSheetShow()
    }

    fun displayAbove(publication: PublicationTag, tags: Array<TagParent>) {
        val menu = SplashMenu()
        for (tag in tags) {
            if (publication.id != tag.tag.id) menu.add(tag.tag.name) {
                ControllerApi.moderation(t(API_TRANSLATE.app_display_above), t(API_TRANSLATE.app_move), {RTagsMoveCategory(publication.id, tag.tag.id, it)}, {
                    EventBus.post(EventFandomTagMove(publication.fandom.id, publication.fandom.languageId, publication.id, publication.parentPublicationId, tag.tag.id))
                    ToolsToast.show(t(API_TRANSLATE.app_done))
                })
            }
        }
        menu.asSheetShow()
    }

    fun parseTags(tagsOriginal: Array<PublicationTag>): Array<TagParent> {


        val tags = ArrayList<PublicationTag>()
        Collections.addAll(tags, *tagsOriginal)
        val map = HashMap<Long, TagParent>()

        var i = 0
        while (i < tags.size) {
            if (tags[i].parentPublicationId == 0L && !map.containsKey(tags[i].id)) map[tags[i].id] = TagParent(tags.removeAt(i--))
            i++
        }

        for (u in tags) {
            val tagParent = map[u.parentPublicationId] ?: continue
            tagParent.tags.add(u)
        }

        val resultTags = ArrayList(map.values).toTypedArray()

        resultTags.sortWith(Comparator { o1, o2 -> (o2.tag.tag_1 - o1.tag.tag_1).toInt() })
        for (n in resultTags) n.tags.sortWith(Comparator { o1, o2 -> (o2.tag_1 - o1.tag_1).toInt() })

        return resultTags
    }

    fun tagsAsLongArray(tags: Array<PublicationTag>) = Array(tags.size) { tags[it].id }


    //
    //  Moderation
    //


    fun showModerationPopup(publication: PublicationModeration) {
        SplashMenu()
                .add(t(API_TRANSLATE.app_copy_link)) {
                    ToolsAndroid.setToClipboard(ControllerLinks.linkToModeration(publication.id))
                    ToolsToast.show(t(API_TRANSLATE.app_copied))
                }.condition(publication.isPublic)
                .asSheetShow()
    }

    //
    //  Requests
    //

    fun changeWatchComments(publicationId: Long) {
        ApiRequestsSupporter.executeProgressDialog(RCommentsWatchChange(publicationId)) { r ->
            EventBus.post(EventPublicationCommentWatchChange(publicationId, r.follow))
            if (r.follow) ToolsToast.show(t(API_TRANSLATE.publication_menu_comments_watch_on))
            else ToolsToast.show(t(API_TRANSLATE.publication_menu_comments_watch_off))
        }
    }

    fun changeBookmark(publicationId: Long, isInBookmarks: Boolean, bookmarkFolderId: Long) {
        ControllerStoryQuest.incrQuest(API.QUEST_STORY_BOOKMARKS)

        if (ControllerSettings.bookmarksFolders.isEmpty()) {
            if (isInBookmarks) removeBookmarkNow(publicationId)
            else addBookmarkNow(publicationId, 0)
        } else {
            val vMenu = SplashMenu()
            for (f in ControllerSettings.bookmarksFolders) vMenu.add(f.name) { addBookmarkNow(publicationId, f.id) }.condition(f.id != bookmarkFolderId)
            vMenu.add(t(API_TRANSLATE.app_root)) { addBookmarkNow(publicationId, 0) }.condition(!isInBookmarks || bookmarkFolderId != 0L)
            vMenu.add(t(API_TRANSLATE.app_remove)) { removeBookmarkNow(publicationId) }.condition(isInBookmarks)
            vMenu.asSheetShow()
        }
    }

    private fun removeBookmarkNow(publicationId: Long) {
        ApiRequestsSupporter.executeProgressDialog(RBookmarksRemove(publicationId)) { rr ->
            EventBus.post(EventPublicationBookmarkChange(publicationId, false))
            ToolsToast.show(t(API_TRANSLATE.bookmarks_removed))
        }
    }

    private fun addBookmarkNow(publicationId: Long, folderId: Long) {
        ApiRequestsSupporter.executeProgressDialog(RBookmarksAdd(publicationId, folderId)) { r ->
            EventBus.post(EventPublicationBookmarkChange(publicationId, true))
            ToolsToast.show(t(API_TRANSLATE.bookmarks_added))
        }
    }

    fun toDrafts(publicationId: Long, onComplete: () -> Unit) {
        ApiRequestsSupporter.executeEnabledConfirm(t(API_TRANSLATE.post_confirm_to_draft), t(API_TRANSLATE.post_confirm_to_draft_enter), RPostToDrafts(publicationId)) {
            EventBus.post(EventPostStatusChange(publicationId, API.STATUS_DRAFT))
            onComplete.invoke()
        }
    }

    fun restoreDeepBlock(publicationId: Long) {
        SplashField()
                .setTitle("Востановить из глубокой блокировки?")
                .setHint(t(API_TRANSLATE.comments_hint))
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(t(API_TRANSLATE.app_restore)) { w, comment ->
                    ApiRequestsSupporter.executeEnabled(w, RPublicationsAdminRestoreDeepBlock(publicationId, comment)) {
                        EventBus.post(EventPublicationDeepBlockRestore(publicationId))
                        ToolsToast.show(t(API_TRANSLATE.app_done))
                    }
                }
                .asSheetShow()
    }

    //
    //  Moderation
    //

    fun setModerationText(vText: ViewText, publication: PublicationModeration) {
        val m = publication.moderation
        var text = ""
        when (m) {
            is ModerationBlock -> {
                val publicationType = if (m.publicationType == API.PUBLICATION_TYPE_POST) tCap(API_TRANSLATE.moderation_publication_post) else if (m.publicationType == API.PUBLICATION_TYPE_COMMENT) tCap(API_TRANSLATE.moderation_publication_comment) else if (m.publicationType == API.PUBLICATION_TYPE_CHAT_MESSAGE) t(API_TRANSLATE.moderation_publication_message) else "null"
                text = tCap(API_TRANSLATE.moderation_card_block_text_main, tSexCap(CampfireConstants.RED, publication.creator.sex, API_TRANSLATE.he_blocked, API_TRANSLATE.she_blocked), publicationType, ControllerLinks.linkToAccount(m.accountName))
                if (m.accountBlockDate > 0) text += "\n" + tCap(API_TRANSLATE.moderation_card_block_text_ban, ToolsDate.dateToString(m.accountBlockDate))
                if (m.lastPublicationsBlocked) text += "\n" + tCap(API_TRANSLATE.moderation_card_block_text_block_last)
            }
            is ModerationTagCreate -> {
                text = tCap(if (m.tagParentId == 0L) API_TRANSLATE.moderation_text_tag_create_category else API_TRANSLATE.moderation_text_tag_create_tag, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_created), t(API_TRANSLATE.she_created)), m.tagName)
                if (m.tagParentId != 0L) text += "\n" + t(API_TRANSLATE.app_category) + ": " + m.tagParentName
            }
            is ModerationTagRemove -> {
                text = tCap(if (m.tagParentId == 0L) API_TRANSLATE.moderation_text_tag_remove_category else API_TRANSLATE.moderation_text_tag_remove_tag, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_remove), t(API_TRANSLATE.she_remove)), m.tagName)
                if (m.tagParentId != 0L) text += "\n" + t(API_TRANSLATE.app_category) + ": " + m.tagParentName
            }
            is ModerationTagChange -> {
                val isTag = m.tagParentId != 0L
                val nameChanged = m.tagName != m.tagOldName
                val imageAdded = m.tagImageId != 0L && m.tagOldImageId == 0L
                val imageChanged = m.tagImageId != 0L && m.tagOldImageId != 0L

                if (isTag && nameChanged && !imageAdded && !imageChanged) text = tCap(API_TRANSLATE.moderation_text_tag_change_tag, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_changed), t(API_TRANSLATE.she_changed)), m.tagOldName, m.tagName)
                if (!isTag && nameChanged && !imageAdded && !imageChanged) text = tCap(API_TRANSLATE.moderation_text_tag_change_category, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_changed), t(API_TRANSLATE.she_changed)), m.tagOldName, m.tagName)
                if (imageChanged) text = tCap(API_TRANSLATE.moderation_text_tag_change_tag_image, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_changed), t(API_TRANSLATE.she_changed)), m.tagName)
                if (imageAdded) text = tCap(API_TRANSLATE.moderation_text_tag_change_tag_add_image, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_add), t(API_TRANSLATE.she_add)), m.tagName)
                if (nameChanged && imageChanged) text = tCap(API_TRANSLATE.moderation_text_tag_change_tag_and_image, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_changed), t(API_TRANSLATE.she_changed)), m.tagOldName, m.tagName)
                if (nameChanged && imageAdded) text = tCap(API_TRANSLATE.moderation_text_tag_change_tag_and_add_image, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_changed), t(API_TRANSLATE.she_changed)), m.tagOldName, m.tagName)


                if (m.tagParentId != 0L) text += "\n" + t(API_TRANSLATE.app_category) + ": " + m.tagParentName
            }
            is ModerationDescription -> {
                text = tCap(API_TRANSLATE.moderation_text_description, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_changed), t(API_TRANSLATE.she_changed)))
                text += "\n${t(API_TRANSLATE.app_text)}: ${m.description}"
            }
            is ModerationGalleryAdd -> {
                text = tCap(API_TRANSLATE.moderation_text_gallery_add, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_add), t(API_TRANSLATE.she_add)))
            }
            is ModerationGalleryRemove -> {
                text = tCap(API_TRANSLATE.moderation_text_gallery_remove, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_remove), t(API_TRANSLATE.she_remove)))
            }
            is ModerationImportant -> {
                if (m.isImportant) text = tCap(API_TRANSLATE.moderation_text_importance_mark, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_mark), t(API_TRANSLATE.she_mark)))
                else text = tCap(API_TRANSLATE.moderation_text_importance_unmark, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_remove), t(API_TRANSLATE.she_remove)))
                text += "\n${t(API_TRANSLATE.app_publication)}: ${ControllerLinks.linkToPost(m.importantPublicationId)}"
            }
            is ModerationTitleImage -> {
                text = tCap(API_TRANSLATE.moderation_text_title_image, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_changed), t(API_TRANSLATE.she_changed)))
            }
            is ModerationLinkAdd -> {
                text = tCap(API_TRANSLATE.moderation_text_link_add, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_add), t(API_TRANSLATE.she_add)))
                text += "\n${t(API_TRANSLATE.app_naming)}: ${m.title}"
                text += "\n${tCap(API_TRANSLATE.app_link)}: ${m.url}"
            }
            is ModerationLinkChange -> {
                text = tCap(API_TRANSLATE.moderation_text_link_change, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_changed), t(API_TRANSLATE.she_changed)))
                text += "\n${t(API_TRANSLATE.app_naming)}: ${m.title}"
                text += "\n${tCap(API_TRANSLATE.app_link)}: ${m.url}"
            }
            is ModerationLinkRemove -> {
                text = tCap(API_TRANSLATE.moderation_text_link_remove, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_remove), t(API_TRANSLATE.she_remove)))
            }
            is ModerationToDrafts -> {
                text = tCap(API_TRANSLATE.moderation_text_to_drafts, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_return), t(API_TRANSLATE.she_return)), ControllerLinks.linkToAccount(m.accountName))
            }
            is ModerationMultilingualNot -> {
                text = tCap(API_TRANSLATE.moderation_text_multilingual_not, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_make), t(API_TRANSLATE.she_make)), ControllerLinks.linkToAccount(m.accountName))
            }
            is ModerationBackgroundImage -> {
                if (m.imageId > 0)
                    text = tCap(API_TRANSLATE.moderation_background_image, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_changed), t(API_TRANSLATE.she_changed)))
                else
                    text = tCap(API_TRANSLATE.moderation_background_image, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_remove), t(API_TRANSLATE.she_remove)))
            }
            is ModerationBackgroundImageSub -> {
                if (m.imageId > 0)
                    text = tCap(API_TRANSLATE.moderation_background_image_sub, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_changed), t(API_TRANSLATE.she_changed)), m.chatName)
                else
                    text = tCap(API_TRANSLATE.moderation_background_image_sub, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_remove), t(API_TRANSLATE.she_remove)), m.chatName)
            }
            is ModerationPostTags -> {
                text = tCap(API_TRANSLATE.moderation_text_post_tags, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_changed), t(API_TRANSLATE.she_changed)), ControllerLinks.linkToPost(m.publicationId))

                if (m.newTags.isNotEmpty()) {
                    text += "\n" + t(API_TRANSLATE.publication_event_fandom_genres_new).capitalize() + " " + m.newTags[0]
                    for (i in 1 until m.newTags.size) text += ", " + m.newTags[i]
                }

                if (m.removedTags.isNotEmpty()) {
                    text += "\n" + t(API_TRANSLATE.publication_event_fandom_genres_remove).capitalize() + " " + m.removedTags[0]
                    for (i in 1 until m.removedTags.size) text += ", " + m.removedTags[i]
                }
            }
            is ModerationNames -> {
                text = tCap(API_TRANSLATE.moderation_text_names, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_changed), t(API_TRANSLATE.she_changed)))

                if (m.newNames.isNotEmpty()) {
                    text += "\n" + t(API_TRANSLATE.publication_event_fandom_genres_new).capitalize() + " " + m.newNames[0]
                    for (i in 1 until m.newNames.size) text += ", " + m.newNames[i]
                }

                if (m.removedNames.isNotEmpty()) {
                    text += "\n" + t(API_TRANSLATE.publication_event_fandom_genres_remove).capitalize() + " " + m.removedNames[0]
                    for (i in 1 until m.removedNames.size) text += ", " + m.removedNames[i]
                }
            }
            is ModerationForgive -> {
                text = tCap(API_TRANSLATE.moderation_text_forgive, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_forgive), t(API_TRANSLATE.she_forgive)), ControllerLinks.linkToAccount(m.accountName))
            }
            is ModerationActivitiesCreate -> {
                text = tCap(API_TRANSLATE.moderation_text_activities_relay_race_create, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_created), t(API_TRANSLATE.she_created)), m.name)
            }
            is ModerationActivitiesChange -> {
                text = tCap(API_TRANSLATE.moderation_text_activities_relay_race_change, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_changed), t(API_TRANSLATE.she_changed)), m.oldName)
                if (m.oldName != m.newName) text += "\n\n"+t(API_TRANSLATE.moderation_text_activities_relay_race_change_name, m.newName)
                if (m.oldDescription != m.newDescription) text += "\n\n"+t(API_TRANSLATE.moderation_text_activities_relay_race_change_description, m.newDescription)
            }
            is ModerationActivitiesRemove -> {
                text = tCap(API_TRANSLATE.moderation_text_activities_relay_race_remove, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_remove), t(API_TRANSLATE.she_remove)), m.name)
            }
            is ModerationChatCreate -> {
                text = tCap(API_TRANSLATE.moderation_text_chat_create, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_created), t(API_TRANSLATE.she_created)), m.name)
            }
            is ModerationChatChange -> {
                text = tCap(API_TRANSLATE.moderation_text_chat_change, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_changed), t(API_TRANSLATE.she_changed)), m.name)
            }
            is ModerationChatRemove -> {
                text = tCap(API_TRANSLATE.moderation_text_chat_remove, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_remove), t(API_TRANSLATE.she_remove)), m.name)
            }
            is ModerationTagMove -> {
                if (m.tagParentId == 0L) text = tCap(API_TRANSLATE.moderation_tag_move_category, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_move), t(API_TRANSLATE.she_move)), m.tagName, m.tagOtherName)
                else text = tCap(API_TRANSLATE.moderation_tag_move_tag, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_move), t(API_TRANSLATE.she_move)), m.tagName, m.tagOtherName)
            }
            is ModerationTagMoveBetweenCategory -> {
                text = tCap(API_TRANSLATE.moderation_tag_move_tag_between_category, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_move), t(API_TRANSLATE.she_move)), m.tagName, m.tagOldName, m.tagNewName)
            }
            is ModerationPinPostInFandom -> {
                if (m.oldPostId < 1)
                    text = tCap(API_TRANSLATE.moderation_pin_post_in_fandom, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_pined), t(API_TRANSLATE.she_pined)), ControllerLinks.linkToPost(m.postId))
                else if (m.postId < 1)
                    text = tCap(API_TRANSLATE.moderation_pin_post_in_fandom_remove, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_unpinned), t(API_TRANSLATE.she_unpinned)), ControllerLinks.linkToPost(m.oldPostId))
                else
                    text = tCap(API_TRANSLATE.moderation_pin_post_in_fandom_replace, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_pined), t(API_TRANSLATE.she_pined)), ControllerLinks.linkToPost(m.postId), ControllerLinks.linkToPost(m.oldPostId))
            }
            is ModerationPostClose -> {
                text = tCap(API_TRANSLATE.moderation_post_close, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_close), t(API_TRANSLATE.she_close)), ControllerLinks.linkToPost(m.postId))
            }
            is ModerationPostCloseNo -> {
                text = tCap(API_TRANSLATE.moderation_post_close_no, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_open), t(API_TRANSLATE.she_open)), ControllerLinks.linkToPost(m.postId))
            }
            is ModerationRubricChangeName -> {
                text = tCap(API_TRANSLATE.moderation_rubric_change_name, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_changed), t(API_TRANSLATE.she_changed)), m.rubricOldName, m.rubricNewName)
            }
            is ModerationRubricChangeOwner -> {
                text = tCap(API_TRANSLATE.moderation_rubric_change_owner, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_changed), t(API_TRANSLATE.she_changed)), m.rubricName, m.oldOwnerName, m.newOwnerName)
            }
            is ModerationRubricCreate -> {
                text = tCap(API_TRANSLATE.moderation_rubric_crete, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_created), t(API_TRANSLATE.she_created)), m.rubricName, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_assign), t(API_TRANSLATE.she_assign)), m.ownerName)
            }
            is ModerationRubricRemove -> {
                text = tCap(API_TRANSLATE.moderation_rubric_remove, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_remove), t(API_TRANSLATE.she_remove)), m.rubricName)
            }
            is ModerationRubricFandomMove -> {
                text = tCap(API_TRANSLATE.moderation_rubric_move_fandom, ToolsResources.sex(publication.creator.sex, t(API_TRANSLATE.he_move), t(API_TRANSLATE.she_move)), m.rubricName, m.srcFandomName, m.destFandomName)
            }
        }

        if (publication.moderation != null)
            if (!ToolsText.empty(publication.moderation!!.comment)) text += "\n\n" + tCap(API_TRANSLATE.moderation_card_block_text_comment, publication.moderation!!.comment)
        vText.text = text
        ControllerLinks.makeLinkable(vText)


        when (m) {
            is ModerationRubricChangeName -> {
                ToolsView.addLink(vText, m.rubricNewName) { SRubricPosts.instance(m.rubricId, Navigator.TO) }
            }
            is ModerationRubricChangeOwner -> {
                ToolsView.addLink(vText, m.rubricName) { SRubricPosts.instance(m.rubricId, Navigator.TO) }
                ToolsView.addLink(vText, m.oldOwnerName) { SProfile.instance(m.oldOwnerId, Navigator.TO) }
                ToolsView.addLink(vText, m.newOwnerName) { SProfile.instance(m.newOwnerId, Navigator.TO) }
            }
            is ModerationRubricCreate -> {
                ToolsView.addLink(vText, m.rubricName) { SRubricPosts.instance(m.rubricId, Navigator.TO) }
                ToolsView.addLink(vText, m.ownerName) { SProfile.instance(m.ownerId, Navigator.TO) }
            }
            is ModerationChatCreate -> {
                ToolsView.addLink(vText, m.name) { SChat.instance(ChatTag(API.CHAT_TYPE_FANDOM_SUB, m.chatId, 0), 0, false, Navigator.TO) }
            }
            is ModerationChatChange -> {
                ToolsView.addLink(vText, m.name) { SChat.instance(ChatTag(API.CHAT_TYPE_FANDOM_SUB, m.chatId, 0), 0, false, Navigator.TO) }
            }
            is ModerationBackgroundImageSub -> {
                ToolsView.addLink(vText, m.chatName) { SChat.instance(ChatTag(API.CHAT_TYPE_FANDOM_SUB, m.chatId, 0), 0, false, Navigator.TO) }
            }
            is ModerationActivitiesCreate -> {
                ToolsView.addLink(vText, m.name) { SRelayRaceInfo.instance(m.activityId, Navigator.TO) }
            }
            is ModerationActivitiesChange -> {
                ToolsView.addLink(vText, m.oldName) { SRelayRaceInfo.instance(m.activityId, Navigator.TO) }
                ToolsView.addLink(vText, m.newName) { SRelayRaceInfo.instance(m.activityId, Navigator.TO) }
            }
        }
    }

}
