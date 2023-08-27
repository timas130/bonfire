package sh.sit.bonfire.formatting

import android.content.Context
import android.text.TextWatcher
import android.text.style.UnderlineSpan
import android.widget.EditText
import android.widget.TextView
import io.noties.markwon.Markwon
import io.noties.markwon.SoftBreakAddsNewLinePlugin
import io.noties.markwon.editor.MarkwonEditor
import io.noties.markwon.editor.MarkwonEditorTextWatcher
import io.noties.markwon.editor.handler.EmphasisEditHandler
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.simple.ext.SimpleExtPlugin
import me.saket.bettermovementmethod.BetterLinkMovementMethod
import java.util.concurrent.Executors

object BonfireMarkdown {
    private fun createMarkwon(context: Context, inlineOnly: Boolean = false): Markwon {
        return Markwon.builder(context)
            .usePlugin(BFMCorePlugin.create(inlineOnly))
            .usePlugin(TaskListPlugin.create(context))
            .usePlugin(SoftBreakAddsNewLinePlugin.create())
            .usePlugin(StrikethroughPlugin())
            .usePlugin(SimpleExtPlugin.create { plugin ->
                plugin.addExtension(2, '_') { _, _ -> UnderlineSpan() }
            })
            .usePlugin(HtmlPlugin.create())
            .build()
    }

    private fun createEditor(context: Context, markwon: Markwon = createMarkwon(context)): MarkwonEditor {
        return MarkwonEditor.builder(markwon)
            .useEditHandler(EmphasisEditHandler())
            .useEditHandler(StrongEmphasisEditHandler())
            .useEditHandler(StrikethroughEditHandler())
            .useEditHandler(UnderlineEditHandler())
            .useEditHandler(CodeEditHandler())
            .useEditHandler(BlockQuoteEditHandler())
            .useEditHandler(ColorEditHandler())
            .useEditHandler(HeadingEditHandler())
            .build()
    }

    private lateinit var markwon: Markwon
    private lateinit var markwonInline: Markwon

    fun init(context: Context) {
        markwon = createMarkwon(context)
        markwonInline = createMarkwon(context, inlineOnly = true)
    }

    fun setMarkdown(view: TextView, text: String) {
        markwon.setMarkdown(view, text)
        view.post {
            view.movementMethod = BetterLinkMovementMethod.getInstance()
        }
    }

    fun setMarkdownInline(view: TextView, text: String) {
        markwonInline.setMarkdown(view, text)
    }

    fun getEditorTextChangedListener(view: EditText): TextWatcher {
        val editor = createEditor(view.context, markwon)
        return MarkwonEditorTextWatcher.withPreRender(
            editor, Executors.newCachedThreadPool(), view
        )
    }

    fun getInlineEditorTextChangedListener(view: EditText): TextWatcher {
        val editor = createEditor(view.context, markwonInline)
        return MarkwonEditorTextWatcher.withPreRender(
            editor, Executors.newCachedThreadPool(), view
        )
    }
}
