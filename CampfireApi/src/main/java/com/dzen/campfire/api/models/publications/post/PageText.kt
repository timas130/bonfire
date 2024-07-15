package com.dzen.campfire.api.models.publications.post

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.images.ImageHolderReceiver
import com.sup.dev.java.libs.json.Json
import com.vdurmont.semver4j.Semver
import sh.sit.bonfire.formatting.core.BonfireFormatter
import sh.sit.bonfire.formatting.core.model.FormattedText

class PageText : Page() {
    companion object {
        const val SIZE_0 = 0
        const val SIZE_1 = 1

        const val ALIGN_LEFT = 0
        const val ALIGN_RIGHT = 1
        const val ALIGN_CENTER = 2
    }

    var text = ""
    var formattedText = FormattedText()
    var size = 0
    var align = 0
    var icon = 0
    var newFormatting = false

    override fun getType() = API.PAGE_TYPE_TEXT

    override fun json(inp: Boolean, json: Json): Json {
        text = json.m(inp, "J_TEXT", text)
        formattedText = json.m(inp, "formattedText", formattedText)
        if (!inp && formattedText.text.isEmpty()) {
            formattedText = BonfireFormatter.parse(text)
        }
        icon = json.m(inp, "icon", icon)
        align = json.m(inp, "align", align)
        newFormatting = json.m(inp, "newFormatting", newFormatting)

        if (inp)
            json.put("J_SIZE", size)
        else
            size = json.getInt("J_SIZE", SIZE_0)

        return super.json(inp, json)
    }

    override fun fillImageRefs(receiver: ImageHolderReceiver) {
        val requestVersion = Semver(receiver.getRequestVersion(), Semver.SemverType.LOOSE)
        val newFormattingVersion = Semver("3.1.0")

        if (requestVersion >= newFormattingVersion) {
            formattedText = BonfireFormatter.parse(text)
        }
    }

    override fun fillResourcesList(list: ArrayList<Long>) {

    }


}
