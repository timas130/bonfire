package com.dzen.campfire.server.executors.accounts


import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.models.fandoms.KarmaInFandom
import com.dzen.campfire.api.requests.accounts.RAccountsKarmaInFandomsGetAll
import com.dzen.campfire.server.tables.TCollisions
import com.dzen.campfire.server.tables.TFandoms
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.Sql
import com.sup.dev.java_pc.sql.SqlQuerySelect

class EAccountsKarmaInFandomsGetAll : RAccountsKarmaInFandomsGetAll(0, 0) {

    override fun check() {

    }

    override fun execute(): Response {

        val v = Database.select("EAccountsKarmaInFandomsGetAll", SqlQuerySelect(TCollisions.NAME,
                TCollisions.value_1,
                TCollisions.collision_id,
                TCollisions.collision_sub_id,
                "(SELECT ${TFandoms.name} FROM ${TFandoms.NAME} WHERE ${TFandoms.id}=${TCollisions.collision_id})",
                "(SELECT ${TFandoms.image_id} FROM ${TFandoms.NAME} WHERE ${TFandoms.id}=${TCollisions.collision_id})",
                "(SELECT ${TFandoms.fandom_closed} FROM ${TFandoms.NAME} WHERE ${TFandoms.id}=${TCollisions.collision_id})",
                "(SELECT ${TFandoms.karma_cof} FROM ${TFandoms.NAME} WHERE ${TFandoms.id}=${TCollisions.collision_id})"
        )
                .where(TCollisions.collision_type, "=", API.COLLISION_KARMA_30)
                .where(TCollisions.owner_id, "=", accountId)
                .where(TCollisions.value_1, "<>", 0)
                .where(Sql.IFNULL("(SELECT ${TFandoms.image_id} FROM ${TFandoms.NAME} WHERE ${TFandoms.id}=${TCollisions.collision_id})", 0), "<>", 0)
                .sort(TCollisions.value_1, false)
                .offset_count(offset, COUNT)
        )

        return Response( Array(v.rowsCount) {
            val k = KarmaInFandom()
            k.karmaCount = v.next()
            k.fandom = Fandom(v.next(), v.next(), v.next(), v.next(), v.nextLongOrZero()==1L, v.next())
            k
        })
    }


}
