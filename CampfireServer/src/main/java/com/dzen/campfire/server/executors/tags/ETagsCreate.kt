package com.dzen.campfire.server.executors.tags

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.moderations.tags.ModerationTagCreate
import com.dzen.campfire.api.models.publications.tags.PublicationTag
import com.dzen.campfire.api.requests.tags.RTagsCreate
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.tables.TPublications
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java.libs.json.Json
import com.dzen.campfire.server.controllers.ControllerResources
import com.dzen.campfire.server.tables.TFandoms

import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.tools.ToolsImage

class ETagsCreate : RTagsCreate("", "", 0, 0, 0, null) {

    private var publicationParent: PublicationTag? = null

    override fun check() {

        comment = ControllerModeration.parseComment(comment, apiAccount.id)

        if (parentId != 0L) {
            publicationParent = ControllerPublications.getPublication(parentId, apiAccount.id) as PublicationTag?
            if (publicationParent == null) throw ApiException(E_PARENT_DONT_EXIST)
            publicationParent!!.restoreFromJsonDB()
            if (publicationParent!!.publicationType != API.PUBLICATION_TYPE_TAG || publicationParent!!.parentPublicationId != 0L) throw ApiException(E_PARENT_BAD_TYPE)
        }

        if (image != null) {
            if (image!!.size > API.TAG_IMAGE_WEIGHT) throw ApiException(E_BAD_IMAGE_WEIGHT)
            if (!ToolsImage.checkImageScaleUnknownType(image!!, API.TAG_IMAGE_SIDE, API.TAG_IMAGE_SIDE, true, false, true)) throw ApiException(E_BAD_IMAGE_SIZE)
        }

        if (name.length < API.TAG_NAME_MIN_L || name.length > API.TAG_NAME_MAX_L) throw ApiException(E_BAD_NAME_SIZE)

        ControllerFandom.checkCan(apiAccount, fandomId, languageId, API.LVL_MODERATOR_TAGS)
    }

    override fun execute(): Response {

        val publication = PublicationTag()
        publication.name = name
        publication.fandom.id = fandomId
        publication.fandom.languageId = languageId
        publication.publicationType = API.PUBLICATION_TYPE_TAG
        publication.dateCreate = System.currentTimeMillis()
        publication.parentPublicationId = parentId
        publication.tag_1 = System.currentTimeMillis()
        publication.status = API.STATUS_PUBLIC
        publication.category = ControllerFandom.get(fandomId, TFandoms.fandom_category).next()
        publication.creator = ControllerAccounts.instance(apiAccount.id,
                apiAccount.accessTag,
                System.currentTimeMillis(),
                apiAccount.name,
                apiAccount.imageId,
                apiAccount.sex,
                apiAccount.accessTagSub)

        if (image != null) publication.imageId = ControllerResources.put(image!!, API.RESOURCES_PUBLICATION_TAG)

        publication.jsonDB = publication.jsonDB(true, Json())
        publication.id = Database.insert("ETagsCreate insert",TPublications.NAME,
                TPublications.fandom_id, publication.fandom.id,
                TPublications.language_id, publication.fandom.languageId,
                TPublications.publication_category,  publication.category,
                TPublications.publication_type, publication.publicationType,
                TPublications.date_create, publication.dateCreate,
                TPublications.creator_id, publication.creator.id,
                TPublications.publication_json, publication.jsonDB,
                TPublications.parent_publication_id, publication.parentPublicationId,
                TPublications.tag_1, publication.tag_1,
                TPublications.status, publication.status)

        ControllerPublications.moderation(ModerationTagCreate(comment, publication.id, if (publicationParent == null) 0 else publicationParent!!.id, publication.name, publication.imageId, if (publicationParent != null) publicationParent!!.name else "", if (publicationParent != null) publicationParent!!.imageId else 0), apiAccount.id, fandomId, languageId, publication.id)

        ControllerOptimizer.putCollisionWithCheck(apiAccount.id, API.COLLISION_ACHIEVEMENT_TAG_CREATE)
        ControllerAchievements.addAchievementWithCheck(apiAccount.id, API.ACHI_CREATE_TAG)

        return Response(publication.id)
    }

}
