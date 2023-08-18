package com.dzen.campfire.server.optimizers

import com.dzen.campfire.api.API
import com.dzen.campfire.server.controllers.ControllerCollisions

object OptimizerSubscribedFandoms {

    val cash = HashMap<Long, Long>()

    fun get(accountId: Long):Long {
        var count:Long?
        synchronized(cash) {
            count = cash[accountId]
        }
        if (count == null) {
            count = ControllerCollisions.getCollisionsCount(accountId, API.COLLISION_FANDOM_SUBSCRIBE)
            synchronized(cash) {
                cash[accountId] = count?:0L
            }
        }
        return count?:0L
    }

    fun increment(accountId: Long) {
        synchronized(OptimizerRatesCount.cash) {
            OptimizerRatesCount.cash[accountId] = (OptimizerRatesCount.cash[accountId]?:0L) + 1
        }
    }

    fun decrement(accountId: Long) {
        synchronized(OptimizerRatesCount.cash) {
            OptimizerRatesCount.cash[accountId] = (OptimizerRatesCount.cash[accountId]?:0L) - 1
        }
    }
}