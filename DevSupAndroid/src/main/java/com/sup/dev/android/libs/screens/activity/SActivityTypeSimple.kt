package com.sup.dev.android.libs.screens.activity

import android.view.View
import com.sup.dev.android.R

class SActivityTypeSimple(
        activity:SActivity
) : SActivityType(activity) {

    override fun addNavigationItem(icon: Int, text: String, hided: Boolean, useIconsFilters: Boolean, onClick: (View) -> Unit, onLongClick: ((View) -> Unit)?): NavigationItem {
        return NavigationItem()
    }

    override fun getLayout() = R.layout.screen_activity

    override fun onCreate() {
    }

    inner class NavigationItem : SActivityType.NavigationItem() {


        override fun setVisible(visible: Boolean) {

        }

    }

    override fun updateIcons() {

    }

}