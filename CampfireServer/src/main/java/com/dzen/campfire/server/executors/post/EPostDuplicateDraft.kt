package com.dzen.campfire.server.executors.post

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.requests.post.RPostDuplicateDraft
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.controllers.ControllerResources
import com.dzen.campfire.server.tables.TFandoms
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java_pc.sql.Database

class EPostDuplicateDraft : RPostDuplicateDraft(0) {
    private lateinit var post: PublicationPost

    override fun check() {
        val p = ControllerPublications.parseSelect(Database.select("EPostGet",
            ControllerPublications.instanceSelect(apiAccount.id)
                .where(TPublications.id, "=", postId)
                .where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_POST)
        ))

        if (p.isEmpty()) throw ApiException(API.ERROR_GONE)

        post = p[0] as PublicationPost

        if ((post.status != API.STATUS_DRAFT && post.status != API.STATUS_DRAFT) || post.creator.id != apiAccount.id) {
            throw ApiException(API.ERROR_GONE)
        }

        if (ControllerFandom[post.fandom.id, TFandoms.status].next<Long>() != API.STATUS_PUBLIC)
            throw ApiException(API.ERROR_ACCESS)
    }

    override fun execute(): Response {
        val publication = PublicationPost()
        publication.id = Database.insert("EPostDuplicateDraft", TPublications.NAME,
            TPublications.publication_type, API.PUBLICATION_TYPE_POST,
            TPublications.fandom_id, post.fandom.id,
            TPublications.language_id, post.fandom.languageId,
            TPublications.date_create, System.currentTimeMillis(),
            TPublications.creator_id, apiAccount.id,
            TPublications.publication_json, publication.jsonDB(true, Json()).toString(),
            TPublications.parent_publication_id, 0,
            TPublications.parent_fandom_closed, ControllerFandom[post.fandom.id, TFandoms.fandom_closed].next<Int>(),
            TPublications.tag_s_1, "",
            TPublications.tag_s_2, "",
            TPublications.publication_category, ControllerFandom.getCategory(post.fandom.id),
            TPublications.status, API.STATUS_DRAFT,
            TPublications.fandom_key, "${post.fandom.id}-${post.fandom.languageId}-0")

        for (p in post.pages) p.duplicateResources(ControllerResources, publication.id)

        publication.pages = post.pages
        ControllerPublications.replaceJson(publication.id, publication)

        return Response()
    }
}
