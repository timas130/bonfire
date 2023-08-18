package com.sayzen.campfiresdk.screens.activities.administration.prison

import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.account.AccountPrison
import com.dzen.campfire.api.requests.accounts.RAccountsPrisonGetAll
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.cards.CardAccount
import com.sayzen.campfiresdk.models.events.fandom.EventFandomAccountBaned
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsDate

class SAdministrationPrison() : SLoadingRecycler<CardAccount, AccountPrison>() {

    private val eventBus = EventBus.subscribe(EventFandomAccountBaned::class) {
        for (i in adapter.get(CardAccount::class))
            if (i.xAccount.getId() == it.accountId){
                if(it.date > 0) {
                    i.setSubtitle(t(API_TRANSLATE.moderation_screen_prison_text, ToolsResources.sex(i.xAccount.getSex(), t(API_TRANSLATE.he_baned), t(API_TRANSLATE.she_baned)), ToolsDate.dateToString(it.date)) + "\n" + t(API_TRANSLATE.app_comment) + ": " + (i.tag as AccountPrison).comment)
                }else{
                    adapter.remove(i)
                }
            }
    }

    init {
        disableShadows()
        disableNavigation()

        setBackgroundImage(API_RESOURCES.IMAGE_BACKGROUND_8)
        setTitle(t(API_TRANSLATE.moderation_screen_prison))
        setTextEmpty(t(API_TRANSLATE.moderation_screen_prison_empty))

        adapter.setBottomLoader { onLoad, cards ->
            RAccountsPrisonGetAll(cards.size.toLong())
                    .onComplete { r -> onLoad.invoke(r.accounts) }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }
    }
    override fun classOfCard() = CardAccount::class

    override fun map(item: AccountPrison): CardAccount {
        val card = CardAccount(item.account)
        card.tag = item
        card.setSubtitle(t(API_TRANSLATE.moderation_screen_prison_text, ToolsResources.sex(item.account.sex, t(API_TRANSLATE.he_baned), t(API_TRANSLATE.she_baned)), ToolsDate.dateToString(item.banDate)) /*+ "\n" + ToolsResources.s(R.string.app_comment) + ": " + item.comment*/)
        return card
    }

}