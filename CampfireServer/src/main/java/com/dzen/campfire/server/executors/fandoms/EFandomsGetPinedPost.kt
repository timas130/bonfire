package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.requests.fandoms.RFandomsGetPinedPost
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.tables.*
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect

class EFandomsGetPinedPost : RFandomsGetPinedPost(0, 0) {

    @Throws(ApiException::class)
    override fun check() {

    }

    @Throws(ApiException::class)
    override fun execute(): Response {

        val pinnedPostId = ControllerCollisions.getCollisionValue1(fandomId, languageId, API.COLLISION_FANDOM_PINNED_POST)
        var pinnedPost: PublicationPost? = if(pinnedPostId > 0) ControllerPublications.getPublication(pinnedPostId, apiAccount.id) as PublicationPost? else null
        if(pinnedPost != null && pinnedPost.status != API.STATUS_PUBLIC) pinnedPost = null

        return Response(pinnedPost)

    }

}
