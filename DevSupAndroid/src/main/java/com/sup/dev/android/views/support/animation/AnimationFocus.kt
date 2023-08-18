package com.sup.dev.android.views.support.animation

import android.graphics.Color
import android.view.MotionEvent
import android.view.View
import com.sup.dev.java.classes.animation.AnimationPendulum
import com.sup.dev.java.classes.animation.AnimationPendulumColor
import com.sup.dev.java.classes.animation.AnimationSpringColor


class AnimationFocus(private val view: View, private val focusColorClick: Int) : View.OnTouchListener {
    private val animationFocus: AnimationSpringColor
    private val animationClick: AnimationPendulumColor
    private val focusColor: Int = Color.argb((Color.alpha(focusColorClick) / 1.5f).toInt(), Color.red(focusColorClick), Color.green(focusColorClick), Color.blue(focusColorClick))
    private val focusColorAlpha: Int = Color.argb(0, Color.red(focusColorClick), Color.green(focusColorClick), Color.blue(focusColorClick))

    private var onTouched: (Boolean)->Unit = {}
    private var touched: Boolean = false

    init {

        animationFocus = AnimationSpringColor(focusColorAlpha, 200)
        animationClick = AnimationPendulumColor(focusColorAlpha, focusColorAlpha, focusColorClick, 150, AnimationPendulum.AnimationType.TO_2_AND_BACK)

        resetOnFocusChangedListener()
        resetTouchListener()
    }

    fun resetTouchListener() {
        view.setOnTouchListener(this)
    }

    fun resetOnFocusChangedListener() {
        view.setOnFocusChangeListener { _, _ -> updateFocusColor() }
    }


    override fun onTouch(view: View, event: MotionEvent): Boolean {
        if (!view.isEnabled || !view.isClickable) {
            touched = false
            updateFocusColor()
            return view.onTouchEvent(event)
        }

        if (event.action == MotionEvent.ACTION_DOWN) {
            touched = true
            updateFocusColor()
        }

        if (event.action == MotionEvent.ACTION_UP && event.x >= 0 && event.x <= view.width && event.y >= 0 && event.y <= view.height) {
            animationClick.set(animationFocus.getColor())
            animationClick.to_2()
        }


        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL
                || event.x < 0 || event.x > view.width || event.y < 0 || event.y > view.height) {
            touched = false
            updateFocusColor()
        }

        view.onTouchEvent(event)
        return true
    }


    fun updateFocusColor() {
        animationFocus.to(if (view.isFocused || touched) focusColor else focusColorAlpha)
        view.invalidate()
        onTouched.invoke(touched)
    }

    fun update(): Int {

        animationFocus.update()
        animationClick.update()

        val color = if (animationClick.a.value > animationFocus.a.value) animationClick.color else animationFocus.getColor()

        if (animationFocus.isNeedUpdate() || animationClick.isNeedUpdate())
            view.invalidate()

        return color
    }

    fun setClickAnimationEnabled(b: Boolean) {
        if (b)
            animationClick.set(focusColorAlpha, focusColorClick)
        else
            animationClick.set(0x00000000, 0x00000000)
    }

    fun setOnTouched(onTouched: (Boolean)->Unit) {
        this.onTouched = onTouched
    }
}
