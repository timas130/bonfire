package com.sup.dev.android.views.settings

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import com.sup.dev.android.R
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.splash.SplashMenu

class SettingsSelection constructor(context: Context, attrs: AttributeSet? = null) : Settings(context, attrs) {

    private val menu: SplashMenu = SplashMenu()
    private var currentIndex = 0
    private val list = ArrayList<String>()
    private var onSelected: (Int) -> Unit = {}
    val vArrow: ImageView = ImageView(context)

    init {
        view.setOnClickListener { menu.asSheetShow() }
        vArrow.setImageDrawable(ToolsResources.getDrawable(R.drawable.ic_keyboard_arrow_down_white_24dp))

        setSubView(vArrow)
    }

    fun add(v: Int, onItemSelected: (Int) -> Unit = {}) {
        add(ToolsResources.s(v), onItemSelected)
    }

    fun add(v: String, onItemSelected: (Int) -> Unit = {}) {
        list.add(v)
        menu.add(v) {
            currentIndex = list.indexOf(v)
            setSubtitle(v)
            onItemSelected.invoke(currentIndex)
            onSelected.invoke(currentIndex)
        }
    }

    fun getCurrentIndex() = currentIndex

    fun setCurrentIndex(index: Int) {
        currentIndex = index
        setSubtitle(list[index])
    }

    fun getTitles() = list

    fun clear() {
        menu.clear()
        list.clear()
    }

    fun onSelected(onSelected: (Int) -> Unit) {
        this.onSelected = onSelected
    }

}
