package com.sayzen.campfiresdk.views

import android.graphics.Point
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.dzen.campfire.api.API_TRANSLATE
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerMention
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.android.views.splash.SplashRecycler

abstract class SplashSearch : SplashRecycler(R.layout.splash_search) {

    private var searchName = ""
    private var myAdapter = instanceAdapter()

    protected val vProgress: View = findViewById(R.id.vProgress)
    protected val vMessage: TextView = findViewById(R.id.vMessage)
    protected var field: ControllerMention.Field? = null

    init {
        vRecycler.layoutManager = LinearLayoutManager(view.context)
        setAdapter<SplashRecycler>(myAdapter)

        vMessage.text = t(API_TRANSLATE.app_nothing_found)
        vMessage.visibility = View.GONE
        myAdapter.setShowLoadingCard(false)
                .addOnStart_Empty {
                    vMessage.visibility = View.GONE
                    vProgress.visibility = View.VISIBLE
                }
                .addOnLoadedPack_Empty { vMessage.visibility = View.VISIBLE }
                .addOnLoadedPack_NotEmpty { vMessage.visibility = View.GONE }


        setMaxH(ToolsView.dpToPx(96).toInt())
        setSizeW(ToolsView.dpToPx(200).toInt())
        allowPopupMirrorHeight()
        setPopupYMirrorOffset(ToolsView.dpToPx(42).toInt())


    }

    fun show(point: Point, field: ControllerMention.Field){
        this.field= field
        asPopupShow(field.vField, point.x, point.y + ToolsView.dpToPx(8).toInt())
    }

    override fun onHide() {
        super.onHide()
        this.field = null
    }

    protected abstract fun instanceAdapter():RecyclerCardAdapterLoading<out Card, out  Any>

    fun setSearchName(searchName: String) {
        if (this.searchName == searchName) return
        this.searchName = searchName
        myAdapter.reloadBottom()
    }

    fun getSearchName() = searchName

    override fun onShow() {
        super.onShow()
        myAdapter.reloadBottom()
    }

}