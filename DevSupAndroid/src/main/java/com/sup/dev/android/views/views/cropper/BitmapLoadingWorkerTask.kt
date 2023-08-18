package com.sup.dev.android.views.views.cropper

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import java.lang.ref.WeakReference


internal class BitmapLoadingWorkerTask(cropImageView: ViewCropImage, val uri: Uri) : AsyncTask<Void, Void, BitmapLoadingWorkerTask.Result>() {

    private val mCropImageViewReference: WeakReference<ViewCropImage> = WeakReference(cropImageView)
    private val mContext: Context= cropImageView.context
    private val mWidth: Int
    private val mHeight: Int

    init {

        val metrics = cropImageView.resources.displayMetrics
        val densityAdj:Double = if (metrics.density > 1)  1.0 else 1.0 / metrics.density;
        mWidth = (metrics.widthPixels * densityAdj).toInt()
        mHeight = (metrics.heightPixels * densityAdj).toInt()
    }

    override fun doInBackground(vararg params: Void): Result? {
        try {
            if (!isCancelled) {

                val decodeResult = BitmapUtils.decodeSampledBitmap(mContext, uri, mWidth, mHeight)

                if (!isCancelled) {

                    val rotateResult = BitmapUtils.rotateBitmapByExif(decodeResult.bitmap!!, mContext, uri)

                    return Result(
                            uri, rotateResult.bitmap, decodeResult.sampleSize, rotateResult.degrees)
                }
            }
            return null
        } catch (e: Exception) {
            return Result(uri, e)
        }

    }

    override fun onPostExecute(result: Result?) {
        if (result != null) {
            var completeCalled = false
            if (!isCancelled) {
                val cropImageView = mCropImageViewReference.get()
                if (cropImageView != null) {
                    completeCalled = true
                    cropImageView.onSetImageUriAsyncComplete(result)
                }
            }
            if (!completeCalled && result.bitmap != null)
                result.bitmap.recycle()
        }
    }

    class Result {

        val uri: Uri
        val bitmap: Bitmap?
        val loadSampleSize: Int
        val degreesRotated: Int
        val error: Exception?

        internal constructor(uri: Uri, bitmap: Bitmap, loadSampleSize: Int, degreesRotated: Int) {
            this.uri = uri
            this.bitmap = bitmap
            this.loadSampleSize = loadSampleSize
            this.degreesRotated = degreesRotated
            this.error = null
        }

        internal constructor(uri: Uri, error: Exception) {
            this.uri = uri
            this.bitmap = null
            this.loadSampleSize = 0
            this.degreesRotated = 0
            this.error = error
        }
    }
}
