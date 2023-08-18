package com.sup.dev.android.views.cards

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sup.dev.android.R
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.image_loader.ImageLink
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.screens.SLoading
import com.sup.dev.java.tools.ToolsThreads

abstract class CardScreenLoading(@LayoutRes layoutRes: Int) : Card(0) {

    enum class State {
        NONE, EMPTY, PROGRESS, ERROR
    }

    protected val vRoot: View = ToolsView.inflate(R.layout.screen_loading)
    protected var vFab: FloatingActionButton = vRoot.findViewById(R.id.vFab)
    private val vAction: Button = vRoot.findViewById(R.id.vAction)
    private var vEmptySubContainer: ViewGroup? = null
    private val vMessage: TextView = vRoot.findViewById(R.id.vMessage)
    private val vProgress: ProgressBar = vRoot.findViewById(R.id.vProgress)
    private val vProgressLine: ProgressBar = vRoot.findViewById(R.id.vProgressLine)
    private val vEmptyImage: ImageView = vRoot.findViewById(R.id.vEmptyImage)
    private val vContainer: ViewGroup = vRoot.findViewById(R.id.vContainer)
    private val vMessageContainer: ViewGroup = vRoot.findViewById(R.id.vMessageContainer)
    private val vEmptyContainer: ViewGroup = vRoot.findViewById(R.id.vEmptyContainer)

    protected var textErrorNetwork = SupAndroid.TEXT_ERROR_NETWORK
    protected var textRetry = SupAndroid.TEXT_APP_RETRY
    protected var textEmptyS: String? = null
    protected var textProgressS: String? = null
    protected var image: ImageLink? = null
    protected var imageError: ImageLink? = SupAndroid.imgErrorNetwork
    protected var textProgressAction: String? = null
    protected var onProgressAction: (() -> Unit)? = null
    protected var textAction: String? = null
    protected var onAction: (() -> Unit)? = null
    protected var stateS: State = State.NONE
    private var setStateKey = 0L

    init {

        (vFab as View).visibility = View.GONE
        vAction.visibility = View.INVISIBLE
        vMessage.visibility = View.INVISIBLE
        vProgress.visibility = View.INVISIBLE
        vProgressLine.visibility = View.INVISIBLE
        vEmptyImage.visibility = View.GONE

        setState(State.PROGRESS)
        setContent(layoutRes)
    }

    abstract fun onReloadClicked()

    override fun instanceView() = vRoot

    protected fun setContent(@LayoutRes res: Int) {
        vContainer.addView(ToolsView.inflate(vRoot.context, res), 0)
        vEmptySubContainer = vContainer.findViewById(R.id.vEmptySubContainer)

        if (vEmptySubContainer != null) {
            (vEmptyContainer.parent as ViewGroup).removeView(vEmptyContainer)
            vEmptySubContainer!!.addView(vEmptyContainer)
        }
    }

    protected fun setTextErrorNetwork(@StringRes t: Int) {
        textErrorNetwork = ToolsResources.s(t)
    }

    protected fun setTextRetry(@StringRes t: Int) {
        textRetry = ToolsResources.s(t)
    }

    protected fun setTextEmpty(@StringRes t: Int) {
        setTextEmpty(ToolsResources.s(t))
    }

    protected fun setTextEmpty(t: String?) {
        textEmptyS = t
    }

    protected fun setAction(@StringRes textAction: Int, onAction: () -> Unit) {
        setAction(ToolsResources.s(textAction), onAction)
    }

    protected fun clearAction() {
        this.textAction = null
        this.onAction = null
    }

    fun setTextProgress(@StringRes textProgress: Int) {
        setTextProgress(ToolsResources.s(textProgress))
    }

    fun setTextProgress(textProgress: String) {
        this.textProgressS = textProgress
    }

    fun setProgressAction(textProgressAction: String, onAction: () -> Unit) {
        this.textProgressAction = textProgressAction
        this.onProgressAction = onAction
    }

    fun setProgressAction(textProgressAction: String) {
        this.textProgressAction = textProgressAction
    }

    protected fun setAction(textAction: String?, onAction: () -> Unit) {
        this.textAction = textAction
        this.onAction = onAction
    }

    fun setBackgroundImage(image: Any) {
        setBackgroundImage(ImageLoader.loadByAny(image))
    }

    fun setBackgroundImage(image: ImageLink?) {
        this.image = image
    }

    fun setState(state: State) {
        //  Защита от мерцаний
        val key = System.currentTimeMillis()
        setStateKey = key
        if (state == State.EMPTY) {
            ToolsThreads.main(50) { if (setStateKey == key) setStateNow(state) }
        } else {
            setStateNow(state)
        }
    }

    private fun setStateNow(state: State) {
        this.stateS = state

        if (state == State.PROGRESS) {
            ToolsThreads.main(600) {
                vProgress.visibility = if (this.stateS != State.PROGRESS || textProgressS != null) View.GONE else View.VISIBLE
                vProgressLine.visibility = if (this.stateS != State.PROGRESS || textProgressS == null) View.GONE else View.VISIBLE
                if (this.stateS == State.PROGRESS && vMessage.text.isNotEmpty()) {
                    vMessage.visibility = View.VISIBLE
                }
            }
        } else {
            vProgress.visibility = View.GONE
            vProgressLine.visibility = View.GONE
        }

        if (state == State.EMPTY && image != null) {
            image?.into(vEmptyImage) {}
            ToolsView.fromAlpha(vEmptyImage)
        } else if (state == State.ERROR && imageError != null) {
            imageError?.into(vEmptyImage)
            ToolsView.fromAlpha(vEmptyImage)
        } else {
            vEmptyImage.setImageBitmap(null)
            vEmptyImage.visibility = View.GONE
        }

        if (state == State.ERROR) {
            vMessage.text = textErrorNetwork
            vAction.text = textRetry
            vMessage.visibility = if (vMessage.text.isEmpty()) View.GONE else View.VISIBLE
            vAction.visibility = if (vAction.text.isEmpty()) View.GONE else View.VISIBLE

            vAction.setOnClickListener { onReloadClicked() }
        }

        if (state == State.EMPTY) {
            vMessage.text = textEmptyS
            vAction.text = textAction
            vMessage.visibility = if (vMessage.text.isEmpty()) View.GONE else View.VISIBLE
            vAction.visibility = if (vAction.text.isEmpty()) View.GONE else View.VISIBLE

            vAction.setOnClickListener { if (onAction != null) onAction!!.invoke() }
        }

        if (state == State.PROGRESS) {
            vMessage.visibility = View.GONE
            vMessage.text = textProgressS
            vAction.text = textProgressAction
            vAction.setOnClickListener { if (onProgressAction != null) onProgressAction!!.invoke() }
        }

        if (state == State.NONE) {
            vMessage.visibility = View.GONE
            vAction.visibility = View.GONE
        }
    }

}
