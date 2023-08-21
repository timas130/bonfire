package com.dzen.campfire.server_media.executors

import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.api_media.APIMedia
import com.dzen.campfire.api_media.requests.RResourcesGetJson
import com.dzen.campfire.server_media.app.App
import com.dzen.campfire.server_media.tables.TResources
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.tools.ToolsMapper
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect
import java.util.*

class EResourcesGetJson : RResourcesGetJson(0) {
    override fun check() {

    }

    override fun execute(): Response {
        val select = SqlQuerySelect(TResources.NAME, TResources.size)
        select.where(TResources.id, "=", resourceId)
        val v = Database.select("EResourcesGetJson", select)

        if (v.isEmpty) throw ApiException(APIMedia.ERROR_GONE).salient()

        val size = v.next<Long>()

        val bytes = App.storage.get(resourceId) ?: throw ApiException(APIMedia.ERROR_GONE)
        if (bytes.size.toLong() != size) {
            err("resource size mismatch id=$resourceId size=$size bytes.size=${bytes.size}")
        }
        val encodedBytes = Base64.getEncoder().encode(bytes)

        return Response(String(encodedBytes))
    }
}
