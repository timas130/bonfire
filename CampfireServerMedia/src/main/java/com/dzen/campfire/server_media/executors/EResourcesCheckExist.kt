package com.dzen.campfire.server_media.executors

import com.dzen.campfire.api_media.APIMedia
import com.dzen.campfire.api_media.requests.RResourcesCheckExist
import com.dzen.campfire.server_media.tables.TResources
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect

class EResourcesCheckExist : RResourcesCheckExist(0) {

    override fun check() {
        if (apiAccount.id != 1L) throw ApiException(APIMedia.ERROR_ACCESS)
    }

    override fun execute(): Response {
        val b = !Database.select("EResourcesCheckExist", SqlQuerySelect(TResources.NAME, TResources.id)
                .where(TResources.id, "=", resourceId)).isEmpty

        return Response(b)
    }


}
