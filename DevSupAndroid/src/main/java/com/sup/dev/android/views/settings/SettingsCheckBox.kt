package com.sup.dev.android.views.settings

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.widget.CheckBox
import com.sup.dev.android.R
import com.sup.dev.android.tools.ToolsView

class SettingsCheckBox @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : Settings(context, attrs) {

    private val vCheckBox: CheckBox = ToolsView.inflate(context, R.layout.z_check_box)

    private var onClickListener: OnClickListener? = null
    private var salient: Boolean = false

    //
    //  Getters
    //

    init {

        vCheckBox.isFocusable = false
        vCheckBox.setOnCheckedChangeListener { _, b ->
            setEnabledSubSettings(b)
            if (!salient) onClick()
        }

        val a = context.obtainStyledAttributes(attrs, R.styleable.Settings, 0, 0)
        val checked = a.getBoolean(R.styleable.Settings_Settings_checked, false)
        a.recycle()

        setChecked(checked)
        setSubView(vCheckBox)

        super.setOnClickListener {
            salient = true
            vCheckBox.isChecked = !vCheckBox.isChecked
            salient = false
            onClick()
        }

    }

    private fun onClick() {
        if (onClickListener != null) onClickListener!!.onClick(this)
    }

    //
    //  State
    //

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

    fun setChecked(checked:Boolean) {
        salient = true
        vCheckBox.isChecked = checked
        salient = false
    }

    override fun setOnClickListener(l: OnClickListener?) {
        this.onClickListener = l
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        vCheckBox.isEnabled = enabled
    }

    //
    //  Getters
    //

    fun isChecked() = vCheckBox.isChecked

}