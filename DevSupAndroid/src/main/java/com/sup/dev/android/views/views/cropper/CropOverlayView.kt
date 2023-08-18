package com.sup.dev.android.views.views.cropper

import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import java.util.*


class CropOverlayView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private var mScaleDetector: ScaleGestureDetector? = null
    private var mMultiTouchEnabled: Boolean = false
    private val mCropWindowHandler = CropWindowHandler()
    private var mCropWindowChangeListener: CropWindowChangeListener? = null
    private val mDrawRect = RectF()
    private var mBorderPaint: Paint? = null
    private var mBorderCornerPaint: Paint? = null
    private var mGuidelinePaint: Paint? = null
    private var mBackgroundPaint: Paint? = null
    private val mPath = Path()
    private val mBoundsPoints = FloatArray(8)
    private val mCalcBounds = RectF()
    private var mViewWidth: Int = 0
    private var mViewHeight: Int = 0
    private var mBorderCornerOffset: Float = 0.toFloat()
    private var mBorderCornerLength: Float = 0.toFloat()
    private var mInitialCropWindowPaddingRatio: Float = 0.toFloat()
    private var mTouchRadius: Float = 0.toFloat()
    private var mSnapRadius: Float = 0.toFloat()
    private var mMoveHandler: CropWindowMoveHandler? = null
    var isFixAspectRatio: Boolean = false
        private set
    var aspectRatioX: Int = 0
        set(aspectRatioX) {
            if (aspectRatioX <= 0) {
                throw IllegalArgumentException(
                        "Cannot set aspect ratio value to a number less than or equal to 0.")
            } else if (this.aspectRatioX != aspectRatioX) {
                field = aspectRatioX
                mTargetAspectRatio = this.aspectRatioX.toFloat() / aspectRatioY

                if (initializedCropWindow) {
                    initCropWindow()
                    invalidate()
                }
            }
        }
    var aspectRatioY: Int = 0
        set(aspectRatioY) {
            if (aspectRatioY <= 0) {
                throw IllegalArgumentException(
                        "Cannot set aspect ratio value to a number less than or equal to 0.")
            } else if (this.aspectRatioY != aspectRatioY) {
                field = aspectRatioY
                mTargetAspectRatio = aspectRatioX.toFloat() / this.aspectRatioY

                if (initializedCropWindow) {
                    initCropWindow()
                    invalidate()
                }
            }
        }
    private var mTargetAspectRatio = aspectRatioX.toFloat() / aspectRatioY
    var guidelines: ViewCropImage.Guidelines? = null
        set(guidelines) {
            if (this.guidelines != guidelines) {
                field = guidelines
                if (initializedCropWindow) {
                    invalidate()
                }
            }
        }
    // TURN off hardware acceleration
    // return hardware acceleration onBackPressed
    var cropShape: ViewCropImage.CropShape? = null
        set(cropShape) {
            if (this.cropShape != cropShape) {
                field = cropShape
                if (Build.VERSION.SDK_INT <= 17) {
                    if (this.cropShape == ViewCropImage.CropShape.OVAL) {
                        mOriginalLayerType = layerType
                        if (mOriginalLayerType != View.LAYER_TYPE_SOFTWARE) {
                            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                        } else {
                            mOriginalLayerType = null
                        }
                    } else if (mOriginalLayerType != null) {
                        setLayerType(mOriginalLayerType!!, null)
                        mOriginalLayerType = null
                    }
                }
                invalidate()
            }
        }
    var initialCropWindowRect: Rect? = Rect()
        set(rect) {
            initialCropWindowRect!!.set(rect ?: BitmapUtils.EMPTY_RECT)
            if (initializedCropWindow) {
                initCropWindow()
                invalidate()
                callOnCropWindowChanged(false)
            }
        }
    private var initializedCropWindow: Boolean = false
    private var mOriginalLayerType: Int? = null

    var cropWindowRect: RectF
        get() = mCropWindowHandler.rect
        set(rect) {
            mCropWindowHandler.rect = rect
        }

    private val isNonStraightAngleRotated: Boolean
        get() = mBoundsPoints[0] != mBoundsPoints[6] && mBoundsPoints[1] != mBoundsPoints[7]

    fun setCropWindowChangeListener(listener: CropWindowChangeListener) {
        mCropWindowChangeListener = listener
    }

    fun fixCurrentCropWindowRect() {
        val rect = cropWindowRect
        fixCropWindowRectByRules(rect)
        mCropWindowHandler.rect = rect
    }

    fun setBounds(boundsPoints: FloatArray?, viewWidth: Int, viewHeight: Int) {
        if (boundsPoints == null || !Arrays.equals(mBoundsPoints, boundsPoints)) {
            if (boundsPoints == null) {
                Arrays.fill(mBoundsPoints, 0f)
            } else {
                System.arraycopy(boundsPoints, 0, mBoundsPoints, 0, boundsPoints.size)
            }
            mViewWidth = viewWidth
            mViewHeight = viewHeight
            val cropRect = mCropWindowHandler.rect
            if (cropRect.width() == 0f || cropRect.height() == 0f) {
                initCropWindow()
            }
        }
    }

    fun resetCropOverlayView() {
        if (initializedCropWindow) {
            cropWindowRect = BitmapUtils.EMPTY_RECT_F
            initCropWindow()
            invalidate()
        }
    }

    fun setFixedAspectRatio(fixAspectRatio: Boolean) {
        if (isFixAspectRatio != fixAspectRatio) {
            isFixAspectRatio = fixAspectRatio
            if (initializedCropWindow) {
                initCropWindow()
                invalidate()
            }
        }
    }

    fun setSnapRadius(snapRadius: Float) {
        mSnapRadius = snapRadius
    }

    fun setMultiTouchEnabled(multiTouchEnabled: Boolean): Boolean {
        if (mMultiTouchEnabled != multiTouchEnabled) {
            mMultiTouchEnabled = multiTouchEnabled
            if (mMultiTouchEnabled && mScaleDetector == null) {
                mScaleDetector = ScaleGestureDetector(context, ScaleListener())
            }
            return true
        }
        return false
    }

    fun setMinCropResultSize(minCropResultWidth: Int, minCropResultHeight: Int) {
        mCropWindowHandler.setMinCropResultSize(minCropResultWidth, minCropResultHeight)
    }

    fun setMaxCropResultSize(maxCropResultWidth: Int, maxCropResultHeight: Int) {
        mCropWindowHandler.setMaxCropResultSize(maxCropResultWidth, maxCropResultHeight)
    }

    fun setCropWindowLimits(
            maxWidth: Float, maxHeight: Float, scaleFactorWidth: Float, scaleFactorHeight: Float) {
        mCropWindowHandler.setCropWindowLimits(
                maxWidth, maxHeight, scaleFactorWidth, scaleFactorHeight)
    }

    fun resetCropWindowRect() {
        if (initializedCropWindow) {
            initCropWindow()
            invalidate()
            callOnCropWindowChanged(false)
        }
    }

    fun setInitialAttributeValues(options: CropImageOptions) {

        mCropWindowHandler.setInitialAttributeValues(options)

        cropShape = options.cropShape

        setSnapRadius(options.snapRadius)

        guidelines = options.guidelines

        setFixedAspectRatio(options.fixAspectRatio)

        aspectRatioX = options.aspectRatioX

        aspectRatioY = options.aspectRatioY

        setMultiTouchEnabled(options.multiTouchEnabled)

        mTouchRadius = options.touchRadius

        mInitialCropWindowPaddingRatio = options.initialCropWindowPaddingRatio

        mBorderPaint = getNewPaintOrNull(options.borderLineThickness, options.borderLineColor)

        mBorderCornerOffset = options.borderCornerOffset
        mBorderCornerLength = options.borderCornerLength
        mBorderCornerPaint = getNewPaintOrNull(options.borderCornerThickness, options.borderCornerColor)

        mGuidelinePaint = getNewPaintOrNull(options.guidelinesThickness, options.guidelinesColor)

        mBackgroundPaint = getNewPaint(options.backgroundColor)
    }

    private fun initCropWindow() {

        val leftLimit = Math.max(BitmapUtils.getRectLeft(mBoundsPoints), 0f)
        val topLimit = Math.max(BitmapUtils.getRectTop(mBoundsPoints), 0f)
        val rightLimit = Math.min(BitmapUtils.getRectRight(mBoundsPoints), width.toFloat())
        val bottomLimit = Math.min(BitmapUtils.getRectBottom(mBoundsPoints), height.toFloat())

        if (rightLimit <= leftLimit || bottomLimit <= topLimit) {
            return
        }

        val rect = RectF()

        // Tells the attribute functions the crop window has already been initialized
        initializedCropWindow = true

        val horizontalPadding = mInitialCropWindowPaddingRatio * (rightLimit - leftLimit)
        val verticalPadding = mInitialCropWindowPaddingRatio * (bottomLimit - topLimit)

        if (initialCropWindowRect!!.width() > 0 && initialCropWindowRect!!.height() > 0) {
            // Get crop window position relative to the displayed image.
            rect.left = leftLimit + initialCropWindowRect!!.left / mCropWindowHandler.scaleFactorWidth
            rect.top = topLimit + initialCropWindowRect!!.top / mCropWindowHandler.scaleFactorHeight
            rect.right = rect.left + initialCropWindowRect!!.width() / mCropWindowHandler.scaleFactorWidth
            rect.bottom = rect.top + initialCropWindowRect!!.height() / mCropWindowHandler.scaleFactorHeight

            // Correct for floating point errors. Crop rect boundaries should not exceed the source Bitmap
            // bounds.
            rect.left = Math.max(leftLimit, rect.left)
            rect.top = Math.max(topLimit, rect.top)
            rect.right = Math.min(rightLimit, rect.right)
            rect.bottom = Math.min(bottomLimit, rect.bottom)

        } else if (isFixAspectRatio && rightLimit > leftLimit && bottomLimit > topLimit) {

            // If the image aspect ratio is wider than the crop aspect ratio,
            // then the image height is the determining initial length. Else, vice-versa.
            val bitmapAspectRatio = (rightLimit - leftLimit) / (bottomLimit - topLimit)
            if (bitmapAspectRatio > mTargetAspectRatio) {

                rect.top = topLimit + verticalPadding
                rect.bottom = bottomLimit - verticalPadding

                val centerX = width / 2f

                // dirty fix for wrong crop overlay aspect ratio when using fixed aspect ratio
                mTargetAspectRatio = aspectRatioX.toFloat() / aspectRatioY

                // Limits the aspect ratio to no less than 40 wide or 40 tall
                val cropWidth = Math.max(mCropWindowHandler.minCropWidth, rect.height() * mTargetAspectRatio)

                val halfCropWidth = cropWidth / 2f
                rect.left = centerX - halfCropWidth
                rect.right = centerX + halfCropWidth

            } else {

                rect.left = leftLimit + horizontalPadding
                rect.right = rightLimit - horizontalPadding

                val centerY = height / 2f

                // Limits the aspect ratio to no less than 40 wide or 40 tall
                val cropHeight = Math.max(mCropWindowHandler.minCropHeight, rect.width() / mTargetAspectRatio)

                val halfCropHeight = cropHeight / 2f
                rect.top = centerY - halfCropHeight
                rect.bottom = centerY + halfCropHeight
            }
        } else {
            // Initialize crop window to have 10% padding w/ respect to image.
            rect.left = leftLimit + horizontalPadding
            rect.top = topLimit + verticalPadding
            rect.right = rightLimit - horizontalPadding
            rect.bottom = bottomLimit - verticalPadding
        }

        fixCropWindowRectByRules(rect)

        mCropWindowHandler.rect = rect
    }

    private fun fixCropWindowRectByRules(rect: RectF) {
        if (rect.width() < mCropWindowHandler.minCropWidth) {
            val adj = (mCropWindowHandler.minCropWidth - rect.width()) / 2
            rect.left -= adj
            rect.right += adj
        }
        if (rect.height() < mCropWindowHandler.minCropHeight) {
            val adj = (mCropWindowHandler.minCropHeight - rect.height()) / 2
            rect.top -= adj
            rect.bottom += adj
        }
        if (rect.width() > mCropWindowHandler.maxCropWidth) {
            val adj = (rect.width() - mCropWindowHandler.maxCropWidth) / 2
            rect.left += adj
            rect.right -= adj
        }
        if (rect.height() > mCropWindowHandler.maxCropHeight) {
            val adj = (rect.height() - mCropWindowHandler.maxCropHeight) / 2
            rect.top += adj
            rect.bottom -= adj
        }

        calculateBounds(rect)
        if (mCalcBounds.width() > 0 && mCalcBounds.height() > 0) {
            val leftLimit = Math.max(mCalcBounds.left, 0f)
            val topLimit = Math.max(mCalcBounds.top, 0f)
            val rightLimit = Math.min(mCalcBounds.right, width.toFloat())
            val bottomLimit = Math.min(mCalcBounds.bottom, height.toFloat())
            if (rect.left < leftLimit) {
                rect.left = leftLimit
            }
            if (rect.top < topLimit) {
                rect.top = topLimit
            }
            if (rect.right > rightLimit) {
                rect.right = rightLimit
            }
            if (rect.bottom > bottomLimit) {
                rect.bottom = bottomLimit
            }
        }
        if (isFixAspectRatio && Math.abs(rect.width() - rect.height() * mTargetAspectRatio) > 0.1) {
            if (rect.width() > rect.height() * mTargetAspectRatio) {
                val adj = Math.abs(rect.height() * mTargetAspectRatio - rect.width()) / 2
                rect.left += adj
                rect.right -= adj
            } else {
                val adj = Math.abs(rect.width() / mTargetAspectRatio - rect.height()) / 2
                rect.top += adj
                rect.bottom -= adj
            }
        }
    }

    override fun onDraw(canvas: Canvas) {

        super.onDraw(canvas)

        // Draw translucent background for the cropped area.
        drawBackground(canvas)

        if (mCropWindowHandler.showGuidelines()) {
            // Determines whether guidelines should be drawn or not
            if (guidelines == ViewCropImage.Guidelines.ON) {
                drawGuidelines(canvas)
            } else if (guidelines == ViewCropImage.Guidelines.ON_TOUCH && mMoveHandler != null) {
                // Draw only when resizing
                drawGuidelines(canvas)
            }
        }

        drawBorders(canvas)

        drawCorners(canvas)
    }

    private fun drawBackground(canvas: Canvas) {

        val rect = mCropWindowHandler.rect

        val left = Math.max(BitmapUtils.getRectLeft(mBoundsPoints), 0f)
        val top = Math.max(BitmapUtils.getRectTop(mBoundsPoints), 0f)
        val right = Math.min(BitmapUtils.getRectRight(mBoundsPoints), width.toFloat())
        val bottom = Math.min(BitmapUtils.getRectBottom(mBoundsPoints), height.toFloat())

        if (cropShape == ViewCropImage.CropShape.RECTANGLE) {
            if (!isNonStraightAngleRotated || Build.VERSION.SDK_INT <= 17) {
                canvas.drawRect(left, top, right, rect.top, mBackgroundPaint!!)
                canvas.drawRect(left, rect.bottom, right, bottom, mBackgroundPaint!!)
                canvas.drawRect(left, rect.top, rect.left, rect.bottom, mBackgroundPaint!!)
                canvas.drawRect(rect.right, rect.top, right, rect.bottom, mBackgroundPaint!!)
            } else {
                mPath.reset()
                mPath.moveTo(mBoundsPoints[0], mBoundsPoints[1])
                mPath.lineTo(mBoundsPoints[2], mBoundsPoints[3])
                mPath.lineTo(mBoundsPoints[4], mBoundsPoints[5])
                mPath.lineTo(mBoundsPoints[6], mBoundsPoints[7])
                mPath.close()

                canvas.save()
                canvas.clipPath(mPath, Region.Op.INTERSECT)
                canvas.clipRect(rect, Region.Op.XOR)
                canvas.drawRect(left, top, right, bottom, mBackgroundPaint!!)
                canvas.restore()
            }
        } else {
            mPath.reset()
            if (Build.VERSION.SDK_INT <= 17 && cropShape == ViewCropImage.CropShape.OVAL) {
                mDrawRect.set(rect.left + 2, rect.top + 2, rect.right - 2, rect.bottom - 2)
            } else {
                mDrawRect.set(rect.left, rect.top, rect.right, rect.bottom)
            }
            mPath.addOval(mDrawRect, Path.Direction.CW)
            canvas.save()
            canvas.clipPath(mPath, Region.Op.XOR)
            canvas.drawRect(left, top, right, bottom, mBackgroundPaint!!)
            canvas.restore()
        }
    }

    private fun drawGuidelines(canvas: Canvas) {
        if (mGuidelinePaint != null) {
            val sw: Float = if (mBorderPaint != null) mBorderPaint!!.strokeWidth else 0f
            val rect = mCropWindowHandler.rect
            rect.inset(sw, sw)

            val oneThirdCropWidth = rect.width() / 3
            val oneThirdCropHeight = rect.height() / 3

            if (cropShape == ViewCropImage.CropShape.OVAL) {

                val w = rect.width() / 2 - sw
                val h = rect.height() / 2 - sw

                // Draw vertical guidelines.
                val x1 = rect.left + oneThirdCropWidth
                val x2 = rect.right - oneThirdCropWidth
                val yv = (h * Math.sin(Math.acos(((w - oneThirdCropWidth) / w).toDouble()))).toFloat()
                canvas.drawLine(x1, rect.top + h - yv, x1, rect.bottom - h + yv, mGuidelinePaint!!)
                canvas.drawLine(x2, rect.top + h - yv, x2, rect.bottom - h + yv, mGuidelinePaint!!)

                // Draw horizontal guidelines.
                val y1 = rect.top + oneThirdCropHeight
                val y2 = rect.bottom - oneThirdCropHeight
                val xv = (w * Math.cos(Math.asin(((h - oneThirdCropHeight) / h).toDouble()))).toFloat()
                canvas.drawLine(rect.left + w - xv, y1, rect.right - w + xv, y1, mGuidelinePaint!!)
                canvas.drawLine(rect.left + w - xv, y2, rect.right - w + xv, y2, mGuidelinePaint!!)
            } else {

                // Draw vertical guidelines.
                val x1 = rect.left + oneThirdCropWidth
                val x2 = rect.right - oneThirdCropWidth
                canvas.drawLine(x1, rect.top, x1, rect.bottom, mGuidelinePaint!!)
                canvas.drawLine(x2, rect.top, x2, rect.bottom, mGuidelinePaint!!)

                // Draw horizontal guidelines.
                val y1 = rect.top + oneThirdCropHeight
                val y2 = rect.bottom - oneThirdCropHeight
                canvas.drawLine(rect.left, y1, rect.right, y1, mGuidelinePaint!!)
                canvas.drawLine(rect.left, y2, rect.right, y2, mGuidelinePaint!!)
            }
        }
    }

    private fun drawBorders(canvas: Canvas) {
        if (mBorderPaint != null) {
            val w = mBorderPaint!!.strokeWidth
            val rect = mCropWindowHandler.rect
            rect.inset(w / 2, w / 2)

            if (cropShape == ViewCropImage.CropShape.RECTANGLE) {
                // Draw rectangle crop window border.
                canvas.drawRect(rect, mBorderPaint!!)
            } else {
                // Draw circular crop window border
                canvas.drawOval(rect, mBorderPaint!!)
            }
        }
    }

    private fun drawCorners(canvas: Canvas) {
        if (mBorderCornerPaint != null) {

            val lineWidth: Float = if (mBorderPaint != null) mBorderPaint!!.strokeWidth else 0f
            val cornerWidth: Float = mBorderCornerPaint!!.strokeWidth

            // for rectangle crop shape we allow the corners to be offset from the borders
            val w: Float = cornerWidth / 2f + if (cropShape == ViewCropImage.CropShape.RECTANGLE) mBorderCornerOffset else 0f

            val rect = mCropWindowHandler.rect
            rect.inset(w, w)

            val cornerOffset = (cornerWidth - lineWidth) / 2
            val cornerExtension = cornerWidth / 2 + cornerOffset

            // Top left
            canvas.drawLine(
                    rect.left - cornerOffset,
                    rect.top - cornerExtension,
                    rect.left - cornerOffset,
                    rect.top + mBorderCornerLength,
                    mBorderCornerPaint!!)
            canvas.drawLine(
                    rect.left - cornerExtension,
                    rect.top - cornerOffset,
                    rect.left + mBorderCornerLength,
                    rect.top - cornerOffset,
                    mBorderCornerPaint!!)

            // Top right
            canvas.drawLine(
                    rect.right + cornerOffset,
                    rect.top - cornerExtension,
                    rect.right + cornerOffset,
                    rect.top + mBorderCornerLength,
                    mBorderCornerPaint!!)
            canvas.drawLine(
                    rect.right + cornerExtension,
                    rect.top - cornerOffset,
                    rect.right - mBorderCornerLength,
                    rect.top - cornerOffset,
                    mBorderCornerPaint!!)

            // Bottom left
            canvas.drawLine(
                    rect.left - cornerOffset,
                    rect.bottom + cornerExtension,
                    rect.left - cornerOffset,
                    rect.bottom - mBorderCornerLength,
                    mBorderCornerPaint!!)
            canvas.drawLine(
                    rect.left - cornerExtension,
                    rect.bottom + cornerOffset,
                    rect.left + mBorderCornerLength,
                    rect.bottom + cornerOffset,
                    mBorderCornerPaint!!)

            // Bottom left
            canvas.drawLine(
                    rect.right + cornerOffset,
                    rect.bottom + cornerExtension,
                    rect.right + cornerOffset,
                    rect.bottom - mBorderCornerLength,
                    mBorderCornerPaint!!)
            canvas.drawLine(
                    rect.right + cornerExtension,
                    rect.bottom + cornerOffset,
                    rect.right - mBorderCornerLength,
                    rect.bottom + cornerOffset,
                    mBorderCornerPaint!!)
        }
    }

    private fun getNewPaint(color: Int): Paint {
        val paint = Paint()
        paint.color = color
        return paint
    }

    private fun getNewPaintOrNull(thickness: Float, color: Int): Paint? {
        if (thickness > 0) {
            val borderPaint = Paint()
            borderPaint.color = color
            borderPaint.strokeWidth = thickness
            borderPaint.style = Paint.Style.STROKE
            borderPaint.isAntiAlias = true
            return borderPaint
        } else {
            return null
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // If this View is not enabled, don't allow for touch interactions.
        if (isEnabled) {
            if (mMultiTouchEnabled) {
                mScaleDetector!!.onTouchEvent(event)
            }

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    onActionDown(event.x, event.y)
                    return true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    parent.requestDisallowInterceptTouchEvent(false)
                    onActionUp()
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    onActionMove(event.x, event.y)
                    parent.requestDisallowInterceptTouchEvent(true)
                    return true
                }
                else -> return false
            }
        } else {
            return false
        }
    }

    private fun onActionDown(x: Float, y: Float) {
        mMoveHandler = mCropWindowHandler.getMoveHandler(x, y, mTouchRadius, cropShape!!)
        if (mMoveHandler != null) {
            invalidate()
        }
    }

    private fun onActionUp() {
        if (mMoveHandler != null) {
            mMoveHandler = null
            callOnCropWindowChanged(false)
            invalidate()
        }
    }

    private fun onActionMove(x: Float, y: Float) {
        if (mMoveHandler != null) {
            var snapRadius = mSnapRadius
            val rect = mCropWindowHandler.rect

            if (calculateBounds(rect)) {
                snapRadius = 0f
            }

            mMoveHandler!!.move(
                    rect,
                    x,
                    y,
                    mCalcBounds,
                    mViewWidth,
                    mViewHeight,
                    snapRadius,
                    isFixAspectRatio,
                    mTargetAspectRatio)
            mCropWindowHandler.rect = rect
            callOnCropWindowChanged(true)
            invalidate()
        }
    }

    private fun calculateBounds(rect: RectF): Boolean {

        var left = BitmapUtils.getRectLeft(mBoundsPoints)
        var top = BitmapUtils.getRectTop(mBoundsPoints)
        var right = BitmapUtils.getRectRight(mBoundsPoints)
        var bottom = BitmapUtils.getRectBottom(mBoundsPoints)

        if (!isNonStraightAngleRotated) {
            mCalcBounds.set(left, top, right, bottom)
            return false
        } else {
            var x0 = mBoundsPoints[0]
            var y0 = mBoundsPoints[1]
            var x2 = mBoundsPoints[4]
            var y2 = mBoundsPoints[5]
            var x3 = mBoundsPoints[6]
            var y3 = mBoundsPoints[7]

            if (mBoundsPoints[7] < mBoundsPoints[1]) {
                if (mBoundsPoints[1] < mBoundsPoints[3]) {
                    x0 = mBoundsPoints[6]
                    y0 = mBoundsPoints[7]
                    x2 = mBoundsPoints[2]
                    y2 = mBoundsPoints[3]
                    x3 = mBoundsPoints[4]
                    y3 = mBoundsPoints[5]
                } else {
                    x0 = mBoundsPoints[4]
                    y0 = mBoundsPoints[5]
                    x2 = mBoundsPoints[0]
                    y2 = mBoundsPoints[1]
                    x3 = mBoundsPoints[2]
                    y3 = mBoundsPoints[3]
                }
            } else if (mBoundsPoints[1] > mBoundsPoints[3]) {
                x0 = mBoundsPoints[2]
                y0 = mBoundsPoints[3]
                x2 = mBoundsPoints[6]
                y2 = mBoundsPoints[7]
                x3 = mBoundsPoints[0]
                y3 = mBoundsPoints[1]
            }

            val a0 = (y3 - y0) / (x3 - x0)
            val a1 = -1f / a0
            val b0 = y0 - a0 * x0
            val b1 = y0 - a1 * x0
            val b2 = y2 - a0 * x2
            val b3 = y2 - a1 * x2

            val c0 = (rect.centerY() - rect.top) / (rect.centerX() - rect.left)
            val c1 = -c0
            val d0 = rect.top - c0 * rect.left
            val d1 = rect.top - c1 * rect.right

            left = Math.max(left, if ((d0 - b0) / (a0 - c0) < rect.right) (d0 - b0) / (a0 - c0) else left)
            left = Math.max(left, if ((d0 - b1) / (a1 - c0) < rect.right) (d0 - b1) / (a1 - c0) else left)
            left = Math.max(left, if ((d1 - b3) / (a1 - c1) < rect.right) (d1 - b3) / (a1 - c1) else left)
            right = Math.min(right, if ((d1 - b1) / (a1 - c1) > rect.left) (d1 - b1) / (a1 - c1) else right)
            right = Math.min(right, if ((d1 - b2) / (a0 - c1) > rect.left) (d1 - b2) / (a0 - c1) else right)
            right = Math.min(right, if ((d0 - b2) / (a0 - c0) > rect.left) (d0 - b2) / (a0 - c0) else right)

            top = Math.max(top, Math.max(a0 * left + b0, a1 * right + b1))
            bottom = Math.min(bottom, Math.min(a1 * left + b3, a0 * right + b2))

            mCalcBounds.left = left
            mCalcBounds.top = top
            mCalcBounds.right = right
            mCalcBounds.bottom = bottom
            return true
        }
    }

    private fun callOnCropWindowChanged(inProgress: Boolean) {
        try {
            if (mCropWindowChangeListener != null) {
                mCropWindowChangeListener!!.onCropWindowChanged(inProgress)
            }
        } catch (e: Exception) {
            Log.e("AIC", "Exception in crop window changed", e)
        }

    }

    interface CropWindowChangeListener {

        fun onCropWindowChanged(inProgress: Boolean)
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val rect = mCropWindowHandler.rect

            val x = detector.focusX
            val y = detector.focusY
            val dY = detector.currentSpanY / 2
            val dX = detector.currentSpanX / 2

            val newTop = y - dY
            val newLeft = x - dX
            val newRight = x + dX
            val newBottom = y + dY

            if (newLeft < newRight
                    && newTop <= newBottom
                    && newLeft >= 0
                    && newRight <= mCropWindowHandler.maxCropWidth
                    && newTop >= 0
                    && newBottom <= mCropWindowHandler.maxCropHeight) {

                rect.set(newLeft, newTop, newRight, newBottom)
                mCropWindowHandler.rect = rect
                invalidate()
            }

            return true
        }
    }
}
