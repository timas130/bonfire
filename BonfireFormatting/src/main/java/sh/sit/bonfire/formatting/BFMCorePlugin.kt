package sh.sit.bonfire.formatting

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.text.TextPaint
import android.text.style.CharacterStyle
import android.text.style.ForegroundColorSpan
import android.text.style.UpdateAppearance
import io.noties.markwon.MarkwonSpansFactory
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.Prop
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.core.MarkwonTheme
import io.noties.prism4j.annotations.PrismBundle
import org.commonmark.ext.autolink.AutolinkExtension
import org.commonmark.node.CustomNode
import org.commonmark.node.Delimited
import org.commonmark.node.Text
import org.commonmark.parser.Parser
import org.commonmark.parser.delimiter.DelimiterProcessor
import org.commonmark.parser.delimiter.DelimiterRun

class GradientColorSpan(
    private val width: Float,
    private val colors: IntArray,
) : CharacterStyle(), UpdateAppearance {
    override fun updateDrawState(tp: TextPaint) {
        tp.shader = LinearGradient(
            0f, 0f, width, 0f,
            colors, null,
            Shader.TileMode.CLAMP
        )
    }
}

class ColorNode : CustomNode(), Delimited {
    var color = Color.RED
    var colorList: IntArray? = null

    override fun getOpeningDelimiter(): String = "{"
    override fun getClosingDelimiter(): String = "}"
}

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
                .split(':')
                .map { color ->
                    val named = colors[color]
                    if (named != null) return@map named

                    val match = colorRegex.matchEntire(color)
                    if (match == null) {
                        opener.insertAfter(Text("{"))
                        closer.insertBefore(Text("}"))
                        return
                    }
                    var colorHex = match.groupValues[1]
                    if (colorHex.length == 3) {
                        colorHex = "${colorHex[0]}${colorHex[0]}" +
                                "${colorHex[1]}${colorHex[1]}" +
                                "${colorHex[2]}${colorHex[2]}"
                    }

                    Color.parseColor("#$colorHex")
                }
            if (colors.isEmpty()) {
                opener.insertAfter(Text("{"))
                closer.insertBefore(Text("}"))
                return
            }

            node.color = colors[0]
            if (colors.size > 1) {
                node.colorList = colors.toIntArray()
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

class ColorExtension : Parser.ParserExtension {
    override fun extend(parserBuilder: Parser.Builder) {
        parserBuilder.customDelimiterProcessor(ColorDelimiterProcessor())
    }
}

@PrismBundle(include = ["json"], grammarLocatorClassName = ".BFMGrammarLocator")
class BFMCorePlugin(val inlineOnly: Boolean) : CorePlugin() {
    companion object {
        @JvmStatic
        fun create(inlineOnly: Boolean): BFMCorePlugin = BFMCorePlugin(inlineOnly)

        private val textWidthProp = Prop.of<Float>("bfm-text-width")
        private val colorProp = Prop.of<Int>("bfm-color")
        private val colorListProp = Prop.of<IntArray?>("bfm-color-list")
    }

    override fun configureParser(builder: Parser.Builder) {
        builder.extensions(setOf(ColorExtension(), AutolinkExtension.create()))
        if (inlineOnly) {
            builder.enabledBlockTypes(setOf())
        }
    }

    override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
        builder.setFactory(ColorNode::class.java) { _, props ->
            val colorList = colorListProp.get(props)
            if (colorList != null) {
                GradientColorSpan(textWidthProp.get(props) ?: 100f, colorList)
            } else {
                ForegroundColorSpan(colorProp.get(props) ?: Color.RED)
            }
        }
    }

    override fun configureVisitor(builder: MarkwonVisitor.Builder) {
        builder.on(ColorNode::class.java) { visitor, node ->
            val length = visitor.length()
            visitor.visitChildren(node)
            colorProp.set(visitor.renderProps(), node.color)
            colorListProp.set(visitor.renderProps(), node.colorList)
            textWidthProp.set(visitor.renderProps(), (visitor.length() - length).toFloat() * 20)
            visitor.setSpansForNodeOptional(node, length)
        }
    }

    override fun configureTheme(builder: MarkwonTheme.Builder) {
        builder
            .headingBreakHeight(0)
            .bulletWidth(15)
    }
}
