package com.sup.dev.android.views.views.cropper

import android.content.Context
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import com.sup.dev.android.R
import java.lang.ref.WeakReference
import java.util.*


class ViewCropImage @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    private val mImageView: ImageView
    private val mCropOverlayView: CropOverlayView?
    private val mImageMatrix = Matrix()
    private val mImageInverseMatrix = Matrix()
    private val mProgressBar: ProgressBar
    private val mImagePoints = FloatArray(8)
    private val mScaleImagePoints = FloatArray(8)
    private var mAnimation: CropImageAnimation? = null
    var originalBitmap: Bitmap? = null
        private set
    private var mInitialDegreesRotated: Int = 0
    private var mDegreesRotated: Int = 0
    private var mFlipHorizontally: Boolean = false
    private var mFlipVertically: Boolean = false
    private var mLayoutWidth: Int = 0
    private var mLayoutHeight: Int = 0
    private var mImageResource: Int = 0
    private var mScaleType: ScaleType? = null
    var isSaveBitmapToInstanceState = false
    private var mShowCropOverlay = true
    private var mShowProgressBar = true
    private var mAutoZoomEnabled = true
    private var mMaxZoom: Int = 0
    private var mOnCropOverlayReleasedListener: OnSetCropOverlayReleasedListener? = null
    private var mOnSetCropOverlayMovedListener: OnSetCropOverlayMovedListener? = null
    private var mOnSetCropWindowChangeListener: OnSetCropWindowChangeListener? = null
    private var mOnSetImageUriCompleteListener: OnSetImageUriCompleteListener? = null
    private var mOnCropImageCompleteListener: OnCropImageCompleteListener? = null
    var imageUri: Uri? = null
        private set
    private var mLoadedSampleSize = 1
    private var mZoom = 1f
    private var mZoomOffsetX: Float = 0.toFloat()
    private var mZoomOffsetY: Float = 0.toFloat()
    private var mRestoreCropWindowRect: RectF? = null
    private var mRestoreDegreesRotated: Int = 0
    private var mSizeChanged: Boolean = false
    private var mSaveInstanceStateBitmapUri: Uri? = null
    private var mBitmapLoadingWorkerTask: WeakReference<BitmapLoadingWorkerTask>? = null
    private var mBitmapCroppingWorkerTask: WeakReference<BitmapCroppingWorkerTask>? = null

    var scaleType: ScaleType?
        get() = mScaleType
        set(scaleType) {
            if (scaleType != mScaleType) {
                mScaleType = scaleType
                mZoom = 1f
                mZoomOffsetY = 0f
                mZoomOffsetX = mZoomOffsetY
                mCropOverlayView!!.resetCropOverlayView()
                requestLayout()
            }
        }

    var cropShape: CropShape?
        get() = mCropOverlayView!!.cropShape
        set(cropShape) {
            mCropOverlayView!!.cropShape = cropShape
        }

    var isAutoZoomEnabled: Boolean
        get() = mAutoZoomEnabled
        set(autoZoomEnabled) {
            if (mAutoZoomEnabled != autoZoomEnabled) {
                mAutoZoomEnabled = autoZoomEnabled
                handleCropWindowChanged(false, false)
                mCropOverlayView!!.invalidate()
            }
        }

    var maxZoom: Int
        get() = mMaxZoom
        set(maxZoom) {
            if (mMaxZoom != maxZoom && maxZoom > 0) {
                mMaxZoom = maxZoom
                handleCropWindowChanged(false, false)
                mCropOverlayView!!.invalidate()
            }
        }


    var rotatedDegrees: Int
        get() = mDegreesRotated
        set(degrees) {
            if (mDegreesRotated != degrees) {
                rotateImage(degrees - mDegreesRotated)
            }
        }

    val isFixAspectRatio: Boolean
        get() = mCropOverlayView!!.isFixAspectRatio

    var isFlippedHorizontally: Boolean
        get() = mFlipHorizontally
        set(flipHorizontally) {
            if (mFlipHorizontally != flipHorizontally) {
                mFlipHorizontally = flipHorizontally
                applyImageMatrix(width.toFloat(), height.toFloat(), true, false)
            }
        }

    var isFlippedVertically: Boolean
        get() = mFlipVertically
        set(flipVertically) {
            if (mFlipVertically != flipVertically) {
                mFlipVertically = flipVertically
                applyImageMatrix(width.toFloat(), height.toFloat(), true, false)
            }
        }

    var guidelines: Guidelines?
        get() = mCropOverlayView!!.guidelines
        set(guidelines) {
            mCropOverlayView!!.guidelines = guidelines
        }

    val aspectRatio: Pair<Int, Int>
        get() = Pair(mCropOverlayView!!.aspectRatioX, mCropOverlayView.aspectRatioY)

    var isShowProgressBar: Boolean
        get() = mShowProgressBar
        set(showProgressBar) {
            if (mShowProgressBar != showProgressBar) {
                mShowProgressBar = showProgressBar
                setProgressBarVisibility()
            }
        }

    var isShowCropOverlay: Boolean
        get() = mShowCropOverlay
        set(showCropOverlay) {
            if (mShowCropOverlay != showCropOverlay) {
                mShowCropOverlay = showCropOverlay
                setCropOverlayVisibility()
            }
        }

    var imageResource: Int
        get() = mImageResource
        set(resId) {
            if (resId != 0) {
                mCropOverlayView!!.initialCropWindowRect = null
                val bitmap = BitmapFactory.decodeResource(resources, resId)
                setBitmap(bitmap, resId, null, 1, 0)
            }
        }

    val wholeImageRect: Rect?
        get() {
            val loadedSampleSize = mLoadedSampleSize
            val bitmap = originalBitmap ?: return null

            val orgWidth = bitmap.width * loadedSampleSize
            val orgHeight = bitmap.height * loadedSampleSize
            return Rect(0, 0, orgWidth, orgHeight)
        }

    var cropRect: Rect?
        get() {
            val loadedSampleSize = mLoadedSampleSize
            val bitmap = originalBitmap ?: return null

            val points = cropPoints

            val orgWidth = bitmap.width * loadedSampleSize
            val orgHeight = bitmap.height * loadedSampleSize

            return BitmapUtils.getRectFromPoints(
                    points,
                    orgWidth,
                    orgHeight,
                    mCropOverlayView!!.isFixAspectRatio,
                    mCropOverlayView.aspectRatioX,
                    mCropOverlayView.aspectRatioY)
        }
        set(rect) {
            mCropOverlayView!!.initialCropWindowRect = rect
        }

    val cropWindowRect: RectF?
        get() {
            return mCropOverlayView?.cropWindowRect
        }

    val cropPoints: FloatArray
        get() {

            val cropWindowRect = mCropOverlayView!!.cropWindowRect

            val points = floatArrayOf(cropWindowRect.left, cropWindowRect.top, cropWindowRect.right, cropWindowRect.top, cropWindowRect.right, cropWindowRect.bottom, cropWindowRect.left, cropWindowRect.bottom)

            mImageMatrix.invert(mImageInverseMatrix)
            mImageInverseMatrix.mapPoints(points)

            for (i in points.indices) {
                points[i] *= mLoadedSampleSize.toFloat()
            }

            return points
        }

    val croppedImage: Bitmap?
        get() = getCroppedImage(0, 0, RequestSizeOptions.NONE)

    init {

        val options = CropImageOptions()

        val a = context.obtainStyledAttributes(attrs, R.styleable.ViewCropImage, 0, 0)
        options.fixAspectRatio = a.getBoolean(R.styleable.ViewCropImage_cropFixAspectRatio, options.fixAspectRatio)
        options.aspectRatioX = a.getInteger(R.styleable.ViewCropImage_cropAspectRatioX, options.aspectRatioX)
        options.aspectRatioY = a.getInteger(R.styleable.ViewCropImage_cropAspectRatioY, options.aspectRatioY)
        options.scaleType = ScaleType.values()[a.getInt(R.styleable.ViewCropImage_cropScaleType, options.scaleType.ordinal)]
        options.autoZoomEnabled = a.getBoolean(R.styleable.ViewCropImage_cropAutoZoomEnabled, options.autoZoomEnabled)
        options.multiTouchEnabled = a.getBoolean(R.styleable.ViewCropImage_cropMultiTouchEnabled, options.multiTouchEnabled)
        options.maxZoom = a.getInteger(R.styleable.ViewCropImage_cropMaxZoom, options.maxZoom)
        options.cropShape = CropShape.values()[a.getInt(R.styleable.ViewCropImage_cropShape, options.cropShape.ordinal)]
        options.guidelines = Guidelines.values()[a.getInt(R.styleable.ViewCropImage_cropGuidelines, options.guidelines.ordinal)]
        options.snapRadius = a.getDimension(R.styleable.ViewCropImage_cropSnapRadius, options.snapRadius)
        options.touchRadius = a.getDimension(R.styleable.ViewCropImage_cropTouchRadius, options.touchRadius)
        options.initialCropWindowPaddingRatio = a.getFloat(R.styleable.ViewCropImage_cropInitialCropWindowPaddingRatio, options.initialCropWindowPaddingRatio)
        options.borderLineThickness = a.getDimension(R.styleable.ViewCropImage_cropBorderLineThickness, options.borderLineThickness)
        options.borderLineColor = a.getInteger(R.styleable.ViewCropImage_cropBorderLineColor, options.borderLineColor)
        options.borderCornerThickness = a.getDimension(R.styleable.ViewCropImage_cropBorderCornerThickness, options.borderCornerThickness)
        options.borderCornerOffset = a.getDimension(R.styleable.ViewCropImage_cropBorderCornerOffset, options.borderCornerOffset)
        options.borderCornerLength = a.getDimension(R.styleable.ViewCropImage_cropBorderCornerLength, options.borderCornerLength)
        options.borderCornerColor = a.getInteger(R.styleable.ViewCropImage_cropBorderCornerColor, options.borderCornerColor)
        options.guidelinesThickness = a.getDimension(R.styleable.ViewCropImage_cropGuidelinesThickness, options.guidelinesThickness)
        options.guidelinesColor = a.getInteger(R.styleable.ViewCropImage_cropGuidelinesColor, options.guidelinesColor)
        options.backgroundColor = a.getInteger(R.styleable.ViewCropImage_cropBackgroundColor, options.backgroundColor)
        options.showCropOverlay = a.getBoolean(R.styleable.ViewCropImage_cropShowCropOverlay, mShowCropOverlay)
        options.showProgressBar = a.getBoolean(R.styleable.ViewCropImage_cropShowProgressBar, mShowProgressBar)
        options.borderCornerThickness = a.getDimension(R.styleable.ViewCropImage_cropBorderCornerThickness, options.borderCornerThickness)
        options.minCropWindowWidth = a.getDimension(R.styleable.ViewCropImage_cropMinCropWindowWidth, options.minCropWindowWidth.toFloat()).toInt()
        options.minCropWindowHeight = a.getDimension(R.styleable.ViewCropImage_cropMinCropWindowHeight, options.minCropWindowHeight.toFloat()).toInt()
        options.minCropResultWidth = a.getFloat(R.styleable.ViewCropImage_cropMinCropResultWidthPX, options.minCropResultWidth.toFloat()).toInt()
        options.minCropResultHeight = a.getFloat(R.styleable.ViewCropImage_cropMinCropResultHeightPX, options.minCropResultHeight.toFloat()).toInt()
        options.maxCropResultWidth = a.getFloat(R.styleable.ViewCropImage_cropMaxCropResultWidthPX, options.maxCropResultWidth.toFloat()).toInt()
        options.maxCropResultHeight = a.getFloat(R.styleable.ViewCropImage_cropMaxCropResultHeightPX, options.maxCropResultHeight.toFloat()).toInt()
        options.flipHorizontally = a.getBoolean(R.styleable.ViewCropImage_cropFlipHorizontally, options.flipHorizontally)
        options.flipVertically = a.getBoolean(R.styleable.ViewCropImage_cropFlipHorizontally, options.flipVertically)
        isSaveBitmapToInstanceState = a.getBoolean(R.styleable.ViewCropImage_cropSaveBitmapToInstanceState, isSaveBitmapToInstanceState)
        if (a.hasValue(R.styleable.ViewCropImage_cropAspectRatioX)
                && a.hasValue(R.styleable.ViewCropImage_cropAspectRatioX)
                && !a.hasValue(R.styleable.ViewCropImage_cropFixAspectRatio)) {
            options.fixAspectRatio = true
        }
        a.recycle()

        options.validate()

        mScaleType = options.scaleType
        mAutoZoomEnabled = options.autoZoomEnabled
        mMaxZoom = options.maxZoom
        mShowCropOverlay = options.showCropOverlay
        mShowProgressBar = options.showProgressBar
        mFlipHorizontally = options.flipHorizontally
        mFlipVertically = options.flipVertically

        val inflater = LayoutInflater.from(context)
        val v = inflater.inflate(R.layout.view_crop_image_view, this, true)

        mImageView = v.findViewById(R.id.vDevSupImage)
        mImageView.scaleType = ImageView.ScaleType.MATRIX

        mCropOverlayView = v.findViewById(R.id.vDevSupOverlay)
        mCropOverlayView!!.setCropWindowChangeListener(
                object : CropOverlayView.CropWindowChangeListener {
                    override fun onCropWindowChanged(inProgress: Boolean) {
                        handleCropWindowChanged(inProgress, true)
                        val listener = mOnCropOverlayReleasedListener
                        if (listener != null && !inProgress) {
                            listener.onCropOverlayReleased(cropRect)
                        }
                        val movedListener = mOnSetCropOverlayMovedListener
                        if (movedListener != null && inProgress) {
                            movedListener.onCropOverlayMoved(cropRect)
                        }
                    }
                })
        mCropOverlayView.setInitialAttributeValues(options)

        mProgressBar = v.findViewById(R.id.vDevSupProgress)
        setProgressBarVisibility()
    }

    fun setMultiTouchEnabled(multiTouchEnabled: Boolean) {
        if (mCropOverlayView!!.setMultiTouchEnabled(multiTouchEnabled)) {
            handleCropWindowChanged(false, false)
            mCropOverlayView.invalidate()
        }
    }

    fun setMinCropResultSize(minCropResultWidth: Int, minCropResultHeight: Int) {
        mCropOverlayView!!.setMinCropResultSize(minCropResultWidth, minCropResultHeight)
    }

    fun setMaxCropResultSize(maxCropResultWidth: Int, maxCropResultHeight: Int) {
        mCropOverlayView!!.setMaxCropResultSize(maxCropResultWidth, maxCropResultHeight)
    }

    fun setFixedAspectRatio(fixAspectRatio: Boolean) {
        mCropOverlayView!!.setFixedAspectRatio(fixAspectRatio)
    }

    fun setAspectRatio(aspectRatioX: Int, aspectRatioY: Int) {
        mCropOverlayView!!.aspectRatioX = aspectRatioX
        mCropOverlayView.aspectRatioY = aspectRatioY
        setFixedAspectRatio(true)
    }

    fun clearAspectRatio() {
        mCropOverlayView!!.aspectRatioX = 1
        mCropOverlayView.aspectRatioY = 1
        setFixedAspectRatio(false)
    }

    fun setSnapRadius(snapRadius: Float) {
        if (snapRadius >= 0) {
            mCropOverlayView!!.setSnapRadius(snapRadius)
        }
    }

    fun resetCropRect() {
        mZoom = 1f
        mZoomOffsetX = 0f
        mZoomOffsetY = 0f
        mDegreesRotated = mInitialDegreesRotated
        mFlipHorizontally = false
        mFlipVertically = false
        applyImageMatrix(width.toFloat(), height.toFloat(), false, false)
        mCropOverlayView!!.resetCropWindowRect()
    }

    @JvmOverloads
    fun getCroppedImage(reqWidth: Int, reqHeight: Int, options: RequestSizeOptions = RequestSizeOptions.RESIZE_INSIDE): Bitmap? {
        var reqWidthV = reqWidth
        var reqHeightV = reqHeight
        var croppedBitmap: Bitmap? = null
        if (originalBitmap != null) {
            mImageView.clearAnimation()

            reqWidthV = if (options != RequestSizeOptions.NONE) reqWidthV else 0
            reqHeightV = if (options != RequestSizeOptions.NONE) reqHeightV else 0

            if (imageUri != null && (mLoadedSampleSize > 1 || options == RequestSizeOptions.SAMPLING)) {
                val orgWidth = originalBitmap!!.width * mLoadedSampleSize
                val orgHeight = originalBitmap!!.height * mLoadedSampleSize
                val bitmapSampled = BitmapUtils.cropBitmap(
                        context,
                        imageUri!!,
                        cropPoints,
                        mDegreesRotated,
                        orgWidth,
                        orgHeight,
                        mCropOverlayView!!.isFixAspectRatio,
                        mCropOverlayView.aspectRatioX,
                        mCropOverlayView.aspectRatioY,
                        reqWidthV,
                        reqHeightV,
                        mFlipHorizontally,
                        mFlipVertically)
                croppedBitmap = bitmapSampled.bitmap
            } else {
                croppedBitmap = BitmapUtils.cropBitmapObjectHandleOOM(
                        originalBitmap!!,
                        cropPoints,
                        mDegreesRotated,
                        mCropOverlayView!!.isFixAspectRatio,
                        mCropOverlayView.aspectRatioX,
                        mCropOverlayView.aspectRatioY,
                        mFlipHorizontally,
                        mFlipVertically)
                        .bitmap
            }

            if(croppedBitmap == null) return null

            croppedBitmap = BitmapUtils.resizeBitmap(croppedBitmap, reqWidthV, reqHeightV, options)
        }

        return croppedBitmap
    }

    fun getCroppedImageAsync() {
        getCroppedImageAsync(0, 0, RequestSizeOptions.NONE)
    }

    @JvmOverloads
    fun getCroppedImageAsync(reqWidth: Int, reqHeight: Int, options: RequestSizeOptions = RequestSizeOptions.RESIZE_INSIDE) {
        if (mOnCropImageCompleteListener == null) {
            throw IllegalArgumentException("mOnCropImageCompleteListener is not set")
        }
        startCropWorkerTask(reqWidth, reqHeight, options, null, null, 0)
    }

    fun saveCroppedImageAsync(saveUri: Uri) {
        saveCroppedImageAsync(saveUri, Bitmap.CompressFormat.JPEG, 90, 0, 0, RequestSizeOptions.NONE)
    }

    fun saveCroppedImageAsync(
            saveUri: Uri, saveCompressFormat: Bitmap.CompressFormat, saveCompressQuality: Int) {
        saveCroppedImageAsync(
                saveUri, saveCompressFormat, saveCompressQuality, 0, 0, RequestSizeOptions.NONE)
    }

    @JvmOverloads
    fun saveCroppedImageAsync(
            saveUri: Uri,
            saveCompressFormat: Bitmap.CompressFormat,
            saveCompressQuality: Int,
            reqWidth: Int,
            reqHeight: Int,
            options: RequestSizeOptions = RequestSizeOptions.RESIZE_INSIDE) {
        if (mOnCropImageCompleteListener == null) {
            throw IllegalArgumentException("mOnCropImageCompleteListener is not set")
        }
        startCropWorkerTask(
                reqWidth, reqHeight, options, saveUri, saveCompressFormat, saveCompressQuality)
    }

    fun setOnSetCropOverlayReleasedListener(listener: OnSetCropOverlayReleasedListener) {
        mOnCropOverlayReleasedListener = listener
    }

    fun setOnSetCropOverlayMovedListener(listener: OnSetCropOverlayMovedListener) {
        mOnSetCropOverlayMovedListener = listener
    }

    fun setOnCropWindowChangedListener(listener: OnSetCropWindowChangeListener) {
        mOnSetCropWindowChangeListener = listener
    }

    fun setOnSetImageUriCompleteListener(listener: OnSetImageUriCompleteListener) {
        mOnSetImageUriCompleteListener = listener
    }

    fun setOnCropImageCompleteListener(listener: OnCropImageCompleteListener) {
        mOnCropImageCompleteListener = listener
    }

    fun setImageBitmap(bitmap: Bitmap) {
        mCropOverlayView!!.initialCropWindowRect = null
        setBitmap(bitmap, 0, null, 1, 0)
    }

    fun setImageBitmap(bitmap: Bitmap?, exif: ExifInterface?) {
        val setBitmap: Bitmap?
        var degreesRotated = 0
        if (bitmap != null && exif != null) {
            val result = BitmapUtils.rotateBitmapByExif(bitmap, exif)
            setBitmap = result.bitmap
            degreesRotated = result.degrees
            mInitialDegreesRotated = result.degrees
        } else {
            setBitmap = bitmap
        }
        mCropOverlayView!!.initialCropWindowRect = null
        setBitmap(setBitmap, 0, null, 1, degreesRotated)
    }

    fun setImageUriAsync(uri: Uri?) {
        if (uri != null) {
            val currentTask = if (mBitmapLoadingWorkerTask != null) mBitmapLoadingWorkerTask!!.get() else null
            if (currentTask != null) {
                currentTask.cancel(true)
            }

            clearImageInt()
            mRestoreCropWindowRect = null
            mRestoreDegreesRotated = 0
            mCropOverlayView!!.initialCropWindowRect = null
            mBitmapLoadingWorkerTask = WeakReference(BitmapLoadingWorkerTask(this, uri))
            mBitmapLoadingWorkerTask!!.get()!!.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            setProgressBarVisibility()
        }
    }

    fun clearImage() {
        clearImageInt()
        mCropOverlayView!!.initialCropWindowRect = null
    }

    fun rotateImage(degrees: Int) {
        var degreesV = degrees
        if (originalBitmap != null) {
            // Force degrees to be a non-zero value between 0 and 360 (inclusive)
            degreesV = if (degreesV < 0) degreesV % 360 + 360 else degreesV % 360


            val flipAxes = !mCropOverlayView!!.isFixAspectRatio && (degreesV in 46..134 || degreesV in 216..304)
            BitmapUtils.RECT.set(mCropOverlayView.cropWindowRect)
            var halfWidth = (if (flipAxes) BitmapUtils.RECT.height() else BitmapUtils.RECT.width()) / 2f
            var halfHeight = (if (flipAxes) BitmapUtils.RECT.width() else BitmapUtils.RECT.height()) / 2f
            if (flipAxes) {
                val isFlippedHorizontally = mFlipHorizontally
                mFlipHorizontally = mFlipVertically
                mFlipVertically = isFlippedHorizontally
            }

            mImageMatrix.invert(mImageInverseMatrix)

            BitmapUtils.POINTS[0] = BitmapUtils.RECT.centerX()
            BitmapUtils.POINTS[1] = BitmapUtils.RECT.centerY()
            BitmapUtils.POINTS[2] = 0f
            BitmapUtils.POINTS[3] = 0f
            BitmapUtils.POINTS[4] = 1f
            BitmapUtils.POINTS[5] = 0f
            mImageInverseMatrix.mapPoints(BitmapUtils.POINTS)

            mDegreesRotated = (mDegreesRotated + degreesV) % 360

            applyImageMatrix(width.toFloat(), height.toFloat(), true, false)

            mImageMatrix.mapPoints(BitmapUtils.POINTS2, BitmapUtils.POINTS)
            mZoom /= Math.sqrt(
                    Math.pow((BitmapUtils.POINTS2[4] - BitmapUtils.POINTS2[2]).toDouble(), 2.0) + Math.pow((BitmapUtils.POINTS2[5] - BitmapUtils.POINTS2[3]).toDouble(), 2.0)).toFloat()
            mZoom = Math.max(mZoom, 1f)

            applyImageMatrix(width.toFloat(), height.toFloat(), true, false)

            mImageMatrix.mapPoints(BitmapUtils.POINTS2, BitmapUtils.POINTS)

            val change = Math.sqrt(
                    Math.pow((BitmapUtils.POINTS2[4] - BitmapUtils.POINTS2[2]).toDouble(), 2.0) + Math.pow((BitmapUtils.POINTS2[5] - BitmapUtils.POINTS2[3]).toDouble(), 2.0))
            halfWidth *= change.toFloat()
            halfHeight *= change.toFloat()

            BitmapUtils.RECT.set(
                    BitmapUtils.POINTS2[0] - halfWidth,
                    BitmapUtils.POINTS2[1] - halfHeight,
                    BitmapUtils.POINTS2[0] + halfWidth,
                    BitmapUtils.POINTS2[1] + halfHeight)

            mCropOverlayView.resetCropOverlayView()
            mCropOverlayView.cropWindowRect = BitmapUtils.RECT
            applyImageMatrix(width.toFloat(), height.toFloat(), true, false)
            handleCropWindowChanged(false, false)

            mCropOverlayView.fixCurrentCropWindowRect()
        }
    }

    fun flipImageHorizontally() {
        mFlipHorizontally = !mFlipHorizontally
        applyImageMatrix(width.toFloat(), height.toFloat(), true, false)
    }

    fun flipImageVertically() {
        mFlipVertically = !mFlipVertically
        applyImageMatrix(width.toFloat(), height.toFloat(), true, false)
    }

    internal fun onSetImageUriAsyncComplete(result: BitmapLoadingWorkerTask.Result) {

        mBitmapLoadingWorkerTask = null
        setProgressBarVisibility()

        if (result.error == null) {
            mInitialDegreesRotated = result.degreesRotated
            setBitmap(result.bitmap, 0, result.uri, result.loadSampleSize, result.degreesRotated)
        }

        val listener = mOnSetImageUriCompleteListener
        if (listener != null) {
            listener.onSetImageUriComplete(this, result.uri, result.error)
        }
    }


    internal fun onImageCroppingAsyncComplete(result: BitmapCroppingWorkerTask.Result) {

        mBitmapCroppingWorkerTask = null
        setProgressBarVisibility()

        val listener = mOnCropImageCompleteListener
        if (listener != null) {
            val cropResult = CropResult(
                    originalBitmap,
                    imageUri,
                    result.bitmap,
                    result.uri,
                    result.error,
                    cropPoints,
                    cropRect,
                    wholeImageRect,
                    rotatedDegrees,
                    result.sampleSize)
            listener.onCropImageComplete(this, cropResult)
        }
    }

    private fun setBitmap(bitmap: Bitmap?, imageResource: Int, imageUri: Uri?, loadSampleSize: Int, degreesRotated: Int) {
        if (originalBitmap == null || originalBitmap != bitmap) {

            mImageView.clearAnimation()

            clearImageInt()

            originalBitmap = bitmap
            mImageView.setImageBitmap(originalBitmap)

            this.imageUri = imageUri
            mImageResource = imageResource
            mLoadedSampleSize = loadSampleSize
            mDegreesRotated = degreesRotated

            applyImageMatrix(width.toFloat(), height.toFloat(), true, false)

            if (mCropOverlayView != null) {
                mCropOverlayView.resetCropOverlayView()
                setCropOverlayVisibility()
            }
        }
    }

    private fun clearImageInt() {

        // if we allocated the bitmap, release it as fast as possible
        if (originalBitmap != null && (mImageResource > 0 || imageUri != null)) {
            originalBitmap!!.recycle()
        }
        originalBitmap = null

        // clean the loaded image flags for new image
        mImageResource = 0
        imageUri = null
        mLoadedSampleSize = 1
        mDegreesRotated = 0
        mZoom = 1f
        mZoomOffsetX = 0f
        mZoomOffsetY = 0f
        mImageMatrix.reset()
        mSaveInstanceStateBitmapUri = null

        mImageView.setImageBitmap(null)

        setCropOverlayVisibility()
    }

    fun startCropWorkerTask(
            reqWidth: Int,
            reqHeight: Int,
            options: RequestSizeOptions,
            saveUri: Uri?,
            saveCompressFormat: Bitmap.CompressFormat?,
            saveCompressQuality: Int) {
        var reqWidth = reqWidth
        var reqHeight = reqHeight
        val bitmap = originalBitmap
        if (bitmap != null) {
            mImageView.clearAnimation()

            val currentTask = if (mBitmapCroppingWorkerTask != null) mBitmapCroppingWorkerTask!!.get() else null
            currentTask?.cancel(true)

            reqWidth = if (options != RequestSizeOptions.NONE) reqWidth else 0
            reqHeight = if (options != RequestSizeOptions.NONE) reqHeight else 0

            val orgWidth = bitmap.width * mLoadedSampleSize
            val orgHeight = bitmap.height * mLoadedSampleSize
            if (imageUri != null && (mLoadedSampleSize > 1 || options == RequestSizeOptions.SAMPLING)) {
                mBitmapCroppingWorkerTask = WeakReference(
                        BitmapCroppingWorkerTask(
                                this,
                                imageUri!!,
                                cropPoints,
                                mDegreesRotated,
                                orgWidth,
                                orgHeight,
                                mCropOverlayView!!.isFixAspectRatio,
                                mCropOverlayView.aspectRatioX,
                                mCropOverlayView.aspectRatioY,
                                reqWidth,
                                reqHeight,
                                mFlipHorizontally,
                                mFlipVertically,
                                options,
                                saveUri!!,
                                saveCompressFormat!!,
                                saveCompressQuality))
            } else {
                mBitmapCroppingWorkerTask = WeakReference(
                        BitmapCroppingWorkerTask(
                                this,
                                bitmap,
                                cropPoints,
                                mDegreesRotated,
                                mCropOverlayView!!.isFixAspectRatio,
                                mCropOverlayView.aspectRatioX,
                                mCropOverlayView.aspectRatioY,
                                reqWidth,
                                reqHeight,
                                mFlipHorizontally,
                                mFlipVertically,
                                options,
                                saveUri!!,
                                saveCompressFormat!!,
                                saveCompressQuality))
            }
            mBitmapCroppingWorkerTask!!.get()?.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            setProgressBarVisibility()
        }
    }

    public override fun onSaveInstanceState(): Parcelable? {
        if (imageUri == null && originalBitmap == null && mImageResource < 1) {
            return super.onSaveInstanceState()
        }

        val bundle = Bundle()
        var imageUri = this.imageUri
        if (isSaveBitmapToInstanceState && imageUri == null && mImageResource < 1) {
            imageUri = BitmapUtils.writeTempStateStoreBitmap(
                    context, originalBitmap!!, mSaveInstanceStateBitmapUri)
            mSaveInstanceStateBitmapUri = imageUri
        }
        if (imageUri != null && originalBitmap != null) {
            val key = UUID.randomUUID().toString()
            BitmapUtils.mStateBitmap = Pair(key, WeakReference<Bitmap>(originalBitmap))
            bundle.putString("LOADED_IMAGE_STATE_BITMAP_KEY", key)
        }
        if (mBitmapLoadingWorkerTask != null) {
            val task = mBitmapLoadingWorkerTask!!.get()
            if (task != null) {
                bundle.putParcelable("LOADING_IMAGE_URI", task.uri)
            }
        }
        bundle.putParcelable("instanceState", super.onSaveInstanceState())
        bundle.putParcelable("LOADED_IMAGE_URI", imageUri)
        bundle.putInt("LOADED_IMAGE_RESOURCE", mImageResource)
        bundle.putInt("LOADED_SAMPLE_SIZE", mLoadedSampleSize)
        bundle.putInt("DEGREES_ROTATED", mDegreesRotated)
        bundle.putParcelable("INITIAL_CROP_RECT", mCropOverlayView!!.initialCropWindowRect)

        BitmapUtils.RECT.set(mCropOverlayView.cropWindowRect)

        mImageMatrix.invert(mImageInverseMatrix)
        mImageInverseMatrix.mapRect(BitmapUtils.RECT)

        bundle.putParcelable("CROP_WINDOW_RECT", BitmapUtils.RECT)
        bundle.putString("CROP_SHAPE", mCropOverlayView.cropShape!!.name)
        bundle.putBoolean("CROP_AUTO_ZOOM_ENABLED", mAutoZoomEnabled)
        bundle.putInt("CROP_MAX_ZOOM", mMaxZoom)
        bundle.putBoolean("CROP_FLIP_HORIZONTALLY", mFlipHorizontally)
        bundle.putBoolean("CROP_FLIP_VERTICALLY", mFlipVertically)

        return bundle
    }

    public override fun onRestoreInstanceState(state: Parcelable) {

        if (state is Bundle) {
            val bundle = state as Bundle

            // prevent restoring state if already set by outside code
            if (mBitmapLoadingWorkerTask == null
                    && imageUri == null
                    && originalBitmap == null
                    && mImageResource == 0) {

                var uri = bundle.getParcelable<Uri>("LOADED_IMAGE_URI")
                if (uri != null) {
                    val key = bundle.getString("LOADED_IMAGE_STATE_BITMAP_KEY")
                    if (key != null) {
                        val stateBitmap = if (BitmapUtils.mStateBitmap != null && BitmapUtils.mStateBitmap!!.first == key)
                            BitmapUtils.mStateBitmap!!.second.get()
                        else
                            null
                        BitmapUtils.mStateBitmap = null
                        if (stateBitmap != null && !stateBitmap.isRecycled) {
                            setBitmap(stateBitmap, 0, uri, bundle.getInt("LOADED_SAMPLE_SIZE"), 0)
                        }
                    }
                    if (imageUri == null) {
                        setImageUriAsync(uri)
                    }
                } else {
                    val resId = bundle.getInt("LOADED_IMAGE_RESOURCE")
                    if (resId > 0) {
                        imageResource = resId
                    } else {
                        uri = bundle.getParcelable("LOADING_IMAGE_URI")
                        if (uri != null) {
                            setImageUriAsync(uri)
                        }
                    }
                }

                mRestoreDegreesRotated = bundle.getInt("DEGREES_ROTATED")
                mDegreesRotated = mRestoreDegreesRotated

                val initialCropRect = bundle.getParcelable<Rect>("INITIAL_CROP_RECT")
                if (initialCropRect != null && (initialCropRect.width() > 0 || initialCropRect.height() > 0)) {
                    mCropOverlayView!!.initialCropWindowRect = initialCropRect
                }

                val cropWindowRect = bundle.getParcelable<RectF>("CROP_WINDOW_RECT")
                if (cropWindowRect != null && (cropWindowRect.width() > 0 || cropWindowRect.height() > 0)) {
                    mRestoreCropWindowRect = cropWindowRect
                }

                mCropOverlayView!!.cropShape = CropShape.valueOf(bundle.getString("CROP_SHAPE")!!)

                mAutoZoomEnabled = bundle.getBoolean("CROP_AUTO_ZOOM_ENABLED")
                mMaxZoom = bundle.getInt("CROP_MAX_ZOOM")

                mFlipHorizontally = bundle.getBoolean("CROP_FLIP_HORIZONTALLY")
                mFlipVertically = bundle.getBoolean("CROP_FLIP_VERTICALLY")
            }

            super.onRestoreInstanceState(bundle.getParcelable("instanceState"))
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        var heightSize = View.MeasureSpec.getSize(heightMeasureSpec)

        if (originalBitmap != null) {

            // Bypasses a baffling bug when used within a ScrollView, where heightSize is set to 0.
            if (heightSize == 0) {
                heightSize = originalBitmap!!.height
            }

            val desiredWidth: Int
            val desiredHeight: Int

            var viewToBitmapWidthRatio = java.lang.Double.POSITIVE_INFINITY
            var viewToBitmapHeightRatio = java.lang.Double.POSITIVE_INFINITY

            // Checks if either width or height needs to be fixed
            if (widthSize < originalBitmap!!.width) {
                viewToBitmapWidthRatio = widthSize.toDouble() / originalBitmap!!.width.toDouble()
            }
            if (heightSize < originalBitmap!!.height) {
                viewToBitmapHeightRatio = heightSize.toDouble() / originalBitmap!!.height.toDouble()
            }

            // If either needs to be fixed, choose smallest ratio and calculate from there
            if (viewToBitmapWidthRatio != java.lang.Double.POSITIVE_INFINITY || viewToBitmapHeightRatio != java.lang.Double.POSITIVE_INFINITY) {
                if (viewToBitmapWidthRatio <= viewToBitmapHeightRatio) {
                    desiredWidth = widthSize
                    desiredHeight = (originalBitmap!!.height * viewToBitmapWidthRatio).toInt()
                } else {
                    desiredHeight = heightSize
                    desiredWidth = (originalBitmap!!.width * viewToBitmapHeightRatio).toInt()
                }
            } else {
                // Otherwise, the picture is within frame layout bounds. Desired width is simply picture
                // size
                desiredWidth = originalBitmap!!.width
                desiredHeight = originalBitmap!!.height
            }

            val width = getOnMeasureSpec(widthMode, widthSize, desiredWidth)
            val height = getOnMeasureSpec(heightMode, heightSize, desiredHeight)

            mLayoutWidth = width
            mLayoutHeight = height

            setMeasuredDimension(mLayoutWidth, mLayoutHeight)

        } else {
            setMeasuredDimension(widthSize, heightSize)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {

        super.onLayout(changed, l, t, r, b)

        if (mLayoutWidth > 0 && mLayoutHeight > 0) {
            // Gets original parameters, and creates the new parameters
            val origParams = this.layoutParams
            origParams.width = mLayoutWidth
            origParams.height = mLayoutHeight
            layoutParams = origParams

            if (originalBitmap != null) {
                applyImageMatrix((r - l).toFloat(), (b - t).toFloat(), true, false)

                // after state restore we want to restore the window crop, possible only after widget size
                // is known
                if (mRestoreCropWindowRect != null) {
                    if (mRestoreDegreesRotated != mInitialDegreesRotated) {
                        mDegreesRotated = mRestoreDegreesRotated
                        applyImageMatrix((r - l).toFloat(), (b - t).toFloat(), true, false)
                    }
                    mImageMatrix.mapRect(mRestoreCropWindowRect)
                    mCropOverlayView!!.cropWindowRect = mRestoreCropWindowRect as RectF
                    handleCropWindowChanged(false, false)
                    mCropOverlayView.fixCurrentCropWindowRect()
                    mRestoreCropWindowRect = null
                } else if (mSizeChanged) {
                    mSizeChanged = false
                    handleCropWindowChanged(false, false)
                }
            } else {
                updateImageBounds(true)
            }
        } else {
            updateImageBounds(true)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mSizeChanged = oldw > 0 && oldh > 0
    }

    private fun handleCropWindowChanged(inProgress: Boolean, animate: Boolean) {
        val width = width
        val height = height
        if (originalBitmap != null && width > 0 && height > 0) {

            val cropRect = mCropOverlayView!!.cropWindowRect
            if (inProgress) {
                if (cropRect.left < 0
                        || cropRect.top < 0
                        || cropRect.right > width
                        || cropRect.bottom > height) {
                    applyImageMatrix(width.toFloat(), height.toFloat(), false, false)
                }
            } else if (mAutoZoomEnabled || mZoom > 1) {
                var newZoom = 0f
                // keep the cropping window covered area to 50%-65% of zoomed sub-area
                if (mZoom < mMaxZoom
                        && cropRect.width() < width * 0.5f
                        && cropRect.height() < height * 0.5f) {
                    newZoom = Math.min(
                            mMaxZoom.toFloat(),
                            Math.min(
                                    width / (cropRect.width() / mZoom / 0.64f),
                                    height / (cropRect.height() / mZoom / 0.64f)))
                }
                if (mZoom > 1 && (cropRect.width() > width * 0.65f || cropRect.height() > height * 0.65f)) {
                    newZoom = Math.max(
                            1f,
                            Math.min(
                                    width / (cropRect.width() / mZoom / 0.51f),
                                    height / (cropRect.height() / mZoom / 0.51f)))
                }
                if (!mAutoZoomEnabled) {
                    newZoom = 1f
                }

                if (newZoom > 0 && newZoom != mZoom) {
                    if (animate) {
                        if (mAnimation == null) {
                            // lazy create animation single instance
                            mAnimation = CropImageAnimation(mImageView, mCropOverlayView)
                        }
                        // set the state for animation to start from
                        mAnimation!!.setStartState(mImagePoints, mImageMatrix)
                    }

                    mZoom = newZoom

                    applyImageMatrix(width.toFloat(), height.toFloat(), true, animate)
                }
            }
            if (mOnSetCropWindowChangeListener != null && !inProgress) {
                mOnSetCropWindowChangeListener!!.onCropWindowChanged()
            }
        }
    }

    private fun applyImageMatrix(width: Float, height: Float, center: Boolean, animate: Boolean) {
        if (originalBitmap != null && width > 0 && height > 0) {

            mImageMatrix.invert(mImageInverseMatrix)
            val cropRect = mCropOverlayView!!.cropWindowRect
            mImageInverseMatrix.mapRect(cropRect)

            mImageMatrix.reset()

            // move the image to the center of the image view first so we can manipulate it from there
            mImageMatrix.postTranslate(
                    (width - originalBitmap!!.width) / 2, (height - originalBitmap!!.height) / 2)
            mapImagePointsByImageMatrix()

            // rotate the image the required degrees from center of image
            if (mDegreesRotated > 0) {
                mImageMatrix.postRotate(
                        mDegreesRotated.toFloat(),
                        BitmapUtils.getRectCenterX(mImagePoints),
                        BitmapUtils.getRectCenterY(mImagePoints))
                mapImagePointsByImageMatrix()
            }

            // scale the image to the image view, image rect transformed to know new width/height
            val scale = Math.min(
                    width / BitmapUtils.getRectWidth(mImagePoints),
                    height / BitmapUtils.getRectHeight(mImagePoints))
            if (mScaleType == ScaleType.FIT_CENTER
                    || mScaleType == ScaleType.CENTER_INSIDE && scale < 1
                    || scale > 1 && mAutoZoomEnabled) {
                mImageMatrix.postScale(
                        scale,
                        scale,
                        BitmapUtils.getRectCenterX(mImagePoints),
                        BitmapUtils.getRectCenterY(mImagePoints))
                mapImagePointsByImageMatrix()
            }

            // scale by the current zoom level
            val scaleX = if (mFlipHorizontally) -mZoom else mZoom
            val scaleY = if (mFlipVertically) -mZoom else mZoom
            mImageMatrix.postScale(
                    scaleX,
                    scaleY,
                    BitmapUtils.getRectCenterX(mImagePoints),
                    BitmapUtils.getRectCenterY(mImagePoints))
            mapImagePointsByImageMatrix()

            mImageMatrix.mapRect(cropRect)

            if (center) {
                // set the zoomed area to be as to the center of cropping window as possible
                mZoomOffsetX = if (width > BitmapUtils.getRectWidth(mImagePoints))
                    0f
                else
                    Math.max(
                            Math.min(
                                    width / 2 - cropRect.centerX(), -BitmapUtils.getRectLeft(mImagePoints)),
                            getWidth() - BitmapUtils.getRectRight(mImagePoints)) / scaleX
                mZoomOffsetY = if (height > BitmapUtils.getRectHeight(mImagePoints))
                    0f
                else
                    (Math.max(
                            Math.min(
                                    height / 2 - cropRect.centerY(), -BitmapUtils.getRectTop(mImagePoints)),
                            getHeight() - BitmapUtils.getRectBottom(mImagePoints)) / scaleY)
            } else {
                // adjust the zoomed area so the crop window rectangle will be inside the area in case it
                // was moved outside
                mZoomOffsetX = (Math.min(Math.max(mZoomOffsetX * scaleX, -cropRect.left), -cropRect.right + width) / scaleX)
                mZoomOffsetY = (Math.min(Math.max(mZoomOffsetY * scaleY, -cropRect.top), -cropRect.bottom + height) / scaleY)
            }

            // apply to zoom offset translate and update the crop rectangle to offset correctly
            mImageMatrix.postTranslate(mZoomOffsetX * scaleX, mZoomOffsetY * scaleY)
            cropRect.offset(mZoomOffsetX * scaleX, mZoomOffsetY * scaleY)
            mCropOverlayView.cropWindowRect = cropRect
            mapImagePointsByImageMatrix()
            mCropOverlayView.invalidate()

            // set matrix to apply
            if (animate) {
                // set the state for animation to end in, start animation now
                mAnimation!!.setEndState(mImagePoints, mImageMatrix)
                mImageView.startAnimation(mAnimation)
            } else {
                mImageView.imageMatrix = mImageMatrix
            }

            // update the image rectangle in the crop overlay
            updateImageBounds(false)
        }
    }

    private fun mapImagePointsByImageMatrix() {
        mImagePoints[0] = 0f
        mImagePoints[1] = 0f
        mImagePoints[2] = originalBitmap!!.width.toFloat()
        mImagePoints[3] = 0f
        mImagePoints[4] = originalBitmap!!.width.toFloat()
        mImagePoints[5] = originalBitmap!!.height.toFloat()
        mImagePoints[6] = 0f
        mImagePoints[7] = originalBitmap!!.height.toFloat()
        mImageMatrix.mapPoints(mImagePoints)
        mScaleImagePoints[0] = 0f
        mScaleImagePoints[1] = 0f
        mScaleImagePoints[2] = 100f
        mScaleImagePoints[3] = 0f
        mScaleImagePoints[4] = 100f
        mScaleImagePoints[5] = 100f
        mScaleImagePoints[6] = 0f
        mScaleImagePoints[7] = 100f
        mImageMatrix.mapPoints(mScaleImagePoints)
    }

    private fun getOnMeasureSpec(measureSpecMode: Int, measureSpecSize: Int, desiredSize: Int): Int {

        // Measure Width
        val spec: Int
        if (measureSpecMode == View.MeasureSpec.EXACTLY) {
            // Must be this size
            spec = measureSpecSize
        } else if (measureSpecMode == View.MeasureSpec.AT_MOST) {
            // Can't be bigger than...; match_parent value
            spec = Math.min(desiredSize, measureSpecSize)
        } else {
            // Be whatever you want; wrap_content
            spec = desiredSize
        }

        return spec
    }

    private fun setCropOverlayVisibility() {
        if (mCropOverlayView != null) {
            mCropOverlayView.visibility = if (mShowCropOverlay && originalBitmap != null) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun setProgressBarVisibility() {
        val visible = (mShowProgressBar && ((originalBitmap == null && mBitmapLoadingWorkerTask != null || mBitmapCroppingWorkerTask != null)))
        mProgressBar.visibility = if (visible) View.VISIBLE else View.INVISIBLE
    }

    private fun updateImageBounds(clear: Boolean) {
        if (originalBitmap != null && !clear) {

            // Get the scale factor between the actual Bitmap dimensions and the displayed dimensions for
            // width/height.
            val scaleFactorWidth = 100f * mLoadedSampleSize / BitmapUtils.getRectWidth(mScaleImagePoints)
            val scaleFactorHeight = 100f * mLoadedSampleSize / BitmapUtils.getRectHeight(mScaleImagePoints)
            mCropOverlayView!!.setCropWindowLimits(
                    width.toFloat(), height.toFloat(), scaleFactorWidth, scaleFactorHeight)
        }

        // set the bitmap rectangle and update the crop window after scale factor is set
        mCropOverlayView!!.setBounds(if (clear) null else mImagePoints, width, height)
    }


    enum class CropShape {
        RECTANGLE, OVAL
    }

    enum class ScaleType {
        FIT_CENTER, CENTER, CENTER_CROP, CENTER_INSIDE
    }

    enum class Guidelines {
        OFF, ON_TOUCH, ON
    }

    enum class RequestSizeOptions {
        NONE, SAMPLING, RESIZE_INSIDE, RESIZE_FIT, RESIZE_EXACT
    }

    interface OnSetCropOverlayReleasedListener {
        fun onCropOverlayReleased(rect: Rect?)
    }

    interface OnSetCropOverlayMovedListener {

        fun onCropOverlayMoved(rect: Rect?)
    }

    interface OnSetCropWindowChangeListener {

        fun onCropWindowChanged()
    }

    interface OnSetImageUriCompleteListener {

        fun onSetImageUriComplete(view: ViewCropImage, uri: Uri, error: Exception?)
    }

    interface OnCropImageCompleteListener {

        fun onCropImageComplete(view: ViewCropImage, result: CropResult)
    }

    class CropResult internal constructor(
            val originalBitmap: Bitmap?,
            val originalUri: Uri?,
            val bitmap: Bitmap?,
            val uri: Uri?,
            val error: Exception?,
            val cropPoints: FloatArray?,
            val cropRect: Rect?,
            val wholeImageRect: Rect?,
            val rotation: Int?,
            val sampleSize: Int?) {

        val isSuccessful: Boolean
            get() = error == null
    }

}
