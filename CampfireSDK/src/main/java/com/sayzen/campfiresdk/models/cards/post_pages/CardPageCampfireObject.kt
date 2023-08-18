package com.sayzen.campfiresdk.models.cards.post_pages

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.LinkParsed
import com.dzen.campfire.api.models.chat.ChatTag
import com.dzen.campfire.api.models.publications.PagesContainer
import com.dzen.campfire.api.models.publications.post.PageCampfireObject
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerCampfireObjects
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.screens.account.profile.SProfile
import com.sayzen.campfiresdk.screens.account.stickers.SStickersView
import com.sayzen.campfiresdk.screens.chat.SChat
import com.sayzen.campfiresdk.screens.fandoms.view.SFandom
import com.sayzen.campfiresdk.screens.post.view.SPost
import com.sayzen.campfiresdk.screens.quests.SQuest
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.views.ViewAvatarTitle

class CardPageCampfireObject(
        pagesContainer: PagesContainer?,
        page: PageCampfireObject
) : CardPage(R.layout.card_page_campfire_object, pagesContainer, page) {

    override fun bindView(view: View) {
        super.bindView(view)
        val page = this.page as PageCampfireObject

        val vPageAvatar: ViewAvatarTitle = view.findViewById(R.id.vPageAvatarTitle_Object)
        val vPageTouch: View = view.findViewById(R.id.vPageTouch)

        vPageAvatar.tag = this
        vPageAvatar.vAvatar.vImageView.setImageResource(R.color.focus)
        vPageAvatar.setTitle(t(API_TRANSLATE.app_loading))
        vPageAvatar.setSubtitle("")
        val link = LinkParsed(page.link)
        ControllerCampfireObjects.load(link) { title, subtitle, imageId ->
            if (vPageAvatar.tag == this) {
                vPageAvatar.setTitle(title)
                vPageAvatar.setSubtitle(subtitle)
                if (imageId > 0) ImageLoader.load(imageId).into(vPageAvatar.vAvatar.vImageView)
                else vPageAvatar.vAvatar.vImageView.setImageResource(R.color.focus)
                ControllerLinks.makeLinkable(vPageAvatar.vTitle)
            }
        }

        vPageTouch.isClickable = true
        vPageTouch.isEnabled = true
        vPageTouch.isFocusable = true
        vPageTouch.setOnClickListener {
            when {
                link.isLinkToAccount() -> {
                    val id = link.getLongParamOrZero(0)
                    val name = if(link.params.size == 1) link.params[0] else if (link.link.length < 3) "" else link.link.removePrefix("@").replace("_", "")
                    if (id > 0) SProfile.instance(id, Navigator.TO)
                    else SProfile.instance(name, Navigator.TO)
                }
                link.isLinkToPost() -> SPost.instance(link.getLongParamOrZero(0), Navigator.TO)
                link.isLinkToComment() -> SPost.instance(link.getLongParamOrZero(0), link.getLongParamOrZero(1), Navigator.TO)
                link.isLinkToChat() -> SChat.instance(ChatTag(API.CHAT_TYPE_FANDOM_ROOT, link.getLongParamOrZero(0), link.getLongParamOrZero(1)), 0, false, Navigator.TO)
                link.isLinkToFandom() -> SFandom.instance(link.getLongParamOrZero(0), link.getLongParamOrZero(1), Navigator.TO)
                link.isLinkToStickersPack() -> SStickersView.instance(link.getLongParamOrZero(0), Navigator.TO)
                link.isLinkToQuest() -> SQuest.instance(link.getLongParamOrZero(0), Navigator.TO)
            }
        }


    }

    override fun notifyItem() {
        ControllerCampfireObjects.load(LinkParsed((page as PageCampfireObject).link)) { _, _, _ -> }
    }


}
