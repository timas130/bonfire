package com.sup.dev.android.views.views.cropper

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import java.lang.ref.WeakReference


internal class BitmapCroppingWorkerTask : AsyncTask<Void, Void, BitmapCroppingWorkerTask.Result> {

    private val mCropImageViewReference: WeakReference<ViewCropImage>
    private val mBitmap: Bitmap?
    val uri: Uri?
    private val mContext: Context
    private val mCropPoints: FloatArray
    private val mDegreesRotated: Int
    private val mOrgWidth: Int
    private val mOrgHeight: Int
    private val mFixAspectRatio: Boolean
    private val mAspectRatioX: Int
    private val mAspectRatioY: Int
    private val mReqWidth: Int
    private val mReqHeight: Int
    private val mFlipHorizontally: Boolean
    private val mFlipVertically: Boolean
    private val mReqSizeOptions: ViewCropImage.RequestSizeOptions
    private val mSaveUri: Uri?
    private val mSaveCompressFormat: Bitmap.CompressFormat
    private val mSaveCompressQuality: Int

    constructor(
            cropImageView: ViewCropImage,
            bitmap: Bitmap,
            cropPoints: FloatArray,
            degreesRotated: Int,
            fixAspectRatio: Boolean,
            aspectRatioX: Int,
            aspectRatioY: Int,
            reqWidth: Int,
            reqHeight: Int,
            flipHorizontally: Boolean,
            flipVertically: Boolean,
            options: ViewCropImage.RequestSizeOptions,
            saveUri: Uri,
            saveCompressFormat: Bitmap.CompressFormat,
            saveCompressQuality: Int) {

        mCropImageViewReference = WeakReference(cropImageView)
        mContext = cropImageView.context
        mBitmap = bitmap
        mCropPoints = cropPoints
        uri = null
        mDegreesRotated = degreesRotated
        mFixAspectRatio = fixAspectRatio
        mAspectRatioX = aspectRatioX
        mAspectRatioY = aspectRatioY
        mReqWidth = reqWidth
        mReqHeight = reqHeight
        mFlipHorizontally = flipHorizontally
        mFlipVertically = flipVertically
        mReqSizeOptions = options
        mSaveUri = saveUri
        mSaveCompressFormat = saveCompressFormat
        mSaveCompressQuality = saveCompressQuality
        mOrgWidth = 0
        mOrgHeight = 0
    }

    constructor(
            cropImageView: ViewCropImage,
            uri: Uri,
            cropPoints: FloatArray,
            degreesRotated: Int,
            orgWidth: Int,
            orgHeight: Int,
            fixAspectRatio: Boolean,
            aspectRatioX: Int,
            aspectRatioY: Int,
            reqWidth: Int,
            reqHeight: Int,
            flipHorizontally: Boolean,
            flipVertically: Boolean,
            options: ViewCropImage.RequestSizeOptions,
            saveUri: Uri,
            saveCompressFormat: Bitmap.CompressFormat,
            saveCompressQuality: Int) {

        mCropImageViewReference = WeakReference(cropImageView)
        mContext = cropImageView.context
        this.uri = uri
        mCropPoints = cropPoints
        mDegreesRotated = degreesRotated
        mFixAspectRatio = fixAspectRatio
        mAspectRatioX = aspectRatioX
        mAspectRatioY = aspectRatioY
        mOrgWidth = orgWidth
        mOrgHeight = orgHeight
        mReqWidth = reqWidth
        mReqHeight = reqHeight
        mFlipHorizontally = flipHorizontally
        mFlipVertically = flipVertically
        mReqSizeOptions = options
        mSaveUri = saveUri
        mSaveCompressFormat = saveCompressFormat
        mSaveCompressQuality = saveCompressQuality
        mBitmap = null
    }

    override fun doInBackground(vararg params: Void): BitmapCroppingWorkerTask.Result? {
        try {
            if (!isCancelled) {

                val bitmapSampled: BitmapUtils.BitmapSampled
                if (uri != null) {
                    bitmapSampled = BitmapUtils.cropBitmap(
                            mContext,
                            uri,
                            mCropPoints,
                            mDegreesRotated,
                            mOrgWidth,
                            mOrgHeight,
                            mFixAspectRatio,
                            mAspectRatioX,
                            mAspectRatioY,
                            mReqWidth,
                            mReqHeight,
                            mFlipHorizontally,
                            mFlipVertically)
                } else if (mBitmap != null) {
                    bitmapSampled = BitmapUtils.cropBitmapObjectHandleOOM(
                            mBitmap,
                            mCropPoints,
                            mDegreesRotated,
                            mFixAspectRatio,
                            mAspectRatioX,
                            mAspectRatioY,
                            mFlipHorizontally,
                            mFlipVertically)
                } else {
                    return Result(null as Bitmap?, 1)
                }

                val bitmap = BitmapUtils.resizeBitmap(bitmapSampled.bitmap!!, mReqWidth, mReqHeight, mReqSizeOptions)

                if (mSaveUri == null) {
                    return Result(bitmap, bitmapSampled.sampleSize)
                } else {
                    BitmapUtils.writeBitmapToUri(
                            mContext, bitmap, mSaveUri, mSaveCompressFormat, mSaveCompressQuality)
                    bitmap.recycle()
                    return Result(mSaveUri, bitmapSampled.sampleSize)
                }
            }
            return null
        } catch (e: Exception) {
            return Result(e, mSaveUri != null)
        }

    }

    override fun onPostExecute(result: Result?) {
        if (result != null) {
            var completeCalled = false
            if (!isCancelled) {
                val cropImageView = mCropImageViewReference.get()
                if (cropImageView != null) {
                    completeCalled = true
                    cropImageView.onImageCroppingAsyncComplete(result)
                }
            }
            if (!completeCalled && result.bitmap != null)
                result.bitmap.recycle()
        }
    }

    internal class Result {

        val bitmap: Bitmap?
        val uri: Uri?
        val error: Exception?
        val isSave: Boolean
        val sampleSize: Int

        constructor(bitmap: Bitmap?, sampleSize: Int) {
            this.bitmap = bitmap
            this.uri = null
            this.error = null
            this.isSave = false
            this.sampleSize = sampleSize
        }

        constructor(uri: Uri, sampleSize: Int) {
            this.bitmap = null
            this.uri = uri
            this.error = null
            this.isSave = true
            this.sampleSize = sampleSize
        }

        constructor(error: Exception, isSave: Boolean) {
            this.bitmap = null
            this.uri = null
            this.error = error
            this.isSave = isSave
            this.sampleSize = 1
        }
    }
}
