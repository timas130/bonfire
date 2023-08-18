package com.sup.dev.android.views.views.pager

import android.content.Context
import android.graphics.Paint
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import android.util.AttributeSet
import android.view.ViewGroup
import com.sup.dev.android.R
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView


abstract class ViewPagerIndicator constructor(context: Context, attrs: AttributeSet?) : ViewGroup(context, attrs), ViewPager.OnPageChangeListener, ViewPager.OnAdapterChangeListener {

    protected val paint: Paint

    protected val pagerId: Int
    protected var color: Int = 0
    protected var colorSelected: Int = 0
    protected var offset: Float = 0.toFloat()

    protected var pager: ViewPager? = null
    protected var position: Int = 0
    protected var positionOffset: Float = 0.toFloat()
    protected var state: Int = 0

    init {

        SupAndroid.initEditMode(this)

        offset = ToolsView.dpToPx(16f)
        color = ToolsResources.getSecondaryAlphaColor(context)
        colorSelected = ToolsResources.getSecondaryColor(context)

        val a = context.obtainStyledAttributes(attrs, R.styleable.ViewPagerIndicator, 0, 0)
        color = a.getColor(R.styleable.ViewPagerIndicator_ViewPagerIndicator_color, color)
        colorSelected = a.getColor(R.styleable.ViewPagerIndicator_ViewPagerIndicator_colorSelected, colorSelected)
        offset = a.getDimension(R.styleable.ViewPagerIndicator_ViewPagerIndicator_offset, offset)
        pagerId = a.getResourceId(R.styleable.ViewPagerIndicator_ViewPagerIndicator_pager, 0)
        a.recycle()

        paint = Paint()
        paint.isAntiAlias = true

    }

    override fun onLayout(b: Boolean, i: Int, i1: Int, i2: Int, i3: Int) {

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (pager == null && pagerId != 0) setPagerView(ToolsView.findViewOnParents(this, pagerId))
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    open fun setPagerView(pager: ViewPager?) {
        this.pager = pager
        pager?.addOnPageChangeListener(this)
        pager?.addOnAdapterChangeListener(this)
        onChanged()
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        this.position = position
        this.positionOffset = positionOffset
        onChanged()
    }

    override fun onPageSelected(position: Int) {
        if (state == ViewPager.SCROLL_STATE_IDLE)
            this.position = position
        onChanged()
    }

    override fun onPageScrollStateChanged(state: Int) {
        this.state = state
        onChanged()
    }

    protected abstract fun onChanged()

    override fun onAdapterChanged(viewPager: ViewPager, oldAdapter: PagerAdapter?, newAdapter: PagerAdapter?) {
    }
}