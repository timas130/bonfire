package com.sup.dev.android.views.splash

import android.view.View
import android.widget.TextView
import androidx.annotation.*
import com.sup.dev.android.R
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.CardSplash
import com.sup.dev.android.views.screens.SSplash
import com.sup.dev.android.views.splash.view.*
import com.sup.dev.android.views.splash.view.SplashViewPopup.Companion.hasSplashViewPopup
import com.sup.dev.android.views.views.layouts.LayoutCorned
import com.sup.dev.java.tools.ToolsThreads

abstract class Splash(layoutRes: Int) {

    val view: View
    protected val vTitle: TextView?

    private var onHide: (Splash) -> Unit = {}
    var isEnabled = true
    var isCancelable = true
    private var isHided = true
    var isCompanion = false
    var noBackground = false
    private var maxW: Int? = null
    private var maxH: Int? = null
    private var minW: Int? = null
    private var minH: Int? = null
    protected var viewWrapper: SplashViewWrapper<out Any>? = null
    //  Popup
    var popupYMirrorOffset = 0
    var allowPopupMirrorHeight = false

    init {
        view = if (layoutRes > 0) ToolsView.inflate(layoutRes) else instanceView()!!
        view.isClickable = true //  Чтоб не закрывался при косании
        vTitle = findViewByIdNullable(R.id.vTitle)

        if (vTitle != null) {
            vTitle.text = null
            vTitle.visibility = View.GONE
        }
    }

    protected open fun instanceView(): View? {
        return null
    }

    fun hide() {
        isHided = true
        if (viewWrapper != null) viewWrapper!!.hide()
    }

    protected fun <K : View> findViewById(@IdRes id: Int): K {
        return view.findViewById(id)
    }

    protected fun <K : View> findViewByIdNullable(@IdRes id: Int): K? {
        return view.findViewById(id)
    }

    //
    //  Getters
    //

    fun isHided() = isHided

    fun isShoved() = !isHided

    fun getMaxW() = maxW
    fun getMaxH() = maxH
    fun getMinW() = minW
    fun getMinH() = minH

    //
    //  Callbacks
    //

    open fun onTryCancelOnTouchOutside(): Boolean {
        return true
    }

    open fun onBackPressed(): Boolean {
        return false
    }

    @CallSuper
    open fun onShow() {
        isHided = false
        if (vTitle != null) {
            ToolsView.setTextOrGone(vTitle, vTitle.text)
            if (viewWrapper is SSplash) {
                vTitle.visibility = View.GONE
                (viewWrapper as SSplash).setTitle(vTitle.text.toString())
            }
        }

    }

    @CallSuper
    open fun onHide() {
        isHided = true
        onHide.invoke(this)
    }

    //
    //  Setters
    //

    open fun makeCompanion(): Splash {
        this.isCompanion = true
        return this
    }

    open fun setTitle(@StringRes title: Int): Splash {
        return setTitle(ToolsResources.s(title))
    }

    open fun setTitle(title: String?): Splash {
        if (vTitle != null) ToolsView.setTextOrGone(vTitle, title)
        return this
    }

    fun setTitleGravity(gravity: Int): Splash {
        vTitle?.gravity = gravity
        return this
    }

    open fun setTitleBackgroundColorRes(@ColorRes color: Int): Splash {
        return setTitleBackgroundColor(ToolsResources.getColor(color))
    }

    open fun setTitleBackgroundColor(@ColorInt color: Int): Splash {
        vTitle?.setBackgroundColor(color)
        return this
    }

    open fun setOnHide(onHide: (Splash) -> Unit): Splash {
        this.onHide = onHide
        return this
    }

    open fun setEnabled(enabled: Boolean): Splash {
        this.isEnabled = enabled
        ToolsThreads.main {
            if (vTitle != null) vTitle.isEnabled = enabled
            if (viewWrapper != null) viewWrapper!!.setWidgetEnabled(enabled)
        }
        return this
    }

    open fun setCancelable(cancelable: Boolean): Splash {
        this.isCancelable = cancelable
        ToolsThreads.main {
            if (viewWrapper != null) viewWrapper!!.setWidgetCancelable(cancelable)
        }
        return this
    }

    open fun setTitleSize(sp: Float): Splash {
        vTitle?.textSize = sp
        return this
    }

    open fun allowPopupMirrorHeight(): Splash {
        this.allowPopupMirrorHeight = true; return this
    }

    open fun setPopupYMirrorOffset(offset:Int): Splash {
        this.popupYMirrorOffset = offset;return this
    }

    //
    //  Sizes
    //

    open fun setMaxSizes(maxW: Int?, maxH: Int?): Splash {
        this.maxW = maxW
        this.maxH = maxH
        return this
    }

    open fun setMinSizes(minW: Int?, minH: Int?): Splash {
        this.minW = minW
        this.minH = minH
        return this
    }

    open fun setSizes(w: Int?, h: Int?): Splash {
        this.maxW = w
        this.maxH = h
        this.minW = w
        this.minH = h
        return this
    }

    open fun setMaxW(maxW: Int?): Splash {
        this.maxW = maxW; return this
    }

    open fun setMaxH(maxH: Int?): Splash {
        this.maxH = maxH; return this
    }

    open fun setMinW(minW: Int?): Splash {
        this.minW = minW; return this
    }

    open fun setMinH(minH: Int?): Splash {
        this.minH = minH; return this
    }

    open fun setSizeW(w: Int?): Splash {
        this.maxW = w;this.minW = w; return this
    }

    open fun setSizeH(h: Int?): Splash {
        this.maxH = h;this.minH = h; return this
    }

    //
    //  Support
    //

    fun fixForAndroid9(): Splash {   //  В Android 9 есть баг, свзяанный с clipPath у LayoutCorned. Из-за него может пропадать часть View внутри диалогов.
        ToolsThreads.main(true) {
            val vX: LayoutCorned? = ToolsView.findViewOnParents(view, R.id.vSplashViewContainer)
            vX?.makeSoftware()
        }
        return this
    }

    fun asSheet(): SplashViewSheet {
        val sheet = SplashViewSheet(this)
        this.viewWrapper = sheet
        return sheet
    }

    open fun asSheetShow(): SplashViewSheet {
        val sheet = asSheet()
        sheet.show()
        return sheet
    }

    fun asSplash(): SplashViewScreen {
        val splash = SplashViewScreen(this)
        this.viewWrapper = splash
        return splash
    }

    open fun asSplashShow(): SplashViewScreen {
        val splash = asSplash()
        splash.show()
        return splash
    }

    fun asDialog(): SplashViewDialog {
        val dialog = SplashViewDialog(this)
        this.viewWrapper = dialog
        return dialog
    }

    fun asDialogShow(): SplashViewDialog {
        val dialog = asDialog()
        dialog.show()
        return dialog
    }

    fun asPopup(): SplashViewPopup {
        val popup = SplashViewPopup(this)
        this.viewWrapper = popup
        return popup
    }

    fun asPopupShow(view: View) = asPopupShow(view, 0, 0)

    fun asPopupShow(view: View, x: Float, y: Float) = asPopupShow(view, x.toInt(), y.toInt())

    fun asPopupShow(view: View, x: Int, y: Int): SplashViewPopup? {
        if (SupAndroid.activity?.hasSplashViewPopup() == true) return null
        val popup = asPopup()
        popup.setAnchor(view, x, y)
        popup.show()
        return popup
    }

    fun showPopupWhenClick(view: View, willShow: (() -> Boolean)? = null): Splash {
        ToolsView.setOnClickCoordinates(view) { view1, x, y ->
            if (willShow == null || willShow.invoke()) asPopup().setAnchor(view1, x, y).show()
        }
        return this
    }

    fun showPopupWhenLongClick(view: View): Splash {
        ToolsView.setOnLongClickCoordinates(view) { view1, x, y ->
            asPopup().setAnchor(view1, x, y).show()
            Unit
        }
        return this
    }

    fun showPopupWhenClickAndLongClick(view: View, willShowClick: () -> Boolean): Splash {
        return showPopupWhenClickAndLongClick(view, willShowClick, null)
    }

    fun showPopupWhenClickAndLongClick(view: View, willShowClick: (() -> Boolean)?, willShowLongClick: (() -> Boolean)?): Splash {
        ToolsView.setOnClickAndLongClickCoordinates(view,
                { if (willShowClick == null || willShowClick.invoke()) asPopup().setAnchor(it.view, it.x, it.y).show() },
                { if (willShowLongClick == null || willShowLongClick.invoke()) asPopup().setAnchor(it.view, it.x, it.y).show() })
        return this
    }

    fun showSheetWhenClickAndLongClick(view: View, willShowClick: () -> Boolean): Splash {
        return showSheetWhenClickAndLongClick(view, willShowClick, null)
    }

    fun showSheetWhenClickAndLongClick(view: View, willShowClick: (() -> Boolean)?, willShowLongClick: (() -> Boolean)?): Splash {
        ToolsView.setOnClickAndLongClickCoordinates(view,
                {if (willShowClick == null || willShowClick.invoke()) asSheetShow()},
                {if (willShowLongClick == null || willShowLongClick.invoke()) asSheetShow()})
        return this
    }

    fun asScreen(): SSplash {
        val screen = SSplash(this)
        this.viewWrapper = screen
        return screen
    }

    fun asScreenTo(): SSplash {
        val screen = asScreen()
        Navigator.to(screen)
        return screen
    }

    fun asScreen(action: NavigationAction): SSplash {
        val screen = asScreen()
        Navigator.action(action, screen)
        return screen
    }

    fun asCard(): CardSplash {
        val card = CardSplash(this)
        this.viewWrapper = card
        return card
    }


}
