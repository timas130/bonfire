package sh.sit.bonfire.formatting.core.bfm.color

import org.commonmark.node.Text
import org.commonmark.parser.delimiter.DelimiterProcessor
import org.commonmark.parser.delimiter.DelimiterRun

class ColorDelimiterProcessor : DelimiterProcessor {
    companion object {
        val whitespaceRegex = Regex("[\u0000-\u001F\u007F-\u009F \u00A0\u1680\u2000-\u200A\u2028\u2029\u202F\u205F\u3000]")
        val colorRegex = Regex("#?([0-9a-fA-F]{3}|[0-9a-fA-F]{6})")
        val colors = hashMapOf(
            "red" to (0xFFD32F2F).toInt(),
            "pink" to (0xFFC2185B).toInt(),
            "purple" to (0xFF7B1FA2).toInt(),
            "indigo" to (0xFF303F9F).toInt(),
            "blue" to (0xFF1976D2).toInt(),
            "cyan" to (0xFF0097A7).toInt(),
            "teal" to (0xFF00796B).toInt(),
            "green" to (0xFF388E3C).toInt(),
            "lime" to (0xFF689F38).toInt(),
            "yellow" to (0xFFFBC02D).toInt(),
            "amber" to (0xFFFFA000).toInt(),
            "orange" to (0xFFF57C00).toInt(),
            "brown" to (0xFF5D4037).toInt(),
            "grey" to (0xFF616161).toInt(),
            "gray" to (0xFF616161).toInt(),
            "campfire" to (0xFFFF6D00).toInt(),
            "bonfire" to (0xFFFF6D00).toInt(),
        )
    }

    override fun getOpeningCharacter(): Char = '{'
    override fun getClosingCharacter(): Char = '}'
    override fun getMinLength(): Int = 1

    override fun getDelimiterUse(opener: DelimiterRun, closer: DelimiterRun): Int {
        return if (opener.length() == 1 && closer.length() == 1) 1
        else 0
    }

    override fun process(opener: Text, closer: Text, delimiterUse: Int) {
        val node = ColorNode()

        var tmp = opener.next
        if (tmp is Text && tmp !== closer) {
            val colorDelimiter = whitespaceRegex.find(tmp.literal)
            if (colorDelimiter == null) {
                opener.insertAfter(Text("{"))
                closer.insertBefore(Text("}"))
                return
            }

            val colorText = tmp.literal.substring(0, colorDelimiter.range.first)
            val colors = colorText
                // split multiple colors for gradients
                .split(':')
                .map { color ->
                    val named = colors[color.lowercase()]
                    if (named != null) return@map named

                    // if no named color matches, try hex
                    val match = colorRegex.matchEntire(color)
                    if (match == null) {
                        opener.insertAfter(Text("{"))
                        closer.insertBefore(Text("}"))
                        return
                    }
                    var colorHex = match.groupValues[1]

                    if (colorHex.length == 3) {
                        // if hex in short format (#fff), convert to full
                        colorHex = "${colorHex[0]}${colorHex[0]}" +
                                "${colorHex[1]}${colorHex[1]}" +
                                "${colorHex[2]}${colorHex[2]}"
                    }

                    // finally, convert hex into android int color
                    colorHex.toInt(16) or 0xFF000000.toInt()
                }
            if (colors.isEmpty()) {
                opener.insertAfter(Text("{"))
                closer.insertBefore(Text("}"))
                return
            }

            node.color = colors[0]
            if (colors.size > 1) {
                node.colorList = colors.toTypedArray()
            }

            tmp.literal = tmp.literal.substring(colorDelimiter.range.first + 1)
        } else {
            opener.insertAfter(Text("{"))
            closer.insertBefore(Text("}"))
            return
        }

        while (tmp != null && tmp !== closer) {
            val next = tmp.next
            node.appendChild(tmp)
            tmp = next
        }

        opener.insertAfter(node)
    }
}
