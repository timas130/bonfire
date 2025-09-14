package com.dzen.campfire.server.executors.project

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.history.HistoryCreate
import com.dzen.campfire.api.models.publications.post.PageText
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.requests.project.RProjectMakeHelloPost
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.tables.TFandoms
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java_pc.sql.Database

class EProjectMakeHelloPost : RProjectMakeHelloPost("", false, "", 0) {

    override fun check() {
    }

    override fun execute(): Response {
        ControllerAccounts.checkAccountBanned(apiAccount.id)

        val fandomId = API.FANDOM_CAMPFIRE_HELLO_ID

        val page_1 = PageText()
        page_1.text = text
        if (title) page_1.size = PageText.SIZE_1

        val page_2 = PageText()
        page_2.text = text_2

        ControllerPost.insertPage(page_1, API.RESOURCES_PUBLICATION_ERROR)
        ControllerPost.insertPage(page_2, API.RESOURCES_PUBLICATION_ERROR)

        val publication = PublicationPost()
        publication.pages = arrayOf(page_1, page_2)

        publication.id = Database.insert("EPostPutPage insertPage", TPublications.NAME,
                TPublications.publication_type, API.PUBLICATION_TYPE_POST,
                TPublications.fandom_id, fandomId,
                TPublications.language_id, languageId,
                TPublications.date_create, System.currentTimeMillis(),
                TPublications.creator_id, apiAccount.id,
                TPublications.publication_json, publication.jsonDB(true, Json()).toString(),
                TPublications.parent_publication_id, 0,
                TPublications.parent_fandom_closed, ControllerFandom.get(fandomId, TFandoms.fandom_closed).next<Int>(),
                TPublications.tag_s_1, API.PROJECT_KEY_CAMPFIRE,
                TPublications.tag_s_2, "",
                TPublications.publication_category, ControllerFandom.getCategory(fandomId),
                TPublications.status, API.STATUS_PUBLIC)

        ControllerPublicationsHistory.put(publication.id, HistoryCreate(apiAccount.id, apiAccount.imageId, apiAccount.name))
        ControllerPublications.watchComments(apiAccount.id, publication.id, true)


        return Response(publication.id)
    }
}
