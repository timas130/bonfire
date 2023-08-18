package com.sup.dev.java_pc.tools

import com.sup.dev.java.tools.ToolsColor
import com.sup.dev.java.tools.ToolsMapper
import java.awt.AlphaComposite
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.IOException
import javax.imageio.ImageIO


object ToolsImage {


    fun replaceColors(source: BufferedImage, color: Int) {
        val raster = source.raster
        val pixel = IntArray(4)
        for (xx in 0 until source.width) {
            for (yy in 0 until source.height) {
                raster.getPixel(xx, yy, pixel)

                pixel[0] = color

                raster.setPixel(xx, yy, pixel)
            }
        }
    }


    fun getBufferedImage(array: ByteArray): BufferedImage {
        try {
            return ImageIO.read(ByteArrayInputStream(array))
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

    }

    fun isPNG(img: ByteArray): Boolean {
        val pngH = byteArrayOf(0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A)
        for (i in pngH.indices)
            if (img[i] != pngH[i] && img[i + 1] != pngH[i])
                return false
        return true
    }

    fun isGIF(img: ByteArray): Boolean {
        val gifH = byteArrayOf(0x47, 0x49, 0x46)
        for (i in gifH.indices) {
            if (img[i] != gifH[i])
                return false
        }
        return true
    }

    @JvmOverloads
    fun getImgScaleUnknownType(img: ByteArray, png: Boolean = true, gif: Boolean = true, jpg: Boolean = true): Array<Int> {
        if (png && isPNG(img))
            return getImgScalePNG(img)
        else if (gif && isGIF(img))
            return getImgScaleGIF(img)
        else if (jpg)
            return getImgScaleJPG(img)
        throw IllegalArgumentException("Unknown Img type")
    }

    fun getImgScaleJPG(img: ByteArray): Array<Int> {
        return getImgScale("jpg", img)
    }

    fun getImgScaleGIF(img: ByteArray): Array<Int> {
        return getImgScale("gif", img)
    }

    fun getImgScalePNG(img: ByteArray): Array<Int> {
        return getImgScale("png", img)
    }

    private fun getImgScale(suffix: String, img: ByteArray): Array<Int> {
        val reader = ImageIO.getImageReadersBySuffix(suffix).next()
        try {
            reader.setInput(ImageIO.createImageInputStream(ByteArrayInputStream(img)), false)
            return arrayOf(reader.getWidth(0), reader.getHeight(0))
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

    }

    @JvmOverloads
    fun checkImageScaleUnknownType(img: ByteArray, w: Int, h: Int, png: Boolean = true, gif: Boolean = true, jpg: Boolean = true): Boolean {
        if (png && isPNG(img)) return checkImageScalePNG(img, w, h)
        else if (gif && isGIF(img)) return checkImageScaleGIF(img, w, h)
        else if (jpg) return checkImageScaleJPG(img, w, h)

        throw IllegalArgumentException("Unknown Img type")
    }

    fun checkImageScaleJPG(img: ByteArray, w: Int, h: Int): Boolean {
        return checkImageScalePNG("jpg", img, w, h)
    }

    fun checkImageScaleGIF(img: ByteArray, w: Int, h: Int): Boolean {
        return checkImageScalePNG("gif", img, w, h)
    }

    fun checkImageScalePNG(img: ByteArray, w: Int, h: Int): Boolean {
        return checkImageScalePNG("png", img, w, h)
    }

    private fun checkImageScalePNG(suffix: String, img: ByteArray, w: Int, h: Int): Boolean {
        val imgScale = getImgScale(suffix, img)
        return imgScale[0] == w && imgScale[1] == h
    }

    @JvmOverloads
    fun checkImageMaxScaleUnknownType(img: ByteArray, w: Int, h: Int, png: Boolean = true, gif: Boolean = true, jpg: Boolean = true): Boolean {
        if (png && isPNG(img))
            return checkImageMaxScalePNG(img, w, h)
        else if (gif && isGIF(img))
            return checkImageMaxScaleGIF(img, w, h)
        else if (jpg)
            return checkImageMaxScaleJPG(img, w, h)
        throw IllegalArgumentException("Unknown Img type")
    }

    fun checkImageMaxScaleJPG(img: ByteArray, w: Int, h: Int): Boolean {
        return checkImageMaxScalePNG("jpg", img, w, h)
    }

    fun checkImageMaxScaleGIF(img: ByteArray, w: Int, h: Int): Boolean {
        return checkImageMaxScalePNG("gif", img, w, h)
    }

    fun checkImageMaxScalePNG(img: ByteArray, w: Int, h: Int): Boolean {
        return checkImageMaxScalePNG("png", img, w, h)
    }

    private fun checkImageMaxScalePNG(suffix: String, img: ByteArray, w: Int, h: Int): Boolean {
        val imgScale = getImgScale(suffix, img)
        return imgScale[0] <= w && imgScale[1] <= h
    }


    fun checkImageScaleHaARD(img: ByteArray, w: Int, h: Int): Boolean {
        return checkImageScale(getBufferedImage(img), w, h)
    }

    fun checkImageScale(bufferedImage: BufferedImage, w: Int, h: Int): Boolean {
        return bufferedImage.width == w && bufferedImage.height == h
    }

    fun scaledInstance(source: BufferedImage, newW: Float, newH: Float): BufferedImage {
        val oldW = source.width.toFloat()
        val oldH = source.height.toFloat()
        val instance = BufferedImage(newW.toInt(), newH.toInt(), BufferedImage.TYPE_INT_ARGB)
        val at = AffineTransform()
        at.scale((newW / oldW).toDouble(), (newH / oldH).toDouble())
        val scaleOp = AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR)
        return scaleOp.filter(source, instance)
    }

    fun filter(source: BufferedImage, color: Int) {
        replaceColors(source, 0, 0, source.width, source.height, color, true)
    }

    fun replaceColors(source: BufferedImage, x: Int, y: Int, w: Int, h: Int, color: Int, ignoreAlpha: Boolean) {
        val a = ToolsColor.alpha(color)
        val r = ToolsColor.red(color)
        val g = ToolsColor.green(color)
        val b = ToolsColor.blue(color)
        val raster = source.raster
        val pixel = IntArray(4)
        for (xx in x until w) {
            for (yy in y until h) {
                raster.getPixel(xx, yy, pixel)
                if (ignoreAlpha) {
                    if (pixel[3] == 0) continue
                } else {
                    pixel[3] = a
                }
                pixel[0] = r
                pixel[1] = g
                pixel[2] = b
                raster.setPixel(xx, yy, pixel)
            }
        }
    }

    fun setAlpha(source: BufferedImage, alpha: Int) {
        setAlpha(source, 0, 0, source.width, source.height, alpha)
    }

    fun setAlpha(source: BufferedImage, x: Int, y: Int, w: Int, h: Int, alpha: Int) {
        val raster = source.raster
        val pixel = IntArray(4)
        for (xx in x until w) {
            for (yy in y until h) {
                raster.getPixel(xx, yy, pixel)
                pixel[3] = alpha
                raster.setPixel(xx, yy, pixel)
            }
        }
    }

    fun toCircle(source: BufferedImage): BufferedImage {
        val diameter = Math.min(source.width, source.height)
        val mask = BufferedImage(source.width, source.height, BufferedImage.TYPE_INT_ARGB)

        var g2d = mask.createGraphics()
        applyQualityRenderingHints(g2d)
        g2d.fillOval(0, 0, diameter - 1, diameter - 1)
        g2d.dispose()

        val masked = BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB)
        g2d = masked.createGraphics()
        applyQualityRenderingHints(g2d)
        val x = (diameter - source.width) / 2
        val y = (diameter - source.height) / 2
        g2d.drawImage(source, x, y, null)
        g2d.composite = AlphaComposite.getInstance(AlphaComposite.DST_IN)
        g2d.drawImage(mask, 0, 0, null)
        g2d.dispose()
        return masked
    }

    fun applyQualityRenderingHints(g2d: Graphics2D) {

        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY)
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY)
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE)
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)

    }


}
