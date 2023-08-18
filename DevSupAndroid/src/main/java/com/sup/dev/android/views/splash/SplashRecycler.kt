package com.sup.dev.android.views.splash

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.sup.dev.android.R
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapter
import com.sup.dev.android.views.splash.view.SplashViewPopup
import com.sup.dev.android.views.splash.view.SplashViewSheet

open class SplashRecycler(
        r:Int = R.layout.splash_recycler
) : Splash(r) {

    val vRoot: ViewGroup = findViewById(R.id.vRoot)
    val vRecycler: RecyclerView = findViewById(R.id.vRecycler)
    val vContainer: ViewGroup = findViewById(R.id.vContainer)

    protected var adapter: RecyclerCardAdapter? = null

    init {
        vRecycler.isVerticalScrollBarEnabled = false
        updateLayoutsSettings()
    }

    override fun onShow() {
        super.onShow()
        updateLayoutsSettings()
    }

    private fun updateLayoutsSettings(){
        vRecycler.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        vRecycler.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        if (viewWrapper is SplashViewSheet || viewWrapper is SplashViewPopup) vRecycler.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
    }

    fun addView(view: View) {
        vRoot.addView(view, 0)
    }

    //
    //  Setters
    //

    fun <K : SplashRecycler> setAdapter(adapter: RecyclerCardAdapter): K {
        vRecycler.adapter = adapter
        return this as K
    }

}
