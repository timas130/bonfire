package com.sup.dev.java.tools

object ToolsColor {

    fun alpha(color: Int): Int {
        return color.ushr(24)
    }

    fun red(color: Int): Int {
        return color shr 16 and 0xFF
    }

    fun green(color: Int): Int {
        return color shr 8 and 0xFF
    }

    fun blue(color: Int): Int {
        return color and 0xFF
    }

    fun rgb(red: Int, green: Int, blue: Int): Int {
        return 0xFF shl 24 or (red shl 16) or (green shl 8) or blue
    }

    fun argb(alpha: Int, red: Int, green: Int, blue: Int): Int {
        return alpha shl 24 or (red shl 16) or (green shl 8) or blue
    }

    fun setAlpha(alpha: Int, color: Int): Int {
        var alphaS = alpha
        if (alphaS < 0) alphaS = 0
        if (alphaS > 255) alphaS = 255
        return argb(alphaS, red(color), green(color), blue(color))
    }

    fun add(color1: Int, color2: Int): Int {
        val a1 = 255f / alpha(color1)
        val a2 = 255f / alpha(color2)
        val a = when {
            alpha(color1) == 0 -> alpha(color2)
            alpha(color2) == 0 -> alpha(color1)
            alpha(color1) != 0 && alpha(color2) != 0 && alpha(color1) != 255 && alpha(color2) != 255 -> (alpha(color1) + alpha(color2)) / 2
            else -> 255
        }

        val r1 = red(color1) / a1
        val r2 = red(color2) / a2
        val r = if (r1 + r2 < 255) r1 + r2 else if (r1 - r2 < 0) r2 - r1 else r1 - r2

        val g1 = green(color1) / a1
        val g2 = green(color2) / a2
        val g = if (g1 + g2 < 255) g1 + g2 else if (g1 - g2 < 0) g2 - g1 else g1 - g2

        val b1 = blue(color1) / a1
        val b2 = blue(color2) / a2
        val b = if (b1 + b2 < 255) b1 + b2 else if (b1 - b2 < 0) b2 - b1 else b1 - b2

        return argb(a, r.toInt(), g.toInt(), b.toInt())
    }

    fun remove(color1: Int, color2: Int): Int {
        return argb(alpha(color1), red(color1) - red(color2), green(color1) - green(color2), blue(color1) - blue(color2))
    }

    fun setAlphaPercent(color: Int, alphaPercent: Int): Int {
        return argb(255 / 100 * alphaPercent, red(color), green(color), blue(color))
    }

    fun getWhitePercent(percent: Int): Int {
        return rgb(255 / 100 * percent, 255 / 100 * percent, 255 / 100 * percent)
    }

    fun getBlackPercent(percent: Int): Int {
        return rgb(255 - 255 / 100 * percent, 255 - 255 / 100 * percent, 255 - 255 / 100 * percent)
    }

    fun toString(color: Int): String {
        return alpha(color).toString() + " " + red(color) + " " + green(color) + " " + blue(color)
    }

    fun random(): Int {
        return rgb((Math.random() * 255).toInt(), (Math.random() * 255).toInt(), (Math.random() * 255).toInt())
    }

}
