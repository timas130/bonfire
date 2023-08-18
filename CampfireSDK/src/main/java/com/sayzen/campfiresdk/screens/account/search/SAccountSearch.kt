package com.sayzen.campfiresdk.screens.account.search


import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.requests.accounts.RAccountsFollowsChange
import com.dzen.campfire.api.requests.accounts.RAccountsGetAll
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.models.cards.CardAccount
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.account.EventAccountsFollowsChange
import com.sayzen.campfiresdk.screens.account.SAccountsOnline
import com.sayzen.campfiresdk.screens.account.rating.SRating
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.android.views.splash.SplashMenu
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsMapper
import com.sup.dev.java.tools.ToolsThreads

class SAccountSearch(
        private val showKeyboard: Boolean = true,
        private val showMyAccount: Boolean = false,
        private val closeScreenAfterSelect: Boolean = true,
        private val accountScreenMode: Boolean = false,
        private val onSelected: (Account) -> Unit
) : SLoadingRecycler<CardAccount, Account>(R.layout.screen_account_search) {

    private val eventBus = EventBus
            .subscribe(EventAccountsFollowsChange::class) { if (!it.isFollow && !isSearchResult) for (c in adapter.get(CardAccount::class)) if (c.account.id == it.accountId) adapter.remove(c) }

    private val vField: SettingsField = findViewById(R.id.vField)
    private val vSearch: ViewIcon = findViewById(R.id.vSearch)
    private var isSearchResult = false

    private var isSubscriptionsOnly = false

    init {
        disableShadows()
        disableNavigation()

        vField.setHint(t(API_TRANSLATE.app_search))
        setTitle(t(API_TRANSLATE.app_search))
        setTextEmpty("")
        setBackgroundImage(0)

        ToolsView.onFieldEnterKey(vField.vField) {
            ToolsView.hideKeyboard(vField.vField)
            reload()
        }
        vSearch.setOnClickListener {
            ToolsView.hideKeyboard(vField.vField)
            reload()
        }

        adapter.setBottomLoader { onLoad, cards ->
            isSearchResult = vField.getText().isNotEmpty()
            RAccountsGetAll()
                    .setUsername(vField.getText().trim())
                    .setOffset(cards.size.toLong())
                    .setSubscriptionsOnly(isSubscriptionsOnly && !isSearchResult)
                    .onComplete { r ->
                        isSubscriptionsOnly = r.isSubscriptions
                        onLoad.invoke(removeMyAccount(r.accounts))
                        setTextEmpty(t(API_TRANSLATE.app_nothing_found))
                        setBackgroundImage(API_RESOURCES.IMAGE_BACKGROUND_4)
                    }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }

        if (accountScreenMode) {
            setTitle(t(API_TRANSLATE.app_users))
            addToolbarIcon(ToolsResources.getDrawable(R.drawable.ic_more_vert_white_24dp)) {
                SplashMenu()
                        .add(t(API_TRANSLATE.app_ratings)) { SRating.instance(Navigator.TO) }
                        .add(t(API_TRANSLATE.app_online)) { Navigator.to(SAccountsOnline()) }
                        .asPopupShow(it)
            }
        }
    }

    override fun onFirstShow() {
        super.onFirstShow()
        //  Хак. Не отображается клавиатура при открытии
        vSearch.requestFocus()
        ToolsThreads.main(200) {
            if (showKeyboard) vField.showKeyboard()
        }

    }

    override fun classOfCard() = CardAccount::class

    override fun map(item: Account): CardAccount {
        return CardAccount(item).setOnClick {
            if (closeScreenAfterSelect) Navigator.remove(this)
            onSelected.invoke(item)
        }.setOnLongClick { cardAvatar, view, x, y ->
            if(isSearchResult) return@setOnLongClick
            SplashMenu()
                    .add(t(API_TRANSLATE.app_unsubscribe)){
                        ApiRequestsSupporter.executeEnabledConfirm(t(API_TRANSLATE.profile_follows_remove_confirm), t(API_TRANSLATE.app_unfollow), RAccountsFollowsChange(item.id, false)) { eventBus.post(EventAccountsFollowsChange(item.id, false)) }
                    }
                    .asPopupShow(view, x, y)
        } as CardAccount
    }

    override fun onResume() {
        super.onResume()
        if (!accountScreenMode) ToolsView.showKeyboard(vField)
    }

    private fun removeMyAccount(accounts: Array<Account>): Array<Account> {
        if (showMyAccount) return accounts
        for (a in accounts)
            if (a.id == ControllerApi.account.getId()) {
                val newAccounts = arrayOfNulls<Account>(accounts.size - 1)
                var x = 0
                for (i in accounts.indices) {
                    if (accounts[i].id == ControllerApi.account.getId()) {
                        x = 1
                        continue
                    }
                    newAccounts[i - x] = accounts[i]
                }
                return ToolsMapper.asNonNull(newAccounts)
            }
        return accounts
    }
}
