package com.dzen.campfire.api.requests.accounts

import com.dzen.campfire.api.models.images.ImageHolderReceiver
import com.dzen.campfire.api.models.publications.PublicationReport
import com.dzen.campfire.api.tools.client.Request
import com.sup.dev.java.libs.json.Json

open class RAccountsReportsGetAllForAccount(
        var accountId: Long,
        var offset: Long
) : Request<RAccountsReportsGetAllForAccount.Response>() {

    companion object {
        val COUNT = 20
    }

    init {
        cashAvailable = false
    }

    override fun jsonSub(inp: Boolean, json: Json) {
        accountId = json.m(inp, "accountId", accountId)
        offset = json.m(inp, "offset", offset)
    }

    override fun instanceResponse(json: Json): Response {
        return Response(json)
    }

    class Response : Request.Response {

        var reports: Array<PublicationReport> = emptyArray()

        constructor(json: Json) {
            json(false, json)
        }

        constructor(reports: Array<PublicationReport>) {
            this.reports = reports
        }

        override fun json(inp: Boolean, json: Json) {
            reports = json.m(inp, "reports", reports, Array<PublicationReport>::class)
        }

        override fun fillImageRefs(receiver: ImageHolderReceiver) {
            for (report in reports) {
                report.fillImageRefs(receiver)
            }
        }
    }

}
