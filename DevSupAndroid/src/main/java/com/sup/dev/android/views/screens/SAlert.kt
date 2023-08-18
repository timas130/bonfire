package com.sup.dev.android.views.screens

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.sup.dev.android.R
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.libs.image_loader.ImageLink
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.NavigationAction
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView

class SAlert(
        title: String?,
        text: String?,
        action: String?,
        var onAction: ((SAlert) -> Unit)?
) : Screen(R.layout.screen_alert) {

    companion object {

        var GLOBAL_SHOW_WHOOPS = true

        fun showNetwork(action: NavigationAction, onRetry: (SAlert) -> Unit) {
            Navigator.action(action, instanceNetwork(onRetry))
        }

        fun instanceNetwork(onRetry: (SAlert) -> Unit) = instanceMessage(SupAndroid.TEXT_ERROR_NETWORK, SupAndroid.TEXT_APP_RETRY, SupAndroid.imgErrorNetwork, true, onRetry)

        fun showGone(action: NavigationAction, goneText:String) {
            Navigator.action(action, instanceGone(goneText))
        }

        fun instanceGone(goneText:String) = instanceMessage(goneText, SupAndroid.TEXT_APP_BACK, SupAndroid.imgErrorGone, true) { Navigator.remove(it) }

        fun showMessage(text: Int, action: Int, img: ImageLink?, actionNavigation: NavigationAction, onAction: ((SAlert) -> Unit)? = { Navigator.remove(it) }) {
            Navigator.action(actionNavigation, instanceMessage(ToolsResources.s(text), ToolsResources.s(action), img, true, onAction))
        }

        fun showMessage(text: Int, action: String, img: ImageLink?, actionNavigation: NavigationAction, onAction: ((SAlert) -> Unit)? = { Navigator.remove(it) }) {
            Navigator.action(actionNavigation, instanceMessage(ToolsResources.s(text), action, img, true, onAction))
        }

        fun showMessage(text: String?, action: String?, img: ImageLink?, actionNavigation: NavigationAction, onAction: ((SAlert) -> Unit)? = { Navigator.remove(it) }) {
            Navigator.action(actionNavigation, instanceMessage(text, action, img, true, onAction))
        }

        fun instanceMessage(text: String?, action: String?, img: ImageLink?, tryToShowWhoops: Boolean, onAction: ((SAlert) -> Unit)?): SAlert {
            return instanceMessage(text, action, tryToShowWhoops, onAction).setImage(img)
        }

        fun instanceMessage(text: String?, action: String?, tryToShowWhoops: Boolean, onAction: ((SAlert) -> Unit)?): SAlert {
            val screen = SAlert(
                    if (tryToShowWhoops && GLOBAL_SHOW_WHOOPS) SupAndroid.TEXT_APP_WHOOPS else null,
                    text,
                    action
            ) { Navigator.back() }
            screen.onAction = onAction
            screen.isNavigationVisible = false
            screen.isNavigationAllowed = false
            screen.isNavigationAnimation = false
            screen.navigationBarColor = ToolsResources.getColorAttr(R.attr.colorPrimarySurface)
            screen.statusBarColor = ToolsResources.getColorAttr(R.attr.colorPrimarySurface)
            return screen
        }

    }

    private val vTitle: TextView = findViewById(R.id.vTitle)
    private val vText: TextView = findViewById(R.id.vText)
    private val vAction: TextView = findViewById(R.id.vAction)
    private val vImage: ImageView = findViewById(R.id.vImage)
    private val vImageFull: ImageView = findViewById(R.id.vImageFull)

    private var imageLoader: ImageLink? = null
    private var imageLoaderFull: ImageLink? = null

    init {

        ToolsView.setTextOrGone(vTitle, title)
        vText.text = text
        vAction.text = action

        vAction.setOnClickListener { onAction?.invoke(this) }

        updateImage()

    }

    fun updateImage(){
        if (imageLoader != null) {
            imageLoader?.into(vImage)
            vImage.visibility = View.VISIBLE
        } else {
            vImage.setImageBitmap(null)
            vImage.visibility = View.GONE
        }

        if (imageLoaderFull != null) {
            imageLoaderFull?.into(vImageFull)
            vImageFull.visibility = View.VISIBLE
        } else {
            vImageFull.setImageBitmap(null)
            vImageFull.visibility = View.GONE
        }

    }

    fun setImage(imageLoader: ImageLink?, imageLoaderFull: ImageLink? = null): SAlert {
        this.imageLoader = imageLoader
        this.imageLoaderFull = imageLoaderFull
        updateImage()
        return this
    }

}