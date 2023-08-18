package com.sayzen.campfiresdk.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.LinkParsed
import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.requests.accounts.RAccountsGet
import com.dzen.campfire.api.requests.chat.RChatGet
import com.dzen.campfire.api.requests.comments.RCommentGet
import com.dzen.campfire.api.requests.fandoms.RFandomsGet
import com.dzen.campfire.api.requests.post.RPostGet
import com.dzen.campfire.api.requests.quests.RQuestsGet
import com.dzen.campfire.api.requests.stickers.RStickersPacksGetInfo
import com.sup.dev.java.classes.items.Item3

object ControllerCampfireObjects {

    private val cash: HashMap<String, Item3<String, String, Long>> = HashMap()
    private val inProgress: HashMap<String, ArrayList<(String, String, Long) -> Unit>> = HashMap()

    fun load(link: LinkParsed, onComplete: (String, String, Long) -> Unit) {

        if (cash.containsKey(link.linkRaw)) {
            val get = cash.get(link.linkRaw)!!
            onComplete.invoke(get.a1, get.a2, get.a3)
            return
        }

        if (inProgress.containsKey(link.linkRaw)) {
            val get = inProgress.get(link.linkRaw)
            get!!.add(onComplete)
            return
        }

        val list = ArrayList<(String, String, Long) -> Unit>()
        inProgress[link.linkRaw] = list
        list.add(onComplete)

        when {
            link.isLinkToAccount() -> loadAccount(link)
            link.isLinkToPost() -> loadPost(link)
            link.isLinkToChat() -> loadChat(link)
            link.isLinkToFandom() -> loadFandom(link)
            link.isLinkToStickersPack() -> loadStickersPack(link)
            link.isLinkToComment() -> loadComment(link)
            link.isLinkToQuest() -> loadQuest(link)
            else -> onError(link)
        }
    }

    private fun loadAccount(link: LinkParsed) {

        val id = link.getLongParamOrZero(0)
        val name = if (link.link.startsWith("@")) {
            if (link.link.length < 3) "" else link.link.removePrefix("@").replace("_", "")
        } else {
            link.params[0]
        }

        RAccountsGet(id, name)
                .onComplete { onComplete(link, it.account.name, t(API_TRANSLATE.app_user), it.account.imageId) }
                .onError { onError(link) }
                .send(api)
    }

    private fun loadPost(link: LinkParsed) {
        val id = link.getLongParamOrZero(0)

        RPostGet(id)
                .onComplete { onComplete(link, it.publication.fandom.name, t(API_TRANSLATE.app_post), it.publication.fandom.imageId) }
                .onError { onError(link) }
                .send(api)
    }

    private fun loadChat(link: LinkParsed) {
        val targetId = link.getLongParamOrZero(0)
        val targetSubId = link.getLongParamOrZero(1)

        RChatGet(ChatTag(API.CHAT_TYPE_FANDOM_ROOT, targetId, targetSubId), 0)
                .onComplete { onComplete(link, it.chat.customName, t(API_TRANSLATE.app_chat), it.chat.customImageId) }
                .onError { onError(link) }
                .send(api)
    }

    private fun loadFandom(link: LinkParsed) {
        val fandomId = link.getLongParamOrZero(0)
        val languageId = link.getLongParamOrZero(1)

        RFandomsGet(fandomId, languageId, ControllerApi.getLanguageId())
                .onComplete { onComplete(link, it.fandom.name, t(API_TRANSLATE.app_fandom), it.fandom.imageId) }
                .onError { onError(link) }
                .send(api)
    }

    private fun loadStickersPack(link: LinkParsed) {
        val id = link.getLongParamOrZero(0)

        RStickersPacksGetInfo(id, 0)
                .onComplete { onComplete(link, it.stickersPack.name, t(API_TRANSLATE.app_stickers), it.stickersPack.imageId) }
                .onError { onError(link) }
                .send(api)
    }

    private fun loadQuest(link: LinkParsed) {
        val publicationId = link.getLongParamOrZero(0)

        RQuestsGet(publicationId)
            .onComplete { onComplete(link, it.questDetails.title, t(API_TRANSLATE.quest), it.questDetails.creator.imageId) }
            .onError { onError(link) }
            .send(api)
    }

    private fun loadComment(link: LinkParsed) {
        val publicationId = link.getLongParamOrZero(0)
        val commentId = link.getLongParamOrZero(1)

        RCommentGet(publicationId, commentId)
                .onComplete { onComplete(link, ControllerPublications.getMaskForComment(it.comment.creator.name + ": " + it.comment.text, it.comment.type), t(API_TRANSLATE.app_comment), it.comment.fandom.imageId) }
                .onError { onError(link) }
                .send(api)
    }

    private fun onComplete(link: LinkParsed, title: String, subtitle: String, image: Long) {
        cash.put(link.linkRaw, Item3(title, subtitle, image))
        val callbacks = inProgress.get(link.linkRaw)
        if (callbacks != null) {
            for (i in callbacks) i.invoke(title, subtitle, image)
            inProgress.remove(link.linkRaw)
        }
    }

    private fun onError(link: LinkParsed) {
        val callbacks = inProgress.get(link.linkRaw)
        if (callbacks != null) {
            for (i in callbacks) i.invoke(t(API_TRANSLATE.post_page_campfire_object_error), t(API_TRANSLATE.app_error), 0)
            inProgress.remove(link.linkRaw)
        }
    }

}