package com.sup.dev.android.views.screens

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sup.dev.android.R
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapterLoading
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.java.classes.Subscription
import com.sup.dev.java.tools.ToolsThreads
import kotlin.reflect.KClass

abstract class SLoadingRecycler<C : Card, V>(res: Int = R.layout.screen_loading_recycler) : SLoading(res) {

    protected var textErrorRetry = SupAndroid.TEXT_APP_RETRY

    protected val vToolbar: Toolbar? = findViewById(R.id.vToolbar)
    protected val vToolbarIconsContainer: ViewGroup? = findViewById(R.id.vToolbarIconsContainer)
    protected val vRecycler: RecyclerView = findViewById(R.id.vRecycler)
    protected val vRefresh: SwipeRefreshLayout? = findViewById(R.id.vRefresh)
    protected val vScreenRoot: ViewGroup? = findViewById(R.id.vScreenRoot)

    protected var adapter = RecyclerCardAdapterLoading<C,V>(classOfCard()) {map(it)}
    protected var subscription: Subscription? = null

    init {
        textErrorNetwork = SupAndroid.TEXT_ERROR_NETWORK

        vRecycler.layoutManager = LinearLayoutManager(context)
        vRefresh?.setOnRefreshListener {
            vRefresh.isRefreshing = false
            onReloadClicked()
        }

        val vFabX: FloatingActionButton? = findViewById(R.id.vFabX)
        if (vFabX != null) {
            if (vFab.parent is ViewGroup) (vFab.parent as ViewGroup).removeView(vFab)
            vFabX.id = R.id.vFab
            (vFabX as View).visibility = vFab.visibility
            vFab = vFabX
        }

        adapter
                .addOnFinish_Empty { setState(State.EMPTY) }
                .addOnError_Empty { setState(State.ERROR) }
                .addOnStart_Empty { setState(State.PROGRESS) }
                .addOnStart_NotEmpty { setState(State.NONE) }
                .addOnLoadedPack_NotEmpty { setState(State.NONE) }
                .setRetryMessage(textErrorNetwork, textErrorRetry)
                .setShowLoadingCardIfEmpty(false)
                .setShowErrorCardIfEmpty(false)
                .setNotifyCount(5)

        vRecycler.adapter = adapter

        ToolsThreads.main(true) {
            reload()
        }
    }

    abstract fun classOfCard():KClass<C>

    abstract fun map(item:V):C

    //
    //  Functions
    //

    override fun onReloadClicked() {
        reload()
    }

    open fun reload() {
        if (subscription != null) subscription!!.unsubscribe()
        adapter.reloadBottom()
    }

    //
    //  Toolbar
    //

    protected fun addToolbarIcon(@DrawableRes res: Int, onClick: (View) -> Unit) = addToolbarIcon(res, true, onClick)

    protected fun addToolbarIcon(drawable: Drawable, onClick: (View) -> Unit) = addToolbarIcon(drawable, true, onClick)

    protected fun addToolbarIcon(@DrawableRes res: Int, useFilter:Boolean, onClick: (View) -> Unit) = addToolbarIcon(ToolsResources.getDrawable(res), useFilter, onClick)

    protected fun addToolbarIcon(drawable: Drawable, useFilter:Boolean, onClick: (View) -> Unit): ViewIcon {
        val viewIcon: ViewIcon = ToolsView.inflate(context, R.layout.z_icon_toolbar)
        viewIcon.setImageDrawable(drawable)
        if (useFilter && useIconsFilter) viewIcon.setFilter(ToolsResources.getColorAttr(R.attr.colorOnPrimary))
        viewIcon.setOnClickListener { onClick.invoke(viewIcon) }

        for (i in 0 until vToolbarIconsContainer?.childCount!!) {
            if (i!=0) (vToolbarIconsContainer.getChildAt(i).layoutParams as ViewGroup.MarginLayoutParams).rightMargin =0
        }

        vToolbarIconsContainer.addView(viewIcon)
        return viewIcon
    }

    protected fun addToolbarView(v: View) {
        vToolbar?.addView(v)
    }

    //
    //  Getters
    //

    fun getAdapterCards() = adapter


}
