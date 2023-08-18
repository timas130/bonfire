package com.sayzen.campfiresdk.views


import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.dzen.campfire.api.API_TRANSLATE
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapter
import com.sup.dev.android.views.splash.SplashRecycler

class SplashCards : SplashRecycler(R.layout.splash_search) {

    private var myAdapter = RecyclerCardAdapter()

    protected val vProgress: View = findViewById(R.id.vProgress)
    protected val vMessage: TextView = findViewById(R.id.vMessage)

    init {
        vRecycler.layoutManager = LinearLayoutManager(view.context)
        setAdapter<SplashRecycler>(myAdapter)

        vMessage.text = t(API_TRANSLATE.app_nothing_found)
        vMessage.visibility = View.GONE

        setMaxH(ToolsView.dpToPx(256).toInt())
        setSizeW(ToolsView.dpToPx(200).toInt())
        allowPopupMirrorHeight()
        setPopupYMirrorOffset(ToolsView.dpToPx(42).toInt())


    }

    fun addCards(vararg cards:Card){
        vProgress.visibility = View.GONE
        vMessage.visibility = View.GONE
        for(c in cards) myAdapter.add(c)
    }



}