package com.sup.dev.android.views.cards

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sup.dev.android.R
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.java.classes.Subscription
import com.sup.dev.java.tools.ToolsThreads

abstract class CardScreenLoadingRecycler<C : Card, V>(res: Int = R.layout.card_loading_recycler) : CardScreenLoading(res) {

    protected var textErrorRetry = SupAndroid.TEXT_APP_RETRY

    protected val vRecycler: RecyclerView = vRoot.findViewById(R.id.vRecyclerCard)
    protected val vRefresh: SwipeRefreshLayout? = vRoot.findViewById(R.id.vRefreshCard)

    protected var adapterSub: RecyclerCardAdapterLoading<C, V>? = null
    protected var subscription: Subscription? = null

    init {
        textErrorNetwork = SupAndroid.TEXT_ERROR_NETWORK

        vRecycler.layoutManager = LinearLayoutManager(vRoot.context)
        vRefresh?.setOnRefreshListener {
            vRefresh.isRefreshing = false
            onReloadClicked()
        }

        val vFab: FloatingActionButton? = vRoot.findViewById(R.id.vFab)
        if (vFab != null) {
            if (this.vFab.parent is ViewGroup) (this.vFab.parent as ViewGroup).removeView(this.vFab)
            vFab.id = R.id.vFab
            (vFab as View).visibility = this.vFab.visibility
            this.vFab = vFab
        }

        ToolsThreads.main(true) {
            adapterSub = instanceAdapter()
                    .addOnFinish_Empty { setState(State.EMPTY) }
                    .addOnError_Empty { setState(State.ERROR) }
                    .addOnStart_Empty { setState(State.PROGRESS) }
                    .addOnStart_NotEmpty { setState(State.NONE) }
                    .addOnLoadedPack_NotEmpty { setState(State.NONE) }
                    .setRetryMessage(textErrorNetwork, textErrorRetry)
                    .setShowLoadingCardIfEmpty(false)
                    .setShowErrorCardIfEmpty(false)
                    .setNotifyCount(5)

            vRecycler.adapter = adapterSub

            ToolsThreads.main(true) {
                reload()
            }
        }
    }

    override fun onReloadClicked() {
        reload()
    }

    open fun reload() {
        if (subscription != null) subscription!!.unsubscribe()
        adapterSub!!.reloadBottom()
    }

    protected abstract fun instanceAdapter(): RecyclerCardAdapterLoading<C, V>

    //
    //  Getters
    //

    fun getAdapterCards() = adapterSub


}
