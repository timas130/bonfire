package com.dzen.campfire.server.executors.comments

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.PublicationComment
import com.dzen.campfire.api.models.publications.history.HistoryEditPublic
import com.dzen.campfire.api.requests.comments.RCommentsChange
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.*

class ECommentsChange : RCommentsChange(0, "", 0, false) {

    private var publication: PublicationComment? = null

    @Throws(ApiException::class)
    override fun check() {
        publication = ControllerPublications.getPublication(commentId, apiAccount.id) as PublicationComment?
        if (publication == null) throw ApiException(API.ERROR_GONE)
        if (publication!!.creator.id != apiAccount.id) throw ApiException(API.ERROR_ACCESS) // see git blame

        text = text.trim { it <= ' ' }
        text = ControllerCensor.cens(text)

        if (text.length < API.COMMENT_MIN_L || text.length > API.COMMENT_MAX_L) throw ApiException(E_BAD_TEXT_SIZE)

        ControllerAccounts.checkAccountBanned(apiAccount.id, publication!!.fandom.id, publication!!.fandom.languageId)
    }


    override fun execute(): Response {

        val oldText = publication!!.text
        publication!!.text = text
        publication!!.changed = true
        publication!!.quoteId = 0
        publication!!.quoteText = ""
        publication!!.quoteImageIds = emptyArray()
        publication!!.newFormatting = newFormatting

        if (quoteId != 0L) {
            val quoteUnit = ControllerPublications.getPublication(quoteId, apiAccount.id)
            if (quoteUnit != null && quoteUnit is PublicationComment) {
                publication!!.quoteId = quoteUnit.id
                publication!!.quoteText = quoteUnit.creator.name + ": " + quoteUnit.text
                publication!!.quoteImageIds = if (quoteUnit.imageId > 0) Array(1) { quoteUnit.imageId } else emptyArray()
                publication!!.quoteCreatorName = quoteUnit.creator.name
            }
        }
        ControllerPublications.replaceJson(commentId, publication!!)

        ControllerOptimizer.putCollisionWithCheck(apiAccount.id, API.COLLISION_ACHIEVEMENT_CHANGE_COMMENT)
        ControllerAchievements.addAchievementWithCheck(apiAccount.id, API.ACHI_CHANGE_COMMENT)
        ControllerPublicationsHistory.put(publication!!.id, HistoryEditPublic(apiAccount.id, apiAccount.imageId, apiAccount.name, oldText))

        return Response()
    }


}
