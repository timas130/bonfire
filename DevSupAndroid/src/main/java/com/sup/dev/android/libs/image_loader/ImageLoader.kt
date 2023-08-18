package com.sup.dev.android.libs.image_loader

import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.sup.dev.android.R
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.tools.ToolsBitmap
import com.sup.dev.android.tools.ToolsGif
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.java.classes.items.Item3
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.tools.ToolsBytes
import com.sup.dev.java.tools.ToolsThreads
import java.io.File
import java.lang.ref.WeakReference
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

object ImageLoader {

    internal val maxCashItemSize = 1024 * 1024 * 5
    internal val maxCashSize = 1024 * 1024 * 50

    private val cash = ArrayList<Item3<String, Bitmap?, ByteArray?>>()
    private var cashSize = 0
    private var threadPool: ThreadPoolExecutor = ThreadPoolExecutor(4, 4, 1, TimeUnit.MINUTES, LinkedBlockingQueue())
    private val processing = ArrayList<Loader>()

    init {
        SupAndroid.addOnLowMemory {
            clearCash()
        }
    }

    //
    //  Tools
    //

    fun loadByAny(any: Any): ImageLink? {
        if (any is File) return load(any)
        if (any is Int) return load(any)
        if (any is Long) return load(any)
        if (any is String) return load(any)
        if (any is ByteArray) return load(any)
        return null
    }

    fun load(file: File) = ImageLoaderFile(file)
    fun load(@DrawableRes res: Int) = ImageLoaderResource(res)
    @JvmOverloads
    fun load(id: Long, pwd: String? = null) = ImageLoaderId(id, pwd)
    fun load(tag: String) = ImageLoaderTag(tag)
    fun load(bytes: ByteArray) = ImageLoaderBytes(bytes)

    fun loadGif(imageId: Long, gifId: Long, vImage: ImageView,
                vGifProgressBar: View? = null, onInit: (ImageLink) -> Unit = {}) =
        loadGif(imageId, gifId, null, vImage, vGifProgressBar, onInit)

    fun loadGif(imageId: Long, gifId: Long, pwd: String?, vImage: ImageView,
                vGifProgressBar: View? = null, onInit: (ImageLink) -> Unit = {}) {
        if (gifId == 0L) {
            ToolsThreads.main { vGifProgressBar?.visibility = View.INVISIBLE }
            val load = load(imageId, pwd)
            onInit.invoke(load)
            load.into(vImage)
        } else if (imageId > 0) {
            val load = load(imageId, pwd)
            onInit.invoke(load)
            load.into(vImage) {
                val loadGif = load(gifId, pwd)
                onInit.invoke(loadGif)
                loadGif.holder(vImage.drawable).into(vImage, vGifProgressBar)
            }
        } else {
            val load = load(gifId, pwd)
            onInit.invoke(load)
            load.holder(vImage.drawable).into(vImage, vGifProgressBar)
        }
    }

    fun clear(imageId: Long) {
        load(imageId).clear()
    }

    //
    //  Public
    //


    fun load(link: ImageLink,
             vImage: ImageView? = null,
             vProgressBar: View? = null,
             onLoadedBytes: ((ByteArray?) -> Unit)? = null,
             onLoadedBitmap: ((Bitmap?) -> Unit)? = null,
             noBitmap: Boolean = false,
             intoCash: Boolean = false) {
        val loader = Loader(link, vImage, vProgressBar, onLoadedBytes, onLoadedBitmap, noBitmap, intoCash)
        vImage?.tag = loader
        load(loader)
    }

    private fun load(loader: Loader) {

        if (loader.link.fastLoad(loader.vImage)) return

        if (loader.vProgressBar != null) loader.vProgressBar.visibility = View.INVISIBLE

        val cashItem = if (loader.link.noLoadFromCash) null else getFromCash(loader.link.getKey())
        if (cashItem != null) {
            if (!loader.intoCash) putImage(loader, cashItem.a2, false, cashItem.a3)
            return
        }

        try {
            putHolder(loader)
        } catch (e: OutOfMemoryError) {
            SupAndroid.onLowMemory()
            putHolder(loader)
        }

        ToolsThreads.thread {

            var bytes: ByteArray? = null
            if (!loader.link.noLoadFromCash) bytes = loader.link.getFromCash()

            if (bytes != null) {
                if (!loader.intoCash) ToolsThreads.thread { putImage(loader, parseImage(loader, bytes), false, bytes) }
                return@thread
            }

            ToolsThreads.main {
                loadStart(loader)
            }

        }
    }

    private fun loadStart(loader: Loader) {
        threadPool.execute {
            synchronized(processing) {
                if(loader.loopCount > 0) {
                    for (i in processing) if (i.link.isKey(loader.link.getKey())) {
                        loader.loopCount--
                        ToolsThreads.main(100) { load(loader) }
                        return@execute
                    }
                }
                processing.add(loader)
            }
            try {
                loadNow(loader)
            } catch (ex: Throwable) {
                err(ex)
            }
            synchronized(processing) { processing.remove(loader) }
        }
    }

    private fun putHolder(loader: Loader) {

        if (loader.vProgressBar != null) loader.vProgressBar.visibility = if (loader.link.allowGif) View.VISIBLE else View.INVISIBLE

        if (loader.link.customSetHolder != null) {
            loader.link.customSetHolder!!.invoke()
        } else {
            if (loader.vImage != null) {
                if (!loader.link.noHolder) {
                    if (loader.link.holder is Int) {
                        loader.vImage.setImageResource(loader.link.holder as Int)
                    } else if (loader.link.holder is Drawable) {
                        loader.vImage.setImageDrawable(loader.link.holder as Drawable)
                    } else if (loader.link.holder is Bitmap) {
                        loader.vImage.setImageBitmap(loader.link.holder as Bitmap)
                    } else if (loader.link.getW() != 0 && loader.link.getH() != 0) {
                        try {
                            val bitmap = Bitmap.createBitmap(loader.link.getW(), loader.link.getH(), Bitmap.Config.ARGB_4444)
                            bitmap.eraseColor(ToolsResources.getColor(R.color.focus))
                            loader.vImage.setImageBitmap(bitmap)
                        } catch (e: OutOfMemoryError) {
                            SupAndroid.onLowMemory()
                            val bitmap = Bitmap.createBitmap(loader.link.getW(), loader.link.getH(), Bitmap.Config.ARGB_4444)
                            bitmap.eraseColor(ToolsResources.getColor(R.color.focus))
                            loader.vImage.setImageBitmap(bitmap)
                        }
                    } else {
                        loader.vImage.setImageDrawable(ColorDrawable(ToolsResources.getColor(R.color.focus)))
                    }
                } else{
                    loader.vImage.setImageDrawable(null)
                }
            }
        }

        loader.link.onSetHolder.invoke()
    }

    //
    //  Cash
    //

    fun addToCash(key: String, bitmap: Bitmap?, bytes: ByteArray?) {
        removeFromCash(key)
        if (bitmap == null && bytes == null) return
        val size = bitmap?.byteCount ?: bytes!!.size
        if (size > maxCashItemSize) return

        cashSize += size
        cash.add(Item3(key, bitmap, bytes))

        while (cashSize > maxCashSize && cash.isNotEmpty()) removeFromCash(cash[0].a1)
    }

    fun getFromCash(key: String): Item3<String, Bitmap?, ByteArray?>? {
        var item: Item3<String, Bitmap?, ByteArray?>? = null
        for (i in cash) if (i.a1 == key) {
            item = i
            break
        }
        return item
    }

    fun removeFromCash(key: String) {
        val item = getFromCash(key) ?: return

        cashSize -= item.a2?.byteCount ?: item.a3!!.size

        var i = 0
        while (i in 0 until cash.size) {
            if (cash[i].a1 == key) cash.removeAt(i--)
            i++
        }

    }

    fun clearCash() {
        cash.clear()
        cashSize = 0
    }

    //
    //  Methods
    //

    private fun loadNow(loader: Loader) {
        val loadedBytes = loader.link.startLoad()
        if (loadedBytes == null) {
            if (loader.link.onError != null) ToolsThreads.main { loader.link.onError?.invoke() }
            else if (loader.tryCount > 0) ToolsThreads.main(5000) {
                loader.tryCount--
                loadStart(loader)
            }
            return
        }
        var bytes = loadedBytes
        var bitmap: Bitmap? = null

        if (loader.link.allowGif && !ToolsBytes.isGif(loadedBytes)) {
            bitmap = parseImage(loader, loadedBytes)
            if (loader.link.cashScaledBytes && bitmap != null) bytes = ToolsBitmap.toJPGBytes(bitmap, 100)
        }

        ToolsThreads.main { putImage(loader, bitmap, true, bytes) }

    }

    private fun parseImage(loader: Loader, bytes: ByteArray): Bitmap? {
        var bm = if (loader.noBitmap) null else ToolsBitmap.decode(bytes, loader.link.getW(), loader.link.getH(), null, resizeByMinSide = loader.link.resizeByMinSide)


        if (bm != null) {
            if (loader.link.cropSquareCenter) bm = ToolsBitmap.cropCenterSquare(bm)
            if (loader.link.getCropW() > 0 && loader.link.getCropH() > 0) bm = ToolsBitmap.cropCenter(bm, loader.link.cropW, loader.link.cropH)
        }
        return bm
    }

    private fun putImage(loader: Loader, bm: Bitmap?, animate: Boolean, bytes: ByteArray?) {
        ToolsThreads.main {
            if (!loader.link.noCash && bm != null) addToCash(loader.link.getKey(), bm, bytes)

            if (loader.vImage != null && loader == loader.vImage.tag) {
                if (loader.link.allowGif && bytes != null && ToolsBytes.isGif(bytes)) {
                    ToolsGif.iterator(bytes, WeakReference(loader.vImage), loader.link.getW(), loader.link.getH(), loader.link.resizeByMinSide) {
                        if (loader.vProgressBar != null) loader.vProgressBar.visibility = View.INVISIBLE
                    }
                } else {
                    if (bm != null) {
                        if (loader.vProgressBar != null) loader.vProgressBar.visibility = View.INVISIBLE
                        loader.vImage.setImageDrawable(DrawableImageLoader(loader.vImage.context, bm, animate && loader.link.fade))
                    }
                }
            }
            loader.link.onLoadedBytes.invoke(bytes)
            loader.link.onLoadedBitmap.invoke(bm)
            loader.onLoadedBytes?.invoke(bytes)
            loader.onLoadedBitmap?.invoke(bm)
        }
    }

    //
    //  Support
    //

    private class Loader(
            val link: ImageLink,
            val vImage: ImageView?,
            val vProgressBar: View?,
            val onLoadedBytes: ((ByteArray?) -> Unit)?,
            val onLoadedBitmap: ((Bitmap?) -> Unit)?,
            val noBitmap: Boolean,
            val intoCash: Boolean
    ) {

        var tryCount = 2
        var loopCount = 20
    }
}
