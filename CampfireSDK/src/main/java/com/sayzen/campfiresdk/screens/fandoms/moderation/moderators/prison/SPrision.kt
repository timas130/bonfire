package com.sayzen.campfiresdk.screens.fandoms.moderation.moderators.prison

import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.account.AccountPrison
import com.dzen.campfire.api.requests.fandoms.RFandomsPrisonGetAll
import com.dzen.campfire.api.requests.fandoms.RFandomsModerationForgive
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.models.cards.CardAccount
import com.sayzen.campfiresdk.models.events.fandom.EventFandomAccountBaned
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.splash.SplashField
import com.sup.dev.android.views.splash.SplashMenu
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsDate

class SPrision(
        private val fandomId: Long,
        private val languageId: Long
) : SLoadingRecycler<CardAccount, AccountPrison>() {

    private val eventBus = EventBus.subscribe(EventFandomAccountBaned::class) {
        if (fandomId == it.fandomId && languageId == it.languageId) {
            for (i in adapter.get(CardAccount::class))
                if (i.xAccount.getId() == it.accountId){
                    if(it.date > 0) {
                        i.setSubtitle(t(API_TRANSLATE.moderation_screen_prison_text, ToolsResources.sex(i.xAccount.getSex(), t(API_TRANSLATE.he_baned), t(API_TRANSLATE.she_baned)), ToolsDate.dateToString(it.date)) + "\n" + t(API_TRANSLATE.app_comment) + ": " + (i.tag as AccountPrison).comment)
                    }else{
                        adapter.remove(i)
                    }
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
            RFandomsPrisonGetAll(fandomId, languageId, cards.size.toLong())
                    .onComplete { r -> onLoad.invoke(r.accounts) }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }
    }
    override fun classOfCard() = CardAccount::class

    override fun map(item: AccountPrison): CardAccount {
        val card = CardAccount(item.account)
        card.tag = item
        card.setSubtitle(t(API_TRANSLATE.moderation_screen_prison_text, ToolsResources.sex(item.account.sex, t(API_TRANSLATE.he_baned), t(API_TRANSLATE.she_baned)), ToolsDate.dateToString(item.banDate)) + "\n" + t(API_TRANSLATE.app_comment) + ": " + item.comment)
        if (ControllerApi.can(fandomId, languageId, API.LVL_MODERATOR_BLOCK))
            card.setOnLongClick { _, _, _, _ ->
                SplashMenu()
                        .add(t(API_TRANSLATE.app_forgive)) { event -> forgive(item.account.id) }
                        .asSheetShow()
            }
        return card
    }

    private fun forgive(accountId: Long) {
        SplashField()
                .setTitle(t(API_TRANSLATE.app_forgive_confirm))
                .setHint(t(API_TRANSLATE.moderation_widget_comment))
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .setOnEnter(t(API_TRANSLATE.app_forgive)) { w, comment ->
                    ApiRequestsSupporter.executeEnabled(w, RFandomsModerationForgive(fandomId, languageId, accountId, comment)) {
                        ToolsToast.show(t(API_TRANSLATE.app_done))
                        EventBus.post(EventFandomAccountBaned(accountId, fandomId, languageId, 0))
                    }
                }
                .asSheetShow()
    }

}