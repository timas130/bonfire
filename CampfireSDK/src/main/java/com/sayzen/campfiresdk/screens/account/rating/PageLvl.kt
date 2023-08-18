package com.sayzen.campfiresdk.screens.account.rating


import com.dzen.campfire.api.requests.accounts.RAccountsRatingGet
import com.sayzen.campfiresdk.R
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.java.tools.ToolsText

class PageLvl(r: RAccountsRatingGet.Response) : PageRating() {

    init {

        var index = 1
        for (i in r.forceAccounts.indices) {
            val c = CardRating(r.forceAccounts[i], ToolsText.numToStringRound(r.forceAccounts[i].lvl / 100.0, 2))
            c.setTextColor(ToolsResources.getColor(R.color.green_700))
            c.setIndex(index++)
            adapterSub.add(c)
        }
    }
}
