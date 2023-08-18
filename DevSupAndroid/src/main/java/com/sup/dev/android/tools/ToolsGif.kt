package com.sup.dev.android.tools

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.java.tools.ToolsFiles
import com.sup.dev.java.tools.ToolsThreads
import com.waynejo.androidndkgif.GifDecoder
import com.waynejo.androidndkgif.GifEncoder
import java.io.File
import java.lang.ref.WeakReference

object ToolsGif {

    fun resize(bytes: ByteArray, w: Int, h: Int, cropX: Int? = null, cropY: Int? = null, cropW: Int? = null, cropH: Int? = null, weakSizesMode: Boolean = false): ByteArray {
        val fName = "ToolsGif_${System.nanoTime()}_Inp"
        ToolsCash.put(bytes, fName)

        val cacheDir = SupAndroid.appContext!!.cacheDir

        val fInp = File(cacheDir, fName)
        val fOut = File(cacheDir, "ToolsGif_${System.nanoTime()}_Out")

        var ww = w
        var hh = h
        if (weakSizesMode) {
            var bm = ToolsBitmap.decode(bytes)!!
            if (cropX != null && cropY != null && cropW != null && cropH != null)
                bm = Bitmap.createBitmap(bm, cropX, cropY, cropW, cropH)
            bm = ToolsBitmap.keepMaxSizes(bm, ww, hh)
            ww = bm.width
            hh = bm.height
        }
        resize(fInp.absolutePath, fOut.absolutePath, ww, hh, cropX, cropY, cropW, cropH)

        val array = ToolsFiles.readFile(fOut)

        fInp.delete()
        fOut.delete()

        return array
    }

    fun resize(file: String, fileOut: String, w: Int, h: Int, cropX: Int? = null, cropY: Int? = null, cropW: Int? = null, cropH: Int? = null) {
        val gifDecoder = GifDecoder()
        val gifEncoder = GifEncoder()

        gifEncoder.init(w, h, fileOut, GifEncoder.EncodingType.ENCODING_TYPE_NORMAL_LOW_MEMORY)

        val iterator = gifDecoder.loadUsingIterator(file)
        while (iterator.hasNext()) {
            val next = iterator.next()
            var bm = next.bitmap
            if (cropX != null && cropY != null && cropW != null && cropH != null) {
                bm = Bitmap.createBitmap(bm, cropX, cropY, cropW, cropH)
            }
            gifEncoder.encodeFrame(ToolsBitmap.resize(bm, w, h), next.delayMs)
        }
        gifEncoder.close()
        iterator.close()
    }

    fun iterator(bytes: ByteArray, vImage: WeakReference<ImageView>, w: Int = 0, h: Int = 0, resizeByMinSide: Boolean = false, onStart: () -> Unit = {}) {

        val key = Any()
        val vv = vImage.get()
        if (vv == null) return
        vv.tag = key

        ToolsThreads.thread {
            val file = "ToolsGif_${System.nanoTime()}_Inp"
            ToolsCash.put(bytes, file)

            val f = File(SupAndroid.appContext!!.cacheDir, file)

            var lastBitmap: Bitmap? = null
            var stop = false
            val decoder = GifDecoder()
            if (!decoder.load(f.absolutePath)) return@thread

            var index = 0
            while (!stop) {
                var bm = next(decoder, index)
                val ms = decoder.delay(index).toLong()
                if (bm == null) {
                    ToolsThreads.sleep(300)
                    continue
                }
                if (w != 0 && h != 0) bm = ToolsBitmap.inscribePin(bm, w, h, resizeByMinSide)
                ToolsThreads.main {
                    val v = vImage.get()
                    if (v == null || v.tag !== key || (lastBitmap != null && (v.drawable !is BitmapDrawable || (v.drawable as BitmapDrawable).bitmap != lastBitmap))) {
                        stop = true
                        return@main
                    }
                    if (lastBitmap == null) {
                        onStart.invoke()
                    }
                    lastBitmap = bm
                    v.setImageBitmap(lastBitmap)
                }
                index++
                if (index >= decoder.frameNum()) index = 0
                ToolsThreads.sleep(Math.max(ms, 30))
            }

            f.delete()
        }
    }

    private fun next(decoder: GifDecoder, index: Int): Bitmap? {
        try {
            return decoder.frame(index)
        } catch (e: OutOfMemoryError) {
            SupAndroid.onLowMemory()
            try {
                return decoder.frame(index)
            } catch (e: OutOfMemoryError) {
                return null
            }
        }
    }

}