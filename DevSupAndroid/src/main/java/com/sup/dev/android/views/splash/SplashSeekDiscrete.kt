package com.sup.dev.android.views.splash


import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.StringRes
import com.sup.dev.android.R

import com.sup.dev.android.tools.ToolsResources

class SplashSeekDiscrete : Splash(R.layout.splash_seek_district), SeekBar.OnSeekBarChangeListener {

    private val vSeekBar: SeekBar = view.findViewById(R.id.seek)
    private val vMin: TextView = view.findViewById(R.id.min)
    private val vCurrent: TextView = view.findViewById(R.id.current)
    private val vMax: TextView = view.findViewById(R.id.max)
    private val vCancel: Button = view.findViewById(R.id.cancel)
    private val vEnter: Button = view.findViewById(R.id.enter)

    private var currentTextMask: (Int) -> String = { "${getProgress()}" }

    init {

        vCurrent.setTextSize(19f)
        vMax.setTextSize(16f)
        vMin.setTextSize(16f)
        vMax.setText(null)
        vMin.setText(null)
        vCurrent.setText(null)

        vCancel.visibility = View.GONE
        vSeekBar.setOnSeekBarChangeListener(this)
        updateText()
    }

    fun setCurrentTextMask(currentTextMask: (Int) -> String): SplashSeekDiscrete {
        this.currentTextMask = currentTextMask
        return this
    }

    fun setMax(max: Int): SplashSeekDiscrete {
        vSeekBar.max = max;
        if (vMax.text.toString().isEmpty())
            vMax.text = "$max"
        return this
    }

    fun setMaxMask(mask: String): SplashSeekDiscrete {
        vMax.text = mask
        return this
    }

    fun setMinMask(mask: String): SplashSeekDiscrete {
        vMin.text = mask
        return this
    }

    fun setProgress(progress: Int): SplashSeekDiscrete {
        vSeekBar.setProgress(progress)
        updateText()
        return this
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        updateText()
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }


    fun updateText() {
        this.vCurrent.text = this.currentTextMask.invoke(getProgress())
    }


    fun getProgress() = vSeekBar.progress


    //
    //  Setters
    //

    override fun setTitle(@StringRes title: Int): SplashSeekDiscrete {
        return super.setTitle(title) as SplashSeekDiscrete
    }

    override fun setTitle(title: String?): SplashSeekDiscrete {
        return super.setTitle(title) as SplashSeekDiscrete;
    }

    fun setOnCancel(s: String): SplashSeekDiscrete {
        return setOnCancel(s) {}
    }

    fun setOnCancel(@StringRes s: Int): SplashSeekDiscrete {
        return setOnCancel(s) {}
    }

    fun setOnCancel(@StringRes s: Int, onCancel: (SplashSeekDiscrete) -> Unit): SplashSeekDiscrete {
        return setOnCancel(ToolsResources.s(s), onCancel)
    }

    fun setOnCancel(s: String, onCancel: (SplashSeekDiscrete) -> Unit): SplashSeekDiscrete {
        vCancel.text = s
        vCancel.visibility = if (s.isEmpty()) View.GONE else View.VISIBLE
        vCancel.setOnClickListener {
            hide()
            onCancel.invoke(this)
        }
        return this
    }

    fun setOnEnter(@StringRes s: Int): SplashSeekDiscrete {
        return setOnEnter(s) { w, i -> }
    }

    fun setOnEnter(s: String): SplashSeekDiscrete {
        return setOnEnter(s) { w, i -> }
    }

    fun setOnEnter(@StringRes s: Int, onEnter: (SplashSeekDiscrete, Int) -> Unit): SplashSeekDiscrete {
        return setOnEnter(ToolsResources.s(s), onEnter)
    }

    fun setOnEnter(s: String, onEnter: (SplashSeekDiscrete, Int) -> Unit): SplashSeekDiscrete {
        vEnter.text = s
        vEnter.setOnClickListener {
            hide()
            onEnter.invoke(this, getProgress())
        }
        return this
    }

    override fun setEnabled(enabled: Boolean): SplashSeekDiscrete {
        vEnter.isEnabled = enabled
        vCancel.isEnabled = enabled
        vSeekBar.isEnabled = enabled
        return super.setEnabled(enabled) as SplashSeekDiscrete
    }

}
