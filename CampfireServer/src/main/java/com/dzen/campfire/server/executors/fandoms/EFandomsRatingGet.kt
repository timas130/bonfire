package com.dzen.campfire.server.executors.fandoms

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.requests.fandoms.RFandomsRatingGet
import com.dzen.campfire.server.tables.TAccounts
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerOptimizer
import com.sup.dev.java.tools.ToolsMapper
import com.dzen.campfire.server.tables.TCollisions
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect
import com.sup.dev.java_pc.sql.SqlWhere

class EFandomsRatingGet : RFandomsRatingGet(0, 0, 0) {

    private var karmaAccounts: Array<Account?>? = null
    private var karmaCounts: Array<Long?>? = null

    @Throws(ApiException::class)
    override fun check() {
        super.check()
    }

    override fun execute(): Response {

        loadK()

        return Response(ToolsMapper.asNonNull(karmaAccounts!!), ToolsMapper.asNonNull(karmaCounts!!))
    }

    private fun loadK() {
        val select = SqlQuerySelect(TCollisions.NAME,
                TCollisions.owner_id,
                TCollisions.value_1)
        select.where(TCollisions.collision_id, "=", fandomId)
        select.where(TCollisions.collision_sub_id, "=", languageId)
        select.where(TCollisions.value_1, "<>", 0)
        select.where(TCollisions.collision_type, "=", API.COLLISION_KARMA_30)
        select.offset_count(offset, COUNT)
        select.sort(TCollisions.value_1, false)
        val v = Database.select("EFandomsRatingGet.loadK",select)

        if (v.isEmpty) {
            karmaAccounts = arrayOfNulls(0)
            karmaCounts = emptyArray()
            return
        }

        val ids = arrayOfNulls<Long>(v.rowsCount)
        karmaCounts = arrayOfNulls(v.rowsCount)

        for (i in 0 until v.rowsCount) {
            ids[i] = v.next()
            karmaCounts!![i] = v.next()
        }

        karmaAccounts = loadAccounts(ids)
    }

    private fun loadAccounts(ids: Array<Long?>): Array<Account?> {
        val select = SqlQuerySelect(TAccounts.NAME,
                TAccounts.id,
                TAccounts.lvl,
                TAccounts.last_online_time,
                TAccounts.name,
                TAccounts.img_id,
                TAccounts.sex,
                TAccounts.karma_count_30)

        select.where(SqlWhere.WhereIN(TAccounts.id, ids))
        val v = Database.select("EFandomsRatingGet.loadAccounts", select)

        val accounts = arrayOfNulls<Account>(v.rowsCount)
        for (i in 0 until v.rowsCount) accounts[i] = ControllerAccounts.instance(v)

        val list = ArrayList<Account>()
        for(i in ids) for (n in accounts) if(n!!.id == i){
            list.add(n)
            break
        }

        return list.toTypedArray()
    }

}
