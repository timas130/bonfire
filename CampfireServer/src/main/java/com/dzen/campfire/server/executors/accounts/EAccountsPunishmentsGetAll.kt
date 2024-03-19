package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.account.AccountPunishment
import com.dzen.campfire.api.requests.accounts.RAccountsPunishmentsGetAll
import com.dzen.campfire.server.tables.TCollisions
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect
import com.sup.dev.java_pc.sql.SqlWhere

class EAccountsPunishmentsGetAll : RAccountsPunishmentsGetAll(0, 0) {

    override fun check() {

    }

    override fun execute(): Response {

        val v = Database.select("EAccountsPunishmentsGetAll", SqlQuerySelect(TCollisions.NAME,
                TCollisions.id,
                TCollisions.owner_id,
                TCollisions.collision_id,
                TCollisions.collision_sub_id,
                TCollisions.FANDOM_IMAGE_ID,
                TCollisions.FANDOM_NAME,
                TCollisions.collision_date_create,
                TCollisions.value_2)
                .where(TCollisions.owner_id, "=", accountId)
                .where(SqlWhere.WhereIN(TCollisions.collision_type, arrayOf(API.COLLISION_PUNISHMENTS_BAN, API.COLLISION_PUNISHMENTS_WARN)))
                .offset_count(offset, COUNT)
                .sort(TCollisions.collision_date_create, false)
        )

        val array = Array(v.rowsCount) {
            val p = AccountPunishment()
            p.id = v.next()
            p.ownerId = v.next()
            p.fandomId = v.next()
            p.languageId = v.next()
            p.fandomAvatarId = v.next()
            p.fandomName = v.next()
            p.dateCreate = v.next()
            p.parseSupportString(v.next())
            p
        }

        return Response(array)
    }


}
