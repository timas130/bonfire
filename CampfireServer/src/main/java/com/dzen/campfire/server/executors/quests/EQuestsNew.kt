package com.dzen.campfire.server.executors.quests

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.history.HistoryCreate
import com.dzen.campfire.api.models.quests.QuestDetails
import com.dzen.campfire.api.requests.quests.RQuestsNew
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.controllers.ControllerCensor.censorNoFormat
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java_pc.sql.Database

class EQuestsNew : RQuestsNew("", 0) {
    override fun check() {
        ControllerFandom.checkCan(apiAccount, API.LVL_CREATE_QUESTS)
        ControllerAccounts.checkAccountBanned(apiAccount.id)

        title = title.trim()
        if (title.length > API.QUEST_TITLE_MAX_L || title.length < API.QUEST_TITLE_MIN_L)
            throw ApiException(E_INVALID_NAME, "bad length: ${title.length}")
        title = ControllerCensor.cens(title.trim(), "%s", "*")
    }

    override fun execute(): Response {
        val quest = QuestDetails()
        quest.title = title.censorNoFormat()
        quest.id = Database.insert(
            "EQuestsNew", TPublications.NAME,
            TPublications.publication_type, API.PUBLICATION_TYPE_QUEST,
            TPublications.fandom_id, 0,
            TPublications.language_id, languageId,
            TPublications.date_create, System.currentTimeMillis(),
            TPublications.creator_id, apiAccount.id,
            TPublications.publication_json, quest.jsonDB(true, Json()).toString(),
            TPublications.parent_publication_id, 0,
            TPublications.parent_fandom_closed, 0,
            TPublications.tag_s_1, "",
            TPublications.tag_s_2, "",
            TPublications.publication_category, API.CATEGORY_BOOKS,
            TPublications.status, API.STATUS_DRAFT,
            TPublications.fandom_key, "0-$languageId-0"
        )

        ControllerPublicationsHistory.put(
            quest.id,
            HistoryCreate(apiAccount.id, apiAccount.imageId, apiAccount.name)
        )

        return Response(ControllerPublications.getPublication(quest.id, apiAccount.id) as QuestDetails)
    }
}
