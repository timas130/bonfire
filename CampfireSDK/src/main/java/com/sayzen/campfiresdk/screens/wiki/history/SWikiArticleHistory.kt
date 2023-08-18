package com.sayzen.campfiresdk.screens.wiki.history

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.wiki.WikiPages
import com.dzen.campfire.api.models.wiki.WikiTitle
import com.dzen.campfire.api.requests.wiki.RWikiItemHistoryGet
import com.sayzen.campfiresdk.controllers.api
import com.sup.dev.android.views.screens.SLoadingRecycler

class SWikiArticleHistory(
        val wikiTitle: WikiTitle,
        var languageId: Long
) : SLoadingRecycler<CardHistory, WikiPages>() {

    init {
        disableShadows()
        disableNavigation()
        setTitle(wikiTitle.getName(API.getLanguage(languageId).code))

        adapter.setBottomLoader { onLoad, cards ->
            subscription = RWikiItemHistoryGet(wikiTitle.itemId, languageId, cards.size.toLong())
                    .onComplete { r ->
                        onLoad.invoke(r.historyList)
                    }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }
    }

    override fun classOfCard() = CardHistory::class

    override fun map(item: WikiPages) = CardHistory(this, item, wikiTitle, languageId)

    fun updateStatus(){
        var wasFound = false
        for(c in adapter.get(CardHistory::class)){
            if(c.pages.wikiStatus == API.STATUS_DRAFT){
                if(!wasFound) {
                    c.pages.wikiStatus = API.STATUS_PUBLIC
                    c.update()
                    wasFound = true
                }
            }
            if(c.pages.wikiStatus == API.STATUS_PUBLIC){
                if(wasFound){
                    c.pages.wikiStatus = API.STATUS_DRAFT
                    c.update()
                }else{
                    wasFound = true
                }
            }
        }
    }

}
