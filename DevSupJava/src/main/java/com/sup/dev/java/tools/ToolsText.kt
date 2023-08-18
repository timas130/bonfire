package com.sup.dev.java.tools

import java.io.PrintWriter
import java.io.StringWriter
import java.io.UnsupportedEncodingException
import java.util.*
import kotlin.experimental.and

object ToolsText {

    val LATIN = charArrayOf('q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p', 'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'z', 'x', 'c', 'v', 'b', 'n', 'm')
    val LATIN_UPPER = charArrayOf('Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P', 'A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L', 'Z', 'X', 'C', 'V', 'B', 'N', 'M')
    val TEXT_CHARS = charArrayOf('.', ',', '"', '\'', '!', '?', ' ', '-')
    val NUMBERS = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
    val BITS_PER_HEX_DIGIT = 4
    val LATIS_S = "qwertyuiopasdfghjklzxcvbnm"
    val NUMBERS_S = "0123456789"
    val TEXT_CHARS_s = ".,\"'!? -:"
    val SPEC = "\"^%`\\|<>{}[] _/;@#$&+()*~"
    val hexSymbols = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f")

    fun empty(s: CharSequence?): Boolean {
        return s == null || s.length == 0
    }

    fun equalsNoCase(vararg s: String): Boolean {
        val s1 = s[0]
        for (i in 1 until s.size) {
            if (s1.lowercase(Locale.ROOT) != s[i].lowercase(Locale.ROOT)) return false
        }
        return true
    }

    fun equals(vararg s: String): Boolean {
        val s1 = s[0]
        for (i in 1 until s.size) {
            if (s1 != s[i]) return false
        }
        return true
    }

    fun inBounds(text: String?, min: Int, max: Int): Boolean {
        return text != null && text.length >= min && text.length <= max
    }

    fun languageCodeAsLong(languageCode: String): Long {
        var languageCodeV = languageCode
        languageCodeV = languageCodeV.trim { it <= ' ' }.lowercase(Locale.ROOT)
        val x = StringBuilder()
        for (i in 0 until languageCodeV.length) {
            val c = languageCodeV[i]
            for (n in LATIN.indices)
                if (LATIN[n] == c)
                    x.append(n)
        }
        return java.lang.Long.parseLong(x.toString())
    }

    fun isContainsLatin(s: String): Boolean {
        return isContainsChars(s, LATIN)
    }

    fun isOnlyLatin(s: String): Boolean {
        return isOnlyChars(s, LATIN)
    }

    fun isOnlyLatinAndTextChars(s: String): Boolean {
        return isOnlyChars(s, LATIN, TEXT_CHARS)
    }

    fun isOnly(s: String, filter: String): Boolean {
        return isOnlyChars(s, filter.toCharArray())
    }

    fun isContainsChars(s: String, vararg charsArrays: CharArray): Boolean {
        for (element in s)
            for (chars in charsArrays)
                for (c in chars)
                    if (element == c)
                        return true

        return false
    }

    fun isOnlyChars(s: String, vararg charsArrays: CharArray): Boolean {
        var sV = s
        sV = sV.lowercase(Locale.ROOT)

        for (element in sV) {
            var b = false

            for (chars in charsArrays) {
                for (c in chars) {
                    if (element == c) {
                        b = true
                        break
                    }
                }
                if (b) break
            }

            if (!b) return false
        }

        return true
    }

    fun isInteger(s: String): Boolean {
        return try {
            Integer.parseInt(s)
            true
        } catch (ex: NumberFormatException) {
            false
        }

    }

    fun isDouble(s: String): Boolean {
        return try {
            java.lang.Double.parseDouble(s.replace(",".toRegex(), "."))
            true
        } catch (ex: NumberFormatException) {
            false
        }

    }

    // Key, Word, Key, Word...
    fun replaceKeys(text: String, vararg values: String): String {
        var textV = text
        var i = 0
        while (i < values.size) {
            textV = textV.replace(values[i].toRegex(), values[i + 1])
            i += 2
        }
        return textV
    }

    fun toString(bytes: ByteArray): String {
        try {
            return String(bytes, charset("UTF-8"))
        } catch (e: UnsupportedEncodingException) {
            throw RuntimeException(e)
        }

    }

    fun toBytes(string: String): ByteArray {
        try {
            return string.toByteArray(charset("UTF-8"))
        } catch (e: UnsupportedEncodingException) {
            throw RuntimeException(e)
        }

    }

    fun toHexColor(color: Int): String {
        return String.format("#%08X", color)
    }

    fun checkSize(s: String?, min: Int, max: Int): Boolean {
        return s != null && s.length >= min && s.length <= max
    }

    @JvmOverloads
    fun checkStringChars(string: String?, chars: String, checkCase: Boolean = false): Boolean {
        var charsV = chars
        if (!checkCase) charsV = charsV.lowercase(Locale.ROOT)
        for (element in string!!) {
            var c = element + ""
            if (!checkCase) c = c.lowercase(Locale.ROOT)
            if (!charsV.contains(c)) return false
        }
        return true
    }

    fun isValidEmailAddress(email: String): Boolean {
        val ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$"
        val p = java.util.regex.Pattern.compile(ePattern)
        val m = p.matcher(email)
        return m.matches()
    }

    fun numToStringRound(num: Float, count: Int) = numToStringRound(num.toDouble(), count)

    fun numToStringRound(num: Double, count: Int): String {
        var countV = count
        val s = num.toString() + ""
        val p = s.indexOf('.')
        countV += 1
        return when {
            p < 0 -> s + "." + zeroString(countV)
            p + countV > s.length -> s.substring(0, s.length) + zeroString(countV - (s.length - p))
            else -> s.substring(0, p + countV)
        }
    }

    fun numToStringRoundAndTrim(num: Float, count: Int) = numToStringRoundAndTrim(num.toDouble(), count)

    fun numToStringRoundAndTrim(num: Double, count: Int): String {
        var countV = count
        if (num.toInt().toDouble() == num) return num.toInt().toString() + ""
        var s = num.toString() + ""
        val p = s.indexOf('.')
        countV += 1

        if (p + countV > s.length)
            s = s.substring(0, s.length)
        else
            s = s.substring(0, p + countV)

        if (s.contains("."))
            for (i in s.length - 1 downTo -1 + 1)
                if (s[i] == '0')
                    s = s.substring(0, s.length - 1)
                else
                    break

        return s
    }

    fun zeroString(count: Int): String {
        var s = ""
        for (i in 0 until count)
            s += "0"
        return s
    }

    fun trim(num: Double): String {
        return if (num.toInt().toDouble() == num) num.toInt().toString() + "" else num.toString() + ""
    }

    fun numToStringSpace(num: Long): String {
        val s = num.toString() + ""
        var ss = ""
        var x = 0
        for (i in s.length - 1 downTo -1 + 1) {
            x++
            ss = s[i] + ss
            if (x % 3 == 0)
                ss = " $ss"
        }
        return ss
    }

    fun numToStringK(num: Long): String {
        var numV = num
        var arg = ""
        if (numV < 0) {
            numV = -numV
            arg = "-"
        }
        if (numV >= 1000000000) {
            return (numV / 1000000000).toString() + "B"
        }
        if (numV >= 1000000) {
            return (numV / 1000000).toString() + "M"
        }
        return if (numV >= 1000) {
            (numV / 1000).toString() + "K"
        } else arg + numV + ""
    }

    fun numToBytesString(bytes: Long): String {
        var l = bytes.toDouble()
        var s = "BT"
        if (bytes >= 1024 * 1024 * 1024) {
            l = bytes.toDouble() / 1024.0 / 1024.0 / 1024.0
            s = "GB"
        } else if (bytes >= 1024 * 1024) {
            l = bytes.toDouble() / 1024.0 / 1024.0
            s = "MB"
        } else if (bytes >= 1024) {
            l = bytes / 1024.0
            s = "KB"
        }
        return numToStringRound(l, 2) + " " + s
    }

    fun toTime(time: Long): String {
        val minutes = (time.toFloat() / 1000f / 60f).toInt()
        val seconds = (time / 1000f).toInt() % 60
        return (if (minutes < 10) "0" else "") + minutes + ":" + (if (seconds < 10) "0" else "") + seconds
    }

    fun isWebLink(s: String): Boolean {
        var x = s.indexOf('.')
        if (x == -1) return false
        while (x != -1) {
            if (x == s.length - 1)
                return false
            x = s.indexOf('.', x + 1)
        }
        return true
    }

    fun exceptionToString(ex: Exception): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        ex.printStackTrace(pw)

        return sw.toString()
    }

    fun isLinkToYoutube(s: String): Boolean {
        val sV = clearWebLinkPrefix(s)
        return sV.contains("youtube.com/watch?v=") || sV.contains("youtu.be/")
    }

    fun castToWebLink(s: String): String {
        return if (s.contains("https://") || s.contains("http://"))
            s
        else
            "http://$s"
    }

    fun clearWebLinkPrefix(s: String): String {
        val l = arrayOf("https://", "http://", "https://www.", "http://www.", "https://m.", "http://m.")
        for (f in l)
            if (s.length > f.length && s.substring(0, f.length).lowercase(Locale.getDefault()) == f)
                return s.substring(f.length)
        return s
    }

    fun bytesToHex(bytes: ByteArray?): String? {
        if (bytes == null)
            return null
        val hexBuffer = StringBuilder(bytes.size * 2)
        for (aByte in bytes) hexBuffer.append(toHex(aByte))
        return hexBuffer.toString()
    }

    private fun toHex(b: Byte): String {
        val leftSymbol = (b.toInt().ushr(BITS_PER_HEX_DIGIT) and 0x0f).toByte()
        val rightSymbol = b and 0x0f
        return hexSymbols[leftSymbol.toInt()] + hexSymbols[rightSymbol.toInt()]
    }


    fun hexToBytes(s: String): ByteArray {
        val len = s.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(s[i], 16) shl 4) + Character.digit(s[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }

    fun isHex(s: String?): Boolean {
        return checkStringChars(s, "1234567890abcdef")
    }

    fun removeMacSeparators(mac: String): String {
        var macV = mac
        if (macV.length != 17 && macV.length != 12 && macV.length != 14)
            throw IllegalArgumentException("Invalid mac address [$macV]")

        if (macV.length == 17)
            macV = ("" + macV[0] + macV[1] + macV[3] + macV[4]
                    + macV[6] + macV[7] + macV[9] + macV[10]
                    + macV[12] + macV[13] + macV[15] + macV[16])

        if (macV.length == 14)
            macV = ("" + macV[0] + macV[1] + macV[2] + macV[3]
                    + macV[5] + macV[6] + macV[7] + macV[8]
                    + macV[10] + macV[11] + macV[12] + macV[13])

        return macV
    }

    fun putMacSeparators(mac: String, sep: String, lengthType: Int): String {

        if (mac.length != 12)
            throw IllegalArgumentException("Invalid mac address [$mac]. Must not include separators [FF00FF00FF00]")

        if (lengthType == 17)
            return ("" + mac[0] + mac[1] + sep + mac[2] + mac[3] + sep
                    + mac[4] + mac[5] + sep + mac[6] + mac[7] + sep
                    + mac[8] + mac[9] + sep + mac[10] + mac[11])

        if (lengthType == 14)
            return ("" + mac[0] + mac[1] + mac[2] + mac[3] + sep
                    + mac[4] + mac[5] + mac[6] + mac[7] + sep
                    + mac[8] + mac[9] + mac[10] + mac[11])

        throw IllegalArgumentException("Unsupported lengthType [$lengthType]")
    }

    fun macToHex(macString: String?): ByteArray? {
        var macStringV = macString
        if (macStringV == null || macStringV.isEmpty()) return null

        macStringV = removeMacSeparators(macStringV)
        if (!isHex(macStringV)) throw IllegalArgumentException("Mac is not a HEX [$macStringV]")

        return hexToBytes(macStringV)
    }

}
