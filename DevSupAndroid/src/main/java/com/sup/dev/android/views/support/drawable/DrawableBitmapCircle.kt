package com.sup.dev.android.views.support.drawable

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import com.sup.dev.android.tools.ToolsResources

class DrawableBitmapCircle(bitmap: Bitmap) : BitmapDrawable(bitmap) {

    constructor(res:Int) : this(ToolsResources.getBitmap(res))

    private val path = Path()
    override fun draw(canvas: Canvas) {

        canvas.save()

        path.reset()
        path.addCircle((bounds.width() / 2).toFloat(), (bounds.height() / 2).toFloat(), (Math.min(bounds.width(), bounds.height()) / 2).toFloat(), Path.Direction.CCW)
        canvas.clipPath(path)

        super.draw(canvas)

        canvas.restore()
    }


}