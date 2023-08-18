package com.sayzen.campfiresdk.screens.post.create.creators

import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.ImageView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.post.Page
import com.dzen.campfire.api.models.publications.post.PageLinkImage
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.controllers.ControllerMention
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.cards.post_pages.CardPage
import com.sayzen.campfiresdk.models.cards.post_pages.CardPageLinkImage
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsBitmap
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.screens.SCrop
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.splash.Splash
import com.sup.dev.android.views.splash.SplashChooseImage
import com.sup.dev.java.tools.ToolsText
import com.sup.dev.java.tools.ToolsThreads

class SplashPageLinkImage(
        private val requestPutPage:(page: Page, screen: Screen?, splash: Splash?, mapper: (Page) -> CardPage, onFinish: ((CardPage)->Unit))->Unit,
        private val requestChangePage: (page: Page, card: CardPage, screen: Screen?, splash: Splash?, (Page)->Unit) -> Unit,
        private val card: CardPage?,
        private val oldPage: PageLinkImage?
) : Splash(R.layout.screen_post_create_link_image) {

    private val vLink: SettingsField = findViewById(R.id.vLink)
    private val vEnter: Button = findViewById(R.id.vEnter)
    private val vCancel: Button = findViewById(R.id.vCancel)
    private val vImage: ImageView = findViewById(R.id.vImage)
    private val vImageIcon: View = findViewById(R.id.vImageIcon)

    private var image: ByteArray? = null


    init {
        vLink.setHint(t(API_TRANSLATE.post_create_link))
        vCancel.text = t(API_TRANSLATE.app_cancel)
        vEnter.text = t(API_TRANSLATE.app_create)

        ControllerMention.startFor(vLink.vField, false)
        vLink.vField.setSingleLine(true)
        vLink.vField.imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
        vLink.vField.addTextChangedListener(TextWatcherChanged { update() })
        vImage.setOnClickListener { chooseImage() }

        var enterText = t(API_TRANSLATE.app_create)

        if (oldPage != null) {
            enterText = t(API_TRANSLATE.app_change)
            vLink.setText(this.oldPage.link)
            ImageLoader.load(oldPage.imageId).into(vImage)
            vImageIcon.visibility = View.GONE
        }

        vEnter.setText(enterText)
        vEnter.setOnClickListener { onEnter() }
        vCancel.setOnClickListener { onCancel() }

        update()
    }

    private fun chooseImage() {
        hide()
        SplashChooseImage()
                .setOnSelectedBitmap { _, b ->
                    Navigator.to(SCrop(b, API.PAGE_LINK_IMAGE_W, API.PAGE_LINK_IMAGE_H) { screen, b2, _, _, _, _ ->
                        this.image = ToolsBitmap.toBytes(ToolsBitmap.resize(b2, API.PAGE_LINK_IMAGE_W, API.PAGE_LINK_IMAGE_H), API.PAGE_LINK_IMAGE_WEIGHT)
                        vImage.setImageBitmap(b2)
                        vImageIcon.visibility = View.GONE
                        screen.back()
                        update()
                    }
                            .setAutoBackOnCrop(false)
                            .setOnHide { ToolsThreads.main(100) { asSheetShow() }}
                    )
                }
                .asSheetShow()
    }

    private fun update() {
        vEnter.isEnabled = (vLink.getText().isNotEmpty()
                && vLink.getText().length <= API.PAGE_LINK_WEB_MAX_L
                && (ToolsText.isWebLink(vLink.getText()) || ControllerLinks.isCorrectLink(vLink.getText()))
                && (oldPage != null || image != null))
    }

    private fun onEnter() {
        val page = PageLinkImage()
        page.link = vLink.getText().trim { it <= ' ' }
        page.insertBytes = image

        hide()
        val w = ToolsView.showProgressDialog()
        if (card == null)
            requestPutPage.invoke(page, null, w, { page1 -> CardPageLinkImage(null, page1 as PageLinkImage) }){}
        else {
            requestChangePage.invoke(page, card, null, w) {}
        }

    }

    private fun onCancel() {
        if (notChanged())
            hide()
        else
            SplashAdd.showConfirmCancelDialog(this)
    }


    private fun notChanged(): Boolean {
        val link = vLink.getText()
        return if (oldPage == null) {
            link.isEmpty() && image == null
        } else {
            ToolsText.equals(oldPage.link, link) && image == null
        }
    }

    override fun onShow() {
        super.onShow()
        ToolsView.showKeyboard(vLink.vField)
    }

    override fun onHide() {
        super.onHide()
        ToolsView.hideKeyboard()
        ControllerMention.hide()
    }

    override fun onTryCancelOnTouchOutside(): Boolean {
        onCancel()
        return false
    }

    override fun setEnabled(enabled: Boolean): Splash {
        super.setEnabled(enabled)
        vLink.isEnabled = enabled
        vEnter.isEnabled = enabled
        vCancel.isEnabled = enabled
        vImage.isEnabled = enabled
        return this
    }
}
