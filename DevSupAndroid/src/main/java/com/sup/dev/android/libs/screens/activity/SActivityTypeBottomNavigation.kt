package com.sup.dev.android.libs.screens.activity

import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.sup.dev.android.R
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.layouts.LayoutFrameMeasureCallback
import com.sup.dev.android.views.splash.SplashMenu
import com.sup.dev.java.tools.ToolsThreads

open class SActivityTypeBottomNavigation(
        activity: SActivity
) : SActivityType(activity) {

    companion object {

        fun setShadow(view: View) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                view.background = GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, intArrayOf(0x40000000, 0x00000000))
            }
        }

    }

    var onExtraNavigationItemClicked: () -> Unit = {
        widgetMenu!!.asSheetShow()
    }

    private val iconsList = ArrayList<NavigationItem>()
    var widgetMenu: SplashMenu? = null

    private var lastH_P = 0
    private var maxH_P = 0
    private var lastH_L = 0
    private var maxH_L = 0
    private var skipNextNavigationAnimation = false
    private var lastScreenOrientationPortrait = ToolsAndroid.isScreenPortrait()

    private var vContainer: LinearLayout? = null
    private var vLine: View? = null
    private var extraNavigationItem: SActivityType.NavigationItem? = null

    override fun getLayout() = R.layout.screen_activity_bottom_navigation

    override fun onCreate() {

        vContainer = activity.findViewById(R.id.vScreenActivityBottomNavigationContainer)
        vLine = activity.findViewById(R.id.vScreenActivityBottomNavigationLine)

        updateNavigationVisible()
        setShadow(vLine!!)

        (activity.vActivityRoot as LayoutFrameMeasureCallback).onMeasure = { _, h -> ToolsThreads.main(true) { recalculateBounds() } }
    }

    private fun recalculateBounds(tryCount: Int = 10) {
        if (ToolsAndroid.getScreenH() < activity.vActivityRoot?.height ?: 0) {
            ToolsThreads.main(100) { recalculateBounds(tryCount - 1) }
            return
        }
        lastScreenOrientationPortrait = ToolsAndroid.isScreenPortrait()
        if (ToolsAndroid.isScreenPortrait()) {
            lastH_P = activity.vActivityRoot?.height ?: 0
            if (maxH_P < lastH_P) maxH_P = lastH_P
        } else {
            lastH_L = activity.vActivityRoot?.height ?: 0
            if (maxH_L < lastH_L) maxH_L = lastH_L
        }
        updateNavigationVisible()
    }

    override fun updateNavigationVisible() {
        vContainer!!.setBackgroundColor(screenBottomNavigationColor)
        if (screenNavigationAllowed && screenNavigationVisible) {
            if (screenHideBottomNavigationWhenKeyboard && isKeyboardShown()) {
                vContainer!!.visibility = View.GONE
                vLine!!.visibility = View.GONE
                skipNextNavigationAnimation = true
            } else {
                if (vContainer!!.tag != "from_alpha") {
                    vContainer!!.tag = "from_alpha"
                    ToolsView.fromAlpha(vContainer!!, if (screenNavigationAnimation && !skipNextNavigationAnimation) ToolsView.ANIMATION_TIME_FASE else 0) {
                        vContainer!!.tag = null
                    }
                }
                if (screenNavigationShadowAvailable) {
                    ToolsView.fromAlpha(vLine!!, if (screenNavigationAnimation && !skipNextNavigationAnimation) ToolsView.ANIMATION_TIME_FASE else 0)
                } else {
                    ToolsView.toAlpha(vLine!!, if (screenNavigationAnimation && !skipNextNavigationAnimation) ToolsView.ANIMATION_TIME_FASE else 0) {
                        vLine!!.visibility = if (screenNavigationAllowed) View.INVISIBLE else View.GONE
                        vContainer!!.tag = null
                    }
                }
            }
        } else {
            if (vContainer!!.tag != "to_alpha") {
                vContainer!!.tag = "to_alpha"
                ToolsView.toAlpha(vContainer!!, if (screenNavigationAnimation) ToolsView.ANIMATION_TIME_FASE else 0) {
                    vContainer!!.visibility = if (screenNavigationAllowed) View.INVISIBLE else View.GONE
                }
            }
            ToolsView.toAlpha(vLine!!, if (screenNavigationAnimation) ToolsView.ANIMATION_TIME_FASE else 0) {
                vLine!!.visibility = if (screenNavigationAllowed) View.INVISIBLE else View.GONE
            }
        }
    }

    fun isKeyboardShown(): Boolean {
        return if (ToolsAndroid.isScreenPortrait()) lastH_P < (maxH_P - ToolsView.dpToPx(100))
        else lastH_L < (maxH_L - ToolsView.dpToPx(100))
    }

    //
    //  Navigation Item
    //

    override fun createExtraNavigationItem(useIconsFilters: Boolean, onClick: (() -> Unit)?): SActivityType.NavigationItem? {
        if (widgetMenu == null) {
            widgetMenu = SplashMenu()
            extraNavigationItem = addNavigationItem(R.drawable.ic_menu_white_24dp, "", false, useIconsFilters) { onExtraNavigationItemClicked.invoke() }
        }
        if (onClick != null) this.onExtraNavigationItemClicked = onClick
        return extraNavigationItem
    }

    override fun addNavigationView(view: View, useIconsFilters: Boolean) {
        createExtraNavigationItem(useIconsFilters)
        widgetMenu!!.addTitleView(view)
    }

    override fun addNavigationItem(icon: Int, text: String, hided: Boolean, useIconsFilters: Boolean, onClick: (View) -> Unit, onLongClick: ((View) -> Unit)?): SActivityType.NavigationItem {
        if (hided) {
            val item = NavigationItem()
            createExtraNavigationItem(useIconsFilters)
            val menuItem = widgetMenu!!.add(text) { onClick.invoke(it.card.getView()!!) }.icon(icon)
            if (onLongClick != null) menuItem.onLongClick { onLongClick.invoke(it.card.getView()!!) }
            if(useIconsFilters) menuItem.iconFilter(getIconsColor())
            widgetMenu!!.finishItemBuilding()
            item.menuIndex = widgetMenu!!.getItemsCount() - 1
            return item
        } else {

            val item = NavigationItem()

            item.view = ToolsView.inflate(activity, R.layout.layout_bottom_navigation_icon)
            item.vIcon = item.view?.findViewById(R.id.vNavigationItemIcon)
            item.vChip = item.view?.findViewById(R.id.vNavigationItemChip)
            item.vText = item.view?.findViewById(R.id.vNavigationItemText)

            item.vIcon?.setImageResource(icon)
            if (useIconsFilters) item.vIcon?.setFilter(getIconsColor())
            item.vIcon?.setOnClickListener(onClick)
            if (onLongClick != null) item.vIcon?.setOnLongClickListener { onLongClick.invoke(it); return@setOnLongClickListener true }
            item.vChip?.visibility = View.GONE
            item.vText?.text = text

            if (extraNavigationItem == null) vContainer?.addView(item.view)
            else vContainer?.addView(item.view, vContainer!!.childCount - 1)
            (item.view?.layoutParams as LinearLayout.LayoutParams).weight = 1f
            (item.view?.layoutParams as LinearLayout.LayoutParams).gravity = Gravity.CENTER

            iconsList.add(item)

            return item
        }
    }


    override fun updateIcons() {
        val currentScreen = Navigator.getCurrent()
        var found = false
        getExtraNavigationItem()?.vIcon?.setFilter(getIconsColor())
        for (i in iconsList) {
            if (currentScreen != null && i.targetScreens.contains(currentScreen::class)) {
                i.vIcon?.setFilter(getIconsColorTarget())
                found = true
            } else i.vIcon?.setFilter(getIconsColor())
        }
        if (!found) for (i in iconsList) if (i.isDefoultTargetItem) i.vIcon?.setFilter(getIconsColorTarget())
    }

    override fun getExtraNavigationItem() = extraNavigationItem

    inner class NavigationItem : SActivityType.NavigationItem() {

        var menuIndex: Int? = null

        override fun setVisible(visible: Boolean) {
            view?.visibility = if (visible) View.VISIBLE else View.GONE
            if (menuIndex != null && widgetMenu != null) widgetMenu!!.setItemVisible(menuIndex!!, visible)
        }

        override fun setChipText(text: String) {
            super.setChipText(text)
            if (menuIndex != null && widgetMenu != null) widgetMenu!!.getMenuItem(menuIndex!!).setChipText(text)
        }

    }
}