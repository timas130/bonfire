package com.sup.dev.android.libs.image_loader

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import com.sup.dev.android.tools.ToolsCash
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.ViewAvatar
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.java.tools.ToolsMath

abstract class ImageLink {

    private var fullImageLoader: ImageLink? = null
    private var previewImageLoader: ImageLink? = null

    internal var onLoadedBytes: (ByteArray?) -> Unit = {}
    internal var onLoadedBitmap: (Bitmap?) -> Unit = {}
    internal var onError: (() -> Unit)? = null
    internal var onSetHolder: () -> Unit = {}
    internal var customSetHolder: (() -> Unit)? = null
    internal var holder: Any? = null

    internal var forsedKey:String? = null

    internal var cropSquareCenter = false
    internal var w = 0
    internal var h = 0
    internal var minW = 0
    internal var minH = 0
    internal var maxW = 0
    internal var maxH = 0
    internal var cropW = 0
    internal var cropH = 0
    internal var allowGif = true
    internal var noHolder = false
    internal var fade = true
    internal var cashScaledBytes = false
    internal var noCash = false
    internal var fromCash = false
    internal var noLoadFromCash = false
    internal var autoDiskCashMaxSize = 1024 * 1024 * 2
    internal var resizeByMinSide = false
    internal var autocropIfLostBounds = true
    internal var immortalCash = false

    internal var created = false

    //
    //  Into
    //

    fun into(vImage: ImageView?, vProgressBar: View? = null) {
        ImageLoader.load(this, vImage, vProgressBar)
    }

    fun into(vImage: ViewAvatarTitle?) {
        into(vImage?.vAvatar)
    }

    fun into(vImage: ViewAvatar?) {
        into(vImage?.vImageView)
    }

    fun intoBytes(onLoadedBytes: (ByteArray?) -> Unit) {
        ImageLoader.load(this, onLoadedBytes = onLoadedBytes, noBitmap = true)
    }

    fun intoBitmap(onLoadedBitmap: (Bitmap?) -> Unit) {
        ImageLoader.load(this, onLoadedBitmap = onLoadedBitmap)
    }

    fun into(vImage: ImageView?, onLoadedBytes: (ByteArray?) -> Unit) {
        ImageLoader.load(this, vImage, onLoadedBytes = onLoadedBytes)
    }

    fun intoCash() {
        ImageLoader.load(this, intoCash = true)
    }


    //
    //  Loading
    //

    fun startLoad(): ByteArray? {
        val bytes = if (!noLoadFromCash) getFromCash() else null
        if (bytes != null) return bytes
        if(fromCash) return null
        val data = load()
        if (data != null && !noCash && data.size <= autoDiskCashMaxSize) ToolsCash.put(data, "" + getKey().replace("/", "_").hashCode(), immortalCash)
        return data
    }

    fun getFromCash() = ToolsCash.get("" + getKey().replace("/", "_").hashCode())

    abstract fun load(): ByteArray?

    open fun fastLoad(vImage: ImageView?): Boolean {
        return false
    }

    //
    //  Methods
    //

    fun getParamsSum(): String {
        return "$cropSquareCenter$w$h$minW$minH$maxW$maxH$cropW$cropH$resizeByMinSide"
    }

    fun copy(): ImageLink {
        val link = copyLocal()
        link.fullImageLoader = this.fullImageLoader
        link.previewImageLoader = this.previewImageLoader
        link.cropSquareCenter = this.cropSquareCenter
        link.w = this.w
        link.h = this.h
        link.minW = this.minW
        link.minH = this.minH
        link.cropW = this.cropW
        link.cropH = this.cropH
        link.allowGif = this.allowGif
        link.holder = this.holder
        link.noHolder = this.noHolder
        link.fade = this.fade
        link.cashScaledBytes = this.cashScaledBytes
        link.noCash = this.noCash
        link.fromCash = this.fromCash
        link.noLoadFromCash = this.noLoadFromCash
        link.autoDiskCashMaxSize = this.autoDiskCashMaxSize
        link.resizeByMinSide = this.resizeByMinSide
        link.autocropIfLostBounds = this.autocropIfLostBounds
        link.immortalCash = this.immortalCash
        return link
    }

    override fun equals(other: Any?): Boolean {
        if (javaClass == other?.javaClass && getParamsSum() == (other as ImageLink).getParamsSum()) return equalsTo(other)
        return false
    }

    abstract fun copyLocal(): ImageLink

    abstract fun equalsTo(imageLoader: ImageLink): Boolean

    fun clear() {
        ToolsCash.clear("" + getKey().replace("/", "_").hashCode())
        ImageLoader.removeFromCash(getKey())
    }

    fun checkCreated() {
        if (created) throw RuntimeException("You can't change ImageLink after is created. (you already call into function)")
    }

    //
    //  SetUp
    //

    fun crop(sides: Int) = crop(sides, sides)

    fun crop(w: Int, h: Int): ImageLink {
        checkCreated()
        cropW = w
        cropH = h
        if (cropH > 1 && cropW < 1) throw RuntimeException("cropW[$cropW] can't be < 1")
        if (cropW > 1 && cropH < 1) throw RuntimeException("cropH[$cropH] can't be < 1")
        return this
    }

    fun immortalCash(): ImageLink{
        immortalCash = true
        return this
    }

    fun resizeByMinSide(): ImageLink {
        checkCreated()
        this.resizeByMinSide = true
        return this
    }

    fun disallowGif(): ImageLink {
        checkCreated()
        allowGif = false
        return this
    }

    fun fullImageLoader(imageLoader: ImageLink): ImageLink {
        checkCreated()
        this.fullImageLoader = imageLoader
        return this
    }

    fun previewImageLoader(imageLoader: ImageLink): ImageLink {
        checkCreated()
        this.previewImageLoader = imageLoader
        return this
    }

    fun size(side: Int) = size(side, side)

    fun size(w: Int, h: Int): ImageLink {
        checkCreated()
        this.w = w
        this.h = h
        if (h > 1 && w < 1) throw RuntimeException("w[$w] can't be < 1")
        if (w > 1 && h < 1) throw RuntimeException("h[$h] can't be < 1")
        return this
    }

    fun minSize(side: Int) = minSize(side, side)

    fun minSize(w: Int, h: Int): ImageLink {
        checkCreated()
        this.minW = w
        this.minH = h
        if (minH > 1 && minW < 1) throw RuntimeException("minW[$minW] can't be < 1")
        if (minW > 1 && minH < 1) throw RuntimeException("minH[$minH] can't be < 1")
        return this
    }

    fun maxSize(side: Int) = maxSize(side, side)

    fun maxSize(w: Int, h: Int): ImageLink {
        checkCreated()
        this.maxW = w
        this.maxH = h
        if (maxH > 1 && maxW < 1) throw RuntimeException("maxW[$maxW] can't be < 1")
        if (maxW > 1 && maxH < 1) throw RuntimeException("maxH[$maxH] can't be < 1")
        return this
    }

    fun noHolder(): ImageLink {
        checkCreated()
        noHolder = true
        return this
    }

    fun setOnLoadedBytes(onLoaded: (ByteArray?) -> Unit): ImageLink {
        checkCreated()
        this.onLoadedBytes = onLoaded
        return this
    }

    fun setOnLoadedBitmap(onLoadedBitmap: (Bitmap?) -> Unit): ImageLink {
        checkCreated()
        this.onLoadedBitmap = onLoadedBitmap
        return this
    }

    fun setOnError(onError: (() -> Unit)?): ImageLink {
        checkCreated()
        this.onError = onError
        return this
    }

    fun setCustomSetHolder(customSetHolder: (() -> Unit)?): ImageLink {
        checkCreated()
        this.customSetHolder = customSetHolder
        return this
    }

    fun setOnSetHolder(onSetHolder: () -> Unit): ImageLink {
        this.onSetHolder = onSetHolder
        return this
    }

    fun cropSquare(): ImageLink {
        checkCreated()
        this.cropSquareCenter = true
        return this
    }

    fun cashScaledBytes(): ImageLink {
        checkCreated()
        this.cashScaledBytes = true
        return this
    }

    fun holder(holder: Int?): ImageLink {
        checkCreated()
        this.holder = holder
        return this
    }

    fun holder(holder: Drawable?): ImageLink {
        checkCreated()
        this.holder = holder
        return this
    }

    fun holder(holder: Bitmap?): ImageLink {
        checkCreated()
        this.holder = holder
        return this
    }

    fun noFade(): ImageLink {
        checkCreated()
        this.fade = false
        return this
    }

    fun noAutocropIfLostBounds(): ImageLink {
        checkCreated()
        this.autocropIfLostBounds = false
        return this
    }

    fun noCash(): ImageLink {
        checkCreated()
        this.noCash = true
        return this
    }

    fun fromCash(): ImageLink {
        checkCreated()
        this.fromCash = true
        return this
    }

    fun noLoadFromCash(): ImageLink {
        checkCreated()
        this.noLoadFromCash = true
        return this
    }

    fun setKey(key:String): ImageLink{
        this.forsedKey = key
        return this
    }

    //
    //  Getters
    //

    protected var generatedW = 0
    protected var generatedH = 0

    fun generateSizesIfNeed() {
        if (created) return

        if (cropW > 0 && cropH > 0) {
            this.generatedW = cropW
            this.generatedH = cropH
            created = true
            return
        }

        var generatedW = (if (w == 0) cropW else w).toFloat()
        var generatedH = (if (h == 0) cropH else h).toFloat()

        if (maxW > 0 && maxH > 0) {
            val inscribeMax = ToolsMath.inscribeInBounds(generatedW, generatedH, maxW.toFloat(), maxH.toFloat())
            generatedW = inscribeMax.w
            generatedH = inscribeMax.h
        }

        if (minW > 0 && minH > 0) {
            val inscribeMin = ToolsMath.inscribeOutBounds(generatedW, generatedH, minW.toFloat(), minH.toFloat())
            generatedW = inscribeMin.w
            generatedH = inscribeMin.h
        }

        generatedW = (generatedW.toInt()).toFloat() // Могут появиться десятичные хвоста
        generatedH = (generatedH.toInt()).toFloat() // Могут появиться десятичные хвоста

        if (minW > 0 && minH > 0 && maxW > 0 && maxH > 0) {
            if (minW > maxW) throw RuntimeException("minW[$minW] > maxW[$maxW]")
            if (minH > maxH) throw RuntimeException("minW[$minH] > maxW[$maxH]")

            if (autocropIfLostBounds) {
                //  ToolsView.dpToPx(1) защита от хвостов при маштабировании
                if (generatedW+ToolsView.dpToPx(1) < minW || generatedH-ToolsView.dpToPx(1) > maxH) {
                    crop(minW, maxH)
                    generateSizesIfNeed()
                    return
                }
                if (generatedH+ToolsView.dpToPx(1) < minH || generatedW-ToolsView.dpToPx(1) > maxW) {
                    crop(maxW, minH)
                    generateSizesIfNeed()
                    return
                }
            }
        }

        this.generatedW = generatedW.toInt()
        this.generatedH = generatedH.toInt()

        created = true
    }

    fun getW(): Int {
        generateSizesIfNeed()
        return generatedW
    }

    fun getH(): Int {
        generateSizesIfNeed()
        return generatedH
    }

    fun getCropW(): Int {
        generateSizesIfNeed()
        return cropW
    }

    fun getCropH(): Int {
        generateSizesIfNeed()
        return cropH
    }

    fun getFullImageLoader() = fullImageLoader

    fun getPreviewImageLoader() = previewImageLoader

    fun getKey(): String {
        if(forsedKey != null) return forsedKey!!
        generateSizesIfNeed()
        return getKeyOfImage() + ":" + getParamsSum().hashCode()
    }

    protected abstract fun getKeyOfImage(): String

    fun isKey(key: Any?): Boolean {
        return key != null && key == this.getKey()
    }


}
