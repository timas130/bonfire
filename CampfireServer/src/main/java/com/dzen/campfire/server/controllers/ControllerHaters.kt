package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.server.optimizers.OptimizerEffects

object ControllerHaters {

    private val MAX_RATE_COUNT = 5
    private val BLOCK_TIME = 1000L * 60 * 5

    private val cash = HashMap<Long, ArrayList<Long>>()

    fun addDown(accountId:Long){
        synchronized(cash){
            if(!cash.containsKey(accountId)) cash[accountId] = ArrayList()
            cash[accountId]!!.add(System.currentTimeMillis())

            if(cash[accountId]!!.size >= MAX_RATE_COUNT){
                while (cash[accountId]!!.isNotEmpty()){
                    if(cash[accountId]!![0] < System.currentTimeMillis() - BLOCK_TIME){
                        cash[accountId]!!.removeAt(0)
                    }else{
                        break
                    }
                }
                if(cash[accountId]!!.size >= MAX_RATE_COUNT){
                    if(canKarmaDown(accountId)){
                        ControllerEffects.makeSystem(ControllerAccounts.getAccount(accountId)!!, API.EFFECT_INDEX_HATE, System.currentTimeMillis() + 1000L * 60 * 5, API.EFFECT_COMMENT_HATE)
                    }
                }
            }
        }
    }

    fun canKarmaDown(accountId:Long):Boolean{
        return OptimizerEffects.get(accountId, API.EFFECT_INDEX_HATE) == null
    }

}
