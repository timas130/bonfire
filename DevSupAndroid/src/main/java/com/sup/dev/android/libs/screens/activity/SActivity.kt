package com.sup.dev.android.libs.screens.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.sup.dev.android.R
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.models.EventActivityPause
import com.sup.dev.android.models.EventActivityResume
import com.sup.dev.android.models.EventConfigurationChanged
import com.sup.dev.android.tools.*
import com.sup.dev.android.views.splash.view.SplashView
import com.sup.dev.android.views.views.draw_animations.ViewDrawAnimations
import com.sup.dev.java.classes.Subscription
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsThreads

abstract class SActivity : AppCompatActivity() {

    companion object {
        var onUrlClicked: ((String) -> Unit)? = null
    }

    var started = false

    var isFullScreen = false
    var screenStatusBarIsLight = 0
    var screenStatusBarColor = 0

    var vActivityRoot: View? = null
    var vActivityDrawAnimations: ViewDrawAnimations? = null
    var vActivityContainer: ViewGroup? = null
    var vSplashContainer: ViewGroup? = null
    var vActivityTouchLock: View? = null
    var parseNotifications = true
    var type = getNavigationType()

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        Navigator.currentStack.clear()
        SupAndroid.activity = this
        SupAndroid.activityIsDestroy = false

        applyTheme()

        setContentView(type.getLayout())
        vActivityRoot = findViewById(R.id.vActivityRoot)
        vActivityDrawAnimations = findViewById(R.id.vActivityDrawAnimations)
        vActivityContainer = findViewById(R.id.vScreenActivityView)
        vActivityTouchLock = findViewById(R.id.vScreenActivityTouchLock)
        vSplashContainer = findViewById(R.id.vSplashContainer)

        vActivityTouchLock!!.visibility = View.GONE

        type.onCreate()

        ToolsThreads.main(true) {
            if (parseIntent(intent)) intent = Intent()
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        //  Не потдерживает востановление состояния.
    }

    protected open fun getNavigationType(): SActivityType = SActivityTypeSimple(this)

    override fun onStart() {
        super.onStart()

        SupAndroid.activity = this  //  Активность может поменяться в случае запуска коппии активности по интенту (Например для виджетов)
        SupAndroid.activityIsVisible = true

        if (!started) {
            started = true
            onFirstStart()
        } else {
            if (vActivityContainer?.childCount == 0) Navigator.resetCurrentView()
            else Navigator.getCurrent()?.onResume()
        }
        if (Navigator.getStackSize() == 0) toMainScreen()

    }

    override fun onPause() {
        super.onPause()
        Navigator.onActivityPaused()
        EventBus.post(EventActivityPause())
    }

    override fun onResume() {
        super.onResume()
        Navigator.onActivityResumed()
        EventBus.post(EventActivityResume())
    }

    override fun onStop() {
        super.onStop()
        removeViews(getOldViews(Navigator.getCurrent()))
        SupAndroid.activityIsVisible = false
        Navigator.onActivityStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        SupAndroid.activityIsDestroy = true
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        EventBus.post(EventConfigurationChanged())
    }

    protected open fun applyTheme() {
    }

    protected open fun onFirstStart() {

    }

    abstract fun toMainScreen()

    fun getViewRoot(): View? {
        return vActivityRoot
    }

    fun getViewContainer(): View? {
        return vActivityContainer
    }

    override fun startActivity(intent: Intent) {
        if (TextUtils.equals(intent.action, Intent.ACTION_VIEW) && onUrlClicked != null && intent.data != null) {
            onUrlClicked!!.invoke(intent.data!!.toString())
        } else {
            super.startActivity(intent)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (parseIntent(intent)) setIntent(Intent())
    }

    open fun parseIntent(intent: Intent?): Boolean {
        if (parseNotifications && intent != null) return ToolsNotifications.parseNotification(intent)
        return false
    }

    //
    //  Events
    //

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        ToolsIntent.onActivityResult(requestCode, resultCode, intent)
        super.onActivityResult(requestCode, resultCode, intent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        ToolsPermission.onRequestPermissionsResult(requestCode, permissions, grantResults.toTypedArray())
    }

    open fun onViewBackPressed() {
        type.onViewBackPressed()
    }

    override fun onBackPressed() {
        if(onBackPressedSplash()) return
        if(type.onBackPressed()) return
        if(Navigator.parseOnBackPressedCallbacks()) return
        onBackPressedScreen()
    }

    open fun onLastBackPressed(screen: Screen?): Boolean {
        return false
    }

    //
    //  Splash
    //

    open fun onBackPressedSplash():Boolean{
        if( vSplashContainer!!.childCount > 0){
            val splash = vSplashContainer!!.getChildAt(vSplashContainer!!.childCount-1).tag as SplashView<out Any>
            return splash.onBackPressed()
        }
        return false
    }

    fun addSplash(splashView: SplashView<out Any>) {
        splashView.getView().tag = splashView
        ToolsThreads.main {
            if (!splashView.splash.isCompanion) ToolsView.hideKeyboard()
            splashView.getView().visibility = View.INVISIBLE
            if (!splashView.splash.isCompanion) {
                vActivityContainer!!.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
                getToSplash()?.getView()?.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
            }
            vSplashContainer!!.addView(splashView.getView())
            if (!splashView.splash.isCompanion) {
                val navigationBarColor = splashView.getNavigationBarColor()
                ToolsThreads.main(splashView.animationMs / 2) { if (navigationBarColor != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) window.navigationBarColor = navigationBarColor }
            }
            ToolsView.fromAlpha(splashView.getView(), splashView.animationMs.toInt()){
                if (!splashView.splash.isCompanion) {splashView.getView().requestFocus() }
            }
        }
    }

    fun removeSplash(splashView: SplashView<out Any>) {
        ToolsThreads.main {
            if (!splashView.splash.isCompanion) {
                ToolsView.hideKeyboard()
                ToolsThreads.main(splashView.animationMs / 2) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && vSplashContainer!!.childCount == 1 && Navigator.getCurrent() != null) window.navigationBarColor = Navigator.getCurrent()!!.navigationBarColor
                }
            }
            ToolsView.toAlpha(splashView.getView(), splashView.animationMs.toInt()) {
                vSplashContainer!!.removeView(splashView.getView())
                if (!splashView.splash.isCompanion) {
                    if (vSplashContainer!!.childCount == 0) vActivityContainer!!.descendantFocusability = ViewGroup.FOCUS_BEFORE_DESCENDANTS
                    else getToSplash()?.getView()?.descendantFocusability = ViewGroup.FOCUS_BEFORE_DESCENDANTS
                }
            }
            splashView.onHide()
        }
    }

    fun hideAllSplash(){
        var splash:SplashView<out Any>? = getToSplash()
        while (splash != null){
            removeSplash(splash)
            splash = getToSplash()
        }
    }

    fun getToSplash():SplashView<out Any>?{
        if( vSplashContainer!!.childCount > 0){
            return vSplashContainer!!.getChildAt(vSplashContainer!!.childCount - 1).tag as SplashView<out Any>
        }
        return null
    }

    fun isSplashShowed(splashView: SplashView<out Any>) = vSplashContainer!!.indexOfChild(splashView.getView()) > -1

    fun isTopSplash(splashView: SplashView<out Any>) = vSplashContainer!!.childCount > 0 && vSplashContainer!!.indexOfChild(splashView.getView()) == (vSplashContainer!!.childCount - 1)

    //
    //  Screens
    //

    private var subscriptionTouchLock: Subscription? = null

    open fun onBackPressedScreen(){
        val screen = Navigator.getCurrent()
        val b1 = Navigator.onBackPressed()
        val b2 = b1 || onLastBackPressed(screen)
        if (!b2) {
            started = false
            finish()
        }
    }

    private fun getOldViews(screen: Screen?): ArrayList<View> {
        val oldViews = ArrayList<View>()
        for (i in 0 until vActivityContainer!!.childCount) if (vActivityContainer!!.getChildAt(i) != screen) oldViews.add(vActivityContainer!!.getChildAt(i))
        return oldViews
    }

    private fun removeViews(views: ArrayList<View>) {
        for (v in views)
            try {
                vActivityContainer!!.removeView(v)
            } catch (e: IndexOutOfBoundsException) {
                err(e)
            }
        views.clear()
    }

    open fun setScreen(screen: Screen?, a: Navigator.Animation, hideDialogs: Boolean) {
        var animation = a
        type.onSetScreen(screen)

        if (screen == null) {
            finish()
            return
        }

        if (hideDialogs && vSplashContainer != null)
            for (i in vSplashContainer!!.childCount - 1 downTo 0) {
                val splash = vSplashContainer!!.getChildAt(i).tag as SplashView<out Any>
                removeSplash(splash)
                if (splash.isDestroyScreenAnimation()) animation = Navigator.Animation.NONE
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            screenStatusBarColor = screen.statusBarColor
            if (window.statusBarColor != screenStatusBarColor) window.statusBarColor = screenStatusBarColor
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!isFullScreen) {
                screenStatusBarIsLight = if (screen.statusBarIsLight) View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR else View.SYSTEM_UI_FLAG_VISIBLE
                if (window.decorView.systemUiVisibility != screenStatusBarIsLight) window.decorView.systemUiVisibility = screenStatusBarIsLight
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.navigationBarColor = screen.navigationBarColor
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (screen.navigationBarIsLight) screen.systemUiVisibility = screen.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            else screen.systemUiVisibility = screen.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
        }

        ToolsView.hideKeyboard()

        val oldViews = getOldViews(screen)

        val old = if (vActivityContainer!!.childCount == 0) null else vActivityContainer!!.getChildAt(0)

        if (animation !== Navigator.Animation.IN) vActivityContainer!!.addView(ToolsView.removeFromParent(screen), 0)
        else vActivityContainer!!.addView(ToolsView.removeFromParent(screen))

        if (old != null && old !== screen) {

            vActivityTouchLock!!.visibility = View.VISIBLE
            ToolsView.clearAnimation(old)
            ToolsView.clearAnimation(screen)


            if (animation == Navigator.Animation.NONE) animateNone(oldViews)
            if (animation == Navigator.Animation.OUT) animateOut(screen, old, oldViews)
            if (animation == Navigator.Animation.IN) animateIn(screen, oldViews)
            if (animation == Navigator.Animation.ALPHA) animateAlpha(screen, old, oldViews)
        }

        if (subscriptionTouchLock != null) subscriptionTouchLock!!.unsubscribe()
        subscriptionTouchLock = ToolsThreads.main(ToolsView.ANIMATION_TIME.toLong()) {
            vActivityTouchLock!!.visibility = View.GONE
        }

        vActivityRoot?.setBackgroundColor(screen.activityRootBackground)

        type.updateIcons()
    }

    private fun animateNone(oldViews: ArrayList<View>) {
        removeViews(oldViews)
    }

    private fun animateAlpha(screen: Screen, old: View, oldViews: ArrayList<View>) {
        screen.visibility = View.INVISIBLE
        ToolsView.toAlpha(old) {
            removeViews(oldViews)
        }
        ToolsView.fromAlpha(screen)
    }

    private fun animateOut(screen: Screen, old: View, oldViews: ArrayList<View>) {
        screen.visibility = View.VISIBLE
        old.animate()
                .alpha(0f)
                .translationX((ToolsAndroid.getScreenW() / 3).toFloat())
                .setDuration(200)
                .setInterpolator(LinearOutSlowInInterpolator())
                .setListener(object : AnimatorListenerAdapter() {

                    override fun onAnimationEnd(animation: Animator) {
                        old.animate().setListener(null)
                        old.alpha = 1f
                        old.translationX = 0f
                        removeViews(oldViews)
                    }
                })
    }

    private fun animateIn(screen: Screen, oldViews: ArrayList<View>) {
        screen.alpha = 0f
        screen.translationX = (ToolsAndroid.getScreenW() / 3).toFloat()
        screen.animate()
                .alpha(1f)
                .translationX(0f)
                .setDuration(200)
                .setInterpolator(LinearOutSlowInInterpolator())
                .setListener(object : AnimatorListenerAdapter() {

                    override fun onAnimationEnd(animation: Animator) {
                        screen.animate().setListener(null)
                        screen.alpha = 1f
                        screen.translationX = 0f
                        removeViews(oldViews)
                    }
                })
    }

}
