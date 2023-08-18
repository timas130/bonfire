package com.sayzen.campfiresdk.screens.post.create.creators

import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.post.Page
import com.dzen.campfire.api.models.publications.post.PageCode
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.cards.post_pages.CardPage
import com.sayzen.campfiresdk.models.cards.post_pages.CardPageCode
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.splash.Splash
import com.sup.dev.android.views.splash.SplashMenu
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.java.tools.ToolsText

class SCreatePageCode(
        private val requestPutPage: (page: Page, screen: Screen?, splash: Splash?, mapper: (Page) -> CardPage, onFinish: ((CardPage) -> Unit)) -> Unit,
        private val requestChangePage: (page: Page, card: CardPage, screen: Screen?, splash: Splash?, (Page) -> Unit) -> Unit,
        private val card: CardPage?,
        private val oldPage: PageCode?
) : Screen(R.layout.screen_post_create_code) {
    private val vFab: FloatingActionButton = findViewById(R.id.vFab)
    private val vField: EditText = findViewById(R.id.vField)
    private val vCodeLanguage: ViewIcon = findViewById(R.id.vCodeLanguage)
    private val vCodeLanguageText: TextView = findViewById(R.id.vCodeLanguageText)

    private var _language: String = oldPage?.language ?: "c"
    private var language: String
        get() = _language
        set(value) {
            _language = value
            vCodeLanguageText.text = API_TRANSLATE.post_page_code_languages[value] ?: "C"
        }

    init {
        disableShadows()
        disableNavigation()
        setTitle(t(API_TRANSLATE.post_page_code))

        vField.hint = t(API_TRANSLATE.post_page_text_hint)
        vField.isSingleLine = false
        vField.imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
        vField.gravity = Gravity.TOP
        vField.addTextChangedListener(TextWatcherChanged { update() })
        vField.setText(oldPage?.code ?: "")

        vCodeLanguage.setOnClickListener {
            val menu = SplashMenu()
            for (language1 in API_TRANSLATE.post_page_code_languages) {
                menu.add(language1.value) { language = language1.key }
            }
            menu.asSheetShow()
        }

        language = _language

        vFab.setOnClickListener { onEnter() }
    }

    private fun onEnter() {
        val page = PageCode()
        page.code = vField.text.toString()
        page.language = language
        if (oldPage == null)
            requestPutPage.invoke(page, this, ToolsView.showProgressDialog(), { page1 ->
                CardPageCode(null, page1 as PageCode)
            }) {}
        else
            requestChangePage.invoke(page, card!!, this, ToolsView.showProgressDialog()) {}
    }

    private fun update() {
        val s = vField.text.toString()

        ToolsView.setFabEnabledR(vFab, s.isNotEmpty() && s.length < API.PAGE_TEXT_MAX_L, R.color.green_700)
    }

    override fun onBackPressed(): Boolean {
        if (notChanged()) return false

        SplashAdd.showConfirmCancelDialog(this)
        return true
    }

    private fun notChanged(): Boolean {
        val s = vField.text.toString()
        return if (oldPage == null) s.isBlank()
        else ToolsText.equals(oldPage.code, s)
    }
}