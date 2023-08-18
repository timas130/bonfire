package com.sayzen.campfiresdk.models.cards.post_pages

import android.view.View
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.PagesContainer
import com.dzen.campfire.api.models.publications.post.PageCode
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.views.ViewIcon
import io.github.kbiakov.codeview.CodeView
import io.github.kbiakov.codeview.adapters.Options
import io.github.kbiakov.codeview.highlight.ColorTheme

class CardPageCode(
        pagesContainer: PagesContainer?,
        page: PageCode
) : CardPage(R.layout.card_page_code, pagesContainer, page) {
    override fun bindView(view: View) {
        super.bindView(view)
        val page = page as PageCode
        val vCode: CodeView = view.findViewById(R.id.vCode)
        val vCopy: ViewIcon = view.findViewById(R.id.vCopy)

        vCode.setOptions(Options.Default.get(view.context)
                .withTheme(ColorTheme.MONOKAI)
                .withLanguage(page.language)
                .withCode(page.code))

        vCopy.visibility = if (editMode) View.INVISIBLE else View.VISIBLE
        vCopy.setOnClickListener {
            ToolsAndroid.setToClipboard(page.code)
            ToolsToast.show(t(API_TRANSLATE.app_copied))
        }
    }

    override fun notifyItem() {}
}