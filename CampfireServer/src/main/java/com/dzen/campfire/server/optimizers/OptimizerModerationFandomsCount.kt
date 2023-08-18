package com.dzen.campfire.server.optimizers

import com.dzen.campfire.server.controllers.ControllerFandom

object OptimizerModerationFandomsCount {

    var lastUpdateTime = System.currentTimeMillis()
    val updateStep = 1000L * 60 * 60 * 12
    val cash = HashMap<Long, Long>()

    fun get(accountId: Long):Long {
        var count:Long?
        synchronized(cash) {
            if(lastUpdateTime < System.currentTimeMillis() - updateStep){
                lastUpdateTime = System.currentTimeMillis()
                cash.clear()
            }
            count = cash[accountId]
        }
        if (count == null) {
            count = ControllerFandom.getModerationFandomsCount(accountId)
            synchronized(cash) {
                cash[accountId] = count?:0L
            }
        }
        return count?:0L
    }


}