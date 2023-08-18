package com.sayzen.campfiresdk.screens.translates

import androidx.recyclerview.widget.LinearLayoutManager
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.translate.TranslateHistory
import com.dzen.campfire.api.requests.translates.RTranslateHistoryGet
import com.sayzen.campfiresdk.controllers.ControllerTranslate.t
import com.sayzen.campfiresdk.controllers.api
import com.sup.dev.android.views.screens.SLoadingRecycler

class STranslatesHistory(
        val languageId:Long,
        val key:String
) : SLoadingRecycler<CardTranslateHistory, TranslateHistory>(){


    init {
        disableShadows()
        disableNavigation()

        setTitle(t(API_TRANSLATE.translates_label_history))

        vRecycler.layoutManager = LinearLayoutManager(context)
        vRecycler.adapter = adapter

        setTextEmpty(t(API_TRANSLATE.translates_label_history_empty))

        adapter.setBottomLoader { onLoad, cards ->
            RTranslateHistoryGet(languageId, key, "", if(cards.isEmpty()) 0L else cards.last().history.dateCreated)
                    .onComplete { r -> onLoad.invoke(r.history) }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }
                .setRemoveSame(true)
    }

    override fun classOfCard() = CardTranslateHistory::class

    override fun map(item: TranslateHistory) = CardTranslateHistory(item)


}