package com.dzen.campfire.server.optimizers

import com.dzen.campfire.api.API
import com.dzen.campfire.server.tables.TSupport
import com.sup.dev.java.tools.ToolsDate
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.Sql
import com.sup.dev.java_pc.sql.SqlQuerySelect
import java.util.concurrent.ConcurrentHashMap

object OptimizerSponsor {

    private val sponsorAmountCache = ConcurrentHashMap<Long, Long>()
    private val sponsorMonthsCache = ConcurrentHashMap<Long, Long>()
    private var lastStartOfMonth = 0L

    private fun sponsorsUpdate() {
        val date = ToolsDate.getStartOfMonth()
        if (lastStartOfMonth != date) {
            sponsorAmountCache.clear()
            sponsorMonthsCache.clear()
        }
        lastStartOfMonth = date
    }

    fun getSponsor(accountId: Long): Long {
        sponsorsUpdate()

        val cachedAmount = sponsorAmountCache[accountId]

        return if (cachedAmount != null) {
            cachedAmount
        } else {
            val v = Database.select(
                "ControllerOptimizer getSponsor 1", SqlQuerySelect(TSupport.NAME, TSupport.id)
                    .where(TSupport.user_id, "=", accountId)
                    .where(TSupport.date, "=", lastStartOfMonth)
                    .where(TSupport.status, "=", API.STATUS_PUBLIC)
            )

            if (v.hasNext()) {
                val vv = Database.select(
                    "ControllerOptimizer getSponsor 2", SqlQuerySelect(TSupport.NAME, Sql.SUM(TSupport.count))
                        .where(TSupport.user_id, "=", accountId)
                        .where(TSupport.status, "=", API.STATUS_PUBLIC)
                )
                val sum = vv.nextLongOrZero()

                sponsorAmountCache[accountId] = sum
                sponsorMonthsCache.remove(accountId)
                sum
            } else {
                sponsorAmountCache[accountId] = 0L
                0L
            }
        }
    }

    fun getSponsorTimes(accountId: Long): Long {
        sponsorsUpdate()

        val cachedCount = sponsorMonthsCache[accountId]

        @Suppress("IfThenToElvis") // nah, that's too kotlin for me
        return if (cachedCount != null) {
            cachedCount
        } else {
            if (getSponsor(accountId) > 0) {
                val v = Database.select(
                    "ControllerOptimizer getSponsorTimes", SqlQuerySelect(TSupport.NAME, Sql.COUNT_DISTINCT(TSupport.date))
                        .where(TSupport.user_id, "=", accountId)
                        .where(TSupport.status, "=", API.STATUS_PUBLIC)
                )
                val months = v.nextLongOrZero()

                sponsorMonthsCache[accountId] = months
                0L
            } else {
                sponsorMonthsCache[accountId] = 0L
                0L
            }
        }
    }

    fun setSponsor(accountId: Long, count: Long) {
        sponsorsUpdate()
        sponsorAmountCache.compute(accountId) { _, previous ->
            (previous ?: 0L) + count
        }
        sponsorMonthsCache.remove(accountId) // flush cache for this one
    }
}
