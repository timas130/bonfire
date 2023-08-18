package com.sayzen.campfiresdk.views

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.stickers.PublicationSticker
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerStickers
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.cards.stickers.CardSticker
import com.sayzen.campfiresdk.models.cards.stickers.CardStickersPack
import com.sayzen.campfiresdk.screens.account.stickers.CardFavorites
import com.sayzen.campfiresdk.screens.account.stickers.SStickersPacksSearch
import com.sayzen.campfiresdk.screens.account.stickers.SStickersView
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.splash.view.SplashViewDialog
import com.sup.dev.android.views.splash.view.SplashViewSheet
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapter
import com.sup.dev.android.views.splash.SplashRecycler

open class SplashStickers : SplashRecycler(R.layout.splash_stickers) {

    private val myAdapter = RecyclerCardAdapter()
    private val vEmptyContainer: View = findViewById(R.id.vEmptyContainer)
    private val vButton: Button = findViewById(R.id.vButton)
    private val vMessage: TextView = findViewById(R.id.vMessage)
    private val vProgress: View = findViewById(R.id.vProgress)

    private var onSelected: (PublicationSticker) -> Unit = { }
    private var spanCount = 4
    private var isPackMode = false

    init {
        vEmptyContainer.visibility = View.GONE



        vMessage.text = t(API_TRANSLATE.stickers_empty)
        vButton.text = t(API_TRANSLATE.app_search)
        spanCount = if (ToolsAndroid.isScreenPortrait()) 4 else 8
        ToolsView.setRecyclerAnimation(vRecycler)

        setAdapter<SplashRecycler>(myAdapter)

        fixForAndroid9()

    }

    private fun loadPacks() {
        isPackMode = true
        myAdapter.clear()
        vProgress.visibility = View.VISIBLE
        vEmptyContainer.visibility = View.GONE
        vRecycler.layoutManager = LinearLayoutManager(view.context)
        val cardFavorites = CardFavorites(ControllerApi.account.getId())
        cardFavorites.onClick = { loadFavorite() }

        ControllerStickers.getPacks(
                { packs ->
                    myAdapter.add(cardFavorites)
                    for (i in packs) {
                        val card = CardStickersPack(i, false, false, false)
                        card.onClick = {
                            loadStickers(it.id)
                        }
                        myAdapter.add(card)
                    }
                    vProgress.visibility = View.GONE
                    if (packs.isEmpty()) {
                        vEmptyContainer.visibility = View.VISIBLE
                        vMessage.setText(t(API_TRANSLATE.stickers_empty))
                        vButton.setText(t(API_TRANSLATE.app_search))
                        vButton.setOnClickListener { Navigator.to(SStickersPacksSearch()) }
                    }
                },
                {
                    vProgress.visibility = View.GONE
                    vEmptyContainer.visibility = View.VISIBLE
                    vMessage.setText(t(API_TRANSLATE.error_network))
                    vButton.setText(t(API_TRANSLATE.app_retry))
                    vButton.setOnClickListener { loadPacks() }
                })
    }

    private fun loadStickers(packsId: Long) {
        isPackMode = false
        myAdapter.clear()
        vProgress.visibility = View.VISIBLE
        vEmptyContainer.visibility = View.GONE
        vRecycler.layoutManager = GridLayoutManager(view.context, spanCount)

        ControllerStickers.getStickers(packsId,
                { stickers ->
                    for (i in stickers) {
                        val card = CardSticker(i)
                        card.onClick = {
                            onSelected.invoke(it)
                            onSelected = {}
                            hide()
                        }
                        card.onLongClick = { SStickersView.instanceBySticker(it.id, Navigator.TO) }
                        myAdapter.add(card)
                    }
                    vProgress.visibility = View.GONE
                    if (stickers.isEmpty()) {
                        vEmptyContainer.visibility = View.VISIBLE
                        vMessage.setText(t(API_TRANSLATE.stickers_pack_view_empty))
                        vButton.setText(t(API_TRANSLATE.app_back))
                        vButton.setOnClickListener { onBackPressed() }
                    }
                },
                {
                    vProgress.visibility = View.GONE
                    vEmptyContainer.visibility = View.VISIBLE
                    vMessage.setText(t(API_TRANSLATE.error_network))
                    vButton.setText(t(API_TRANSLATE.app_retry))
                    vButton.setOnClickListener { loadStickers(packsId) }
                })
    }

    private fun loadFavorite() {
        isPackMode = false
        myAdapter.clear()
        vProgress.visibility = View.VISIBLE
        vEmptyContainer.visibility = View.GONE
        vRecycler.layoutManager = GridLayoutManager(view.context, spanCount)

        ControllerStickers.getStickersFavorite(
                { stickers ->
                    for (i in stickers) {
                        val card = CardSticker(i)
                        card.onClick = {
                            onSelected.invoke(it)
                            onSelected = {}
                            hide()
                        }
                        card.onLongClick = { SStickersView.instanceBySticker(it.id, Navigator.TO) }
                        myAdapter.add(card)
                    }
                    vProgress.visibility = View.GONE
                    if (stickers.isEmpty()) {
                        vEmptyContainer.visibility = View.VISIBLE
                        vMessage.setText(t(API_TRANSLATE.stickers_pack_view_empty))
                        vButton.setText(t(API_TRANSLATE.app_back))
                        vButton.setOnClickListener { onBackPressed() }
                    }
                },
                {
                    vProgress.visibility = View.GONE
                    vEmptyContainer.visibility = View.VISIBLE
                    vMessage.setText(t(API_TRANSLATE.error_network))
                    vButton.setText(t(API_TRANSLATE.app_retry))
                    vButton.setOnClickListener { loadFavorite() }
                })
    }

    override fun onBackPressed(): Boolean {
        if (!isPackMode) {
            loadPacks()
            return true
        }
        return super.onBackPressed()
    }

    override fun onShow() {
        super.onShow()

        (vRecycler.layoutParams as ViewGroup.MarginLayoutParams).setMargins(0, 0, 0, 0)
        vRecycler.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

        if (viewWrapper is SplashViewDialog)
            (vRecycler.layoutParams as ViewGroup.MarginLayoutParams).setMargins(ToolsView.dpToPx(8).toInt(), ToolsView.dpToPx(2).toInt(), ToolsView.dpToPx(8).toInt(), 0)
        else if (viewWrapper is SplashViewSheet)
            vRecycler.layoutParams.height = ToolsView.dpToPx(320).toInt()

        loadPacks()
    }

    //
    //  Setters
    //

    fun onSelected(onSelected: (PublicationSticker) -> Unit): SplashStickers {
        this.onSelected = onSelected
        return this
    }

}
