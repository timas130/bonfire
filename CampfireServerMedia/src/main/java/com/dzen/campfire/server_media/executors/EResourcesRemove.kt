package com.dzen.campfire.server_media.executors

import com.dzen.campfire.api_media.APIMedia
import com.dzen.campfire.api_media.requests.RResourcesRemove
import com.dzen.campfire.server_media.tables.TResources
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryRemove

class EResourcesRemove : RResourcesRemove(0) {

    override fun check() {
        if(apiAccount.id != 1L) throw ApiException(APIMedia.ERROR_ACCESS)
    }

    override fun execute(): Response {

        Database.remove("EResourcesRemove", SqlQueryRemove(TResources.NAME)
                .where(TResources.id, "=", resourceId))

        return Response()
    }


}
