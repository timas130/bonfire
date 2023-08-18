package com.sayzen.campfiresdk.controllers

import android.text.util.Linkify
import android.view.Gravity
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.models.publications.PublicationComment
import com.dzen.campfire.api.requests.achievements.RAchievementsOnFinish
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.animations.*
import com.sayzen.campfiresdk.screens.account.profile.SProfile
import com.sayzen.campfiresdk.screens.account.stickers.SStickersView
import com.sayzen.campfiresdk.screens.activities.support.SDonate
import com.sayzen.campfiresdk.screens.activities.support.SDonateMake
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
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsIntent
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.splash.SplashAlert
import com.sup.dev.android.views.views.ViewText
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.libs.debug.info
import com.sup.dev.java.tools.ToolsMath
import com.sup.dev.java.tools.ToolsText
import com.sup.dev.java.tools.ToolsThreads
import java.util.regex.Pattern

object ControllerLinks {

    var LINKS_ENABLED = true

    fun parseLink(link: String): Boolean {
        if (!LINKS_ENABLED) return false
        try {

            var t: String
            if (link.startsWith("@")) {
                t = link.substring(1)
            } else if (link.startsWith(API.DOMEN)) {
                t = link.substring(API.DOMEN.length)
            } else if (link.startsWith(API.DOMEN_OLD)) {
                t = link.substring(API.DOMEN_OLD.length)
            } else {
                t = link.substring("http://@".length)
                t = t.replace("_", "-")
            }

            val s1 = t.split("-")
            val linkV = s1[0]
            val params: List<String> = if (s1.size > 1) s1[1].split("_") else emptyList()

            when (linkV) {
                API.LINK_ABOUT.link -> Navigator.to(Navigator.to(SWikiList(API.FANDOM_CAMPFIRE_ID, ControllerApi.getLanguageId(), 0, "")))
                API.LINK_DONATE.link -> SDonate.instance(Navigator.TO)
                API.LINK_DONATE_MAKE.link -> Navigator.to(SDonateMake())
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
                API.LINK_BOX_WITH_MAGIC_X2.link -> ControllerScreenAnimations.addAnimationWithClear(DrawAnimationMagic(2f))
                API.LINK_BOX_WITH_MAGIC.link -> ControllerScreenAnimations.addAnimationWithClear(DrawAnimationMagic())
                API.LINK_BOX_WITH_GOOSE.link -> ControllerScreenAnimations.addAnimationWithClear(DrawAnimationGoose())
                API.LINK_BOX_WITH_CONFETTI.link -> ControllerScreenAnimations.addAnimationWithClear(DrawAnimationConfetti())
                API.LINK_BOX_WITH_BOX.link -> {
                    var counter = 0
                    for (i in params) if (i == "box") counter++
                    ControllerScreenAnimations.box(ToolsMath.max(1, counter + 1))
                }
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
                API.LINK_QUEST.link -> SQuest.instance(params[0].toLong(), Navigator.TO)
                else -> {
                    if (link.startsWith("@") && ToolsText.isOnly(t, API.ACCOUNT_LOGIN_CHARS)) {
                        SProfile.instance(t, Navigator.TO)
                        return true
                    }
                    info("ControllerExecutorLinks link was't found [$link]")
                    return false
                }

            }
            return true

        } catch (e: Throwable) {
            err(e)
            return false
        }
    }

    fun isCorrectLink(link: String): Boolean {
        if (!LINKS_ENABLED) return false
        try {

            var t: String
            if (link.startsWith("@")) {
                t = link.substring(1)
            } else if (link.startsWith(API.DOMEN)) {
                t = link.substring(API.DOMEN.length)
            } else if (link.startsWith(API.DOMEN_OLD)) {
                t = link.substring(API.DOMEN_OLD.length)
            } else {
                t = link.substring("http://@".length)
                t = t.replace("_", "-")
            }

            val s1 = t.split("-")
            val linkV = s1[0]
            val params: List<String> = if (s1.size > 1) s1[1].split("_") else emptyList()

            return when (linkV) {
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
                API.LINK_BOX_WITH_BOX.link -> true
                API.LINK_BOX_WITH_MAGIC.link -> true
                API.LINK_BOX_WITH_MAGIC_SCREEN.link -> true
                API.LINK_BOX_WITH_MAGIC_X2.link -> true
                API.LINK_BOX_WITH_MAGIC_SCREEN_X2.link -> true
                API.LINK_BOX_WITH_GOOSE.link -> true
                API.LINK_BOX_WITH_CONFETTI.link -> true
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
                else -> {
                    if (link.startsWith("@") && ToolsText.isOnly(t, API.ACCOUNT_LOGIN_CHARS)) {
                        return true
                    }
                    info("ControllerExecutorLinks link was't found [$link]")
                    return false
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
    fun linkToEvent(eventId: Long) = API.LINK_EVENT.asWeb() + eventId
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

    fun makeLinkable(vText: ViewText, onReplace: () -> Unit = {}) {

        for (i in API.LINKS_ARRAY) replaceLinkable(vText, i.asLink(), i.asWeb())
        for (i in API.LINKS_ARRAY) replaceLinkable(vText, i.asLink(), i.asWebOld())
        replaceLinkable(vText, API.LINK_SHORT_PROFILE, API.LINK_PROFILE_NAME)


        onReplace.invoke()
        ControllerApi.makeTextHtml(vText)

        for (i in API.LINKS_ARRAY)
            if (i == API.LINK_BOX_WITH_BOX) makeLinkable(vText, i.asLink(), i.asWeb(), "([_with_box]*)")
            else if (i.isInnerLink) makeLinkableInner(vText, i.asLink(), i.asWeb())
            else makeLinkable(vText, i.asLink(), i.asWeb())

        makeLinkable(vText, API.LINK_SHORT_PROFILE, API.LINK_PROFILE_NAME, "([A-Za-z0-9#]+)")

        Linkify.addLinks(vText, Pattern.compile("${API.LINK_SHORT_PROFILE_SECOND}([A-Za-z0-9#]+)"), API.LINK_PROFILE_NAME,
                {s,i1,i2 ->
                    if(i1 == 0) return@addLinks true
                    if(s[i1-1] != ' ' && s[i1-1] != '.' && s[i1-1] != '!' && s[i1-1] != '?' && s[i1-1] != ',') return@addLinks false
                    return@addLinks true
                },
                { _, url ->
                    API.LINK_PROFILE_NAME + url.substring(API.LINK_SHORT_PROFILE_SECOND.length)
                })

        if (ControllerHoliday.isBirthday()) {
            ToolsView.addLink(vText, "День рождения") { ControllerScreenAnimations.parseHolidayClick() }
            ToolsView.addLink(vText, "Днём рождения") { ControllerScreenAnimations.parseHolidayClick() }
            ToolsView.addLink(vText, "Birthday") { ControllerScreenAnimations.parseHolidayClick() }
            ToolsView.addLink(vText, "день рождения") { ControllerScreenAnimations.parseHolidayClick() }
            ToolsView.addLink(vText, "днём рождения") { ControllerScreenAnimations.parseHolidayClick() }
            ToolsView.addLink(vText, "birthday") { ControllerScreenAnimations.parseHolidayClick() }
        }

        ToolsView.makeLinksClickable(vText)
    }

    private fun replaceLinkable(vText: TextView, short: String, link: String) {
        vText.text = vText.text.toString().replace(link, short)
    }

    private fun makeLinkableInner(vText: TextView, short: String, link: String) {
        makeLinkable(vText, short, link, "(?!\\_)")
    }

    private fun makeLinkable(vText: TextView, short: String, link: String) {
        makeLinkable(vText, short, link, "([A-Za-z0-9_-]+)")
    }

    private fun makeLinkable(vText: TextView, short: String, link: String, spec: String) {
        Linkify.addLinks(vText, Pattern.compile("$short$spec"), link, null, { _, url ->
            link + url.substring(short.length)
        })
    }


}