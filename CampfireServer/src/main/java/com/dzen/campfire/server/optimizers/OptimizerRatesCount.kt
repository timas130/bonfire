package com.dzen.campfire.server.optimizers

import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.tables.TAccounts

object OptimizerRatesCount {

    val cash = HashMap<Long, Long>()

    fun get(accountId: Long):Long {
        var count:Long?
        synchronized(cash) {
            count = cash[accountId]
        }
        if (count == null) {
            val v = ControllerAccounts.get(accountId,
                    TAccounts.RATES_COUNT_NO_ANON
            )
            count = if (v.hasNext()) v.next() else 0L
            synchronized(cash) {
                cash[accountId] = count?:0L
            }
        }
        return count?:0L
    }

    fun increment(accountId: Long) {
        synchronized(cash) {
            cash[accountId] = get(accountId) + 1
        }
    }


}