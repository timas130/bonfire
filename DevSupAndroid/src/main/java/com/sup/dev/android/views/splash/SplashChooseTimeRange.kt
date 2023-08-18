package com.sup.dev.android.views.splash

import androidx.annotation.StringRes
import android.view.View
import android.widget.Button
import com.sup.dev.android.R
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.java.tools.ToolsDate


class SplashChooseTimeRange : Splash(R.layout.splash_choose_time_range) {

    private val vStart: Button = view.findViewById(R.id.vStart)
    private val vEnd: Button = view.findViewById(R.id.vEnd)
    private val vCancel: Button = view.findViewById(R.id.vCancel)
    private val vEnter: Button = view.findViewById(R.id.vEnter)

    private var h1 = 0
    private var m1 = 0
    private var h2 = 0
    private var m2 = 0

    private var autoHideOnEnter = true

    init {

        vCancel.visibility = View.GONE
        vEnter.visibility = View.GONE

        vStart.setOnClickListener {
            SplashChooseTime()
                .setOnCancel(SupAndroid.TEXT_APP_CANCEL)
                .setOnEnter(SupAndroid.TEXT_APP_CHOOSE) { _, h, m -> setTimeStart(h, m) }
                .asSheetShow()
        }

        vEnd.setOnClickListener {
            SplashChooseTime()
                .setOnCancel(SupAndroid.TEXT_APP_CANCEL)
                .setOnEnter(SupAndroid.TEXT_APP_CHOOSE) { _, h, m -> setTimeEnd(h, m) }
                .asSheetShow()
        }

        setTimeStart(12, 0)
        setTimeEnd(24, 0)
    }

    //
    //  Setters
    //

    fun setTimeStart(h: Int, m: Int): SplashChooseTimeRange {
        h1 = h
        m1 = m
        vStart.text = ToolsDate.timeToString(h, m)
        return this
    }

    fun setTimeEnd(h: Int, m: Int): SplashChooseTimeRange {
        h2 = h
        m2 = m
        vEnd.text = ToolsDate.timeToString(h, m)
        return this
    }

    fun setOnEnter(@StringRes s: Int): SplashChooseTimeRange {
        return setOnEnter(ToolsResources.s(s))
    }

    fun setOnEnter(@StringRes s: Int, onEnter: (SplashChooseTimeRange, Int, Int, Int, Int) -> Unit): SplashChooseTimeRange {
        return setOnEnter(ToolsResources.s(s), onEnter)
    }

    @JvmOverloads
    fun setOnEnter(
        s: String?,
        onEnter: (SplashChooseTimeRange, Int, Int, Int, Int) -> Unit = { _, _, _, _, _ -> }
    ): SplashChooseTimeRange {
        ToolsView.setTextOrGone(vEnter, s)
        vEnter.setOnClickListener {
            if (autoHideOnEnter)
                hide()
            else
                setEnabled(false)
            onEnter.invoke(this, h1, m1, h2, m2)
        }
        return this
    }

    fun setAutoHideOnEnter(autoHideOnEnter: Boolean): SplashChooseTimeRange {
        this.autoHideOnEnter = autoHideOnEnter
        return this
    }

    fun setOnCancel(onCancel: (SplashChooseTimeRange) -> Unit = {}): SplashChooseTimeRange {
        return setOnCancel(null, onCancel)
    }

    fun setOnCancel(@StringRes s: Int, onCancel: (SplashChooseTimeRange) -> Unit): SplashChooseTimeRange {
        return setOnCancel(ToolsResources.s(s), onCancel)
    }

    @JvmOverloads
    fun setOnCancel(s: String?, onCancel: (SplashChooseTimeRange) -> Unit = {}): SplashChooseTimeRange {
        super.setOnHide { onCancel.invoke(this) }
        ToolsView.setTextOrGone(vCancel, s)
        vCancel.setOnClickListener {
            hide()
            onCancel.invoke(this)
        }
        return this
    }

}
