package com.sup.dev.android.views.splash

import androidx.annotation.StringRes
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatCheckBox
import com.sup.dev.android.R
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.CardDividerTitleMini
import java.util.ArrayList

class SplashCheckBoxes : Splash(R.layout.splash_container) {

    private val vOptionsContainer: LinearLayout = view.findViewById(R.id.vContentContainer)
    private val vCancel: Button = view.findViewById(R.id.vCancel)
    private val vEnter: Button = view.findViewById(R.id.vEnter)

    private var minimumSelectedCount = 0
    private var autoHideOnEnter = true

    fun setOnEnter(@StringRes s: Int, onEnter: (Array<Int>)->Unit = {}): SplashCheckBoxes {
        return setOnEnter(ToolsResources.s(s),onEnter)
    }

    fun setOnEnter(s: String?, onEnter: (Array<Int>)->Unit = {}): SplashCheckBoxes {
        ToolsView.setTextOrGone(vEnter, s)
        vEnter.setOnClickListener {
            val list = ArrayList<Int>()
            for (i in 0 until vOptionsContainer.childCount) {
                if (vOptionsContainer.getChildAt(i) !is AppCompatCheckBox) continue
                val v = vOptionsContainer.getChildAt(i) as AppCompatCheckBox
                val item = v.tag as Item
                if (v.isChecked) {
                    item.onSelected.invoke(SelectedEvent(this, item))
                    list.add(i)
                }
                if (!v.isChecked) item.onNotSelected.invoke(NotSelectedEvent(this, item))
            }
            onEnter.invoke(list.toTypedArray())
            if (autoHideOnEnter)
                hide()
            else
                setEnabled(false)
        }
        return this
    }

    fun setAutoHideOnEnter(autoHideOnEnter: Boolean): SplashCheckBoxes {
        this.autoHideOnEnter = autoHideOnEnter
        return this
    }

    fun setOnCancel(onCancel: (SplashCheckBoxes) -> Unit = {}): SplashCheckBoxes {
        return setOnCancel(null, onCancel)
    }

    fun setOnCancel(@StringRes s: Int, onCancel: (SplashCheckBoxes) -> Unit): SplashCheckBoxes {
        return setOnCancel(ToolsResources.s(s), onCancel)
    }

    fun setOnCancel(@StringRes s: Int): SplashCheckBoxes {
        return setOnCancel(ToolsResources.s(s)) {}
    }

    @JvmOverloads
    fun setOnCancel(s: String?, onCancel: (SplashCheckBoxes) -> Unit = {}): SplashCheckBoxes {
        super.setOnHide { onCancel.invoke(this) }
        ToolsView.setTextOrGone(vCancel, s)
        vCancel.setOnClickListener {
            hide()
            onCancel.invoke(this)
        }

        return this
    }

    fun setMinimumSelectedCount(minimumSelectedCount:Int): SplashCheckBoxes {
        this.minimumSelectedCount = minimumSelectedCount
        return this
    }

    fun getSelectedCount():Int{
        var count = 0
        for (i in 0 until vOptionsContainer.childCount) {
            if (vOptionsContainer.getChildAt(i) !is AppCompatCheckBox) continue
            val v = vOptionsContainer.getChildAt(i) as AppCompatCheckBox
            if (v.isChecked) count++
        }
        return count
    }

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

    private fun finishItemBuilding() {
        if (buildItem != null) {
            buildItem = null
        }
    }

    fun add(@StringRes text: Int): SplashCheckBoxes {
        return add(ToolsResources.s(text))
    }

    fun add(@StringRes text: Int, key: Any): SplashCheckBoxes {
        return add(ToolsResources.s(text), key)
    }

    fun add(text: String): SplashCheckBoxes {
        return add(text, text)
    }

    fun add(text: String, key: Any): SplashCheckBoxes {
        finishItemBuilding()
        buildItem = Item(key)
        buildItem!!.v.text = text
        return this
    }

    fun onChange(onChange: (ChangeEvent) -> Unit): SplashCheckBoxes {
        buildItem!!.onChange = onChange
        return this
    }

    fun onSelected(onSelected: (SelectedEvent) -> Unit): SplashCheckBoxes {
        buildItem!!.onSelected = onSelected
        return this
    }

    fun onNotSelected(onNotSelected: (NotSelectedEvent) -> Unit): SplashCheckBoxes {
        buildItem!!.onNotSelected = onNotSelected
        return this
    }

    fun text(@StringRes text: Int): SplashCheckBoxes {
        return text(ToolsResources.s(text))
    }

    fun text(text: String?): SplashCheckBoxes {

        buildItem!!.v.text = text
        return this
    }

    fun checked(b: Boolean): SplashCheckBoxes {
        buildItem!!.callbackLock = true
        buildItem!!.v.isChecked = b
        buildItem!!.callbackLock = false
        return this
    }

    fun condition(b: Boolean): SplashCheckBoxes {
        skipThisItem = !b
        return this
    }

    fun groupCondition(b: Boolean): SplashCheckBoxes {
        skipGroup = !b
        return this
    }

    fun reverseGroupCondition(): SplashCheckBoxes {
        skipGroup = !skipGroup
        return this
    }

    fun clearGroupCondition(): SplashCheckBoxes {
        skipGroup = false
        return this
    }

    fun group(@StringRes title: Int): SplashCheckBoxes {
        return group(ToolsResources.s(title))
    }

    fun group(@StringRes title: Int, divider: Boolean): SplashCheckBoxes {
        return group(ToolsResources.s(title), divider)
    }

    fun addView(v:View): SplashCheckBoxes {
        vOptionsContainer.addView(v)
        if (vOptionsContainer.childCount > 1)
            (v.layoutParams as ViewGroup.MarginLayoutParams).topMargin = ToolsView.dpToPx(8f).toInt()
        return this
    }

    @JvmOverloads
    fun group(title: String?, divider: Boolean = false): SplashCheckBoxes {
        finishItemBuilding()
        val card = CardDividerTitleMini().setText(title).setDividerBottom(divider)
        val v = card.instanceView(vOptionsContainer)
        card.bindCardView(v)
        vOptionsContainer.addView(v)
        if (vOptionsContainer.childCount > 1)
            (v.layoutParams as ViewGroup.MarginLayoutParams).topMargin = ToolsView.dpToPx(8f).toInt()
        return this
    }

    public inner class Item(val key: Any) {

        var callbackLock = false
        val v: AppCompatCheckBox = AppCompatCheckBox(SupAndroid.activity!!)

        var onChange: (ChangeEvent) -> Unit = {run { } }
        var onSelected: (SelectedEvent) -> Unit = { }
        var onNotSelected: (NotSelectedEvent) -> Unit = { }

        init {
            v.tag = this
            v.setOnCheckedChangeListener { _, _ ->
                if(! v.isChecked && getSelectedCount() < minimumSelectedCount){
                    v.isChecked = true
                } else {
                    if (!callbackLock) onChange.invoke(ChangeEvent(this@SplashCheckBoxes, this, v.isChecked))
                }
            }
            vOptionsContainer.addView(v)
            (v.layoutParams as ViewGroup.MarginLayoutParams).leftMargin = ToolsView.dpToPx(8f).toInt()
            (v.layoutParams as ViewGroup.MarginLayoutParams).rightMargin = ToolsView.dpToPx(8f).toInt()
            v.setPadding(ToolsView.dpToPx(16).toInt(), v.paddingTop, v.paddingRight, v.paddingBottom)

        }

    }

    //
    //  ClickEvent
    //

    public class ChangeEvent(
            val splash: SplashCheckBoxes,
            val item: Item,
            val isChecked:Boolean
    )

    public class SelectedEvent(
            val splash: SplashCheckBoxes,
            val item: Item
    )

    public class NotSelectedEvent(
            val splash: SplashCheckBoxes,
            val item: Item
    )


}
