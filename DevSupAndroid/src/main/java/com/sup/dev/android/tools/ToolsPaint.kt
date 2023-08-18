package com.sup.dev.android.tools

import android.graphics.Canvas
import android.graphics.drawable.GradientDrawable
import com.sup.dev.java.tools.ToolsColor

object ToolsPaint {

    fun gradientLineBottomTop(canvas: Canvas, color: Int, size: Int) {
        gradientLine(canvas, color, size, GradientDrawable.Orientation.BOTTOM_TOP)
    }

    fun gradientLineLeftRight(canvas: Canvas, color: Int, size: Int) {
        gradientLine(canvas, color, size, GradientDrawable.Orientation.LEFT_RIGHT)
    }

    private fun gradientLine(canvas: Canvas, color: Int, size: Int, orientation: GradientDrawable.Orientation) {
        val gradientDrawable = GradientDrawable(
                orientation,
                intArrayOf(color, ToolsColor.setAlpha(0, color)))
        gradientDrawable.setSize(canvas.width, size)
        gradientDrawable.setBounds(0, canvas.height - size, canvas.width, canvas.height)
        gradientDrawable.draw(canvas)
    }
}