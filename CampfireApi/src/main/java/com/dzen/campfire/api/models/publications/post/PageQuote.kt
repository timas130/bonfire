package com.dzen.campfire.api.models.publications.post

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.images.ImageHolderReceiver
import com.sup.dev.java.libs.json.Json
import com.vdurmont.semver4j.Semver
import sh.sit.bonfire.formatting.core.BonfireFormatter
import sh.sit.bonfire.formatting.core.model.FormattedText

class PageQuote : Page() {
    var author: String = ""
    var text: String = ""
    var formattedText = FormattedText()

    override fun getType() = API.PAGE_TYPE_QUOTE

    override fun json(inp: Boolean, json: Json): Json {
        text = json.m(inp, "text", text)
        author = json.m(inp, "author", author)
        formattedText = json.m(inp, "formattedText", formattedText)

        return super.json(inp, json)
    }

    override fun fillResourcesList(list: ArrayList<Long>) {

    }

    override fun fillImageRefs(receiver: ImageHolderReceiver) {
        val requestVersion = Semver(receiver.getRequestVersion(), Semver.SemverType.LOOSE)
        val newFormattingVersion = Semver("3.1.0")

        if (requestVersion >= newFormattingVersion) {
            formattedText = BonfireFormatter.parse(text)
        }
    }
}

