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
import org.commonmark.ext.autolink.AutolinkExtension
import org.commonmark.parser.Parser
import sh.sit.bonfire.formatting.core.bfm.color.ColorExtension
import sh.sit.bonfire.formatting.core.bfm.color.ColorNode

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

class BFMCorePlugin(val inlineOnly: Boolean) : CorePlugin() {
    companion object {
        @JvmStatic
        fun create(inlineOnly: Boolean): BFMCorePlugin = BFMCorePlugin(inlineOnly)

        private val textWidthProp = Prop.of<Float>("bfm-text-width")
        private val colorProp = Prop.of<Int>("bfm-color")
        private val colorListProp = Prop.of<Array<Int>?>("bfm-color-list")
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
                GradientColorSpan(textWidthProp.get(props) ?: 100f, colorList.toIntArray())
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
