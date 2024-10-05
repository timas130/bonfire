package com.sayzen.campfiresdk.controllers

import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.compose.other.LinkAlertSplash
import com.sup.dev.android.tools.ToolsIntent
import com.sup.dev.android.tools.ToolsStorage
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.libs.json.JsonParsable
import java.net.URI

object ControllerExternalLinks {
    class TrustedDomains() : JsonParsable {
        var domains: Array<String> = emptyArray()

        override fun json(inp: Boolean, json: Json): Json {
            domains = json.m(inp, "domains", domains)
            return json
        }
    }

    private val trustedDomains = TrustedDomains()

    fun init() {
        trustedDomains.json(false, ToolsStorage.getJson("ControllerExternalLinks_trustedDomains") ?: Json())
    }

    private fun addTrustedDomain(domain: String) {
        trustedDomains.domains += domain
        ToolsStorage.put("ControllerExternalLinks_trustedDomains", trustedDomains.json(true, Json()))
    }

    private fun checkTrustedDomain(domain: String): Boolean {
        return domain in trustedDomains.domains
    }

    fun openLink(link: String) {
        if (ControllerLinks.parseLink(link)) return

        try {
            val uri = URI(link)

            if (uri.host?.let { checkTrustedDomain(it) } == true) {
                ToolsIntent.openLink(link)
                return
            }

            LinkAlertSplash(
                link = link,
                onVisit = fun(trust) {
                    uri.host.takeIf { trust }?.let { addTrustedDomain(it) }
                    ToolsIntent.openLink(link)
                }
            ).asSheetShow()
        } catch (e: Exception) {
            err(e)
            ToolsToast.show(R.string.link_parse_error)
        }
    }
}
