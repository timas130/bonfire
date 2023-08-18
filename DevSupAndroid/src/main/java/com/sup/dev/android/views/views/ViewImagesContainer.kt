package com.sup.dev.android.views.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.sup.dev.android.R
import com.sup.dev.android.libs.image_loader.ImageLink
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.screens.SImageView

class ViewImagesContainer @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    companion object {

        private val cash = ArrayList<ViewCircleImage>()

        private fun putToCash(v: ViewCircleImage) {
            cash.add(v)
        }

        private fun getViewFromCash(): ViewCircleImage? {
            if (cash.isEmpty()) return null
            else return ToolsView.removeFromParent(cash.removeAt(0))
        }

    }

    private val items = ArrayList<Item>()
    private var itemIndex = 0
    private var onClickGlobal: (Item) -> Boolean = { false }

    init {
        orientation = VERTICAL
    }

    fun add(imageLoader: ImageLink, onClick: ((ClickEvent) -> Unit)? = null, onLongClick: ((ClickEvent) -> Unit)? = null) {
        items.add(Item(imageLoader, onClick, onLongClick))
        rebuild()
    }

    fun clear() {
        for (i in items) {
            i.vImage.setImageDrawable(null)
            putToCash(i.vImage)
        }
        items.clear()
        rebuild()
    }

    fun rebuild() {
        itemIndex = 0
        removeAllViews()

        when {
            items.size == 1 -> addView(items[0].vImage)
            items.size == 2 -> addItems(2)
            items.size == 3 -> addItems(3)
            items.size == 4 -> {;addItems(2);addItems(2); }
            items.size == 5 -> {;addItems(2);addItems(3); }
            items.size == 6 -> {;addItems(4);addItems(2); }
            items.size == 7 -> {;addItems(4);addItems(3); }
            items.size == 8 -> {;addItems(2);addItems(3);addItems(3); }
            items.size == 9 -> {;addItems(2);addItems(3);addItems(4); }
            items.size == 10 -> {;addItems(3);addItems(4);addItems(3); }
        }
    }

    private fun addItems(count: Int) {

        val vLinear = LinearLayout(context)
        vLinear.orientation = HORIZONTAL
        addView(vLinear)
        (vLinear.layoutParams as LayoutParams).height = ViewGroup.LayoutParams.MATCH_PARENT
        (vLinear.layoutParams as LayoutParams).width = ViewGroup.LayoutParams.MATCH_PARENT
        (vLinear.layoutParams as LayoutParams).weight = 1f
        (vLinear.layoutParams as LayoutParams).topMargin = if (childCount > 1) ToolsView.dpToPx(2).toInt() else 0

        for (i in 0 until count) {
            val vLinearV = getChildAt(childCount - 1) as LinearLayout
            val item = items[itemIndex++]
            ToolsView.removeFromParent(item.vImage)
            vLinearV.addView(item.vImage)
            (item.vImage.layoutParams as LayoutParams).weight = 1f
            (item.vImage.layoutParams as LayoutParams).leftMargin = if (vLinearV.childCount > 1) ToolsView.dpToPx(2).toInt() else 0
            (item.vImage.layoutParams as LayoutParams).width = ViewGroup.LayoutParams.MATCH_PARENT
            (item.vImage.layoutParams as LayoutParams).height = ViewGroup.LayoutParams.MATCH_PARENT
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        val arg = if(childCount == 1) 2.5f else if(childCount == 2) 1.5f else 1f
        if(h == 0 || MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED){
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec((w/arg).toInt(), MeasureSpec.getMode(widthMeasureSpec)))
        }else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun setLayoutParams(params: ViewGroup.LayoutParams?) {
        super.setLayoutParams(params)
        rebuild()
    }


    fun toImageView(imageLoader:ImageLink) {
        val array = Array(items.size){items[it].imageLoader}
        var index = 0
        for (i in array.indices) if (array[i] == imageLoader) index = i
        Navigator.to(SImageView(index, array))
    }

    //
    //  Item
    //

    @Suppress("UNCHECKED_CAST")
    private inner class Item(
            val imageLoader: ImageLink,
            val onClick: ((ClickEvent) -> Unit)?,
            val onLongClick: ((ClickEvent) -> Unit)?
    ) {

        val vImage: ViewCircleImage = getViewFromCash() ?: ToolsView.inflate(R.layout.view_images_container_item)

        init {
            imageLoader.into(vImage)

            vImage.setOnClickListener {
                when {
                    onClickGlobal(this) -> return@setOnClickListener
                    onClick == null -> toImageView(imageLoader)
                    else -> onClick.invoke(ClickEvent(vImage, imageLoader, it.x, it.y))
                }
            }
            if (onLongClick != null)
                vImage.setOnLongClickListener {
                    onLongClick.invoke(ClickEvent(vImage, imageLoader, it.x, it.y))
                    true
                }
        }

    }



    class ClickEvent(
            val view: View,
            val imageLink:ImageLink,
            val x:Float,
            val y:Float
    )


}