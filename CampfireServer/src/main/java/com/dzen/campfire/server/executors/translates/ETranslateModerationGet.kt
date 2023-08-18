package com.dzen.campfire.server.executors.translates

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.translates.RTranslateGetMap
import com.dzen.campfire.api.requests.translates.RTranslateModerationGet
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.controllers.ControllerOptimizer
import com.dzen.campfire.server.controllers.ControllerServerTranslates
import com.dzen.campfire.server.optimizers.OptimizerEffects
import com.dzen.campfire.server.tables.TTranslatesHistory
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect
import com.sup.dev.java_pc.sql.SqlWhere

class ETranslateModerationGet : RTranslateModerationGet(0) {

    override fun check() {
        if(OptimizerEffects.get(apiAccount.id, API.EFFECT_INDEX_TRANSLATOR) == null) ControllerFandom.checkCan(apiAccount, API.LVL_ADMIN_TRANSLATE_MODERATOR)
    }

    override fun execute(): Response {

        val v = Database.select("ETranslateModerationGet", ControllerServerTranslates.instanceSelectHistory()
                .where(TTranslatesHistory.history_creator_id, "<>", if(ControllerFandom.can(apiAccount, API.LVL_PROTOADMIN)) -1 else apiAccount.id)
                .where(SqlWhere.WhereString(
                        "(${TTranslatesHistory.confirm_account_1}=0 OR ${TTranslatesHistory.confirm_account_2}=0 OR ${TTranslatesHistory.confirm_account_3}=0)" +
                                "AND" +
                                "(${TTranslatesHistory.confirm_account_1}<>${apiAccount.id} AND ${TTranslatesHistory.confirm_account_2}<>${apiAccount.id} AND ${TTranslatesHistory.confirm_account_3}<>${apiAccount.id})"
                ))
                .offset_count(offset, COUNT)
        )


        return Response(ControllerServerTranslates.parseSelectHistory(v))
    }

}
