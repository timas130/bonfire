package com.dzen.campfire.server.optimizers

import com.dzen.campfire.api.API
import com.dzen.campfire.server.tables.TSupport
import com.sup.dev.java.tools.ToolsDate
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.Sql
import com.sup.dev.java_pc.sql.SqlQuerySelect

object OptimizerSponsor {

    private val sponsorsCash = HashMap<Long, Long>()
    private val sponsorsCountCash = HashMap<Long, Long>()
    private var sponsorsDate = 0L

    private fun sponsorsUpdate() {
        val date = ToolsDate.getStartOfMonth()
        if (sponsorsDate != date) {
            sponsorsCash.clear()
            sponsorsCountCash.clear()
        }
        sponsorsDate = date
    }

    fun getSponsor(accountId: Long): Long {
        sponsorsUpdate()
        if (sponsorsCash[accountId] == null) {

            val v = Database.select("ControllerOptimizer getSponsor 1", SqlQuerySelect(TSupport.NAME, TSupport.id)
                    .where(TSupport.user_id, "=", accountId)
                    .where(TSupport.date, "=", sponsorsDate)
                    .where(TSupport.status, "=", API.STATUS_PUBLIC)
            )

            if(v.hasNext()){

                val vv = Database.select("ControllerOptimizer getSponsor 2", SqlQuerySelect(TSupport.NAME, Sql.SUM(TSupport.count))
                        .where(TSupport.user_id, "=", accountId)
                        .where(TSupport.status, "=", API.STATUS_PUBLIC)
                )

                sponsorsCash[accountId] = vv.nextLongOrZero()
                sponsorsCountCash.remove(accountId)
            } else{
                sponsorsCash[accountId] = 0L
            }


        }

        return sponsorsCash[accountId]!!
    }

    fun getSponsorTimes(accountId: Long): Long {
        sponsorsUpdate()

        if (sponsorsCountCash[accountId] == null) {

            if (getSponsor(accountId) > 0) {

                val v = Database.select("ControllerOptimizer getSponsorTimes", SqlQuerySelect(TSupport.NAME, TSupport.date)
                        .where(TSupport.user_id, "=", accountId)
                        .where(TSupport.status, "=", API.STATUS_PUBLIC)
                )

                val dates = ArrayList<Long>()
                while (v.hasNext()) {
                    val date = v.nextLongOrZero()
                    if (!dates.contains(date)) dates.add(date)
                }

                sponsorsCountCash[accountId] = dates.size.toLong()
            } else {
                sponsorsCountCash[accountId] = 0
            }
        }
        return sponsorsCountCash[accountId]!!
    }

    fun setSponsor(accountId: Long, count: Long) {
        sponsorsUpdate()
        sponsorsCash[accountId] = getSponsor(accountId) + count
        sponsorsCountCash.remove(accountId)
    }

}