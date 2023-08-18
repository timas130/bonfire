package com.dzen.campfire.server.executors.quests

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.quests.QuestDetails
import com.dzen.campfire.api.requests.quests.RQuestsModify
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerCensor
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect
import com.sup.dev.java_pc.sql.SqlQueryUpdate

class EQuestsModify : RQuestsModify(QuestDetails()) {
    override fun check() {
        checkQuestEditable(edit.id, apiAccount)
        ControllerAccounts.checkAccountBanned(apiAccount.id)

        edit.title = edit.title.trim()
        if (edit.title.length > API.QUEST_TITLE_MAX_L || edit.title.length < API.QUEST_TITLE_MIN_L)
            throw ApiException(E_INVALID_NAME, "bad length: ${edit.title.length}")
        edit.title = ControllerCensor.cens(edit.title, "%s", "*")

        edit.description = edit.description.trim()
        if (edit.description.length > API.QUEST_DESCRIPTION_MAX_L)
            throw ApiException(E_INVALID_DESCRIPTION, "bad length: ${edit.description.length}")
        edit.description = ControllerCensor.cens(edit.description)

        if (edit.variables.size > API.QUEST_VARIABLES_MAX)
            throw ApiException(E_INVALID_VARS, "max length: ${API.QUEST_VARIABLES_MAX}")
        for (variable in edit.variables) {
            // whether the variable is used in a QuestPart or not
            // is checked on the client. it's checked on the server
            // once again when publishing the quest.

            if (variable.devName.length > API.QUEST_VARIABLE_MAX_NAME_L)
                throw ApiException(E_INVALID_VARS, "name too long: ${variable.devName.length}")
            if (variable.type != API.QUEST_TYPE_TEXT &&
                variable.type != API.QUEST_TYPE_NUMBER &&
                variable.type != API.QUEST_TYPE_BOOL)
            {
                throw ApiException(E_INVALID_VARS, "invalid type: ${variable.type}")
            }
        }

        if (! API.isLanguageExsit(edit.fandom.languageId))
            throw ApiException(E_INVALID_LANG, "invalid language: ${edit.fandom.languageId}")

        val v = Database.select("EQuestsModify select", SqlQuerySelect(
            TPublications.NAME, TPublications.status, TPublications.creator_id
        ).where(TPublications.id, "=", edit.id))
        val status = v.next<Long>()
        val creator = v.next<Long>()
        if (status != API.STATUS_DRAFT) throw ApiException(E_NOT_DRAFT)
        if (apiAccount.id != creator) throw ApiException(API.ERROR_ACCESS)
    }

    override fun execute(): Response {
        Database.update("EQuestsModify update", SqlQueryUpdate(TPublications.NAME)
            .where(TPublications.id, "=", edit.id)
            .update(TPublications.language_id, edit.fandom.languageId)
            .updateValue(TPublications.publication_json, edit.jsonDB(true, Json()).toString()))

        return Response(ControllerPublications.getPublication(edit.id, apiAccount.id) as QuestDetails)
    }
}
