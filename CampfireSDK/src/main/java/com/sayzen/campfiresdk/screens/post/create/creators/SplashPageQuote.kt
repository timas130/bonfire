package com.sayzen.campfiresdk.screens.post.create.creators

import android.view.inputmethod.EditorInfo
import android.widget.Button

import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.post.Page
import com.dzen.campfire.api.models.publications.post.PageQuote
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.cards.post_pages.CardPage
import com.sayzen.campfiresdk.models.cards.post_pages.CardPageQuote
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.settings.SettingsField
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.splash.Splash
import com.sup.dev.java.tools.ToolsText

class SplashPageQuote(
        private val requestPutPage:(page: Page, screen: Screen?, splash: Splash?, mapper: (Page) -> CardPage, onFinish: ((CardPage)->Unit))->Unit,
        private val requestChangePage: (page: Page, card: CardPage, screen: Screen?, splash: Splash?, (Page)->Unit) -> Unit,
        private val card: CardPage?,
        private val oldPage: PageQuote?
) : Splash(R.layout.screen_post_create_quote) {

    private val vAuthor: SettingsField = findViewById(R.id.vAuthor)
    private val vText: SettingsField = findViewById(R.id.vText)
    private val vEnter: Button = findViewById(R.id.vEnter)
    private val vCancel: Button = findViewById(R.id.vCancel)

    init {

        vText.setHint(t(API_TRANSLATE.post_create_text))
        vAuthor.setHint(t(API_TRANSLATE.post_create_quote))
        vCancel.text = t(API_TRANSLATE.app_cancel)
        vEnter.text = t(API_TRANSLATE.app_create)

        vAuthor.vField.imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
        vAuthor.vField.addTextChangedListener(TextWatcherChanged { update() })

        vText.vField.setSingleLine(false)
        vText.vField.imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
        vText.vField.addTextChangedListener(TextWatcherChanged {  update() })

        vEnter.isEnabled = false

        var enterText = t(API_TRANSLATE.app_create)

        if (oldPage != null) {
            enterText = t(API_TRANSLATE.app_change)
            vAuthor.setText(oldPage.author)
            vText.setText(oldPage.text)
            vAuthor.vField.setSelection(vAuthor.getText().length)
        }

        vEnter.setText(enterText)
        vEnter.setOnClickListener { onEnter() }
        vCancel.setOnClickListener { onCancel() }
    }

    private fun update() {
        vEnter.isEnabled = !vText.getText().isEmpty()
                && vAuthor.getText().length <= API.PAGE_QUOTE_AUTHOR_MAX_L
                && vText.getText().length <= API.PAGE_QUOTE_TEXT_MAX_L
    }

    private fun onEnter() {
        val page = PageQuote()
        page.author = vAuthor.getText().trim { it <= ' ' }
        page.text = vText.getText().trim { it <= ' ' }

        if (card == null)
            requestPutPage.invoke(page, null, this, { page1 -> CardPageQuote(null, page1 as PageQuote) }){}
        else
            requestChangePage.invoke(page, card, null, this){}

    }

    private fun onCancel() {
        if (notChanged())
            hide()
        else
            SplashAdd.showConfirmCancelDialog(this)
    }


    private fun notChanged(): Boolean {
        val author = vAuthor.getText()
        val text = vText.getText()
        return if (oldPage == null)
            author.isEmpty() && text.isEmpty()
        else
            ToolsText.equals(oldPage.author, author) && ToolsText.equals(oldPage.text, text)
    }

    override fun onShow() {
        super.onShow()
        ToolsView.showKeyboard(vText)
    }

    override fun onHide() {
        super.onHide()
        ToolsView.hideKeyboard()
    }

    override fun onTryCancelOnTouchOutside(): Boolean {
        onCancel()
        return false
    }

    override fun setEnabled(enabled: Boolean): Splash {
        super.setEnabled(enabled)
        vAuthor.isEnabled = enabled
        vText.isEnabled = enabled
        vEnter.isEnabled = enabled
        vCancel.isEnabled = enabled
        return this
    }
}
