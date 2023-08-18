package com.sup.dev.android.views.views.cropper

import android.graphics.RectF


internal class CropWindowHandler {

    private val mEdges = RectF()
    private val mGetEdges = RectF()
    private var mMinCropWindowWidth: Float = 0.toFloat()
    private var mMinCropWindowHeight: Float = 0.toFloat()
    private var mMaxCropWindowWidth: Float = 0.toFloat()
    private var mMaxCropWindowHeight: Float = 0.toFloat()
    private var mMinCropResultWidth: Float = 0.toFloat()
    private var mMinCropResultHeight: Float = 0.toFloat()
    private var mMaxCropResultWidth: Float = 0.toFloat()
    private var mMaxCropResultHeight: Float = 0.toFloat()
    var scaleFactorWidth = 1f
        private set
    var scaleFactorHeight = 1f
        private set

    var rect: RectF
        get() {
            mGetEdges.set(mEdges)
            return mGetEdges
        }
        set(rect) = mEdges.set(rect)

    val minCropWidth: Float
        get() = Math.max(mMinCropWindowWidth, mMinCropResultWidth / scaleFactorWidth)

    val minCropHeight: Float
        get() = Math.max(mMinCropWindowHeight, mMinCropResultHeight / scaleFactorHeight)

    val maxCropWidth: Float
        get() = Math.min(mMaxCropWindowWidth, mMaxCropResultWidth / scaleFactorWidth)

    val maxCropHeight: Float
        get() = Math.min(mMaxCropWindowHeight, mMaxCropResultHeight / scaleFactorHeight)

    fun setMinCropResultSize(minCropResultWidth: Int, minCropResultHeight: Int) {
        mMinCropResultWidth = minCropResultWidth.toFloat()
        mMinCropResultHeight = minCropResultHeight.toFloat()
    }

    fun setMaxCropResultSize(maxCropResultWidth: Int, maxCropResultHeight: Int) {
        mMaxCropResultWidth = maxCropResultWidth.toFloat()
        mMaxCropResultHeight = maxCropResultHeight.toFloat()
    }

    fun setCropWindowLimits(
            maxWidth: Float, maxHeight: Float, scaleFactorWidth: Float, scaleFactorHeight: Float) {
        mMaxCropWindowWidth = maxWidth
        mMaxCropWindowHeight = maxHeight
        this.scaleFactorWidth = scaleFactorWidth
        this.scaleFactorHeight = scaleFactorHeight
    }

    fun setInitialAttributeValues(options: CropImageOptions) {
        mMinCropWindowWidth = options.minCropWindowWidth.toFloat()
        mMinCropWindowHeight = options.minCropWindowHeight.toFloat()
        mMinCropResultWidth = options.minCropResultWidth.toFloat()
        mMinCropResultHeight = options.minCropResultHeight.toFloat()
        mMaxCropResultWidth = options.maxCropResultWidth.toFloat()
        mMaxCropResultHeight = options.maxCropResultHeight.toFloat()
    }

    fun showGuidelines(): Boolean {
        return !(mEdges.width() < 100 || mEdges.height() < 100)
    }

    fun getMoveHandler(x: Float, y: Float, targetRadius: Float, cropShape: ViewCropImage.CropShape): CropWindowMoveHandler? {
        val type = if (cropShape == ViewCropImage.CropShape.OVAL)
            getOvalPressedMoveType(x, y)
        else
            getRectanglePressedMoveType(x, y, targetRadius)
        return if (type != null) CropWindowMoveHandler(type, this, x, y) else null
    }

    private fun getRectanglePressedMoveType(
            x: Float, y: Float, targetRadius: Float): CropWindowMoveHandler.Type? {
        var moveType: CropWindowMoveHandler.Type? = null

        // Note: corner-handles take precedence, then side-handles, then center.
        if (isInCornerTargetZone(x, y, mEdges.left, mEdges.top, targetRadius)) {
            moveType = CropWindowMoveHandler.Type.TOP_LEFT
        } else if (isInCornerTargetZone(
                        x, y, mEdges.right, mEdges.top, targetRadius)) {
            moveType = CropWindowMoveHandler.Type.TOP_RIGHT
        } else if (isInCornerTargetZone(
                        x, y, mEdges.left, mEdges.bottom, targetRadius)) {
            moveType = CropWindowMoveHandler.Type.BOTTOM_LEFT
        } else if (isInCornerTargetZone(
                        x, y, mEdges.right, mEdges.bottom, targetRadius)) {
            moveType = CropWindowMoveHandler.Type.BOTTOM_RIGHT
        } else if (isInCenterTargetZone(
                        x, y, mEdges.left, mEdges.top, mEdges.right, mEdges.bottom) && focusCenter()) {
            moveType = CropWindowMoveHandler.Type.CENTER
        } else if (isInHorizontalTargetZone(
                        x, y, mEdges.left, mEdges.right, mEdges.top, targetRadius)) {
            moveType = CropWindowMoveHandler.Type.TOP
        } else if (isInHorizontalTargetZone(
                        x, y, mEdges.left, mEdges.right, mEdges.bottom, targetRadius)) {
            moveType = CropWindowMoveHandler.Type.BOTTOM
        } else if (isInVerticalTargetZone(
                        x, y, mEdges.left, mEdges.top, mEdges.bottom, targetRadius)) {
            moveType = CropWindowMoveHandler.Type.LEFT
        } else if (isInVerticalTargetZone(
                        x, y, mEdges.right, mEdges.top, mEdges.bottom, targetRadius)) {
            moveType = CropWindowMoveHandler.Type.RIGHT
        } else if (isInCenterTargetZone(
                        x, y, mEdges.left, mEdges.top, mEdges.right, mEdges.bottom) && !focusCenter()) {
            moveType = CropWindowMoveHandler.Type.CENTER
        }

        return moveType
    }


    private fun getOvalPressedMoveType(x: Float, y: Float): CropWindowMoveHandler.Type {

        /*
       Use a 6x6 grid system divided into 9 "handles", with the center the biggest region. While
       this is not perfect, it's a good quick-to-ship approach.

       TL T T T T TR
        L C C C C R
        L C C C C R
        L C C C C R
        L C C C C R
       BL B B B B BR
    */

        val cellLength = mEdges.width() / 6
        val leftCenter = mEdges.left + cellLength
        val rightCenter = mEdges.left + 5 * cellLength

        val cellHeight = mEdges.height() / 6
        val topCenter = mEdges.top + cellHeight
        val bottomCenter = mEdges.top + 5 * cellHeight

        val moveType: CropWindowMoveHandler.Type
        if (x < leftCenter) {
            if (y < topCenter) {
                moveType = CropWindowMoveHandler.Type.TOP_LEFT
            } else if (y < bottomCenter) {
                moveType = CropWindowMoveHandler.Type.LEFT
            } else {
                moveType = CropWindowMoveHandler.Type.BOTTOM_LEFT
            }
        } else if (x < rightCenter) {
            if (y < topCenter) {
                moveType = CropWindowMoveHandler.Type.TOP
            } else if (y < bottomCenter) {
                moveType = CropWindowMoveHandler.Type.CENTER
            } else {
                moveType = CropWindowMoveHandler.Type.BOTTOM
            }
        } else {
            if (y < topCenter) {
                moveType = CropWindowMoveHandler.Type.TOP_RIGHT
            } else if (y < bottomCenter) {
                moveType = CropWindowMoveHandler.Type.RIGHT
            } else {
                moveType = CropWindowMoveHandler.Type.BOTTOM_RIGHT
            }
        }

        return moveType
    }

    private fun isInCornerTargetZone(
            x: Float, y: Float, handleX: Float, handleY: Float, targetRadius: Float): Boolean {
        return Math.abs(x - handleX) <= targetRadius && Math.abs(y - handleY) <= targetRadius
    }

    private fun isInHorizontalTargetZone(
            x: Float, y: Float, handleXStart: Float, handleXEnd: Float, handleY: Float, targetRadius: Float): Boolean {
        return x > handleXStart && x < handleXEnd && Math.abs(y - handleY) <= targetRadius
    }

    private fun isInVerticalTargetZone(
            x: Float, y: Float, handleX: Float, handleYStart: Float, handleYEnd: Float, targetRadius: Float): Boolean {
        return Math.abs(x - handleX) <= targetRadius && y > handleYStart && y < handleYEnd
    }

    private fun isInCenterTargetZone(
            x: Float, y: Float, left: Float, top: Float, right: Float, bottom: Float): Boolean {
        return x > left && x < right && y > top && y < bottom
    }


    private fun focusCenter(): Boolean {
        return !showGuidelines()
    }

}
