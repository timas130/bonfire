package com.sup.dev.android.views.screens

import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sup.dev.android.R
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.ViewIcon

abstract class SRecycler(res: Int = R.layout.screen_recycler) : Screen(res) {

    protected val vToolbar: Toolbar = findViewById(R.id.vToolbar)
    protected val vToolbarIconsContainer: ViewGroup = findViewById(R.id.vToolbarIconsContainer)
    protected val vRecycler: RecyclerView = findViewById(R.id.vRecycler)
    protected val vFab: FloatingActionButton = findViewById(R.id.vFab)

    init {
        vRecycler.layoutManager = LinearLayoutManager(context)
    }

    fun onReloadClicked() {

    }

    protected fun addToolbarIcon(@DrawableRes res: Int, onClick: (View) -> Unit): ViewIcon {
        val viewIcon: ViewIcon = ToolsView.inflate(context, R.layout.z_icon)
        viewIcon.setImageResource(res)
        viewIcon.setOnClickListener { onClick.invoke(viewIcon) }
        vToolbarIconsContainer.addView(viewIcon)
        return viewIcon
    }

    protected fun addToolbarView(v: View) {
        vToolbar.addView(v)
    }

}
