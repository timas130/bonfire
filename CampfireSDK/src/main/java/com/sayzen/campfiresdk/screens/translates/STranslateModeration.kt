package com.sayzen.campfiresdk.screens.translates

import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.ApiResources
import com.dzen.campfire.api.models.translate.TranslateHistory
import com.dzen.campfire.api.requests.translates.RTranslateModerationGet
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.support.load
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.views.screens.SLoadingRecycler

class STranslateModeration() : SLoadingRecycler<CardTranslateModeration, TranslateHistory>() {

    init {
        disableNavigation()

        setTitle(t(API_TRANSLATE.translates_title_translate_moderation))
        setTextEmpty(t(API_TRANSLATE.translates_mod_empty))
        setBackgroundImage(ImageLoader.load(ApiResources.IMAGE_BACKGROUND_30))

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

