package com.dzen.campfire.server.executors.post

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.post.RPostMenuInfoGet
import com.dzen.campfire.server.controllers.ControllerCollisions
import com.dzen.campfire.api.tools.ApiException

class EPostMenuInfoGet : RPostMenuInfoGet(0, emptyArray()) {

    @Throws(ApiException::class)
    override fun check() {

    }

    override fun execute(): Response {

        var folderId = 0L

        var bookmark = ControllerCollisions.checkCollisionExist(publicationId, apiAccount.id, API.COLLISION_BOOKMARK)

        if (bookmark) {
            folderId = ControllerCollisions.getCollisionValue1(publicationId, apiAccount.id, API.COLLISION_BOOKMARK)

            if(folderId > 0 && !foldersIds.contains(folderId)){
                ControllerCollisions.removeCollisions(publicationId, apiAccount.id, API.COLLISION_BOOKMARK)
                folderId = 0L
                bookmark = false
            }
        }

        val follow = ControllerCollisions.checkCollisionExist(apiAccount.id, publicationId, API.COLLISION_COMMENTS_WATCH)

        return Response(bookmark, folderId, follow)
    }


}
