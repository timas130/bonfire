package com.dzen.campfire.server_media.executors

import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.api_media.APIMedia
import com.dzen.campfire.api_media.requests.RResourcesGet
import com.dzen.campfire.server_media.tables.TResources
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect

class EResourcesGet : RResourcesGet(0, "") {

    override fun check() {

    }

    override fun execute(): Response {

        val select = SqlQuerySelect(TResources.NAME, TResources.pwd, TResources.image_bytes)
        select.where(TResources.id, "=", resourceId)
        val v = Database.select("EResourcesGet", select)

        if (v.isEmpty) throw  ApiException(APIMedia.ERROR_GONE).salient()

        val correctPwd: String? = v.nextMayNull()
        if (!correctPwd.isNullOrEmpty()) if (correctPwd != pwd) throw ApiException(APIMedia.ERROR_ACCESS)

        return Response(v.next())
    }


}
