package com.sup.dev.android.views.views.cropper

import android.graphics.Matrix
import android.graphics.RectF
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.ImageView


internal class CropImageAnimation(private val mImageView: ImageView, private val mCropOverlayView: CropOverlayView) : Animation(), Animation.AnimationListener {

    private val mStartBoundPoints = FloatArray(8)

    private val mEndBoundPoints = FloatArray(8)

    private val mStartCropWindowRect = RectF()

    private val mEndCropWindowRect = RectF()

    private val mStartImageMatrix = FloatArray(9)

    private val mEndImageMatrix = FloatArray(9)

    private val mAnimRect = RectF()

    private val mAnimPoints = FloatArray(8)

    private val mAnimMatrix = FloatArray(9)

    init {

        duration = 300
        fillAfter = true
        interpolator = AccelerateDecelerateInterpolator()
        setAnimationListener(this)
    }

    fun setStartState(boundPoints: FloatArray, imageMatrix: Matrix) {
        reset()
        System.arraycopy(boundPoints, 0, mStartBoundPoints, 0, 8)
        mStartCropWindowRect.set(mCropOverlayView.cropWindowRect)
        imageMatrix.getValues(mStartImageMatrix)
    }

    fun setEndState(boundPoints: FloatArray, imageMatrix: Matrix) {
        System.arraycopy(boundPoints, 0, mEndBoundPoints, 0, 8)
        mEndCropWindowRect.set(mCropOverlayView.cropWindowRect)
        imageMatrix.getValues(mEndImageMatrix)
    }

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {

        mAnimRect.left = mStartCropWindowRect.left + (mEndCropWindowRect.left - mStartCropWindowRect.left) * interpolatedTime
        mAnimRect.top = mStartCropWindowRect.top + (mEndCropWindowRect.top - mStartCropWindowRect.top) * interpolatedTime
        mAnimRect.right = mStartCropWindowRect.right + (mEndCropWindowRect.right - mStartCropWindowRect.right) * interpolatedTime
        mAnimRect.bottom = mStartCropWindowRect.bottom + (mEndCropWindowRect.bottom - mStartCropWindowRect.bottom) * interpolatedTime
        mCropOverlayView.cropWindowRect = mAnimRect

        for (i in mAnimPoints.indices) {
            mAnimPoints[i] = mStartBoundPoints[i] + (mEndBoundPoints[i] - mStartBoundPoints[i]) * interpolatedTime
        }
        mCropOverlayView.setBounds(mAnimPoints, mImageView.width, mImageView.height)

        for (i in mAnimMatrix.indices) {
            mAnimMatrix[i] = mStartImageMatrix[i] + (mEndImageMatrix[i] - mStartImageMatrix[i]) * interpolatedTime
        }
        val m = mImageView.imageMatrix
        m.setValues(mAnimMatrix)
        mImageView.imageMatrix = m

        mImageView.invalidate()
        mCropOverlayView.invalidate()
    }

    override fun onAnimationStart(animation: Animation) {}

    override fun onAnimationEnd(animation: Animation) {
        mImageView.clearAnimation()
    }

    override fun onAnimationRepeat(animation: Animation) {}
}
