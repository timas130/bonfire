package com.sayzen.campfiresdk.screens.wiki

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.wiki.WikiTitle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerWiki
import com.sayzen.campfiresdk.models.events.wiki.EventWikiChanged
import com.sayzen.campfiresdk.models.events.wiki.EventWikiRemove
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.support.adapters.NotifyItem
import com.sup.dev.java.libs.eventBus.EventBus

class CardWikiItem(
        var wikiItem: WikiTitle,
        var prefLanguageId: Long,
        val onClick: (() -> Unit)?,
        val onSelectSection: ((WikiTitle) -> Unit)?,
        val listGetter: () -> List<WikiTitle>
) : Card(R.layout.screen_wiki_card_item), NotifyItem {

    private val eventBus = EventBus
            .subscribe(EventWikiRemove::class) { if (it.itemId == wikiItem.itemId) adapter.remove(this) }
            .subscribe(EventWikiChanged::class) { if (it.item.itemId == wikiItem.itemId) wikiItem = it.item; update(); }

    init {
        if(prefLanguageId < 1) prefLanguageId = ControllerApi.getLanguageId()
    }

    override fun bindView(view: View) {
        super.bindView(view)

        val vImage: ImageView = view.findViewById(R.id.vImage)
        val vName: TextView = view.findViewById(R.id.vName)
        val vSectionIcon: ImageView = view.findViewById(R.id.vSectionIcon)
        val vFab: FloatingActionButton = view.findViewById(R.id.vSelectFab)

        ImageLoader.loadGif(wikiItem.imageId, 0, vImage)
        vName.text = wikiItem.getName(API.getLanguage(prefLanguageId).code)
        vSectionIcon.visibility = if (wikiItem.itemType == API.WIKI_TYPE_SECION) View.VISIBLE else View.GONE

        view.setOnClickListener {
            when {
                onClick != null -> if (wikiItem.itemType == API.WIKI_TYPE_SECION) onClick.invoke()
                wikiItem.itemType == API.WIKI_TYPE_SECION -> Navigator.to(SWikiList(wikiItem.fandomId, prefLanguageId, wikiItem.itemId, wikiItem.getName(API.getLanguage(prefLanguageId).code)))
                else -> Navigator.to(SWikiArticleView(wikiItem, prefLanguageId))
            }
        }

        if (onSelectSection == null) {
            ToolsView.setOnLongClickCoordinates(view) { v, x, y ->
                ControllerWiki.showMenu(wikiItem, prefLanguageId, listGetter(), v, x, y)
            }
        }

        if (onSelectSection != null && wikiItem.itemType == API.WIKI_TYPE_SECION) {
            vFab.visibility = View.VISIBLE
            vFab.setOnClickListener {
                onSelectSection.invoke(wikiItem)
            }
            ToolsView.setFabEnabledR(vFab, true, R.color.green_700)
        } else {
            vFab.visibility = View.GONE
        }
    }


    override fun notifyItem() {
        ImageLoader.load(wikiItem.imageId).intoCash()
    }

}