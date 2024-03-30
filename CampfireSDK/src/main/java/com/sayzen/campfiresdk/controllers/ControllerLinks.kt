package com.sayzen.campfiresdk.controllers

import android.text.util.Linkify
import android.view.Gravity
import android.widget.TextView
import androidx.core.text.util.LinkifyCompat
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.models.publications.PublicationComment
import com.dzen.campfire.api.requests.achievements.RAchievementsOnFinish
import com.dzen.campfire.api.requests.project.RProjectGetEvents
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.animations.*
import com.sayzen.campfiresdk.screens.account.profile.SProfile
import com.sayzen.campfiresdk.screens.account.stickers.SStickersView
import com.sayzen.campfiresdk.screens.achievements.SAchievements
import com.sayzen.campfiresdk.screens.achievements.daily_task.followLink
import com.sayzen.campfiresdk.screens.activities.support.SDonate
import com.sayzen.campfiresdk.screens.activities.user_activities.relay_race.SRelayRaceInfo
import com.sayzen.campfiresdk.screens.chat.SChat
import com.sayzen.campfiresdk.screens.comments.SComments
import com.sayzen.campfiresdk.screens.fandoms.moderation.view.SModerationView
import com.sayzen.campfiresdk.screens.fandoms.rubrics.SRubricPosts
import com.sayzen.campfiresdk.screens.fandoms.view.SFandom
import com.sayzen.campfiresdk.screens.other.about.SAboutCreators
import com.sayzen.campfiresdk.screens.other.gallery.SGallery
import com.sayzen.campfiresdk.screens.other.minigame.SMagic
import com.sayzen.campfiresdk.screens.other.minigame.SMinigame
import com.sayzen.campfiresdk.screens.other.rules.SRulesModerators
import com.sayzen.campfiresdk.screens.other.rules.SRulesUser
import com.sayzen.campfiresdk.screens.post.search.SPostsSearch
import com.sayzen.campfiresdk.screens.post.view.SPost
import com.sayzen.campfiresdk.screens.quests.SQuest
import com.sayzen.campfiresdk.screens.translates.STranslates
import com.sayzen.campfiresdk.screens.wiki.SWikiArticleView
import com.sayzen.campfiresdk.screens.wiki.SWikiList
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsIntent
import com.sup.dev.android.views.splash.SplashAlert
import com.sup.dev.android.views.views.ViewText
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.libs.debug.info
import com.sup.dev.java.tools.ToolsMath
import com.sup.dev.java.tools.ToolsText
import com.sup.dev.java.tools.ToolsThreads
import java.util.regex.Pattern

object ControllerLinks {
    private fun getRawLink(link: CharSequence): String {
        return (if (link.startsWith("@") || link.startsWith("#")) {
            link.substring(1)
        } else if (link.startsWith(API.DOMEN)) {
            link.substring(API.DOMEN.length)
        } else if (link.startsWith(API.DOMEN_DL)) {
            link.substring(API.DOMEN_DL.length)
        } else {
            link.toString()
        }).replace('_', '-').removeSuffix("-")
    }

    fun parseLink(link: String): Boolean {
        try {
            val t = getRawLink(link)

            val s1 = t.split('-')
            val linkV = s1[0]
            val params: List<String> = if (s1.size > 1) s1.subList(1, s1.size) else emptyList()

            var passed = true
            when (t) {
                API.LINK_ABOUT.link -> Navigator.to(
                    SWikiList(
                        API.FANDOM_CAMPFIRE_ID,
                        ControllerApi.getLanguageId(),
                        0,
                        ""
                    )
                )

                API.LINK_DONATE.link -> SDonate.instance(Navigator.TO)
                API.LINK_DONATE_MAKE.link -> SplashAlert()
                    .setOnEnter(t(API_TRANSLATE.app_ok))
                    .setText(t(API_TRANSLATE.donates_restricted))
                    .asSheetShow()

                API.LINK_RULES_MODER.link -> Navigator.to(SRulesModerators())
                API.LINK_RULES_USER.link -> Navigator.to(SRulesUser())
                API.LINK_TRANSLATES.link -> Navigator.to(STranslates())
                API.LINK_RULES_GALLERY.link -> Navigator.to(SGallery())
                API.LINK_CREATORS.link -> Navigator.to(SAboutCreators())
                API.LINK_BOX_WITH_FIREWORKS.link -> {
                    ControllerScreenAnimations.fireworks()
                    ToolsThreads.main(10000) { RAchievementsOnFinish(API.ACHI_FIREWORKS.index).send(api) }
                }

                API.LINK_BOX_WITH_SUMMER.link -> ControllerScreenAnimations.addAnimationWithClear(DrawAnimationSummer())
                API.LINK_BOX_WITH_AUTUMN.link -> ControllerScreenAnimations.addAnimationWithClear(DrawAnimationAutumn())
                API.LINK_BOX_WITH_WINTER.link -> ControllerScreenAnimations.addAnimationWithClear(DrawAnimationWinter())
                API.LINK_BOX_WITH_BOMB.link -> ControllerScreenAnimations.addAnimationWithClear(DrawAnimationBomb())
                API.LINK_BOX_WITH_CRASH.link -> ToolsThreads.main(true) { ("Do Crash").toInt() }
                API.LINK_BOX_WITH_SNOW.link -> ControllerScreenAnimations.addAnimationWithClear(DrawAnimationSnow(100))
                API.LINK_BOX_WITH_MINIGAME.link -> SMinigame.instance(Navigator.TO)
                API.LINK_BOX_WITH_MAGIC_SCREEN_X2.link -> Navigator.to(SMagic(2f))
                API.LINK_BOX_WITH_MAGIC_SCREEN.link -> Navigator.to(SMagic())
                API.LINK_BOX_WITH_MAGIC_X2.link -> ControllerScreenAnimations.addAnimationWithClear(
                    DrawAnimationMagic(
                        2f
                    )
                )

                API.LINK_BOX_WITH_MAGIC.link -> ControllerScreenAnimations.addAnimationWithClear(DrawAnimationMagic())
                API.LINK_BOX_WITH_GOOSE.link -> ControllerScreenAnimations.addAnimationWithClear(DrawAnimationGoose())
                API.LINK_BOX_WITH_CONFETTI.link -> ControllerScreenAnimations.addAnimationWithClear(
                    DrawAnimationConfetti()
                )
                else -> passed = false
            }

            if (passed) return true

            if (linkV == "box" && params.getOrNull(0) == "with" && params.getOrNull(1) == "box") {
                var counter = 0
                for (i in params) if (i == "box") counter++
                ControllerScreenAnimations.box(ToolsMath.max(1, counter + 1))
                return true
            }

            when (linkV) {
                API.LINK_STICKER.link -> SStickersView.instanceBySticker(params[0].toLong(), Navigator.TO)
                API.LINK_STICKERS_PACK.link -> {
                    if (params.size == 1) SStickersView.instance(params[0].toLong(), Navigator.TO)
                    if (params.size == 2) Navigator.to(SComments(params[0].toLong(), params[1].toLong()))
                }
                API.LINK_POST.link -> {
                    if (params.size == 1) SPost.instance(params[0].toLong(), 0, Navigator.TO)
                    if (params.size == 2) SPost.instance(params[0].toLong(), params[1].toLong(), Navigator.TO)
                }
                API.LINK_FANDOM.link -> {
                    if (params.size == 1) SFandom.instance(params[0].toLong(), 0, Navigator.TO)
                    if (params.size == 2) SFandom.instance(params[0].toLong(), params[1].toLong(), Navigator.TO)
                }
                API.LINK_PROFILE_ID.link -> SProfile.instance(params[0].toLong(), Navigator.TO)
                API.LINK_TAG_PROFILE_NAME -> SProfile.instance(params[0], Navigator.TO)
                API.LINK_TAG.link -> SPostsSearch.instance(params[0].toLong(), Navigator.TO)
                API.LINK_MODERATION.link -> {
                    if (params.size == 1) SModerationView.instance(params[0].toLong(), 0, Navigator.TO)
                    if (params.size == 2) SModerationView.instance(params[0].toLong(), params[1].toLong(), Navigator.TO)
                }
                API.LINK_CHAT.link -> {
                    if (params.size == 1) SChat.instance(ChatTag(API.CHAT_TYPE_FANDOM_ROOT, params[0].toLong(), 0), 0, true, Navigator.TO)
                    if (params.size == 2) SChat.instance(ChatTag(API.CHAT_TYPE_FANDOM_ROOT, params[0].toLong(), params[1].toLong()), 0, true, Navigator.TO)
                }
                API.LINK_CONF.link -> {
                    if (params.size == 1) SChat.instance(ChatTag(API.CHAT_TYPE_CONFERENCE, params[0].toLong(), 0), 0, true, Navigator.TO)
                    if (params.size == 2) SChat.instance(ChatTag(API.CHAT_TYPE_CONFERENCE, params[0].toLong(), params[1].toLong()), 0, true, Navigator.TO)
                }
                API.LINK_WIKI_FANDOM.link -> SWikiList.instanceFandomId(params[0].toLong(), Navigator.TO)
                API.LINK_WIKI_SECTION.link -> SWikiList.instanceItemId(params[0].toLong(), Navigator.TO)
                API.LINK_WIKI_ARTICLE.link -> SWikiArticleView.instance(params[0].toLong(), Navigator.TO)
                API.LINK_FANDOM_CHAT.link -> SChat.instance(ChatTag(API.CHAT_TYPE_FANDOM_SUB, params[0].toLong(), 0), 0, false, Navigator.TO)
                API.LINK_ACTIVITY.link -> SRelayRaceInfo.instance(params[0].toLong(), Navigator.TO)
                API.LINK_RUBRIC.link -> SRubricPosts.instance(params[0].toLong(), Navigator.TO)
                API.LINK_QUEST.link -> {
                    if (params.size == 1) SQuest.instance(params[0].toLong(), Navigator.TO)
                    if (params.size == 2) SQuest.instance(params[0].toLong(), params[1].toLong(), Navigator.TO)
                }
                API.LINK_EVENT.link -> {
                    ApiRequestsSupporter.executeProgressDialog(RProjectGetEvents()) { r ->
                        val event = r.events.find { it.id == params[0] }
                        if (event?.url != null) {
                            event.followLink()
                        } else {
                            SAchievements.instance(false, Navigator.TO)
                        }
                    }
                }
                else -> {
                    if (ToolsText.isValidUsername(t)) {
                        SProfile.instance(t, Navigator.TO)
                        return true
                    }
                    info("ControllerExecutorLinks link wasn't found [$link][$t]")
                    return false
                }
            }
            return true
        } catch (e: Throwable) {
            err(e)
            return false
        }
    }

    fun isCorrectLink(link: CharSequence): Boolean {
        try {
            val t = getRawLink(link)

            val s1 = t.split('-')
            val linkV = s1[0]
            val params: List<String> = if (s1.size > 1) s1.subList(1, s1.size) else emptyList()

            val v = when (t) {
                API.LINK_ABOUT.link -> true
                API.LINK_DONATE.link -> true
                API.LINK_DONATE_MAKE.link -> true
                API.LINK_RULES_USER.link -> true
                API.LINK_TRANSLATES.link -> true
                API.LINK_RULES_MODER.link -> true
                API.LINK_RULES_GALLERY.link -> true
                API.LINK_CREATORS.link -> true
                API.LINK_BOX_WITH_FIREWORKS.link -> true
                API.LINK_BOX_WITH_SUMMER.link -> true
                API.LINK_BOX_WITH_AUTUMN.link -> true
                API.LINK_BOX_WITH_WINTER.link -> true
                API.LINK_BOX_WITH_BOMB.link -> true
                API.LINK_BOX_WITH_CRASH.link -> true
                API.LINK_BOX_WITH_SNOW.link -> true
                API.LINK_BOX_WITH_MINIGAME.link -> true
                API.LINK_BOX_WITH_MAGIC.link -> true
                API.LINK_BOX_WITH_MAGIC_SCREEN.link -> true
                API.LINK_BOX_WITH_MAGIC_X2.link -> true
                API.LINK_BOX_WITH_MAGIC_SCREEN_X2.link -> true
                API.LINK_BOX_WITH_GOOSE.link -> true
                API.LINK_BOX_WITH_CONFETTI.link -> true
                else -> false
            }

            if (v) return true

            if (linkV == "box" && params.getOrNull(0) == "with" && params.getOrNull(1) == "box") {
                return true
            }

            return when (linkV) {
                API.LINK_STICKER.link -> true
                API.LINK_PROFILE_ID.link -> true
                API.LINK_TAG_PROFILE_NAME -> true
                API.LINK_TAG.link -> true
                API.LINK_WIKI_FANDOM.link -> true
                API.LINK_WIKI_SECTION.link -> true
                API.LINK_WIKI_ARTICLE.link -> true
                API.LINK_FANDOM_CHAT.link -> true
                API.LINK_ACTIVITY.link -> true
                API.LINK_RUBRIC.link -> true
                API.LINK_STICKERS_PACK.link -> params.size == 1 || params.size == 2
                API.LINK_POST.link -> params.size == 1 || params.size == 2
                API.LINK_FANDOM.link -> params.size == 1 || params.size == 2
                API.LINK_MODERATION.link -> params.size == 1 || params.size == 2
                API.LINK_CHAT.link -> params.size == 1 || params.size == 2
                API.LINK_CONF.link -> params.size == 1 || params.size == 2
                API.LINK_QUEST.link -> params.size == 1 || params.size == 2
                API.LINK_EVENT.link -> params.size == 1
                else -> {
                    if (ToolsText.isValidUsername(t)) {
                        true
                    } else {
                        info("ControllerExecutorLinks link wasn't found [$link][$t]")
                        false
                    }
                }

            }
        } catch (e: Throwable) {
            err(e)
            return false
        }
    }

    fun openLink(link: String) {
        if (parseLink(link)) return
        SplashAlert()
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .setOnEnter(t(API_TRANSLATE.app_open)) { ToolsIntent.openLink(link) }
                .setText(t(API_TRANSLATE.message_link))
                .setTextGravity(Gravity.CENTER)
                .setTitleImage(R.drawable.ic_security_white_48dp)
                .setTitleImageBackgroundRes(R.color.blue_700)
                .asSheetShow()
    }

    fun linkToAccount(name: String) = API.LINK_PROFILE_NAME + name
    fun linkToAccount(id: Long) = API.LINK_PROFILE_ID.asWeb() + id
    fun linkToFandom(fandomId: Long) = API.LINK_FANDOM.asWeb() + fandomId
    fun linkToFandom(fandomId: Long, languageId: Long) = API.LINK_FANDOM.asWeb() + fandomId + "_" + languageId
    fun linkToPost(postId: Long) = API.LINK_POST.asWeb() + postId
    fun linkToModeration(moderationId: Long) = API.LINK_MODERATION.asWeb() + moderationId
    fun linkToWikiFandomId(fandomId: Long) = API.LINK_WIKI_FANDOM.asWeb() + fandomId
    fun linkToWikiItemId(itemId: Long) = API.LINK_WIKI_SECTION.asWeb() + itemId
    fun linkToWikiArticle(itemId: Long) = API.LINK_WIKI_ARTICLE.asWeb() + itemId
    fun linkToFandomChat(chatId: Long) = API.LINK_FANDOM_CHAT.asWeb() + chatId
    fun linkToActivity(activityId: Long) = API.LINK_ACTIVITY.asWeb() + activityId
    fun linkToRubric(rubricId: Long) = API.LINK_RUBRIC.asWeb() + rubricId
    fun linkToSticker(id: Long) = API.LINK_STICKER.asWeb() + id
    fun linkToStickersPack(id: Long) = API.LINK_STICKERS_PACK.asWeb() + id
    fun linkToPostComment(parentPublicationId: Long, commentId: Long) = API.LINK_POST.asWeb() + parentPublicationId + "_" + commentId
    fun linkToModerationComment(parentPublicationId: Long, commentId: Long) = API.LINK_MODERATION.asWeb() + parentPublicationId + "_" + commentId
    fun linkToStickersComment(parentPublicationId: Long, commentId: Long) = API.LINK_STICKERS_PACK.asWeb() + parentPublicationId + "_" + commentId
    fun linkToChat(fandomId: Long) = API.LINK_CHAT.asWeb() + fandomId
    fun linkToChat(fandomId: Long, languageId: Long) = API.LINK_CHAT.asWeb() + fandomId + "_" + languageId
    fun linkToChatMessage(messageId: Long, fandomId: Long, languageId: Long) = API.LINK_CHAT.asWeb() + fandomId + "_" + languageId + "_" + messageId
    fun linkToConf(chatId: Long) = API.LINK_CONF.asWeb() + chatId
    fun linkToConfMessage(messageId: Long, chatId: Long) = API.LINK_CONF.asWeb() + chatId + "_" + messageId
    fun linkToEvent(eventId: String) = API.LINK_EVENT.asWeb() + eventId
    fun linkToTag(tagId: Long) = API.LINK_TAG.asWeb() + tagId
    fun linkToComment(comment: PublicationComment) = linkToComment(comment.id, comment.parentPublicationType, comment.parentPublicationId)
    fun linkToComment(commentId: Long, publicationType: Long, publicationId: Long): String {
        return when (publicationType) {
            API.PUBLICATION_TYPE_POST -> linkToPostComment(publicationId, commentId)
            API.PUBLICATION_TYPE_MODERATION -> linkToModerationComment(publicationId, commentId)
            API.PUBLICATION_TYPE_STICKERS_PACK -> linkToStickersComment(publicationId, commentId)
            API.PUBLICATION_TYPE_QUEST -> linkToQuestComment(publicationId, commentId)
            else -> ""
        }
    }
    fun linkToQuest(questId: Long) = API.LINK_QUEST.asWeb() + questId
    fun linkToQuestComment(questId: Long, commentId: Long) = API.LINK_QUEST.asWeb() + questId + "_" + commentId

    fun makeLinkable(vText: ViewText) {
        // fixme: don't do this maybe?
        vText.text = vText.text.toString()
            .replace(API.LINK_PROFILE_NAME, "@")
            .replace(API.DOMEN, "@")
        ControllerApi.makeTextHtml(vText)
        LinkifyCompat.addLinks(vText, Linkify.WEB_URLS)
        linkifyShort(vText)
    }

    private val linkRegex by lazy { Pattern.compile("[#@]([A-Za-z0-9-_#]+)") }

    fun linkifyShort(vText: TextView) {
        LinkifyCompat.addLinks(
            vText,
            linkRegex,
            API.DOMEN,
            null,
            { link, start, end -> isCorrectLink(link.subSequence(start, end)) },
            { match, _ -> "https://bonfire.moe/r/${match.group(1)}" }
        )
    }

    fun getAnswerText(answerName: String, text: String): String {
        val myName = ControllerApi.account.getName()
        return if (text.startsWith("$myName, ")) {
            "{bonfire $myName}, ${text.substring(myName.length + 2)}"
        } else if (answerName.isNotEmpty() && text.startsWith("$answerName, ")) {
            "{90A4AE $answerName}, ${text.substring(answerName.length + 2)}"
        } else {
            text
        }
    }

    fun getQuoteText(quoteCreatorName: String, quoteText: String): String {
        return if (quoteCreatorName.isEmpty() || !quoteText.startsWith("$quoteCreatorName: ")) {
            quoteText
        } else {
            val color = if (quoteCreatorName == ControllerApi.account.getName()) "FF6D00" else "90A4AE"
            "{$color $quoteCreatorName}: " + quoteText.substring(quoteCreatorName.length + 2)
        }
    }
}
