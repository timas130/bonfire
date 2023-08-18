package com.sup.dev.android.views.splash

import androidx.annotation.StringRes
import android.view.View
import android.widget.Button
import android.widget.TimePicker
import com.sup.dev.android.R
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView

class SplashChooseTime : Splash(R.layout.splash_choose_time) {

    private val vTimePicker: TimePicker = view.findViewById(R.id.vTimePicker)
    private val vCancel: Button = view.findViewById(R.id.vCancel)
    private val vEnter: Button = view.findViewById(R.id.vEnter)

    private var autoHideOnEnter = true

    init {

        vCancel.visibility = View.GONE
        vEnter.visibility = View.GONE

        vTimePicker.setIs24HourView(true)
    }


    @Suppress("DEPRECATION")
    fun setTime(h: Int, m: Int): SplashChooseTime {
        vTimePicker.currentHour = h
        vTimePicker.currentMinute = m
        return this
    }

    fun setOnEnter(@StringRes s: Int): SplashChooseTime {
        return setOnEnter(ToolsResources.s(s))
    }

    fun setOnEnter(@StringRes s: Int, onEnter: (SplashChooseTime, Int, Int) -> Unit): SplashChooseTime {
        return setOnEnter(ToolsResources.s(s), onEnter)
    }

    @JvmOverloads
    @Suppress("DEPRECATION")
    fun setOnEnter(s: String?, onEnter: (SplashChooseTime, Int, Int) -> Unit = { _, _, _ -> }): SplashChooseTime {
        ToolsView.setTextOrGone(vEnter, s)
        vEnter.setOnClickListener {
            if (autoHideOnEnter)
                hide()
            else
                setEnabled(false)
            onEnter.invoke(this, vTimePicker.currentHour, vTimePicker.currentMinute)
        }

        return this
    }

    fun setAutoHideOnEnter(autoHideOnEnter: Boolean): SplashChooseTime {
        this.autoHideOnEnter = autoHideOnEnter
        return this
    }

    fun setOnCancel(onCancel: (SplashChooseTime) -> Unit = {}): SplashChooseTime {
        return setOnCancel(null, onCancel)
    }

    fun setOnCancel(@StringRes s: Int, onCancel: (SplashChooseTime) -> Unit): SplashChooseTime {
        return setOnCancel(ToolsResources.s(s), onCancel)
    }

    @JvmOverloads
    fun setOnCancel(s: String?, onCancel: (SplashChooseTime) -> Unit = {}): SplashChooseTime {
        super.setOnHide { onCancel.invoke(this) }
        ToolsView.setTextOrGone(vCancel, s)
        vCancel.setOnClickListener {
            hide()
            onCancel.invoke(this)
        }
        return this
    }


}