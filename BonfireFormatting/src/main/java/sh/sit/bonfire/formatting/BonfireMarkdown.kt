package sh.sit.bonfire.formatting

import android.content.Context
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.UnderlineSpan
import android.widget.EditText
import android.widget.TextView
import com.posthog.PostHog
import io.noties.markwon.Markwon
import io.noties.markwon.SoftBreakAddsNewLinePlugin
import io.noties.markwon.editor.MarkwonEditor
import io.noties.markwon.editor.handler.EmphasisEditHandler
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.simple.ext.SimpleExtPlugin
import me.saket.bettermovementmethod.BetterLinkMovementMethod
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

object BonfireMarkdown {
    private fun createMarkwon(context: Context, inlineOnly: Boolean = false, shortenLinks: Boolean = false): Markwon {
        return Markwon.builder(context)
            .usePlugin(BFMCorePlugin.create(inlineOnly, shortenLinks))
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
        markwon = createMarkwon(context, shortenLinks = true)
        markwonInline = createMarkwon(context, inlineOnly = true, shortenLinks = true)
    }

    fun setMarkdown(view: TextView, text: String) {
        val start = System.currentTimeMillis()
        try {
            markwon.setMarkdown(view, text)
        } catch (e: StackOverflowError) {
            view.text = text
        }

        PostHog.capture("markdown_parse", properties = mapOf(
            "length" to text.length,
            "time_taken" to (System.currentTimeMillis() - start)
        ))

        view.post {
            view.movementMethod = BetterLinkMovementMethod.getInstance()
        }
    }

    fun setMarkdownInline(view: TextView, text: String) {
        try {
            markwonInline.setMarkdown(view, text)
        } catch (e: StackOverflowError) {
            view.text = text
        }
        view.post {
            view.movementMethod = BetterLinkMovementMethod.getInstance()
        }
    }

    private fun createEditorExecutor(): ExecutorService {
        return ThreadPoolExecutor(1, 1, 60L, TimeUnit.SECONDS, LinkedBlockingQueue<Runnable>()).apply {
            allowCoreThreadTimeOut(true)
        }
    }

    private class PreRenderTextWatcher(
        private val editor: MarkwonEditor,
        private val executor: ExecutorService,
        private val view: EditText,
    ) : TextWatcher {
        private var generation = 0
        private var selfChange = false
        private var future: Future<*>? = null

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable) {
            if (selfChange) return

            val key = ++generation
            future?.cancel(true)

            val snapshot = SpannableStringBuilder(s)
            future = executor.submit(Runnable {
                try {
                    editor.preRender(snapshot) { result ->
                        view.post {
                            if (key == generation) {
                                selfChange = true
                                try {
                                    result.dispatchTo(view.text)
                                } finally {
                                    selfChange = false
                                }
                            }
                        }
                    }
                } catch (t: Throwable) {
                    view.post { throw RuntimeException(t) }
                }
            })
        }
    }

    fun getEditorTextChangedListener(view: EditText): TextWatcher {
        val editor = createEditor(view.context, createMarkwon(view.context))
        return PreRenderTextWatcher(editor, createEditorExecutor(), view)
    }

    fun getInlineEditorTextChangedListener(view: EditText): TextWatcher {
        val editor = createEditor(view.context, createMarkwon(view.context, inlineOnly = true))
        return PreRenderTextWatcher(editor, createEditorExecutor(), view)
    }
}
