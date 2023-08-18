package com.sup.dev.android.views.settings

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.widget.Switch
import com.sup.dev.android.R

class   SettingsSwitcher @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : Settings(context, attrs) {

    private val vSwitcher: Switch = Switch(context)

    private var onClickListener: OnClickListener? = null
    private var setOnSwitchListener: (Boolean)->Unit = {  }
    private var salient: Boolean = false

    //
    //  Getters
    //

    init {

        vSwitcher.isFocusable = false
        vSwitcher.setOnCheckedChangeListener { _, b ->
            setEnabledSubSettings(b)
            if (!salient) setOnSwitchListener.invoke(b)
            if (!salient) onClick()
        }

        val a = context.obtainStyledAttributes(attrs, R.styleable.Settings, 0, 0)
        val checked = a.getBoolean(R.styleable.Settings_Settings_checked, false)
        a.recycle()

        setChecked(checked)
        setSubView(vSwitcher)

        super.setOnClickListener {
            setChecked(!vSwitcher.isChecked)
            onClick()
        }
    }

    private fun onClick() {
        if (onClickListener != null) onClickListener!!.onClick(this)
    }

    //
    //  State
    //

    public fun setChecked(checked:Boolean){
        salient = true
        vSwitcher.isChecked = checked
        salient = false
        setEnabledSubSettings(checked)
    }

    public override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable("SUPER_STATE", super.onSaveInstanceState())
        bundle.putBoolean("checked", isChecked())
        return bundle
    }

    public override fun onRestoreInstanceState(state: Parcelable?) {
        var stateV = state
        if (stateV is Bundle) {
            val bundle = stateV as Bundle?
            salient = true
            setChecked(bundle!!.getBoolean("checked"))
            salient = false
            stateV = bundle.getParcelable("SUPER_STATE")
        }
        super.onRestoreInstanceState(stateV)
    }

    //
    //  Setters
    //

    override fun setOnClickListener(l: OnClickListener?) {
        this.onClickListener = l
    }

    fun setOnRootClickListener(l: OnClickListener?) {
        super.setOnClickListener(l)
    }

    fun setOnSwitchListener(l: (Boolean)->Unit) {
        this.setOnSwitchListener = l
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        vSwitcher.isEnabled = enabled
    }

    //
    //  Getters
    //

    fun isChecked() = vSwitcher.isChecked


}
