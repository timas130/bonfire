package com.sayzen.campfiresdk.screens.account.rating


import com.dzen.campfire.api.requests.accounts.RAccountsRatingGet
import com.sayzen.campfiresdk.R
import com.sup.dev.android.tools.ToolsResources

class PageKarma(r: RAccountsRatingGet.Response) : PageRating() {

    init {

        var index = 1
        for (i in r.karmaAccounts.indices) {
            val c = CardRating(r.karmaAccounts[i], (r.karmaAccounts[i].karma30 / 100).toString() + "")
            c.setTextColor(ToolsResources.getColor(R.color.green_700))
            c.setIndex(index++)
            adapterSub.add(c)
        }

    }

}
