package com.sayzen.campfiresdk.screens.fandoms.search

import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.view.View
import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.requests.fandoms.RFandomsGetAll
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.cards.CardFandom
import com.sayzen.campfiresdk.screens.fandoms.suggest.SFandomSuggest
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.devsupandroidgoogle.ControllerFirebaseAnalytics
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.cards.CardDividerTitle
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.java.tools.ToolsThreads
import java.util.*

class SFandomsSearch private constructor(
        private var name: String,
        private var categoryId: Long,
        private var params1: Array<Long>,
        private var params2: Array<Long>,
        private var params3: Array<Long>,
        private var params4: Array<Long>,
        private var backWhenSelect: Boolean,
        private val onSelected: ((Fandom) -> Unit)?
) : SLoadingRecycler<CardFandom, SFandomsSearch.NFandom>(R.layout.screen_fandoms_search) {


    companion object {

        var ROOT_CATEGORY_ID = 0L

        fun instance(action: NavigationAction, backWhenSelect: Boolean = false, onSelected: ((Fandom) -> Unit)? = null) {
            instance("", 0, emptyArray(), emptyArray(), emptyArray(), emptyArray(), action, backWhenSelect, onSelected)
        }

        fun instance(name: String, categoryId: Long,
                     params1: Array<Long>,
                     params2: Array<Long>,
                     params3: Array<Long>,
                     params4: Array<Long>,
                     action: NavigationAction, backWhenSelect: Boolean = false, onSelected: ((Fandom) -> Unit)? = null) {
            Navigator.action(action, SFandomsSearch(name, categoryId, params1, params2, params3, params4, backWhenSelect, onSelected))
        }

    }

    private var subscribedLoaded = false
    private var lockOnEmpty = false

    init {
        disableShadows()
        disableNavigation()

        setTitle(t(API_TRANSLATE.app_fandoms))
        setTextEmpty(t(API_TRANSLATE.fandoms_empty))
        setTextProgress(t(API_TRANSLATE.fandoms_loading))
        setBackgroundImage(API_RESOURCES.IMAGE_BACKGROUND_7)

        if (ROOT_CATEGORY_ID > 0) categoryId = ROOT_CATEGORY_ID

        val vFab: FloatingActionButton = findViewById(R.id.vFab)
        (vFab as View).visibility = View.VISIBLE
        vFab.setImageResource(R.drawable.ic_search_white_24dp)
        vFab.setOnClickListener {
            ControllerFirebaseAnalytics.post("Screen_FandomsSearch", "Search")
            Navigator.to(SFandomsSearchParams(name, categoryId,
                    params1,
                    params2,
                    params3,
                    params4
            )
                    .onFinish { name, categoryId, params1, params2, params3, params4 ->
                        this.name = name
                        this.categoryId = categoryId
                        this.params1 = params1
                        this.params2 = params2
                        this.params3 = params3
                        this.params4 = params4
                        reload()
                    })
        }


        if (onSelected == null) {
            addToolbarIcon(R.drawable.ic_add_white_24dp) {
                SFandomSuggest.instance(Navigator.TO)
            }
        }
        adapter.setBottomLoader { onLoad, cards -> load(onLoad, cards) }

        adapter.addOnLoadedPack_NotEmpty {
            if (subscribedLoaded || isSearchMode()) setState(State.NONE)
            else {
                adapter.loadBottom()
            }
        }
        adapter.addOnFinish_Empty {
            if (lockOnEmpty) return@addOnFinish_Empty
            if (subscribedLoaded || isSearchMode()) setState(State.EMPTY)
            else {
                adapter.loadBottom()
            }
        }
    }

    override fun classOfCard() = CardFandom::class

    override fun map(fandom: NFandom): CardFandom {
        if (fandom.fandom.languageId == 0L) fandom.fandom.languageId = ControllerApi.getLanguageId()

        val card = if (onSelected == null) CardFandom(fandom.fandom)
        else CardFandom(fandom.fandom, onClick = { language ->
            val selectedFandom = fandom.fandom.copy()
            selectedFandom.languageId = language
            if (backWhenSelect) Navigator.back()
            onSelected.invoke(selectedFandom)
        }, showLanguage = true)

        card.removeIfUnsubscribe = true
        card.setSubscribed(fandom.subscribed && !isSearchMode())
        return card
    }

    override fun reload() {
        when {
            name.isNotEmpty() -> setTitle(name)
            params1.isNotEmpty() -> setTitle(t(API_TRANSLATE.app_search))
            params2.isNotEmpty() -> setTitle(t(API_TRANSLATE.app_search))
            params3.isNotEmpty() -> setTitle(t(API_TRANSLATE.app_search))
            params4.isNotEmpty() -> setTitle(t(API_TRANSLATE.app_search))
            categoryId != 0L -> setTitle(t(API_TRANSLATE.app_search))
            else -> setTitle(t(API_TRANSLATE.app_fandoms))
        }
        adapter.remove(CardDividerTitle::class)
        lockOnEmpty = true
        subscribedLoaded = false
        super.reload()
    }

    private fun load(onLoad: (Array<NFandom>?) -> Unit, cards: ArrayList<CardFandom>) {
        lockOnEmpty = false
        if (!subscribedLoaded && !isSearchMode()) {
            RFandomsGetAll(RFandomsGetAll.SUBSCRIBE_YES, getLastSubscribedOffset(), ControllerApi.getLanguageId(), categoryId, "", emptyArray(), emptyArray(), emptyArray(), emptyArray())
                    .onComplete { r ->
                        if (r.fandoms.size < RFandomsGetAll.COUNT) {
                            subscribedLoaded = true
                            if (!adapter.isEmpty || r.fandoms.isNotEmpty()) ToolsThreads.main(true) { adapter.add(CardDividerTitle(t(API_TRANSLATE.fandoms_global))) }
                        }
                        onLoad.invoke(Array(r.fandoms.size) { NFandom(r.fandoms[it], true) })
                        if (adapter.isEmpty) ToolsThreads.main(true) { adapter.loadBottom() }
                    }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        } else if (!isSearchMode()) {
            RFandomsGetAll(RFandomsGetAll.SUBSCRIBE_NO, getLastUnsubscribedOffset(), ControllerApi.getLanguageId(), categoryId, "", emptyArray(), emptyArray(), emptyArray(), emptyArray())
                    .onComplete { r ->
                        onLoad.invoke(Array(r.fandoms.size) { NFandom(r.fandoms[it], false) })
                    }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        } else {
            RFandomsGetAll(RFandomsGetAll.SUBSCRIBE_NONE, cards.size.toLong(), ControllerApi.getLanguageId(), categoryId, name, params1, params2, params3, params4)
                    .onComplete { r ->
                        onLoad.invoke(Array(r.fandoms.size) { NFandom(r.fandoms[it], false) })
                    }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }
    }

    //
    //  Getters
    //

    private fun isSearchMode() = name.isNotEmpty()
            || categoryId != 0L
            || params1.isNotEmpty()
            || params2.isNotEmpty()
            || params3.isNotEmpty()
            || params4.isNotEmpty()


    private fun getLastSubscribedOffset(): Long {
        var offset = 0L
        val cards = adapter.get(CardFandom::class)
        for (i in cards.size - 1 downTo 0) if (cards[i].subscribed) offset++
        return offset
    }

    private fun getLastUnsubscribedOffset(): Long {
        var offset = 0L
        val cards = adapter.get(CardFandom::class)
        for (c in cards) if (!c.subscribed) {
            offset++
        }
        return offset
    }

    class NFandom(
            val fandom: Fandom,
            val subscribed: Boolean)


}
