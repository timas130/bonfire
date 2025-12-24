package com.dzen.campfire.server.executors.tags

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.moderations.tags.ModerationTagChange
import com.dzen.campfire.api.models.publications.tags.PublicationTag
import com.dzen.campfire.api.requests.tags.RTagsChange
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.controllers.ControllerModeration
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.controllers.ControllerResources
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate
import com.sup.dev.java_pc.tools.ToolsImage


class ETagsChange : RTagsChange(0, null, "", null, false) {

    private var publication: PublicationTag? = null
    private var publicationParent: PublicationTag? = null
    private var oldName: String? = null
    private var oldImageId: Long = 0

    @Throws(ApiException::class)
    override fun check() {

        if (removeImage && image != null) throw ApiException(E_BAD_PARAMS)
        comment = ControllerModeration.parseComment(comment, apiAccount.id)

        publication = ControllerPublications.getPublication(publicationId, apiAccount.id) as PublicationTag?

        if (publication == null) throw ApiException(API.ERROR_GONE)

        if (image != null) {
            if (image!!.size > API.TAG_IMAGE_WEIGHT) throw ApiException(E_BAD_IMAGE_WEIGHT)
            if (!ToolsImage.checkImageScaleUnknownType(image!!, API.TAG_IMAGE_SIDE, API.TAG_IMAGE_SIDE, true, false, true)) throw ApiException(E_BAD_IMAGE_SIZE)
        }

        if (name != null && (name!!.length < API.TAG_NAME_MIN_L || name!!.length > API.TAG_NAME_MAX_L)) throw ApiException(E_BAD_NAME_SIZE)

        ControllerFandom.checkCan(apiAccount, publication!!.fandom.id, publication!!.fandom.languageId, API.LVL_MODERATOR_TAGS)
    }

    override fun execute(): Response {
        oldName = publication!!.name
        oldImageId = publication!!.imageId

        if (publication!!.parentPublicationId != 0L) publicationParent = ControllerPublications.getPublication(publication!!.parentPublicationId, apiAccount.id) as PublicationTag?

        publication!!.name = name!!
        if (image != null) {
            publication!!.imageId = ControllerResources.removeAndPut(publication!!.imageId, image!!, API.RESOURCES_PUBLICATION_TAG)
        }
        if (removeImage) {
            ControllerResources.remove(publication!!.imageId)
        }

        Database.update("ETagsChange", SqlQueryUpdate(TPublications.NAME)
                .where(TPublications.id, "=", publicationId)
                .updateValue(TPublications.publication_json, publication!!.jsonDB(true, Json())))

        ControllerPublications.moderation(ModerationTagChange(comment, publication!!.id, publication!!.parentPublicationId, publication!!.name, oldName!!, publication!!.imageId, oldImageId, if (publicationParent != null) publicationParent!!.name else "", if (publicationParent != null) publicationParent!!.imageId else 0),
                apiAccount.id, publication!!.fandom.id,  publication!!.fandom.languageId, publication!!.id)

        return Response()
    }

}
