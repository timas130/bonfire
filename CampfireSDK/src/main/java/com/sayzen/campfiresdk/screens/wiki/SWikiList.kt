package com.sayzen.campfiresdk.screens.wiki

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.ApiResources
import com.dzen.campfire.api.models.wiki.WikiTitle
import com.dzen.campfire.api.requests.wiki.RWikiItemGet
import com.dzen.campfire.api.requests.wiki.RWikiListGet
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.wiki.EventWikiCreated
import com.sayzen.campfiresdk.models.events.wiki.EventWikiListChanged
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sayzen.campfiresdk.support.load
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.java.libs.eventBus.EventBus

class SWikiList(
        val fandomId: Long,
        val prefLanguageId: Long,
        val itemId: Long,
        val itemName: String,
        // would be nice if selecting articles was possible as well, but i'm too lazy
        val onSelectSection: ((WikiTitle) -> Unit)? = null,
) : SLoadingRecycler<CardWikiItem, WikiTitle>() {

    companion object {

        fun instanceFandomId(fandomId: Long, action: NavigationAction) {
            Navigator.action(action, SWikiList(fandomId, 0, 0, ""))
        }

        fun instanceItemId(wikiItemId: Long, action: NavigationAction) {
            ApiRequestsSupporter.executeInterstitial(action, RWikiItemGet(wikiItemId)) { r ->
                SWikiList(r.wikiTitle.fandomId, 0, r.wikiTitle.itemId, r.wikiTitle.getName(ControllerApi.getLanguageCode()))
            }
        }


    }

    private val eventBus = EventBus
            .subscribe(EventWikiCreated::class) { if (it.item.fandomId == fandomId && it.item.parentItemId == itemId) adapter.reloadBottom() }
            .subscribe(EventWikiListChanged::class) {
                if (it.item == null || (it.item.fandomId == fandomId && it.item.parentItemId == itemId)) adapter.reloadBottom()
            }

    init {
        disableShadows()
        disableNavigation()

        addToolbarIcon(R.drawable.ic_insert_link_white_24dp) {
            if (itemId > 0) ToolsAndroid.setToClipboard(ControllerLinks.linkToWikiItemId(itemId))
            else ToolsAndroid.setToClipboard(ControllerLinks.linkToWikiFandomId(fandomId))
            ToolsToast.show(t(API_TRANSLATE.app_copied))
        }

        if (itemName.isEmpty()) setTitle(t(API_TRANSLATE.app_wiki)) else setTitle(itemName)
        setTextEmpty(t(API_TRANSLATE.wiki_list_empty))
        setBackgroundImage(ImageLoader.load(ApiResources.IMAGE_BACKGROUND_29))
        (vFab as View).visibility = if (API.LANGUAGES.any { ControllerApi.can(fandomId, it.id, API.LVL_MODERATOR_WIKI_EDIT) }) View.VISIBLE else View.GONE
        if (onSelectSection != null) {
            vFab.setImageResource(R.drawable.ic_done_white_24dp)
            vFab.setOnClickListener {
                Navigator.remove(this)
                onSelectSection.invoke(WikiTitle().apply {
                    fandomId = this@SWikiList.fandomId
                    itemId = this@SWikiList.itemId
                })
            }
            ToolsView.setFabColorR(vFab, R.color.green_700)
        } else {
            vFab.setImageResource(R.drawable.ic_add_white_24dp)
            vFab.setOnClickListener { Navigator.to(SWikiItemCreate(fandomId, itemId)) }
        }

        adapter.setBottomLoader { onLoad, cards ->
            subscription = RWikiListGet(fandomId, itemId, cards.size.toLong())
                    .onComplete { r ->
                        onLoad.invoke(r.items)
                    }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }
    }

    override fun classOfCard() = CardWikiItem::class

    override fun map(item: WikiTitle) = CardWikiItem(
        wikiItem = item,
        prefLanguageId = prefLanguageId,
        onClick = onSelectSection.let {
            if (it != null) {
                {
                    Navigator.to(SWikiList(
                        item.fandomId, 0, item.itemId,
                        item.getName(ControllerApi.getLanguageCode())
                    ) { item ->
                        Navigator.remove(this)
                        it(item)
                    })
                }
            } else {
                null
            }
        },
        onSelectSection = onSelectSection.let {
            if (it != null) {
                { item ->
                    Navigator.remove(this)
                    it(item)
                }
            } else {
                null
            }
        },
        listGetter = {
            adapter.directItems().mapNotNull { (it as? CardWikiItem)?.wikiItem }
        },
    )
}
