package com.sayzen.campfiresdk.screens.rates

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.Rate
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.controllers.ControllerPublications
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.screens.fandoms.moderation.view.SModerationView
import com.sayzen.campfiresdk.screens.fandoms.view.SFandom
import com.sayzen.campfiresdk.screens.post.view.SPost
import com.sayzen.campfiresdk.screens.account.stickers.SStickersView
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.cards.CardAvatar
import com.sup.dev.android.views.support.adapters.NotifyItem
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.java.tools.ToolsDate

class CardRate(
        val rate: Rate
) : CardAvatar(R.layout.card_rate), NotifyItem {

    init {
        var textR: String
        var link: String

        when (rate.publicationType) {
            API.PUBLICATION_TYPE_POST -> {
                textR = t(API_TRANSLATE.profile_rate_post)
                link = ControllerLinks.linkToPost(rate.publicationId)
                setOnClick { SPost.instance(rate.publicationId, Navigator.TO) }
            }
            API.PUBLICATION_TYPE_COMMENT -> {
                textR = t(API_TRANSLATE.profile_rate_comment)
                link = ControllerLinks.linkToComment(rate.publicationId, rate.publicationParentType, rate.publicationParentId)
                setOnClick { ControllerPublications.toPublication(rate.publicationParentType, rate.publicationParentId, rate.publicationId) }
            }
            API.PUBLICATION_TYPE_MODERATION -> {
                textR = t(API_TRANSLATE.profile_rate_moderation)
                link = ControllerLinks.linkToModeration(rate.publicationId)
                setOnClick { SModerationView.instance(rate.publicationId, Navigator.TO) }
            }
            API.PUBLICATION_TYPE_STICKER -> {
                textR = t(API_TRANSLATE.profile_rate_sticker)
                link = ControllerLinks.linkToSticker(rate.publicationId)
                setOnClick { SStickersView.instanceBySticker(rate.publicationId, Navigator.TO) }
            }
            API.PUBLICATION_TYPE_STICKERS_PACK -> {
                textR = t(API_TRANSLATE.profile_rate_stikers_pack)
                link = ControllerLinks.linkToStickersPack(rate.publicationId)
                setOnClick { SStickersView.instance(rate.publicationId, Navigator.TO) }
            }
            else -> {
                textR = t(API_TRANSLATE.error_unknown)
                link = ""
                setOnClick {  }
            }
        }

        setTitle(String.format(textR, ToolsResources.sex(rate.account.sex, t(API_TRANSLATE.he_rate), t(API_TRANSLATE.she_rate)), link).capitalize())
        setSubtitle(ToolsDate.dateToString(rate.date))
        setDividerVisible(true)

        setOnCLickAvatar { SFandom.instance(rate.fandom.id, rate.fandom.languageId, Navigator.TO) }
    }

    override fun bindView(view: View) {
        super.bindView(view)

        val vRate: ViewIcon = view.findViewById(R.id.vRate)

        vRate.setImageResource(if (rate.karmaCount > 0) R.drawable.ic_keyboard_arrow_up_white_24dp else R.drawable.ic_keyboard_arrow_down_white_24dp)
        vRate.setFilter(ToolsResources.getColor(if (rate.karmaCount > 0) R.color.green_700 else R.color.red_700))
    }

    override fun onBind(vAvatar: ViewAvatarTitle) {
        ControllerLinks.makeLinkable(vAvatar.vTitle)
        ImageLoader.load(rate.fandom.imageId).into(vAvatar.vAvatar.vImageView)
    }

    override fun notifyItem() {
        ImageLoader.load(rate.fandom.imageId).intoCash()
    }
}
