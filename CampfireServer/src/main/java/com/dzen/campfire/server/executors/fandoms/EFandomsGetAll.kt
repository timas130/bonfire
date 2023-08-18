package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.requests.fandoms.RFandomsGetAll
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.tables.TCollisions
import com.dzen.campfire.server.tables.TFandoms
import com.sup.dev.java.classes.items.Item2
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.Sql
import com.sup.dev.java_pc.sql.SqlQuerySelect
import com.sup.dev.java_pc.sql.SqlWhere
import java.util.*

class EFandomsGetAll : RFandomsGetAll(0, 0, 0, 0, "", emptyArray(), emptyArray(),emptyArray(),emptyArray()) {

    @Throws(ApiException::class)
    override fun check() {

    }

    override fun execute(): Response {
        if (name.isEmpty()
                && categoryId == 0L
                && params1.isEmpty()
                && params2.isEmpty()
                && params3.isEmpty()
                && params4.isEmpty()
        ) return Response(loadNoSearch())
        else return Response(loadSearch())
    }

    private fun instanceSelect(): SqlQuerySelect {
        return ControllerFandom.instanceSelect()
                .where(TFandoms.status, "=", API.STATUS_PUBLIC)
                .sort(TFandoms.subscribers_count, false, TFandoms.date_create)
                .offset_count(offset, COUNT)
    }

    private fun select(select: SqlQuerySelect, infoArray: Array<Item2<Long, Long>>): Array<Fandom> {
        val v = Database.select("EFandomsGetAll.select", select)

        val fandomsResult = ArrayList<Fandom>()

        for (i in 0 until v.rowsCount) {
            val fandom = ControllerFandom.parseSelectOne(v)

            if (subscribedStatus != SUBSCRIBE_YES) {
                fandomsResult.add(fandom)
            } else {
                for (anInfoArray in infoArray)
                    if (anInfoArray.a1 == fandom.id) {
                        val f = Fandom()
                        f.id = fandom.id
                        f.name = fandom.name
                        f.imageId = fandom.imageId
                        f.imageTitleId = fandom.imageTitleId
                        f.dateCreate = fandom.dateCreate
                        f.status = fandom.status
                        f.subscribesCount = fandom.subscribesCount
                        f.karmaCof = fandom.karmaCof
                        f.languageId = anInfoArray.a2
                        fandomsResult.add(f)
                    }
            }

        }

        return Array(fandomsResult.size) { fandomsResult[it] }
    }

    //
    //  No Search
    //

    private fun loadNoSearch(): Array<Fandom> {
        val vv = Database.select("EFandomsGetAll.loadNoSearch", SqlQuerySelect(TCollisions.NAME, TCollisions.collision_id, TCollisions.collision_sub_id)
                .where(TCollisions.collision_type, "=", API.COLLISION_FANDOM_SUBSCRIBE)
                .where(TCollisions.owner_id, "=", apiAccount.id)
                .sort(TCollisions.collision_date_create, false))

        if (subscribedStatus == SUBSCRIBE_YES && vv.isEmpty) return emptyArray()

        val infoArray = Array(vv.rowsCount) { Item2(vv.next<Long>(), vv.next<Long>()) }
        val ids = Array(infoArray.size) { infoArray[it].a1 }

        val select = instanceSelect()

        if (subscribedStatus != SUBSCRIBE_NONE)
            if (!ids.isEmpty()) select.where(SqlWhere.WhereIN(TFandoms.id, subscribedStatus != SUBSCRIBE_YES, ids))

        return select(select, infoArray)
    }

    //
    //  Search
    //

    private fun loadSearch(): Array<Fandom>{
        val select = instanceSelect()

        if (name.isNotEmpty()) {

            val v = Database.select("EFandomsGetAll.loadSearch",SqlQuerySelect(TCollisions.NAME, TCollisions.owner_id)
                    .where(TCollisions.collision_type, "=", API.COLLISION_FANDOM_NAMES)
                    .whereValue(SqlWhere.WhereLIKE(TCollisions.value_2, false), "%${Sql.mirror(name.lowercase(Locale.getDefault()))}%"))
            val ids = Array<Long>(v.rowsCount){v.next()}

            if(ids.isNotEmpty()) select.whereValue(SqlWhere.WhereString("(${SqlWhere.WhereLIKE(TFandoms.name, false).toQuery()} OR ${SqlWhere.WhereIN(TFandoms.id, ids).toQuery()})"), "%${Sql.mirror(
                name.lowercase(Locale.getDefault())
            )}%")
            else select.whereValue(SqlWhere.WhereLIKE(TFandoms.name, false), "%${Sql.mirror(name.lowercase(Locale.getDefault()))}%")
        }

        if(categoryId != 0L) select.where(TFandoms.fandom_category, "=", categoryId)

        if(params1.isNotEmpty())  addParamsWhere(select, params1, API.COLLISION_FANDOM_PARAMS_1)
        if(params2.isNotEmpty())  addParamsWhere(select, params2, API.COLLISION_FANDOM_PARAMS_2)
        if(params3.isNotEmpty())  addParamsWhere(select, params3, API.COLLISION_FANDOM_PARAMS_3)
        if(params4.isNotEmpty())  addParamsWhere(select, params4, API.COLLISION_FANDOM_PARAMS_4)

        return select(select, emptyArray())
    }

    private fun addParamsWhere(select:SqlQuerySelect,params:Array<Long>,paramType:Long){
        if(params.isEmpty())return
        if (params.isNotEmpty())
            for (genre in params) {
                select.where(SqlWhere.WhereString(TFandoms.id + "=(SELECT ${TCollisions.owner_id} FROM ${TCollisions.NAME} " +
                        "WHERE ${TCollisions.collision_type}=$paramType AND " +
                        "${TCollisions.collision_id}=$genre AND " +
                        "${TCollisions.owner_id}=${TFandoms.NAME}.${TFandoms.id} ${Sql.LIMIT} 1)"))
            }

    }
}
