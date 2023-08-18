package com.dzen.campfire.server.executors.bookmarks

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.bookmarks.RBookmarksAdd
import com.dzen.campfire.api.requests.bookmarks.RBookmarksRemove
import com.dzen.campfire.api.requests.bookmarks.RBookmarksStatus
import com.dzen.campfire.server.controllers.ControllerCollisions
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerPublications

class EBookmarksAdd : RBookmarksAdd(0, 0) {

    @Throws(ApiException::class)
    override fun check() {
    }

    override fun execute(): Response {


        ControllerCollisions.updateOrCreate(publicationId, apiAccount.id, 0, API.COLLISION_BOOKMARK, System.currentTimeMillis(), folderId, null)


        return Response()
    }


}