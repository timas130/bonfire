package com.sup.dev.android.views.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import com.google.android.material.chip.Chip
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.sup.dev.android.R
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.support.drawable.DrawableBitmapCircle

class ViewChip(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : Chip(context, attributeSet, defStyleAttr) {

    companion object {

        fun instance(context: Context, name: String? = null, tag: Any? = null): ViewChip {
            val v: ViewChip = ToolsView.inflate(context, R.layout.z_chip)
            v.text = name
            v.tag = tag
            return v
        }

        fun instanceMini(vParent: ViewGroup, name: String? = null, tag: Any? = null): ViewChip {
            val v: ViewChip = ToolsView.inflate(vParent, R.layout.z_chip_mini)
            v.text = name
            v.tag = tag
            return v
        }

        fun instanceOutline(context: Context, name: String? = null, tag: Any? = null): ViewChip {
            val v: ViewChip = ToolsView.inflate(context, R.layout.z_chip_outline)
            v.text = name
            v.tag = tag
            return v
        }

        fun instanceChoose(context: Context, name: String? = null, tag: Any? = null): ViewChip {
            val v: ViewChip = ToolsView.inflate(context, R.layout.z_chip_choose)
            v.text = name
            v.tag = tag
            return v
        }

        fun instanceChooseOutline(context: Context, name: String? = null, tag: Any? = null): ViewChip {
            val v: ViewChip = ToolsView.inflate(context, R.layout.z_chip_choose_outline)
            v.text = name
            v.tag = tag
            return v
        }

    }

    private var chipIconSizePadding = 0f
    private var textEndPaddingLocal = 0f
    private var textStartPaddingLocal = 0f
    var autoControlVisibility = true

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)

    init {
        updateChipVisible()
    }

    override fun setLayoutParams(params: ViewGroup.LayoutParams?) {
        super.setLayoutParams(params)
        if (params != null) {
            if (params.height < 0) params.height = ToolsView.dpToPx(32).toInt()
            setChipSizePx(params.height)
        }
    }

    fun setChipSizePx(size: Int) {
        layoutParams.height = size
        chipIconSize = size.toFloat() - chipIconSizePadding
        updatePadding()
    }

    fun setTextPaddings(start:Float, end:Float){
        textStartPaddingLocal = start
        textEndPaddingLocal = end
        textStartPadding = start
        textEndPadding = end
    }

    private fun updatePadding() {
        if (textEndPaddingLocal == 0f) textEndPaddingLocal = textEndPadding
        if (textStartPaddingLocal == 0f) textStartPaddingLocal = textStartPadding

        if (text.isEmpty()) {
            textEndPadding = 0f
            textStartPadding = 0f
        } else {
            textEndPadding = textEndPaddingLocal
            textStartPadding = textStartPaddingLocal
            if (chipIcon != null) textStartPadding /= 2
        }

    }

    fun updateChipVisible() {

        if (!autoControlVisibility) return

        if (chipIcon == null && text.isEmpty())
            visibility = View.GONE
        else
            visibility = View.VISIBLE
    }

    fun setChipIconSizePadding(p: Float) {
        this.chipIconSizePadding = p
        setChipSizePx(layoutParams.height)
    }

    fun setChipIconPadding(p: Float) {
        iconEndPadding = p
        iconStartPadding = p
    }

    fun setIcon(src: Int) {
        setIcon(if (src > 0) ToolsResources.getBitmap(src) else null)
    }

    fun setIcon(bitmap: Bitmap?) {
        if (bitmap != null) chipIcon = DrawableBitmapCircle(bitmap)
        else chipIcon = null
        updateChipVisible()
        updatePadding()
    }

    fun setBackgroundRes(res: Int) {
        setBackground(ToolsResources.getColor(res))
    }

    fun setBackground(color: Int) {
        chipBackgroundColor = ColorStateList.valueOf(color)
    }

    fun setIconPadding(p: Float) {
        iconStartPadding = p
        iconEndPadding = p
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(text, type)
        updateChipVisible()
        updatePadding()
    }


}