package com.sup.dev.android.views.settings

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import android.util.AttributeSet
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import com.sup.dev.android.R
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.ViewIcon


class SettingsSeek @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : Settings(context, attrs, R.layout.settings_seek), SeekBar.OnSeekBarChangeListener {

    private val vSeekBar: SeekBar = findViewById(R.id.vDevSupSeekBar)

    private var onProgressChanged: ((Int) -> Unit)? = null
    private var onInstantProgressChanged: ((Int) -> Unit)? = null

    //
    //  Getters
    //

    var progress: Int
        get() = vSeekBar.progress + compatMin
        set(progress) {
            vSeekBar.progress = progress
        }

    //
    //  Dpad
    //

    private var dpadStep = 1

    init {

        vSeekBar.id = View.NO_ID  //   Чтоб система не востонавливала состояние

        val a = context.obtainStyledAttributes(attrs, R.styleable.SettingsSeek, 0, 0)
        val minProgress = a.getInteger(R.styleable.SettingsSeek_Settings_minProgress, 0)
        val maxProgress = a.getInteger(R.styleable.SettingsSeek_Settings_maxProgress, 100)
        val progress = a.getInteger(R.styleable.SettingsSeek_Settings_progress, 70)
        a.recycle()

        vSeekBar.setOnSeekBarChangeListener(this)

        setMinProgress(minProgress)
        setMaxProgress(maxProgress)
        vSeekBar.progress = progress
        isFocusable = false
    }

    //
    //  State
    //

    public override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable("SUPER_STATE", super.onSaveInstanceState())
        bundle.putInt("progress", vSeekBar.progress)
        return bundle
    }

    public override fun onRestoreInstanceState(s: Parcelable?) {
        var state = s
        if (state is Bundle) {
            val bundle = state as Bundle?
            progress = bundle!!.getInt("progress")
            state = bundle.getParcelable("SUPER_STATE")
        }
        super.onRestoreInstanceState(state)
    }

    //
    //  Setters
    //

    private var compatMax = 0
    private var compatMin = 0

    fun setMaxProgress(max: Int) {
        vSeekBar.max = max - compatMin
        compatMax = max
    }

    fun setMinProgress(min: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vSeekBar.min = min
        } else {
            compatMin = min
            vSeekBar.max = compatMax - compatMin
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        vSeekBar.isEnabled = enabled
    }

    fun setOnProgressChanged(onProgressChanged: (Int) -> Unit) {
        this.onProgressChanged = onProgressChanged
    }

    fun setOnInstantProgressChanged(onProgressChanged: (Int) -> Unit) {
        this.onInstantProgressChanged = onProgressChanged
    }

    //
    //  Events
    //

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        onInstantProgressChanged?.invoke(this.progress)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        onProgressChanged?.invoke(progress)
    }

}
