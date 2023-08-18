package com.sup.dev.android.views.support.animation

import android.view.View
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.java.classes.Subscription
import com.sup.dev.java.classes.animation.AnimationSpring
import com.sup.dev.java.tools.ToolsThreads

class AnimationViewHideVertical(private val view: View) {

    private val spring: AnimationSpring

    private var onVisibleChange: ((Boolean)->Unit)? = null
    private var onVisibleStartChange: ((Boolean)->Unit)? = null
    private var animationTime = ToolsView.ANIMATION_TIME.toLong()
    private var autoHideMs: Long = 0

    private var shovedM = true
    private var subscriptionAutoHide: Subscription? = null
    private var subscriptionAnimation: Subscription? = null
    private var lastH = 0

    //
    //  Getters
    //

    var isShoved: Boolean
        get() = shovedM
        private set(b) {

            if (onVisibleStartChange != null) onVisibleStartChange!!.invoke(b)
            if (subscriptionAnimation != null) subscriptionAnimation!!.unsubscribe()

            spring.to((if (b) 0 else view.height).toFloat())

            subscriptionAnimation = ToolsThreads.timerMain(17) { subscription ->
                spring.update()
                view.y = spring.value
                if (!spring.isNeedUpdate()) {
                    subscription.unsubscribe()
                    if (onVisibleChange != null) onVisibleChange!!.invoke(shovedM)
                }
            }
        }

    init {

        spring = AnimationSpring(0f, AnimationSpring.SpeedType.TIME_MS, animationTime.toFloat())

        view.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            if (lastH != view.height) {
                lastH = view.height
                isShoved = shovedM
            }
        }
    }

    @JvmOverloads
    fun switchShow(animated: Boolean = true) {
        if (shovedM)
            hide(animated)
        else
            show(animated)
    }

    @JvmOverloads
    fun show(animated: Boolean = true) {

        if (shovedM) return

        shovedM = true
        spring.setSpeed(AnimationSpring.SpeedType.TIME_MS, (if (animated) animationTime else 0).toFloat())
        updateAutoHide()
        isShoved = true
    }

    @JvmOverloads
    fun hide(animated: Boolean = true) {

        if (!shovedM) return

        shovedM = false
        spring.setSpeed(AnimationSpring.SpeedType.TIME_MS, (if (animated) animationTime else 0).toFloat())
        updateAutoHide()
        isShoved = false
    }

    fun updateAutoHide() {

        if (subscriptionAutoHide != null) subscriptionAutoHide!!.unsubscribe()

        if (autoHideMs > 0) subscriptionAutoHide = ToolsThreads.main(autoHideMs) { hide() }

    }

    //
    //  Setters
    //

    fun setOnVisibleStartChange(onVisibleStartChange: ((Boolean)->Unit)?) {
        this.onVisibleStartChange = onVisibleStartChange
    }

    fun setOnVisibleChange(onVisibleChange: ((Boolean)->Unit)?) {
        this.onVisibleChange = onVisibleChange
    }

    fun setAutoHide(autoHideMs: Long) {
        this.autoHideMs = autoHideMs
        updateAutoHide()
    }

    fun setAnimationTime(animationTime: Long) {
        this.animationTime = animationTime
    }
}
