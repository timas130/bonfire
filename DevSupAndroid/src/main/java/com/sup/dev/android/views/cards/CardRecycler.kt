package com.sup.dev.android.views.cards

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sup.dev.android.R
import com.sup.dev.android.tools.ToolsView

abstract class CardRecycler(res: Int = R.layout.card_recycler) : Card(0) {

    protected val vRoot: View = ToolsView.inflate(res)
    protected val vRecycler: RecyclerView = vRoot.findViewById(R.id.vRecyclerCard)
    protected val vRefresh: SwipeRefreshLayout = vRoot.findViewById(R.id.vRefreshCard)
    protected val vFab: FloatingActionButton = vRoot.findViewById(R.id.vFab)
    protected val vMessage: TextView = vRoot.findViewById(R.id.vMessage)

    init {
        vRecycler.layoutManager = LinearLayoutManager(vRoot.context)
        vRefresh.setOnRefreshListener {
            vRefresh.isRefreshing = false
            onReloadClicked()
        }
        (vFab as View).visibility = View.GONE
    }

    override fun instanceView() = vRoot

    open fun onReloadClicked() {
    }



}
