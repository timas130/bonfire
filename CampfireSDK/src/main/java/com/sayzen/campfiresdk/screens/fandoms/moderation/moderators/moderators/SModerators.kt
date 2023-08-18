package com.sayzen.campfiresdk.screens.fandoms.moderation.moderators.moderators

import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.requests.fandoms.RFandomsAdminRemoveModerator
import com.dzen.campfire.api.requests.fandoms.RFandomsModeratorsGetAll
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.models.cards.CardAccount
import com.sayzen.campfiresdk.models.events.fandom.EventFandomRemoveModerator
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.splash.SplashField
import com.sup.dev.android.views.splash.SplashMenu
import com.sup.dev.java.libs.eventBus.EventBus

class SModerators(
        private val fandomId: Long,
        private val languageId: Long
) : SLoadingRecycler<CardAccount, Account>() {

    private val eventBus = EventBus.subscribe(EventFandomRemoveModerator::class) {
        if (it.fandomId == fandomId && it.languageId == languageId) {
            val accounts = adapter.get(CardAccount::class)
            for (c in accounts) if (c.xAccount.getId() == it.accountId) adapter.remove(c)
        }
    }

    init {
        disableShadows()
        disableNavigation()
        setBackgroundImage(API_RESOURCES.IMAGE_BACKGROUND_14)
        setTitle(t(API_TRANSLATE.moderation_screen_moderators))
        setTextEmpty(t(API_TRANSLATE.moderation_screen_moderators_empty))

        adapter.setBottomLoader { onLoad, cards ->
            RFandomsModeratorsGetAll(fandomId, languageId, cards.size.toLong())
                    .onComplete { r -> onLoad.invoke(r.accounts) }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }
    }

    override fun classOfCard() = CardAccount::class

    override fun map(item: Account): CardAccount {
        val card = CardAccount(item)
        if (ControllerApi.can(API.LVL_ADMIN_REMOVE_MODERATOR)) card.setOnLongClick { _, v, _, _ ->
            SplashMenu()
                    .add(t(API_TRANSLATE.app_deprive_moderator)) { removeModerator(item.id) }
                    .asPopupShow(v)
        }
        return card
    }

    private fun removeModerator(accountId: Long) {
        SplashField()
                .setHint(t(API_TRANSLATE.moderation_widget_comment))
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .setOnEnter(t(API_TRANSLATE.app_deprive)) { w, comment ->
                    ApiRequestsSupporter.executeEnabled(w, RFandomsAdminRemoveModerator(fandomId, languageId, accountId, comment)) {
                        EventBus.post(EventFandomRemoveModerator(fandomId, languageId, accountId))
                        ToolsToast.show(t(API_TRANSLATE.app_done))
                    }
                }
                .asSheetShow()
    }

}