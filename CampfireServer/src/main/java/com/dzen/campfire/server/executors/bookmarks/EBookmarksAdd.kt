package com.dzen.campfire.server.executors.bookmarks

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.bookmarks.RBookmarksAdd
import com.dzen.campfire.api.requests.bookmarks.RBookmarksRemove
import com.dzen.campfire.api.requests.bookmarks.RBookmarksStatus
import com.dzen.campfire.server.controllers.ControllerCollisions
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TPublications

class EBookmarksAdd : RBookmarksAdd(0, 0) {

    @Throws(ApiException::class)
    override fun check() {
        val v = ControllerPublications[publicationId, TPublications.publication_type, TPublications.status]
        if (v.isEmpty()) throw ApiException(API.ERROR_GONE)
        if (v.next<Long>() != API.PUBLICATION_TYPE_POST) throw ApiException(API.ERROR_ACCESS)
        if (v.next<Long>() != API.STATUS_PUBLIC) throw ApiException(API.ERROR_GONE)
    }

    override fun execute(): Response {


        ControllerCollisions.updateOrCreate(publicationId, apiAccount.id, 0, API.COLLISION_BOOKMARK, System.currentTimeMillis(), folderId, null)


        return Response()
    }


}