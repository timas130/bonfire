package com.sayzen.campfiresdk.screens.account.rating

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import com.sayzen.campfiresdk.R

import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapter
import com.sup.dev.android.views.cards.Card

open class PageRating() : Card(0) {

    companion object {

        val bg1 = ToolsResources.getColor(R.color.yellow_a_200)
        val bg2 = ToolsResources.getColor(R.color.grey_300)
        val bg3 = ToolsResources.getColor(R.color.brown_600)
        val bg4 = ToolsResources.getColor(R.color.focus)
    }

    protected val adapterSub: RecyclerCardAdapter

    init {
        adapterSub = RecyclerCardAdapter()
    }

    override fun bindView(view: View) {
        super.bindView(view)
        (view as RecyclerView).adapter = adapterSub
    }

    override fun instanceView(): View {
        val v = RecyclerView(SupAndroid.activity!!)
        v.layoutManager = LinearLayoutManager(SupAndroid.activity)
        return v
    }

}
