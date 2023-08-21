package com.dzen.campfire.server_media.executors

import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.api_media.APIMedia
import com.dzen.campfire.api_media.requests.RResourcesGet
import com.dzen.campfire.server_media.app.App
import com.dzen.campfire.server_media.tables.TResources
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect

class EResourcesGet : RResourcesGet(0, "") {
    override fun check() {
    }

    override fun execute(): Response {
        val select = SqlQuerySelect(TResources.NAME, TResources.pwd, TResources.size)
        select.where(TResources.id, "=", resourceId)
        val v = Database.select("EResourcesGet", select)

        if (v.isEmpty) throw ApiException(APIMedia.ERROR_GONE).salient()

        val correctPwd: String? = v.nextMayNull()
        if (!correctPwd.isNullOrEmpty()) if (correctPwd != pwd) throw ApiException(APIMedia.ERROR_ACCESS)

        val size: Long = v.next()

        val bytes = App.storage.get(resourceId) ?: throw ApiException(APIMedia.ERROR_GONE)
        if (bytes.size.toLong() != size) {
            err("resource size mismatch id=$resourceId size=$size bytes.size=${bytes.size}")
        }

        return Response(bytes)
    }
}
