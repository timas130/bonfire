package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.notifications.fandom.NotificationFandomMakeModerator
import com.dzen.campfire.api.models.publications.Publication
import com.dzen.campfire.api.models.publications.events_admins.ApiEventAdminFandomMakeModerator
import com.dzen.campfire.api.models.publications.events_fandoms.ApiEventFandomMakeModerator
import com.dzen.campfire.api.models.publications.events_user.ApiEventUserFandomMakeModerator
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.requests.fandoms.RFandomsAdminMakeModerator
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.tables.TAccounts
import com.dzen.campfire.api.tools.ApiException

class EFandomsAdminMakeModerator : RFandomsAdminMakeModerator(0, "") {

    var publication: Publication = PublicationPost()
    var karma30 = 0L

    @Throws(ApiException::class)
    override fun check() {
        ControllerFandom.checkCan(apiAccount, API.LVL_ADMIN_MAKE_MODERATOR)
        publication = ControllerPublications.getPublication(publicationId, apiAccount.id)!!
        karma30 = ControllerFandom.getKarma30(publication.creator.id, publication.fandom.id, publication.fandom.languageId)

        if (karma30 > API.LVL_MODERATOR_BLOCK.karmaCount) throw ApiException(E_ALREADY)
        if (ControllerFandom.getModerationFandomsCount(publication.creator.id) > 1) throw ApiException(E_TOO_MANY)
        if (ControllerFandom.getModerators(publication.fandom.id, publication.fandom.languageId).size > 1) throw ApiException(E_FANDOM_HAVE_MODERATORS)
        if (publication.creator.lvl < API.LVL_MODERATOR_BLOCK.lvl) throw ApiException(E_LOW_LVL)
        if (publication.fandom.languageId == -1L) throw ApiException(API.ERROR_ACCESS)
        comment = ControllerModeration.parseComment(comment, apiAccount.id)
    }

    override fun execute(): Response {

        val vA = ControllerAccounts.get(publication.creator.id, TAccounts.name, TAccounts.img_id, TAccounts.sex)
        val tName: String = vA.next()
        val tImageId: Long = vA.next()
        val tSex: Long = vA.next()
        val karmaCount = API.LVL_MODERATOR_TAGS.karmaCount - karma30

        ControllerKarma.addKarmaTransaction(apiAccount, karmaCount, 100, true, publication.creator.id, publication.fandom.id, publication.fandom.languageId, 0, false)
        ControllerCollisions.incrementCollisionValueOrCreate(publication.creator.id, publication.fandom.id, publication.fandom.languageId, API.COLLISION_KARMA_30, 0)
        ControllerKarma.recountKarma30(publication.creator.id)

        ControllerNotifications.push(publication.creator.id, NotificationFandomMakeModerator(publication.fandom.imageId, publication.fandom.id, publication.fandom.languageId, publication.fandom.name, comment))

        ControllerPublications.event(ApiEventAdminFandomMakeModerator(apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, publication.creator.id, tName, tImageId, tSex, comment, publication.fandom.id, publication.fandom.languageId, publication.fandom.imageId, publication.fandom.name), apiAccount.id)
        ControllerPublications.event(ApiEventUserFandomMakeModerator(publication.creator.id, tName, tImageId, tSex,apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, comment, publication.fandom.id, publication.fandom.languageId, publication.fandom.imageId, publication.fandom.name), publication.creator.id)
        ControllerPublications.event(ApiEventFandomMakeModerator(apiAccount.id, apiAccount.name, apiAccount.imageId, apiAccount.sex, publication.creator.id, tName, tImageId, tSex, publication.fandom.id, publication.fandom.languageId, publication.fandom.name, publication.fandom.imageId, comment), apiAccount.id, publication.fandom.id, publication.fandom.languageId)

        ControllerOptimizer.putCollisionWithCheck(apiAccount.id, API.COLLISION_ACHIEVEMENT_MAKE_MODER)
        ControllerAchievements.addAchievementWithCheck(apiAccount.id, API.ACHI_MAKE_MODER)

        return Response()
    }


}
