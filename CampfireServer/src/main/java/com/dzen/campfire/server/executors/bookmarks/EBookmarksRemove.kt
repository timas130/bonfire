package com.dzen.campfire.server.executors.bookmarks

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.bookmarks.RBookmarksRemove
import com.dzen.campfire.api.requests.bookmarks.RBookmarksStatus
import com.dzen.campfire.server.controllers.ControllerCollisions
import com.dzen.campfire.api.tools.ApiException

class EBookmarksRemove : RBookmarksRemove(0) {

    @Throws(ApiException::class)
    override fun check() {

    }

    override fun execute(): Response {

        ControllerCollisions.removeCollisions(publicationId, apiAccount.id, API.COLLISION_BOOKMARK)

        return Response()
    }


}