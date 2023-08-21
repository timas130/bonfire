package com.dzen.campfire.server_media.executors

import com.dzen.campfire.api_media.APIMedia
import com.dzen.campfire.api_media.requests.RResourcesPut
import com.dzen.campfire.server_media.tables.TResources
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server_media.app.App
import com.sup.dev.java_pc.sql.Database

class EResourcesPut : RResourcesPut(null, 0) {
    override fun check() {
        if (apiAccount.id != 1L) throw ApiException(APIMedia.ERROR_ACCESS)
    }

    override fun execute(): Response {
        val id = if (resourceId == 0L) {
            Database.insert("EResourcesPut 1", TResources.NAME,
                TResources.publication_id, publicationId,
                TResources.tag_s_1, tag,
                TResources.size, if(resource != null)resource?.size else 0
            )
        } else {
            Database.insert("EResourcesPut 2", TResources.NAME,
                TResources.publication_id, publicationId,
                TResources.tag_s_1, tag,
                TResources.size, if(resource != null)resource?.size else 0,
                TResources.id, resourceId)
        }
        resource?.let { App.storage.put(id, it) }
        return Response(id)
    }
}
