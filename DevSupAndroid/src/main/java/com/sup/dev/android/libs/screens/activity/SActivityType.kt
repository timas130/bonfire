package com.sup.dev.android.libs.screens.activity

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.TextView
import androidx.annotation.CallSuper
import com.sup.dev.android.R
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.views.ViewChipMini
import com.sup.dev.android.views.views.ViewIcon
import kotlin.reflect.KClass

abstract class SActivityType(
        val activity: SActivity
) {

    var screenNavigationVisible = true
    var screenNavigationAllowed = true
    var screenNavigationAnimation = false
    var screenNavigationShadowAvailable = true
    var screenHideBottomNavigationWhenKeyboard = true
    var screenBottomNavigationColor = 0

    private var iconsColorTarget:Int? = null
    private var iconsColor:Int? = null

    abstract fun onCreate()

    @CallSuper
    open fun onSetScreen(screen: Screen?) {
        if (screen != null) {
            screenNavigationVisible = screen.isNavigationVisible
            screenNavigationAllowed = screen.isNavigationAllowed
            screenNavigationAnimation = screen.isNavigationAnimation
            screenNavigationShadowAvailable = screen.isNavigationShadowAvailable
            screenHideBottomNavigationWhenKeyboard = screen.isHideBottomNavigationWhenKeyboard
            screenBottomNavigationColor = screen.navigationBarColor
            updateNavigationVisible()
        }
    }

    open fun updateNavigationVisible() {

    }

    abstract fun getLayout(): Int

    open fun onViewBackPressed() {
        activity.onBackPressed()
    }

    open fun onBackPressed():Boolean{
        return false
    }

    open fun getNavigationDrawable(screen: Screen): Drawable? {
        if (Navigator.hasBackStack() || screen.forceBackIcon) {
            return ToolsResources.getDrawable(screen.toolbarNavigationIcon)
        } else {
            return null
        }
    }

    fun getIconsColorTarget():Int{
        if(iconsColorTarget == null) iconsColorTarget = ToolsResources.getColorAttr(activity, R.attr.colorSecondary)
        return iconsColorTarget!!
    }

    fun getIconsColor():Int{
        if(iconsColor == null) iconsColor = ToolsResources.getColorAttr(activity, R.attr.colorOnPrimary)
        return iconsColor!!
    }

    fun setIconsColorTarget(color:Int){ iconsColorTarget = color }
    fun setIconsColor(color:Int){ iconsColor = color }

    //
    //  Navigation Item
    //

    open fun getExtraNavigationItem(): NavigationItem? {
        return null
    }

    open fun createExtraNavigationItem(useIconsFilters: Boolean, onClick:(()->Unit)?=null): NavigationItem? {
        return null
    }

    open fun addNavigationView(view: View, useIconsFilters: Boolean) {

    }

    open fun addNavigationDivider() {

    }

    fun addNavigationItem(icon: Int, text: Int, hided: Boolean, useIconsFilters: Boolean = false, onClick: (View) -> Unit, onLongClick: ((View) -> Unit)?): NavigationItem {
        return addNavigationItem(icon, ToolsResources.s(text), hided, useIconsFilters, onClick, onLongClick)
    }

    fun addNavigationItem(icon: Int, text: Int, hided: Boolean, useIconsFilters: Boolean = false, onClick: (View) -> Unit): NavigationItem {
        return addNavigationItem(icon, ToolsResources.s(text), hided, useIconsFilters, onClick, null)
    }

    fun addNavigationItem(icon: Int, text: String, hided: Boolean, useIconsFilters: Boolean = false, onClick: (View) -> Unit) = addNavigationItem(icon, text, hided, useIconsFilters, onClick, null)

    abstract fun addNavigationItem(icon: Int, text: String, hided: Boolean, useIconsFilters: Boolean = false, onClick: (View) -> Unit, onLongClick: ((View) -> Unit)?): NavigationItem

    abstract fun updateIcons()

    abstract class NavigationItem {

        val targetScreens = ArrayList<KClass<out Screen>>()
        var isDefoultTargetItem = false
        var view: View? = null
        var vIcon: ViewIcon? = null
        var vChip: ViewChipMini? = null
        var vText: TextView? = null

        abstract fun setVisible(visible: Boolean)

        open fun setChipText(text: String) {
            vChip?.setText(text)
        }

        fun setTargetScreen(vararg screenClass: KClass<out Screen>): NavigationItem {
            targetScreens.addAll(screenClass)
            return this
        }

        fun makeDefaultTargetItem(): NavigationItem {
            isDefoultTargetItem = true
            return this
        }

    }
}