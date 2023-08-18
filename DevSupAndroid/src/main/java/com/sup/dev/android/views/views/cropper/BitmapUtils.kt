package com.sup.dev.android.views.views.cropper

import android.content.ContentResolver
import android.content.Context
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.util.Log
import android.util.Pair
import androidx.annotation.RequiresApi
import com.sup.dev.java.libs.debug.err
import java.io.*
import java.lang.NullPointerException
import java.lang.ref.WeakReference
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext


internal object BitmapUtils {

    val EMPTY_RECT = Rect()
    val EMPTY_RECT_F = RectF()
    val RECT = RectF()
    val POINTS = FloatArray(6)
    val POINTS2 = FloatArray(6)
    private var mMaxTextureSize: Int = 0
    var mStateBitmap: Pair<String, WeakReference<Bitmap>>? = null

    private val maxTextureSize: Int
        get() {
            val IMAGE_MAX_BITMAP_DIMENSION = 2048

            try {
                val egl = EGLContext.getEGL() as EGL10
                val display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)

                val version = IntArray(2)
                egl.eglInitialize(display, version)

                val totalConfigurations = IntArray(1)
                egl.eglGetConfigs(display, null, 0, totalConfigurations)

                val configurationsList = arrayOfNulls<EGLConfig>(totalConfigurations[0])
                egl.eglGetConfigs(display, configurationsList, totalConfigurations[0], totalConfigurations)

                val textureSize = IntArray(1)
                var maximumTextureSize = 0

                for (i in 0 until totalConfigurations[0]) {
                    egl.eglGetConfigAttrib(display, configurationsList[i], EGL10.EGL_MAX_PBUFFER_WIDTH, textureSize)

                    if (maximumTextureSize < textureSize[0]) {
                        maximumTextureSize = textureSize[0]
                    }
                }

                egl.eglTerminate(display)

                return Math.max(maximumTextureSize, IMAGE_MAX_BITMAP_DIMENSION)
            } catch (e: Exception) {
                return IMAGE_MAX_BITMAP_DIMENSION
            }

        }

    @RequiresApi(Build.VERSION_CODES.N)
    fun rotateBitmapByExif(bitmap: Bitmap, context: Context, uri: Uri): RotateBitmapResult {
        var ei: ExifInterface? = null
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                ei = ExifInterface(inputStream)
                inputStream.close()
            }
        } catch (ignored: Exception) {
        }

        return if (ei != null) rotateBitmapByExif(bitmap, ei) else RotateBitmapResult(bitmap, 0)
    }

    fun rotateBitmapByExif(bitmap: Bitmap, exif: ExifInterface): RotateBitmapResult {
        val degrees: Int = when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
        return RotateBitmapResult(bitmap, degrees)
    }

    fun decodeSampledBitmap(context: Context, uri: Uri, reqWidth: Int, reqHeight: Int): BitmapSampled {

        try {
            val resolver = context.contentResolver
            val options = decodeImageForOption(resolver, uri)
            options.inSampleSize = Math.max(
                    calculateInSampleSizeByReqestedSize(
                            options.outWidth, options.outHeight, reqWidth, reqHeight),
                    calculateInSampleSizeByMaxTextureSize(options.outWidth, options.outHeight))

            val bitmap = decodeImage(resolver, uri, options)

            return BitmapSampled(bitmap, options.inSampleSize)

        } catch (e: Exception) {
            throw RuntimeException(
                    "Failed to load sampled bitmap: " + uri + "\r\n" + e.message, e)
        }

    }

    fun cropBitmapObjectHandleOOM(
            bitmap: Bitmap,
            points: FloatArray,
            degreesRotated: Int,
            fixAspectRatio: Boolean,
            aspectRatioX: Int,
            aspectRatioY: Int,
            flipHorizontally: Boolean,
            flipVertically: Boolean): BitmapSampled {
        var scale = 1
        while (true) {
            try {
                val cropBitmap = cropBitmapObjectWithScale(
                        bitmap,
                        points,
                        degreesRotated,
                        fixAspectRatio,
                        aspectRatioX,
                        aspectRatioY,
                        1 / scale.toFloat(),
                        flipHorizontally,
                        flipVertically)
                return BitmapSampled(cropBitmap, scale)
            } catch (e: OutOfMemoryError) {
                scale *= 2
                if (scale > 8) {
                    throw e
                }
            }

        }
    }

    private fun cropBitmapObjectWithScale(
            bitmap: Bitmap,
            points: FloatArray,
            degreesRotated: Int,
            fixAspectRatio: Boolean,
            aspectRatioX: Int,
            aspectRatioY: Int,
            scale: Float,
            flipHorizontally: Boolean,
            flipVertically: Boolean): Bitmap? {

        try {
            val rect = getRectFromPoints(
                    points,
                    bitmap.width,
                    bitmap.height,
                    fixAspectRatio,
                    aspectRatioX,
                    aspectRatioY)

            val matrix = Matrix()
            matrix.setRotate(degreesRotated.toFloat(), (bitmap.width / 2).toFloat(), (bitmap.height / 2).toFloat())
            matrix.postScale(if (flipHorizontally) -scale else scale, if (flipVertically) -scale else scale)
            var result = Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height(), matrix, true)

            if (result == bitmap)
                result = bitmap.copy(bitmap.config, false)

            if (degreesRotated % 90 != 0)
                result = cropForRotatedImage(result, points, rect, degreesRotated, fixAspectRatio, aspectRatioX, aspectRatioY)

            return result
        }catch (e:NullPointerException){
            err(e)
            return null
        }


    }

    fun cropBitmap(
            context: Context,
            loadedImageUri: Uri,
            points: FloatArray,
            degreesRotated: Int,
            orgWidth: Int,
            orgHeight: Int,
            fixAspectRatio: Boolean,
            aspectRatioX: Int,
            aspectRatioY: Int,
            reqWidth: Int,
            reqHeight: Int,
            flipHorizontally: Boolean,
            flipVertically: Boolean): BitmapSampled {
        var sampleMulti = 1
        while (true) {
            try {
                return cropBitmap(
                        context,
                        loadedImageUri,
                        points,
                        degreesRotated,
                        orgWidth,
                        orgHeight,
                        fixAspectRatio,
                        aspectRatioX,
                        aspectRatioY,
                        reqWidth,
                        reqHeight,
                        flipHorizontally,
                        flipVertically,
                        sampleMulti)
            } catch (e: OutOfMemoryError) {
                sampleMulti *= 2
                if (sampleMulti > 16) {
                    throw RuntimeException(
                            "Failed to handle OOM by sampling ("
                                    + sampleMulti
                                    + "): "
                                    + loadedImageUri
                                    + "\r\n"
                                    + e.message,
                            e)
                }
            }

        }
    }

    fun getRectLeft(points: FloatArray): Float {
        return Math.min(Math.min(Math.min(points[0], points[2]), points[4]), points[6])
    }

    fun getRectTop(points: FloatArray): Float {
        return Math.min(Math.min(Math.min(points[1], points[3]), points[5]), points[7])
    }

    fun getRectRight(points: FloatArray): Float {
        return Math.max(Math.max(Math.max(points[0], points[2]), points[4]), points[6])
    }

    fun getRectBottom(points: FloatArray): Float {
        return Math.max(Math.max(Math.max(points[1], points[3]), points[5]), points[7])
    }

    fun getRectWidth(points: FloatArray): Float {
        return getRectRight(points) - getRectLeft(points)
    }

    fun getRectHeight(points: FloatArray): Float {
        return getRectBottom(points) - getRectTop(points)
    }

    fun getRectCenterX(points: FloatArray): Float {
        return (getRectRight(points) + getRectLeft(points)) / 2f
    }

    fun getRectCenterY(points: FloatArray): Float {
        return (getRectBottom(points) + getRectTop(points)) / 2f
    }

    fun getRectFromPoints(
            points: FloatArray,
            imageWidth: Int,
            imageHeight: Int,
            fixAspectRatio: Boolean,
            aspectRatioX: Int,
            aspectRatioY: Int): Rect {
        val left = Math.round(Math.max(0f, getRectLeft(points)))
        val top = Math.round(Math.max(0f, getRectTop(points)))
        val right = Math.round(Math.min(imageWidth.toFloat(), getRectRight(points)))
        val bottom = Math.round(Math.min(imageHeight.toFloat(), getRectBottom(points)))

        val rect = Rect(left, top, right, bottom)
        if (fixAspectRatio) {
            fixRectForAspectRatio(rect, aspectRatioX, aspectRatioY)
        }

        return rect
    }

    private fun fixRectForAspectRatio(rect: Rect, aspectRatioX: Int, aspectRatioY: Int) {
        if (aspectRatioX == aspectRatioY && rect.width() != rect.height()) {
            if (rect.height() > rect.width()) {
                rect.bottom -= rect.height() - rect.width()
            } else {
                rect.right -= rect.width() - rect.height()
            }
        }
    }

    fun writeTempStateStoreBitmap(context: Context, bitmap: Bitmap, uri: Uri?): Uri? {
        var uriV = uri
        try {
            var needSave = true
            if (uriV == null) {
                uriV = Uri.fromFile(
                        File.createTempFile("aic_state_store_temp", ".jpg", context.cacheDir))
            } else if (File(uriV.path).exists()) {
                needSave = false
            }
            if (needSave) {
                writeBitmapToUri(context, bitmap, uriV, Bitmap.CompressFormat.JPEG, 95)
            }
            return uriV
        } catch (e: Exception) {
            Log.w("AIC", "Failed to write bitmap to temp file for image-cropper save instance state", e)
            return null
        }

    }

    @Throws(FileNotFoundException::class)
    fun writeBitmapToUri(
            context: Context,
            bitmap: Bitmap,
            uri: Uri?,
            compressFormat: Bitmap.CompressFormat,
            compressQuality: Int) {
        var outputStream: OutputStream? = null
        try {
            outputStream = context.contentResolver.openOutputStream(uri!!)
            bitmap.compress(compressFormat, compressQuality, outputStream)
        } finally {
            closeSafe(outputStream)
        }
    }

    fun resizeBitmap(
            bitmap: Bitmap, reqWidth: Int, reqHeight: Int, options: ViewCropImage.RequestSizeOptions): Bitmap {
        try {
            if ((reqWidth > 0
                            && reqHeight > 0
                            && ((options == ViewCropImage.RequestSizeOptions.RESIZE_FIT
                            || options == ViewCropImage.RequestSizeOptions.RESIZE_INSIDE
                            || options == ViewCropImage.RequestSizeOptions.RESIZE_EXACT)))) {

                var resized: Bitmap? = null
                if (options == ViewCropImage.RequestSizeOptions.RESIZE_EXACT) {
                    resized = Bitmap.createScaledBitmap(bitmap, reqWidth, reqHeight, false)
                } else {
                    val width = bitmap.width
                    val height = bitmap.height
                    val scale = Math.max(width / reqWidth.toFloat(), height / reqHeight.toFloat())
                    if (scale > 1 || options == ViewCropImage.RequestSizeOptions.RESIZE_FIT) {
                        resized = Bitmap.createScaledBitmap(
                                bitmap, (width / scale).toInt(), (height / scale).toInt(), false)
                    }
                }
                if (resized != null) {
                    if (resized != bitmap) {
                        bitmap.recycle()
                    }
                    return resized
                }
            }
        } catch (e: Exception) {
            Log.w("AIC", "Failed to resize cropped image, return bitmap before resize", e)
        }

        return bitmap
    }

    private fun cropBitmap(
            context: Context,
            loadedImageUri: Uri,
            points: FloatArray,
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
            sampleMulti: Int): BitmapSampled {

        val rect = getRectFromPoints(points, orgWidth, orgHeight, fixAspectRatio, aspectRatioX, aspectRatioY)

        val width = if (reqWidth > 0) reqWidth else rect.width()
        val height = if (reqHeight > 0) reqHeight else rect.height()

        var result: Bitmap? = null
        var sampleSize = 1
        try {
            val bitmapSampled = decodeSampledBitmapRegion(context, loadedImageUri, rect, width, height, sampleMulti)
            result = bitmapSampled.bitmap
            sampleSize = bitmapSampled.sampleSize
        } catch (ignored: Exception) {
        }

        if (result != null) {
            try {
                // rotate the decoded region by the required amount
                result = rotateAndFlipBitmapInt(result, degreesRotated, flipHorizontally, flipVertically)

                // rotating by 0, 90, 180 or 270 degrees doesn't require extra cropping
                if (degreesRotated % 90 != 0) {

                    // extra crop because non rectangular crop cannot be done directly on the image without
                    // rotating first
                    result = cropForRotatedImage(
                            result, points, rect, degreesRotated, fixAspectRatio, aspectRatioX, aspectRatioY)
                }
            } catch (e: OutOfMemoryError) {
                result.recycle()
                throw e
            }

            return BitmapSampled(result, sampleSize)
        } else {
            // failed to decode region, may be skia issue, try full decode and then crop
            return cropBitmap(
                    context,
                    loadedImageUri,
                    points,
                    degreesRotated,
                    fixAspectRatio,
                    aspectRatioX,
                    aspectRatioY,
                    sampleMulti,
                    rect,
                    width,
                    height,
                    flipHorizontally,
                    flipVertically)
        }
    }

    private fun cropBitmap(
            context: Context,
            loadedImageUri: Uri,
            points: FloatArray,
            degreesRotated: Int,
            fixAspectRatio: Boolean,
            aspectRatioX: Int,
            aspectRatioY: Int,
            sampleMulti: Int,
            rect: Rect,
            width: Int,
            height: Int,
            flipHorizontally: Boolean,
            flipVertically: Boolean): BitmapSampled {
        var result: Bitmap? = null
        val sampleSize: Int
        try {
            val options = BitmapFactory.Options()
            sampleSize = (sampleMulti * calculateInSampleSizeByReqestedSize(rect.width(), rect.height(), width, height))
            options.inSampleSize = sampleSize

            val fullBitmap = decodeImage(context.contentResolver, loadedImageUri, options)
            if (fullBitmap != null) {
                try {
                    val points2 = FloatArray(points.size)
                    System.arraycopy(points, 0, points2, 0, points.size)
                    for (i in points2.indices) {
                        points2[i] = points2[i] / options.inSampleSize
                    }

                    result = cropBitmapObjectWithScale(
                            fullBitmap,
                            points2,
                            degreesRotated,
                            fixAspectRatio,
                            aspectRatioX,
                            aspectRatioY,
                            1f,
                            flipHorizontally,
                            flipVertically)
                } finally {
                    if (result != fullBitmap) {
                        fullBitmap.recycle()
                    }
                }
            }
        } catch (e: OutOfMemoryError) {
            if (result != null) {
                result.recycle()
            }
            throw e
        } catch (e: Exception) {
            throw RuntimeException(
                    "Failed to load sampled bitmap: " + loadedImageUri + "\r\n" + e.message, e)
        }

        return BitmapSampled(result, sampleSize)
    }

    @Throws(FileNotFoundException::class)
    private fun decodeImageForOption(resolver: ContentResolver, uri: Uri): BitmapFactory.Options {
        var stream: InputStream? = null
        try {
            stream = resolver.openInputStream(uri)
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeStream(stream, EMPTY_RECT, options)
            options.inJustDecodeBounds = false
            return options
        } finally {
            closeSafe(stream)
        }
    }

    @Throws(FileNotFoundException::class)
    private fun decodeImage(
            resolver: ContentResolver, uri: Uri, options: BitmapFactory.Options): Bitmap? {
        do {
            var stream: InputStream? = null
            try {
                stream = resolver.openInputStream(uri)
                return BitmapFactory.decodeStream(stream, EMPTY_RECT, options)
            } catch (e: OutOfMemoryError) {
                options.inSampleSize *= 2
            } finally {
                closeSafe(stream)
            }
        } while (options.inSampleSize <= 512)
        throw RuntimeException("Failed to decode image: $uri")
    }

    private fun decodeSampledBitmapRegion(
            context: Context, uri: Uri, rect: Rect, reqWidth: Int, reqHeight: Int, sampleMulti: Int): BitmapSampled {
        var stream: InputStream? = null
        var decoder: BitmapRegionDecoder? = null
        try {
            val options = BitmapFactory.Options()
            options.inSampleSize = (sampleMulti * calculateInSampleSizeByReqestedSize(
                    rect.width(), rect.height(), reqWidth, reqHeight))

            stream = context.contentResolver.openInputStream(uri)
            decoder = BitmapRegionDecoder.newInstance(stream!!, false)
            do {
                try {
                    return BitmapSampled(decoder!!.decodeRegion(rect, options), options.inSampleSize)
                } catch (e: OutOfMemoryError) {
                    options.inSampleSize *= 2
                }

            } while (options.inSampleSize <= 512)
        } catch (e: Exception) {
            throw RuntimeException(
                    "Failed to load sampled bitmap: " + uri + "\r\n" + e.message, e)
        } finally {
            closeSafe(stream)
            if (decoder != null) {
                decoder.recycle()
            }
        }
        return BitmapSampled(null, 1)
    }

    private fun cropForRotatedImage(
            bitmap: Bitmap?,
            points: FloatArray,
            rect: Rect,
            degreesRotated: Int,
            fixAspectRatio: Boolean,
            aspectRatioX: Int,
            aspectRatioY: Int): Bitmap? {
        var bitmapV = bitmap
        if (degreesRotated % 90 != 0) {

            var adjLeft = 0
            var adjTop = 0
            var width = 0
            var height = 0
            val rads = Math.toRadians(degreesRotated.toDouble())
            val compareTo = if (degreesRotated < 90 || (degreesRotated > 180 && degreesRotated < 270))
                rect.left
            else
                rect.right
            var i = 0
            while (i < points.size) {
                if (points[i] >= compareTo - 1 && points[i] <= compareTo + 1) {
                    adjLeft = Math.abs(Math.sin(rads) * (rect.bottom - points[i + 1])).toInt()
                    adjTop = Math.abs(Math.cos(rads) * (points[i + 1] - rect.top)).toInt()
                    width = Math.abs((points[i + 1] - rect.top) / Math.sin(rads)).toInt()
                    height = Math.abs((rect.bottom - points[i + 1]) / Math.cos(rads)).toInt()
                    break
                }
                i += 2
            }

            rect.set(adjLeft, adjTop, adjLeft + width, adjTop + height)
            if (fixAspectRatio) {
                fixRectForAspectRatio(rect, aspectRatioX, aspectRatioY)
            }

            val bitmapTmp = bitmapV
            bitmapV = Bitmap.createBitmap(bitmapV!!, rect.left, rect.top, rect.width(), rect.height())
            if (bitmapTmp != bitmapV) {
                bitmapTmp!!.recycle()
            }
        }
        return bitmapV
    }

    private fun calculateInSampleSizeByReqestedSize(
            width: Int, height: Int, reqWidth: Int, reqHeight: Int): Int {
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            while ((height / 2 / inSampleSize) > reqHeight && (width / 2 / inSampleSize) > reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun calculateInSampleSizeByMaxTextureSize(width: Int, height: Int): Int {
        var inSampleSize = 1
        if (mMaxTextureSize == 0) {
            mMaxTextureSize = maxTextureSize
        }
        if (mMaxTextureSize > 0) {
            while (((height / inSampleSize) > mMaxTextureSize || (width / inSampleSize) > mMaxTextureSize)) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun rotateAndFlipBitmapInt(
            bitmap: Bitmap, degrees: Int, flipHorizontally: Boolean, flipVertically: Boolean): Bitmap {
        if (degrees > 0 || flipHorizontally || flipVertically) {
            val matrix = Matrix()
            matrix.setRotate(degrees.toFloat())
            matrix.postScale((if (flipHorizontally) -1 else 1).toFloat(), (if (flipVertically) -1 else 1).toFloat())
            val newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)
            if (newBitmap != bitmap) {
                bitmap.recycle()
            }
            return newBitmap
        } else {
            return bitmap
        }
    }

    private fun closeSafe(closeable: Closeable?) {
        if (closeable != null) {
            try {
                closeable.close()
            } catch (ignored: IOException) {
            }

        }
    }

    internal class BitmapSampled(val bitmap: Bitmap?, val sampleSize: Int)

    internal class RotateBitmapResult(val bitmap: Bitmap, val degrees: Int)

}
