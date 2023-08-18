package com.sayzen.campfiresdk.screens.translates

import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.translate.TranslateHistory
import com.dzen.campfire.api.requests.translates.RTranslateModerationGet
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.libs.image_loader.ImageLink
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.views.screens.SLoadingRecycler

class STranslateModeration() : SLoadingRecycler<CardTranslateModeration, TranslateHistory>() {

    init {
        disableNavigation()

        setTitle(t(API_TRANSLATE.translates_title_translate_moderation))
        setTextEmpty(t(API_TRANSLATE.translates_mod_empty))
        setBackgroundImage(ImageLoader.load(API_RESOURCES.IMAGE_BACKGROUND_30) as ImageLink?)

        adapter.setBottomLoader { onLoad, cards ->
            RTranslateModerationGet(cards.size.toLong())
                    .onComplete { r -> onLoad.invoke(r.histories) }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }
    }

    override fun classOfCard() = CardTranslateModeration::class

    override fun map(item: TranslateHistory) = CardTranslateModeration(item)

}

