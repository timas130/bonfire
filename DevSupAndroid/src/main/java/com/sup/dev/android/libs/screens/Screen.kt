package com.sup.dev.android.libs.screens

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import com.google.android.material.appbar.AppBarLayout
import com.sup.dev.android.R
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.libs.screens.activity.SActivity
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView

open class Screen(
        protected val viewScreen: View
) : FrameLayout(SupAndroid.activity!!) {

    private var onBackPressed: () -> Boolean = { false }
    private var onHide: () -> Unit = {}

    //  Params
    var wasShowed = false
    var toolbarNavigationIcon = R.drawable.ic_arrow_back_white_24dp
    var toolbarContentColor = ToolsResources.getColorAttr(R.attr.colorOnPrimary)
    var isToolbarShadowAvailable = true
    var isBackStackAllowed = true
    var hasToolbarBackIcon = true
    var forceBackIcon = false
    var isSingleInstanceInBackStack = false
    var statusBarColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) ToolsResources.getColorAttr(android.R.attr.statusBarColor) else Color.BLACK
    var navigationBarColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) ToolsResources.getColorAttr(android.R.attr.navigationBarColor) else Color.BLACK
    var navigationBarIsLight = ToolsResources.getBooleanAttr(R.attr.themeNavigationBarIsLight)
    var statusBarIsLight = ToolsResources.getBooleanAttr(R.attr.themStatusBarIsLight)
    var useIconsFilter = ToolsResources.getBooleanAttr(R.attr.themNavigationUseIconsFilters)
    //  All activity navigation types
    var activityRootBackground = ToolsResources.getColorAttr(android.R.attr.windowBackground)
    var isNavigationAllowed = true
    var isNavigationVisible = true
    var isNavigationAnimation = false
    var isNavigationShadowAvailable = true
    //  Bottom navigation
    var isHideBottomNavigationWhenKeyboard = true

    protected var isAppbarExpanded: Boolean = false /* Обход разворачивания бара при повторном создании вью */

    constructor(@LayoutRes layoutRes: Int) : this(ToolsView.inflate<View>(SupAndroid.activity!!, layoutRes))

    init {
        addView(viewScreen)
    }

    protected fun removeAppbar() {
        findViewById<View>(R.id.vAppBar).visibility = View.GONE
    }

    protected fun removeAppbarNavigation() {
        hasToolbarBackIcon = false
        updateToolbar()
    }

    private fun updateToolbar(){
        val toolbar: Toolbar? = findViewById(R.id.vToolbar)
        if (toolbar != null) {
            toolbar.setTitleTextColor(toolbarContentColor)
            if (hasToolbarBackIcon) {
                toolbar.navigationIcon = getActivity().type.getNavigationDrawable(this)
                if (useIconsFilter) toolbar.navigationIcon?.setColorFilter(toolbarContentColor, PorterDuff.Mode.SRC_ATOP)
                toolbar.setNavigationOnClickListener { SupAndroid.activity!!.onViewBackPressed() }
            } else {
                toolbar.navigationIcon = null
            }
        }

        val v = findViewById<View>(R.id.vBack)
        if (v != null && v is ImageView) {
            v.setImageDrawable(getActivity().type.getNavigationDrawable(this))
            v.setOnClickListener { SupAndroid.activity!!.onViewBackPressed() }
        }

        val appBarLayout: AppBarLayout? = findViewById(R.id.vAppBar)
        appBarLayout?.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset -> isAppbarExpanded = verticalOffset == 0 })
        if(!isToolbarShadowAvailable){
            appBarLayout?.elevation = 0f
          //  appBarLayout?.background = null
        }
    }

    fun disableNavigation(){
        isNavigationVisible = false
        isNavigationAllowed = false
        isNavigationAnimation = false
    }

    fun disableShadows(){
        isNavigationShadowAvailable = false
        isToolbarShadowAvailable = false
    }

    //
    //  LifeCircle
    //

    @CallSuper
    open fun onFirstShow() {

    }

    @CallSuper
    open fun onResume() {
        updateToolbar()
    }

    @CallSuper
    open fun onPause() {

    }

    @CallSuper
    open fun onStop() {

    }

    @CallSuper
    open fun onStackChanged() {

    }

    @CallSuper
    open fun onDestroy() {
        onHide.invoke()
    }

    open fun onBackPressed(): Boolean {
        return onBackPressed.invoke()
    }

    //
    //  Setters
    //

    open fun setTitle(@StringRes title: Int) {
        setTitle(ToolsResources.s(title))
    }

    open fun setTitle(title: String?) {
        (findViewById<View>(R.id.vToolbar) as Toolbar).title = title
    }

    fun setOnBackPressed(onBackPressed: () -> Boolean) {
        this.onBackPressed = onBackPressed
    }

    fun setOnHide(onHide: () -> Unit): Screen {
        this.onHide = onHide
        return this
    }

    fun setScreenColor(color: Int) {
        findViewById<View>(R.id.vScreenRoot).setBackgroundColor(color)
    }

    fun setScreenColorBackground() {
        setScreenColor(ToolsResources.getColorAttr(android.R.attr.windowBackground))
    }

    //
    //  Getters
    //

    fun equalsNView(view: Screen): Boolean {
        return this === view
    }

    fun getActivity() = context as SActivity

}
