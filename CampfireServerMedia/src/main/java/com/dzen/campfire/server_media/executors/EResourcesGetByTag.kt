package com.dzen.campfire.server_media.executors

import com.dzen.campfire.api_media.APIMedia
import com.dzen.campfire.api_media.requests.RResourcesGet
import com.dzen.campfire.api_media.requests.RResourcesGetByTag
import com.dzen.campfire.server_media.tables.TResources
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server_media.app.App
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect

class EResourcesGetByTag : RResourcesGetByTag("") {
    override fun check() {
    }

    override fun execute(): Response {
        val select = SqlQuerySelect(TResources.NAME, TResources.id, TResources.size)
        select.whereValue(TResources.tag_s_1, "=", tag)
        val v = Database.select("EResourcesGetByTag", select)

        if (v.isEmpty) throw ApiException(APIMedia.ERROR_GONE).salient()

        val id = v.next<Long>()
        val size = v.next<Long>()

        val bytes = App.storage.get(id) ?: throw ApiException(APIMedia.ERROR_GONE)

        if (bytes.size.toLong() != size) {
            err("resource size mismatch id=$id size=$size bytes.size=${bytes.size}")
        }

        return Response(v.next())
    }
}
