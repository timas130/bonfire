package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.fandoms.RFandomsSubscribersGetAll
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.tables.TAccounts
import com.dzen.campfire.server.tables.TCollisions
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect


class EFandomsSubscribersGetAll : RFandomsSubscribersGetAll(0, 0, 0) {

    override fun check() {

    }

    override fun execute(): Response {

        val v = Database.select("EFandomsSubscribersGetAll",SqlQuerySelect(TCollisions.NAME)
                .where(TCollisions.collision_id, "=", fandomId)
                .where(TCollisions.collision_sub_id, "=", languageId)
                .where(TCollisions.collision_type, "=", API.COLLISION_FANDOM_SUBSCRIBE)
                .where(TCollisions.value_1, "<>", API.PUBLICATION_IMPORTANT_NONE)
                .where(TAccounts.NAME +"." + TAccounts.id, "=", TCollisions.owner_id)
                .join(ControllerAccounts.instanceSelect())
                .offset_count(offset, COUNT))

       return Response(ControllerAccounts.parseSelect(v))
    }


}
