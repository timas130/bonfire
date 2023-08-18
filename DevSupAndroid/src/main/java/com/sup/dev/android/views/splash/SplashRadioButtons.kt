package com.sup.dev.android.views.splash

import androidx.annotation.StringRes
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.appcompat.widget.AppCompatRadioButton
import com.sup.dev.android.R
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import java.util.ArrayList

class SplashRadioButtons : Splash(R.layout.splash_container) {

    private val items = ArrayList<Item?>()
    private val vOptionsContainer: LinearLayout = view.findViewById(R.id.vContentContainer)
    private val vCancel: Button = view.findViewById(R.id.vCancel)
    private val vEnter: Button = view.findViewById(R.id.vEnter)

    private var autoHideOnEnter = true


    //
    //  Item
    //

    private var buildItem: Item? = null
    private var skipThisItem = false
    private var skipGroup = false

    init {
        vCancel.visibility = View.GONE
        vEnter.visibility = View.GONE
    }

    override fun onShow() {
        super.onShow()
        finishItemBuilding()
    }

    private fun add(item: Item?) {
        items.add(item)
    }

    private fun finishItemBuilding() {
        if (buildItem != null) {
            val i = buildItem
            buildItem = null
            if (!skipThisItem && !skipGroup) add(i)
        }
    }

    fun add(@StringRes text: Int): SplashRadioButtons {
        return add(ToolsResources.s(text))
    }

    fun add(text: String?): SplashRadioButtons {
        finishItemBuilding()
        buildItem = Item()
        buildItem!!.v.text = text
        return this
    }

    fun onChange(onChange: ((SplashRadioButtons, Boolean) -> Unit)?): SplashRadioButtons {
        buildItem!!.onChange = onChange
        return this
    }

    fun onSelected(onSelected: (SplashRadioButtons) -> Unit): SplashRadioButtons {
        buildItem!!.onSelected = onSelected
        return this
    }

    fun onNotSelected(onNotSelected: (SplashRadioButtons) -> Unit): SplashRadioButtons {
        buildItem!!.onNotSelected = onNotSelected
        return this
    }

    fun text(@StringRes text: Int): SplashRadioButtons {
        return text(ToolsResources.s(text))
    }

    fun text(text: String?): SplashRadioButtons {

        buildItem!!.v.text = text
        return this
    }

    fun checked(b: Boolean): SplashRadioButtons {
        buildItem!!.v.isChecked = b
        return this
    }

    fun condition(b: Boolean): SplashRadioButtons {
        skipThisItem = !b
        return this
    }

    fun groupCondition(b: Boolean): SplashRadioButtons {
        skipGroup = !b
        return this
    }

    fun reverseGroupCondition(): SplashRadioButtons {
        skipGroup = !skipGroup
        return this
    }

    fun clearGroupCondition(): SplashRadioButtons {
        skipGroup = false
        return this
    }

    //
    //  Setters
    //

    fun setOnEnter(@StringRes s: Int, onEnter: (Array<Int>) -> Unit = {}): SplashRadioButtons {
        return setOnEnter(ToolsResources.s(s))
    }

    fun setOnEnter(s: String?, onEnter: (Array<Int>) -> Unit = {}): SplashRadioButtons {
        ToolsView.setTextOrGone(vEnter, s)
        vEnter.setOnClickListener {
            if (autoHideOnEnter)
                hide()
            else
                setEnabled(false)
            val list = ArrayList<Int>()
            for (i in 0 until vOptionsContainer.childCount) {
                val v = vOptionsContainer.getChildAt(i) as RadioButton
                val item = v.tag as Item
                if (item.onChange != null) item.onChange!!.invoke(this, v.isChecked)
                if (v.isChecked && item.onSelected != null) {
                    item.onSelected!!.invoke(this)
                    list.add(i)
                }
                if (!v.isChecked && item.onNotSelected != null) item.onNotSelected!!.invoke(this)
                onEnter.invoke(list.toTypedArray())
            }
        }
        return this
    }

    fun setAutoHideOnEnter(autoHideOnEnter: Boolean): SplashRadioButtons {
        this.autoHideOnEnter = autoHideOnEnter
        return this
    }

    fun setOnCancel(@StringRes s: Int): SplashRadioButtons {
        return setOnCancel(ToolsResources.s(s))
    }

    fun setOnCancel(onCancel: (SplashRadioButtons) -> Unit): SplashRadioButtons {
        return setOnCancel(null, onCancel)
    }

    fun setOnCancel(@StringRes s: Int, onCancel: (SplashRadioButtons) -> Unit = {}): SplashRadioButtons {
        return setOnCancel(ToolsResources.s(s), onCancel)
    }

    @JvmOverloads
    fun setOnCancel(s: String?, onCancel: (SplashRadioButtons) -> Unit = {}): SplashRadioButtons {
        super.setOnHide { onCancel.invoke(this) }
        ToolsView.setTextOrGone(vCancel, s)
        vCancel.setOnClickListener {
            hide()
            onCancel.invoke(this)
        }

        return this
    }

    //
    //  Item
    //

    private inner class Item {

        var v: RadioButton = AppCompatRadioButton(SupAndroid.activity!!)

        var onChange: ((SplashRadioButtons, Boolean) -> Unit)? = null
        var onSelected: ((SplashRadioButtons) -> Unit)? = null
        var onNotSelected: ((SplashRadioButtons) -> Unit)? = null

        init {
            v.tag = this
            v.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked)
                    for (i in 0 until vOptionsContainer.childCount)
                        if (vOptionsContainer.getChildAt(i) !== v)
                            (vOptionsContainer.getChildAt(i) as RadioButton).isChecked = false
            }
            vOptionsContainer.addView(v)
            (v.layoutParams as ViewGroup.MarginLayoutParams).leftMargin = ToolsView.dpToPx(8f).toInt()
            (v.layoutParams as ViewGroup.MarginLayoutParams).rightMargin = ToolsView.dpToPx(8f).toInt()

        }

    }


}
