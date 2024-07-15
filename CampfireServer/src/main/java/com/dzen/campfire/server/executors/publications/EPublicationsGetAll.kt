package com.dzen.campfire.server.executors.publications

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.publications.RPublicationsGetAll
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerPost.filterNsfw
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java.tools.ToolsCollections
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlWhere

class EPublicationsGetAll : RPublicationsGetAll() {

    @Throws(ApiException::class)
    override fun check() {

    }

    override fun execute(): Response {
        val select = ControllerPublications.instanceSelect(apiAccount.id)

        select.filterNsfw(apiAccount.id, requestApiVersion)
        if (onlyWithFandom) select.where(TPublications.fandom_id, ">", 0)
        if (accountId != 0L) select.where(TPublications.creator_id, "=", accountId)
        if (publicationsTypes != null) select.where(SqlWhere.WhereIN(TPublications.publication_type, publicationsTypes!!))
        if (parentPublicationId != 0L) select.where(TPublications.parent_publication_id, "=", parentPublicationId)
        if (fandomId != 0L) select.where(TPublications.fandom_id, "=", fandomId)
        if (fandomsIds.isNotEmpty()) select.where(SqlWhere.WhereIN(TPublications.fandom_id, fandomsIds))
        if (appKey != null) select.whereValue(TPublications.tag_s_1, "=", appKey!!)
        if (appSubKey != null) select.whereValue(TPublications.tag_s_2, "=", appSubKey!!)
        if (tags != null) for (tagId in tags!!) select.where(SqlWhere.WhereString(TPublications.whereCollisionsExist(API.COLLISION_TAG, tagId)))
        if (drafts && apiAccount.id > 0) {
            select.where(TPublications.creator_id, "=", apiAccount.id)
            select.where(TPublications.status, "=", API.STATUS_DRAFT)
        } else {
            select.where(TPublications.status, "=", API.STATUS_PUBLIC)
        }
        if (languageId != 0L) {
            var languages = arrayOf(languageId)
            if (includeZeroLanguages) languages = ToolsCollections.add(0L, languages)
            if (includeMultilingual) languages = ToolsCollections.add(-1L, languages)
            select.where(SqlWhere.WhereIN(TPublications.language_id, languages))
        } else {
            if (includeZeroLanguages) select.where(TPublications.language_id, "=", 0)
        }

        if (publicationsTypes != null && publicationsTypes!!.contains(API.PUBLICATION_TYPE_MODERATION)) {
            if (!includeModerationsBlocks)
                select.where(SqlWhere.WhereString("(${TPublications.publication_type}<>${API.PUBLICATION_TYPE_MODERATION} OR ${TPublications.tag_1}<>${API.MODERATION_TYPE_BLOCK})"))
            if (!includeModerationsOther)
                select.where(SqlWhere.WhereString("(${TPublications.publication_type}<>${API.PUBLICATION_TYPE_MODERATION} OR ${TPublications.tag_1}=${API.MODERATION_TYPE_BLOCK})"))

        }

        if (important == API.PUBLICATION_IMPORTANT_IMPORTANT) select.where(TPublications.important, "=", API.PUBLICATION_IMPORTANT_IMPORTANT)

        select.where(SqlWhere.WhereString("(${TPublications.publication_type}<>${API.PUBLICATION_TYPE_CHAT_MESSAGE} OR ${TPublications.tag_1}=${API.CHAT_TYPE_FANDOM_ROOT})"))
        select.offset_count(offset, count)

        if (order == ORDER_NEW)
            select.sort(TPublications.date_create, false)
        else if (order == ORDER_OLD)
            select.sort(TPublications.date_create, true)
        else if (order == ORDER_KARMA)
            select.sort("karma_count", false)
        else if (order == ORDER_DOWNLOADS)
            select.sort("downloads_count", false)

        var publications = ControllerPublications.parseSelect( Database.select("EPublicationsGetAll",select))
        publications = ControllerPublications.loadSpecDataForPosts(apiAccount.id, publications)

        return Response(publications)
    }


}
