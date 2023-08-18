package com.sup.dev.android.views.cards

import android.view.View
import android.view.ViewGroup
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.support.adapters.CardAdapter
import com.sup.dev.android.views.support.adapters.CardAdapterStub
import com.sup.dev.java.libs.debug.Debug

abstract class Card(
        private val layout: Int
) {

    var adapter: CardAdapter = CardAdapterStub.INSTANCE
    private var view: View? = null
    var isBinding = false

    var tag: Any? = null

    //
    //  Bind
    //

    fun update() {
        if (isBinding) return
        val view = getView()
        if (view != null) bindCardView(view)
    }

    fun bindCardView(view: View) {
        isBinding = true
        this.view = view
        view.tag = this
        bindView(view)
        isBinding = false
    }

    fun detachView() {
        onDetachView()
        this.view = null
    }

    open fun onDetachView() {

    }

    open fun bindView(view: View) {
    }

    protected open fun instanceView(): View {
        return View(SupAndroid.appContext)
    }

    fun getView(): View? {
        val view = adapter.getView(this)
        if (view != null) {
            this.view = view
            view.tag = this
        }

        if (this.view != null && this.view!!.tag != this) this.view = null

        return this.view
    }

    fun remove() {
        adapter.remove(this)
    }

    //
    //  Adapter
    //

    open fun instanceView(vParent: ViewGroup): View {
        return if (layout > 0) ToolsView.inflate(vParent, layout) else instanceView()
    }

    open fun setCardAdapter(adapter: CardAdapter?) {
        this.adapter = adapter?:CardAdapterStub.INSTANCE
    }


}
