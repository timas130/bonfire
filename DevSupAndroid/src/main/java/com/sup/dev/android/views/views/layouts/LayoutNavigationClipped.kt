package com.sup.dev.android.views.views.layouts

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.sup.dev.android.R
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.ViewIcon
import java.lang.IllegalArgumentException

class LayoutNavigationClipped @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    private val ICON_SIZE = ToolsView.dpToPx(58)
    private val DP = ToolsView.dpToPx(2)
    private val path = Path()
    private val pathShadow = Path()
    private val pathShadow_2 = Path()
    private var paint: Paint = Paint()
    private var cornedSize = ToolsView.dpToPx(16)
    private var cornedSides = true
    private val vIconMain: View = ToolsView.inflate(this, R.layout.layout_bottom_navigation_icon)
    private val vIcon1: View = ToolsView.inflate(this, R.layout.layout_bottom_navigation_icon)
    private val vIcon2: View = ToolsView.inflate(this, R.layout.layout_bottom_navigation_icon)
    private val vIcon3: View = ToolsView.inflate(this, R.layout.layout_bottom_navigation_icon)
    private val vIcon4: View = ToolsView.inflate(this, R.layout.layout_bottom_navigation_icon)

    init {
        setWillNotDraw(false)
        paint.isAntiAlias = true

        addView(vIconMain)
        addView(vIcon1)
        addView(vIcon2)
        addView(vIcon3)
        addView(vIcon4)

        vIconMain.findViewById<ViewIcon>(R.id.vNavigationItemIcon).setIconBackgroundColor(ToolsResources.getColorAttr(R.attr.colorPrimary))
        vIconMain.findViewById<ViewIcon>(R.id.vNavigationItemIcon).setPadding(ToolsView.dpToPx(16).toInt(), ToolsView.dpToPx(16).toInt(), ToolsView.dpToPx(16).toInt(), ToolsView.dpToPx(16).toInt())
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.translate(0f, -DP)
        paint.color = 0x30000000
        canvas?.drawPath(pathShadow, paint)
        canvas?.translate(0f, DP)
        canvas?.drawPath(pathShadow_2, paint)
        paint.color = ToolsResources.getColorAttr(R.attr.colorPrimary)
        canvas?.drawPath(path, paint)
        super.onDraw(canvas)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        update()
    }

    private fun update() {
        path.reset()
        pathShadow.reset()
        pathShadow_2.reset()

        val buttonSize = ICON_SIZE / 2
        val buttonOffset = ToolsView.dpToPx(6)

        val arg3 = buttonSize + buttonOffset
        val r = Math.min(Math.min(cornedSize, width.toFloat() / 2), height.toFloat() / 2)
        val r2 = ToolsView.dpToPx(8)

        path.addRect(r, arg3, width - r, r + arg3, Path.Direction.CCW)
        path.addRect(0f, height.toFloat(), width.toFloat(), height - r, Path.Direction.CCW)
        path.addRect(0f, r + arg3, width.toFloat(), height - r, Path.Direction.CCW)

        val pBigCircle = Path()
        pBigCircle.addArc(width / 2f - arg3, r2 / 2, width / 2f + arg3, arg3 * 2 + r2 / 2, 0f, 180f)
        pBigCircle.addRect(width / 2f - arg3 - r2, arg3, width / 2f + arg3 + r2, arg3 + r2, Path.Direction.CW)
        path.op(pBigCircle, Path.Op.XOR)

        val pCircles = Path()
        pCircles.addCircle(width / 2f - arg3 - r2, arg3 + r2, r2, Path.Direction.CW)
        pCircles.addCircle(width / 2f + arg3 + r2, arg3 + r2, r2, Path.Direction.CW)
        if (cornedSides) pCircles.addCircle(r, r + arg3, r, Path.Direction.CCW); else pCircles.addRect(0f, arg3, r, r + arg3, Path.Direction.CCW)
        if (cornedSides) pCircles.addCircle(width - r, r + arg3, r, Path.Direction.CCW); else pCircles.addRect(width - r, arg3, width.toFloat(), r + arg3, Path.Direction.CCW)
        path.op(pCircles, Path.Op.UNION)





        pathShadow.addRect(r, arg3, width - r, r + arg3, Path.Direction.CCW)
        pathShadow.addRect(0f, height.toFloat(), width.toFloat(), height - r, Path.Direction.CCW)
        pathShadow.addRect(0f, r + arg3, width.toFloat(), height - r, Path.Direction.CCW)

        val pShadowBigCircle = Path()
        pShadowBigCircle.addArc(width / 2f - arg3 + DP, r2 / 2 + DP, width / 2f + arg3 - DP, arg3 * 2 + r2 / 2, 0f, 180f)
        pShadowBigCircle.addRect(width / 2f - arg3 - r2, arg3, width / 2f + arg3 + r2, arg3 + r2, Path.Direction.CW)
        pathShadow.op(pShadowBigCircle, Path.Op.XOR)

        val pShadowCircles = Path()
        pShadowCircles.addCircle(width / 2f - arg3 - r2 + DP, arg3 + r2, r2, Path.Direction.CW)
        pShadowCircles.addCircle(width / 2f + arg3 + r2 - DP, arg3 + r2, r2, Path.Direction.CW)
        if (cornedSides) pShadowCircles.addCircle(r - DP, r + arg3, r, Path.Direction.CCW); else pShadowCircles.addRect(0f, arg3, r, r + arg3, Path.Direction.CCW)
        if (cornedSides) pShadowCircles.addCircle(width - r + DP, r + arg3, r, Path.Direction.CCW); else pShadowCircles.addRect(width - r, arg3, width.toFloat(), r + arg3, Path.Direction.CCW)
        pathShadow.op(pShadowCircles, Path.Op.UNION)


        vIconMain.x = width / 2f - ICON_SIZE / 2
        vIconMain.y = arg3 - ICON_SIZE / 2

        vIcon1.x = (width / 2 - arg3) / 4 - ICON_SIZE / 2
        vIcon2.x = (width / 2 - arg3) / 4 * 3 - ICON_SIZE / 2
        vIcon3.x = (width - (width / 2 - arg3) / 4) - ICON_SIZE / 2
        vIcon4.x = (width - (width / 2 - arg3) / 4 * 3) - ICON_SIZE / 2

        vIcon1.y = arg3
        vIcon2.y = arg3
        vIcon3.y = arg3
        vIcon4.y = arg3


        pathShadow_2.addCircle(vIconMain.x + ICON_SIZE/2, vIconMain.y + ICON_SIZE/2, ICON_SIZE/2 + DP, Path.Direction.CW)

        invalidate()
    }

    fun getItem(index: Int): View {
        when (index) {
            0 -> return vIcon1
            1 -> return vIcon2
            2 -> return vIcon3
            3 -> return vIcon4
        }
        throw IllegalArgumentException("Unknown item index [$index]")
    }

    fun getItemMain() = vIconMain


}