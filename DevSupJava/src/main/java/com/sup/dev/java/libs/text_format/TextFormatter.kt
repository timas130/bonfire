package com.sup.dev.java.libs.text_format

import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.tools.ToolsText

class TextFormatter(
        val text: String
) {
    companion object {
        private const val char_protector = '\\'
        private const val char_protector_word = '@'
        private const val char_no_format = "[noFormat]"
        private const val char_no_format_end = "[/noFormat]"
        private val chars_spec = arrayOf(char_protector, char_protector_word, '*', '^', '~', '_', '{', '}')
        private const val chars_format_close = "*^~_}"
        private const val chars_url_trailing = "$chars_format_close.,\"'!?:;)"
        private val colors = hashMapOf(
            "red" to "D32F2F", "pink" to "C2185B", "purple" to "7B1FA2", "indigo" to "303F9F",
            "blue" to "1976D2", "cyan" to "0097A7", "teal" to "00796B", "green" to "388E3C",
            "lime" to "689F38", "yellow" to "FBC02D", "amber" to "FFA000", "orange" to "F57C00",
            "brown" to "5D4037", "grey" to "616161", "campfire" to "FF6D00", "rainbow" to "-",
            "gay" to "-", "xmas" to "-", "christmas" to "-", "bonfire" to "FF6D00",
        )

        private val colored_unit = Regex("&amp;|&#60;|.", RegexOption.DOT_MATCHES_ALL)

        private const val char_typed_lt = '\uE000'
    }

    private var result: StringBuilder = StringBuilder()
    private var i = 0
    private var skipToSpace = false
    private var skipToNextNoFormat = false
    private var urlEnd = 0

    fun parseHtml(): String {
        if (result.length == 0) parseText()
        return result.toString()
    }

    fun parseNoTags(): String {
        return TextFormatter(text.replace('<', char_typed_lt)).parseHtml()
                .replace(Regex("<[^>]*>"), "")
                .replace(char_typed_lt, '<')
    }

    private fun parseText() {
        result = StringBuilder(text.length + 128)
        while (i < text.length) {
            val thisChar = text[i]

            if (skipToSpace) {
                if (thisChar.isWhitespace()) {
                    skipToSpace = false
                    if (thisChar == ' ' && text[i - 1] != char_protector_word && chars_spec.contains(text[i - 1])) i--
                } else {
                    result.append(thisChar)
                    i++
                    continue
                }
            }

            if (skipToNextNoFormat) {
                if (thisChar == '[' && text.length - i >= char_no_format_end.length && text.substring(i, i + char_no_format_end.length) == char_no_format_end
                        && (i == 0 || text[i - 1] != char_protector)) {
                    i += char_no_format_end.length
                    skipToNextNoFormat = false
                    continue
                } else {
                    result.append(thisChar)
                    i++
                    continue
                }
            }

            if (thisChar == char_protector
                    && text.length > i + 1
                    && (text[i + 1] != char_protector && chars_spec.contains(text[i + 1]))) {
                result.append(text[++i])
                i++
                continue
            }

            if (thisChar == '[' && text.length - i >= char_no_format.length && text.substring(i, i + char_no_format.length) == char_no_format
                    && (i == 0 || text[i - 1] != char_protector)) {
                i += char_no_format.length
                skipToNextNoFormat = true
                continue
            }

            if (i >= urlEnd && isUrlStart(i)) urlEnd = findUrlEnd(i)
            if (i < urlEnd) {
                result.append(thisChar)
                i++
                continue
            }

            if (thisChar == char_protector_word) {
                skipToSpace = true
                result.append(thisChar)
                i++
                continue
            }
            val skip = when (thisChar) {
                '*' -> parseHtml('*', "<\$b>", "</\$b>")
                '^' -> parseHtml('^', "<\$i>", "</\$i>")
                '~' -> parseHtml('~', "<\$s>", "</\$s>")
                '_' -> parseHtml('_', "<\$u>", "</\$u>")
                '[' -> parseLink()
                '{' -> run {
                    if (parseColorHash()) return@run true
                    for (color in colors) {
                        var matches = true
                        if (text.length - i < color.key.length + 2) continue

                        val colorName = color.key + " "
                        for (c in colorName.indices) {
                            if (text[i + c + 1].lowercaseChar() != colorName[c]) {
                                matches = false
                                break
                            }
                        }
                        if (matches) return@run parseColorName(color.key, color.value)
                    }
                    false
                }
                else -> false
            }
            if (skip) continue
            result.append(thisChar)
            i++
        }
    }

    private fun parseHtml(c: Char, open: String, close: String): Boolean {
        val next = findNext(c, 0)
        if (next != -1) {
            result.append(open + TextFormatter(text.substring(i + 1, next)).parseHtml() + close)
            i = next + 1
            return true
        }
        return false
    }

    private fun findNext(c: Char, offset: Int): Int {
        var next = -1
        var skip = false
        var skipToSpace = false
        val skipUrls = chars_format_close.contains(c)
        var n = i + 1 + offset
        while (n < text.length) {
            if (skip) {
                skip = false
                n++
                continue
            }
            if (skipToSpace) {
                if (text[n].isWhitespace()) {
                    skipToSpace = false
                    if (text[n - 1] != char_protector_word && chars_spec.contains(text[n - 1])) {
                        n--
                    }
                } else {
                    n++
                    continue
                }
            }
            if (skipUrls && isUrlStart(n)) {
                n = findUrlEnd(n)
                continue
            }
            if (text[n] == c) {
                next = n
                break
            } else if (text[n] == char_protector) {
                skip = true
            } else if (text[n] == char_protector_word) {
                skipToSpace = true
            }
            n++
        }
        return next
    }

    private fun isUrlStart(n: Int): Boolean {
        if (n > 0 && text[n - 1].isLetterOrDigit()) return false
        return text.startsWith("http://", n, ignoreCase = true)
                || text.startsWith("https://", n, ignoreCase = true)
                || text.startsWith("www.", n, ignoreCase = true)
    }

    private fun findUrlEnd(start: Int): Int {
        var end = start
        while (end < text.length && !text[end].isWhitespace() && !isMarkerBeforeTypography(end)) end++
        while (chars_url_trailing.contains(text[end - 1]) && text[end - 2] != char_protector) end--
        return end
    }

    private fun isMarkerBeforeTypography(n: Int): Boolean {
        if (!chars_format_close.contains(text[n]) || text[n - 1] == char_protector || n + 1 >= text.length) return false
        val next = text[n + 1]
        return next.code > 127 && !next.isLetterOrDigit()
    }

    private fun parseColorName(name: String, hash: String): Boolean {
        try {
            val next = findNext('}', name.length + 1)
            if (next != -1) {
                if (name == "rainbow") {
                    val t = text.substring(i + name.length + 2, next)
                    var x = -1
                    for (unit in colored_unit.findAll(t)) result.append(rainbow(unit.value, x++))
                } else if (name == "gay") {
                    val t = text.substring(i + name.length + 2, next)
                    var x = -1
                    for (unit in colored_unit.findAll(t)) result.append(gay(unit.value, x++))
                } else if (name == "xmas" || name == "christmas") {
                    val t = text.substring(i + name.length + 2, next)
                    var x = -1
                    for (unit in colored_unit.findAll(t)) result.append(xmas(unit.value, x++))
                } else {
                    val t = TextFormatter(text.substring(i + name.length + 2, next)).parseHtml()
                    result.append("<font color=\"#$hash\">$t</font>")
                }
                i = next + 1
                return true
            }
            return false
        } catch (e: Exception) {
            err(e)
            return false
        }
    }

    private fun rainbow(s: String, index: Int): String {
        if(s.length > 200) return s
        return when ((index + 1) % 7) {
            0 -> "<font color=\"#d5302e\">$s</font>"
            1 -> "<font color=\"#f67c01\">$s</font>"
            2 -> "<font color=\"#f8c129\">$s</font>"
            3 -> "<font color=\"#3c8f3d\">$s</font>"
            4 -> "<font color=\"#1e75d2\">$s</font>"
            5 -> "<font color=\"#014efc\">$s</font>"
            6 -> "<font color=\"#77229b\">$s</font>"
            else -> "<font color=\"#000000\">$s</font>"
        }
    }
    private fun gay(s: String, index: Int): String {
        if(s.length > 200) return s
        return when ((index + 1) % 6) {
            0 -> "<font color=\"#d5302e\">$s</font>"
            1 -> "<font color=\"#f67c01\">$s</font>"
            2 -> "<font color=\"#f8c129\">$s</font>"
            3 -> "<font color=\"#3c8f3d\">$s</font>"
            4 -> "<font color=\"#1e75d2\">$s</font>"
            5 -> "<font color=\"#77229b\">$s</font>"
            else -> "<font color=\"#000000\">$s</font>"
        }
    }

    private fun xmas(s: String, index: Int): String {
        if(s.length > 200) return s
        return when ((index + 1) % 2) {
            0 -> "<font color=\"#D32F2F\">$s</font>"
            1 -> "<font color=\"#D1D1D1\">$s</font>"
            else -> "<font color=\"#000000\">$s</font>"
        }
    }

    private fun parseColorHash(): Boolean {
        try {
            val c1 = nextColorChar(i + 1)
            val c2 = nextColorChar(i + 2)
            val c3 = nextColorChar(i + 3)
            val c4 = nextColorChar(i + 4)
            val c5 = nextColorChar(i + 5)
            val c6 = nextColorChar(i + 6)
            if (c1 != null && c2 != null && c3 != null && c4 != null && c5 != null && c6 != null && text[i + 7] == ' ') {
                val color = "$c1$c2$c3$c4$c5$c6"
                val next = findNext('}', 7)
                if (next != -1) {
                    result.append("<font color=\"#$color\">${
                        TextFormatter(text.substring(i + 8, next)).parseHtml()
                    }</font>")
                    i = next + 1
                    return true
                }
            }
            return false
        } catch (e: Exception) {
            err(e)
            return false
        }
    }

    private fun parseLink(): Boolean {
        try {
            val nextClose = findNext(']', 0)

            if (nextClose == -1) return false

            var nextSpace = findNext(' ', nextClose - i)
            if (nextSpace == -1) nextSpace = text.length
            nextSpace = (nextClose + 1 until nextSpace).firstOrNull { text[it].isWhitespace() } ?: nextSpace

            if (ToolsText.TEXT_CHARS_s.contains(text[nextSpace - 1])) nextSpace--
            val name = text.substring(i + 1, nextClose)
            val link = text.substring(nextClose + 1, nextSpace)

            if (isNamedLinkTarget(link) || link.startsWith(char_protector_word)) {
                result.append("<a href=\"${ToolsText.castToWebLink(link)}\">$name</a>")
                i = nextSpace
                return true
            }
            return false
        } catch (e: Exception) {
            err(e)
            return false
        }
    }

    private fun isNamedLinkTarget(link: String): Boolean {
        if (!ToolsText.isWebLink(link)) return false
        if (link.startsWith("http://", ignoreCase = true) || link.startsWith("https://", ignoreCase = true)) return true
        val host = link.substringBefore('/').substringBefore('?').substringBefore('#').substringBefore(':')
        val tld = host.substringAfterLast('.', "")
        return !host.startsWith(".") && tld.length >= 2 && tld[0].isLetter() && tld.all { it.isLetterOrDigit() || it == '-' }
    }

    private fun nextColorChar(i: Int): Char? {
        if ("0123456789abcdef".contains(text[i].lowercaseChar())) return text[i]
        return null
    }
}
