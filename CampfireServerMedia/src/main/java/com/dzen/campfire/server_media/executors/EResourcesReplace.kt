package com.dzen.campfire.server_media.executors

import com.dzen.campfire.api_media.APIMedia
import com.dzen.campfire.api_media.requests.RResourcesReplace
import com.dzen.campfire.server_media.tables.TResources
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class EResourcesReplace : RResourcesReplace(0, null) {

    override fun check() {
        if(apiAccount.id != 1L) throw ApiException(APIMedia.ERROR_ACCESS)
    }

    override fun execute(): Response {

        Database.update("EResourcesReplace", SqlQueryUpdate(TResources.NAME)
                .where(TResources.id, "=", resourceId)
                .updateValue(TResources.image_bytes, resource!!))

        return Response()
    }


}
