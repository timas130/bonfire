package com.sayzen.campfiresdk.screens.fandoms.rating

import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.requests.fandoms.RFandomsRatingGet
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.screens.SLoadingRecycler

class SRating(
        fandomId: Long,
        languageId: Long
) : SLoadingRecycler<CardRating, CardRating>() {


    init {
        disableShadows()
        disableNavigation()
        setTitle(t(API_TRANSLATE.app_users))
        setTextEmpty(t(API_TRANSLATE.app_empty))
        setBackgroundImage(API_RESOURCES.IMAGE_BACKGROUND_10)

        adapter.setBottomLoader { onLoad, cards ->

            RFandomsRatingGet(fandomId, languageId, cards.size.toLong())
                    .onComplete { r ->
                        val list = ArrayList<CardRating>()
                        var index = cards.size + 1
                        for (i in r.karmaAccounts.indices) {
                            val c = CardRating(r.karmaAccounts[i], (r.karmaCounts[i] / 100).toString() + "")
                            c.setTextColor(if(r.karmaCounts[i] > 0)ToolsResources.getColor(R.color.green_700)else ToolsResources.getColor(R.color.red_700))
                            c.setIndex(index++)
                            list.add(c)
                        }
                        onLoad.invoke(list.toTypedArray())
                    }.onError {
                        onLoad.invoke(null)
                    }.send(api)

        }
    }

    override fun classOfCard() = CardRating::class

    override fun map(item: CardRating) = item

}
