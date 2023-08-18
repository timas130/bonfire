package com.sup.dev.android.libs.screens.navigator

import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.java.classes.callbacks.CallbacksList2
import com.sup.dev.java.tools.ToolsThreads
import java.util.ArrayList
import kotlin.reflect.KClass

object Navigator {

    var currentStack = NavigatorStack()
        private set

    val TO = NavigationAction.to().immutable()
    val SET = NavigationAction.set().immutable()
    val REPLACE = NavigationAction.replace().immutable()
    val REORDER = NavigationAction.reorder().immutable()
    val TO_BACK_STACK_OR_NEW = NavigationAction.toBackStackOrNew().immutable()

    //
    //  Listeners
    //

    private val onBack = CallbacksList2<Screen, Screen>()
    private val onBackCallbacks = ArrayList<() -> Boolean>()
    private val onScreenChangedCallbacks = ArrayList<() -> Boolean>()

    enum class Animation {
        IN, OUT, ALPHA, NONE
    }

    //
    //  Stack Actions
    //

    private fun removeScreen(screen: Screen) {
        screen.onPause()
        screen.onStop()
        screen.onDestroy()
        currentStack.stack.remove(screen)
    }

    private fun removeAllScreens(screenClass: KClass<out Screen>) {
        var i = 0
        while (i < currentStack.stack.size) {
            if (currentStack.stack[i]::class == screenClass) {
                removeScreen(currentStack.stack[i--])
            }
            i++
        }
    }


    //
    //  Navigation
    //

    fun action(action: NavigationAction, screen: Screen) {
        action.doAction(screen)
    }

    @JvmOverloads
    fun to(screen: Screen, animation: Animation = Animation.IN) {
        if (currentStack.stack.isNotEmpty()) {
            if (!getCurrent()!!.isBackStackAllowed) {
                removeScreen(getCurrent()!!)
            } else {
                getCurrent()!!.onPause()
                getCurrent()!!.onStop()
            }
            if (screen.isSingleInstanceInBackStack) {
                removeAllScreens(screen::class)
            }
        }
        currentStack.stack.add(screen)
        setCurrentViewNew(animation)
    }

    fun replace(screen: Screen, newScreen: Screen) {
        if (currentStack.stack.isEmpty()) return
        if (getCurrent() == screen) {
            replace(newScreen)
            return
        }
        for (i in currentStack.stack.indices) if (currentStack.stack[i] == screen) currentStack.stack[i] = newScreen
    }

    fun replace(screen: Screen) {
        if (currentStack.stack.isNotEmpty()) removeScreen(getCurrent()!!)

        if (screen.isSingleInstanceInBackStack) removeAllScreens(screen::class)

        currentStack.stack.add(screen)
        setCurrentViewNew(Animation.ALPHA)
    }

    fun set(screen: Screen, animation: Animation = Animation.ALPHA) {
        while (currentStack.stack.size != 0) removeScreen(currentStack.stack[0])
        to(screen, animation)
    }

    fun reorder(screen: Screen) {
        currentStack.stack.remove(screen)
        to(screen)
    }

    fun toBackStackOrNew(screen: Screen) {
        reorderOrCreate(screen::class) { screen }
    }

    fun reorderOrCreate(viewClass: KClass<out Screen>, provider: () -> Screen) {
        if (getCurrent() != null && getCurrent()!!::class == viewClass)
            return

        for (i in currentStack.stack.size - 1 downTo -1 + 1)
            if (currentStack.stack[i]::class == viewClass) {
                reorder(currentStack.stack[i])
                return
            }

        to(provider.invoke())
    }

    fun removeAllEqualsAndTo(view: Screen) {

        var i = 0
        while (i < currentStack.stack.size) {
            if (currentStack.stack[i].equalsNView(view))
                remove(currentStack.stack[i--])
            i++
        }

        to(view)
    }

    fun removeAll(screenClass: KClass<out Screen>) {
        val current = getCurrent()
        val needUpdate = current != null && current::class == screenClass

        var i = 0
        while (i < currentStack.stack.size) {
            if (currentStack.stack[i]::class == screenClass) {
                remove(currentStack.stack[i--])
            }
            i++
        }

        if (needUpdate) setCurrentViewNew(Animation.OUT)
    }


    fun back(): Boolean {
        val current = getCurrent()
        if (current != null) removeScreen(current)
        if (currentStack.stack.size == 0) return false
        setCurrentViewNew(Animation.OUT)

        onBack.invoke(current, current)

        return true
    }

    fun remove(screen: Screen) {
        if (hasBackStack() && getCurrent() == screen) {
            back()
        } else {
            removeScreen(screen)
            if (currentStack.isEmpty()) SupAndroid.activity?.onLastBackPressed(screen)
        }
    }

    fun setStack(stack: NavigatorStack) {
        if (currentStack == stack) return
        val oldStack = currentStack
        currentStack = stack
        for (screen in oldStack.stack) screen.onStackChanged()
        setCurrentViewNew(Animation.ALPHA)
    }

    //
    //  Activity Callbacks
    //

    private fun setCurrentViewNew(animation: Animation) {
        setCurrentView(animation, true)

        val array = Array(onScreenChangedCallbacks.size) { onScreenChangedCallbacks[it] }
        for (i in array) if (i.invoke()) onScreenChangedCallbacks.remove(i)
    }

    fun resetCurrentView() {
        setCurrentView(Animation.NONE, false)
    }

    private fun setCurrentView(animation: Animation, hideDialogs: Boolean) {
        val screen = getCurrent() ?: return

        SupAndroid.activity!!.setScreen(screen, animation, hideDialogs)

        if (getCurrent() != null) ToolsThreads.main(true) {
            if (!screen.wasShowed) {
                screen.onFirstShow()
                screen.wasShowed = true
            }
            screen.onResume()
        }   //  В следующем проходе, чтоб все успело инициализироваться
    }

    fun onActivityStop() {
        getCurrent()?.onStop()
    }

    fun onActivityPaused() {
        getCurrent()?.onPause()
    }

    fun onActivityResumed() {
        getCurrent()?.onResume()
    }

    fun parseOnBackPressedCallbacks():Boolean{
        for (i in onBackCallbacks.size - 1 downTo -1 + 1) {
            val c = onBackCallbacks[i]
            if (c.invoke()) {
                onBackCallbacks.remove(c)
                return true
            }
        }
        return false
    }

    fun onBackPressed(): Boolean {

        return getCurrent() != null && getCurrent()!!.onBackPressed() || back()
    }

    //
    //  Getters
    //


    fun getStackSize(): Int {
        return currentStack.stack.size
    }

    fun getPrevious(): Screen? {
        return if (hasPrevious()) currentStack.stack[currentStack.stack.size - 2] else null
    }

    fun getCurrent(): Screen? {
        return if (currentStack.stack.isEmpty()) null else currentStack.stack[currentStack.stack.size - 1]
    }

    fun getLast(screenClass: KClass<out Screen>): Screen? {
        for (i in currentStack.stack.indices.reversed()) if (currentStack.stack[i]::class == screenClass) return currentStack.stack[i]
        return null
    }

    fun isEmpty(): Boolean {
        return currentStack.stack.isEmpty()
    }

    fun hasInBackStack(screenClass: KClass<out Screen>): Boolean {
        for (i in currentStack.stack) if (i::class == screenClass) return true
        return false
    }

    fun hasBackStack(): Boolean {
        return currentStack.stack.size > 1
    }

    fun hasPrevious(): Boolean {
        return currentStack.stack.size > 1
    }

    fun addOnBackScreenListener(onBack: (Screen?, Screen?) -> Unit) {
        Navigator.onBack.remove(onBack)
        Navigator.onBack.add(onBack)
    }

    fun removeOnBackScreenListener(onBack: (Screen?, Screen?) -> Unit) {
        Navigator.onBack.remove(onBack)
    }

    fun addOnBack(onBack: () -> Boolean) {
        if (onBackCallbacks.contains(onBack)) onBackCallbacks.remove(onBack)
        onBackCallbacks.add(onBack)
    }

    fun removeOnBack(onBack: () -> Boolean) {
        onBackCallbacks.remove(onBack)
    }

    fun addOnScreenChanged(onScreenChanged: () -> Boolean) {
        if (onScreenChangedCallbacks.contains(onScreenChanged)) onScreenChangedCallbacks.remove(onScreenChanged)
        onScreenChangedCallbacks.add(onScreenChanged)
    }

    fun removeOnScreenChanged(onScreenChanged: () -> Boolean) {
        onScreenChangedCallbacks.remove(onScreenChanged)
    }

}