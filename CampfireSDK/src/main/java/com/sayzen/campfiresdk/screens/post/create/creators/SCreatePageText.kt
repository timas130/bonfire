package com.sayzen.campfiresdk.screens.post.create.creators

import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.post.Page
import com.dzen.campfire.api.models.publications.post.PageText
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.controllers.ControllerMention
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.cards.post_pages.CardPage
import com.sayzen.campfiresdk.models.cards.post_pages.CardPageText
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.support.watchers.TextWatcherChanged
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.android.views.splash.Splash
import com.sup.dev.android.views.splash.SplashFieldTwo
import com.sup.dev.android.views.splash.SplashGreed
import com.sup.dev.java.tools.ToolsText

class SCreatePageText(
        private val requestPutPage: (page: Page, screen: Screen?, splash: Splash?, mapper: (Page) -> CardPage, onFinish: ((Card) -> Unit)) -> Unit,
        private val requestChangePage: (page: Page, card: CardPage, screen: Screen?, splash: Splash?, (Page) -> Unit) -> Unit,
        private val card: CardPage?,
        private val oldPage: PageText?
) : Screen(R.layout.screen_post_create_text) {

    private val vField: EditText = findViewById(R.id.vField)
    private val vFab: FloatingActionButton = findViewById(R.id.vFab)
    private val vTextTitle: ViewIcon = findViewById(R.id.vTextTitle)
    private val vIconAttach: ViewIcon = findViewById(R.id.vIconAttach)
    private val vLink: ViewIcon = findViewById(R.id.vLink)
    private val vAlignLeft: ViewIcon = findViewById(R.id.vAlignLeft)
    private val vAlignCenter: ViewIcon = findViewById(R.id.vAlignCenter)
    private val vAlignRight: ViewIcon = findViewById(R.id.vAlignRight)

    private var size = PageText.SIZE_0
    private var align = PageText.ALIGN_LEFT
    private var icon = 0

    private val maxL: Int
        get() = if (size == PageText.SIZE_0) API.PAGE_TEXT_MAX_L else API.PAGE_TEXT_TITLE_MAX_L

    init {
        disableShadows()
        disableNavigation()
        setTitle(t(API_TRANSLATE.post_page_text))
        vField.setHint(t(API_TRANSLATE.post_page_text_hint))

        vField.setSingleLine(false)
        vField.imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
        vField.gravity = Gravity.TOP
        vField.addTextChangedListener(TextWatcherChanged { update() })
        ControllerMention.startFor(vField)

        vTextTitle.setOnClickListener {
            if (vTextTitle.isIconSelected)
                onTextSizeClicked(PageText.SIZE_0)
            else
                onTextSizeClicked(PageText.SIZE_1)
        }


        if (oldPage != null) {
            vField.setText(this.oldPage.text)
            vField.setSelection(vField.text.length)
            onTextSizeClicked(this.oldPage.size)
            icon = this.oldPage.icon
            align = this.oldPage.align
        } else {
            onTextSizeClicked(size)
        }

        vFab.setOnClickListener { onEnter() }

        vIconAttach.setOnClickListener {
            val w = SplashGreed()
            w.setSpanCount(10, 20)
            for (i in CampfireConstants.TEXT_ICONS.indices) w.add(CampfireConstants.TEXT_ICONS[i]) { _, _ ->
                this.icon = i
                updateIcon()
            }
            w.asSheetShow()
        }

        vAlignLeft.setOnClickListener { setAlign(PageText.ALIGN_LEFT) }
        vAlignCenter.setOnClickListener { setAlign(PageText.ALIGN_CENTER) }
        vAlignRight.setOnClickListener { setAlign(PageText.ALIGN_RIGHT) }
        vLink.setOnClickListener { addLink() }

        updateIcon()
        update()
        updateAlign()
    }

    private fun addLink() {
        val s1 = vField.selectionStart
        val s2 = vField.selectionEnd
        SplashFieldTwo()
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .setHint_1(t(API_TRANSLATE.app_name_s))
                .setLinesCount_1(1)
                .setMin_1(1)
                .setMax_1(100)
                .setHint_2(t(API_TRANSLATE.app_link))
                .addChecker_2(t(API_TRANSLATE.error_not_url)) { ToolsText.isWebLink(it) }
                .setOnEnter(t(API_TRANSLATE.app_add)) { w, name, link ->
                    val text = vField.text.toString()
                    val linkS = "[$name]$link"
                    vField.setText(text.substring(0, s1) + linkS + text.subSequence(s2, text.length))
                    vField.setSelection(s1 + linkS.length)
                }
                .asSheetShow()
    }

    private fun updateIcon() {
        vIconAttach.isIconSelected = icon != 0
        if (icon == 0) vIconAttach.setImageDrawable(ToolsResources.getDrawable(R.drawable.ic_mood_white_24dp))
        else vIconAttach.setImageDrawable(ToolsResources.getDrawable(CampfireConstants.TEXT_ICONS[icon]))
    }

    private fun setAlign(align:Int){
        this.align = align
        updateAlign()
    }

    private fun updateAlign() {
        vAlignLeft.isIconSelected = align == PageText.ALIGN_LEFT
        vAlignCenter.isIconSelected = align == PageText.ALIGN_CENTER
        vAlignRight.isIconSelected = align == PageText.ALIGN_RIGHT

        vField.gravity = Gravity.LEFT or Gravity.TOP
        if (align == PageText.ALIGN_CENTER) vField.gravity = Gravity.CENTER or Gravity.TOP
        if (align == PageText.ALIGN_RIGHT) vField.gravity = Gravity.RIGHT or Gravity.TOP
    }

    override fun onResume() {
        super.onResume()
        ToolsView.showKeyboard(vField)
    }

    private fun onTextSizeClicked(size: Int) {
        this.size = size
        vTextTitle.isIconSelected = size == PageText.SIZE_1
        update()
    }

    private fun update() {
        val s = vField.text.toString()

        vField.textSize = if (vTextTitle.isIconSelected) 32f else if (s.length < 200) 22f else 16f

        ToolsView.setFabEnabledR(vFab, s.isNotEmpty() && s.length <= maxL, R.color.green_700)
    }

    private fun onEnter() {
        val page = PageText()
        page.text = vField.text.toString().trim { it <= ' ' }
        page.size = size
        page.align = align
        page.icon = icon
        if (oldPage == null)
            requestPutPage.invoke(page, this, ToolsView.showProgressDialog(), { page1 -> CardPageText(null, page1 as PageText) }) {}
        else
            requestChangePage.invoke(page, card!!, this, ToolsView.showProgressDialog()) {}
    }

    override fun onBackPressed(): Boolean {
        if (notChanged()) return false

        SplashAdd.showConfirmCancelDialog(this)
        return true
    }

    private fun notChanged(): Boolean {
        val s = vField.text.toString()
        return if (oldPage == null) {
            s.isEmpty()
        } else {
            ToolsText.equals(oldPage.text, s)
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        vFab.isEnabled = enabled
        vField.isEnabled = enabled
        vTextTitle.isEnabled = enabled
        vIconAttach.isEnabled = enabled
        vLink.isEnabled = enabled
    }

}
