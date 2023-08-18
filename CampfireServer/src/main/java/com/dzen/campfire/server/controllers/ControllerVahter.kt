package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.server.optimizers.OptimizerEffects

object ControllerVahter {


    fun isCanBlock(accountId:Long):Long{
        return OptimizerEffects.get(accountId, API.EFFECT_INDEX_VAHTER)?.commentTag?:-1
    }

    fun addAdminRejected(accountId: Long) {
        synchronized(this) {
            if(isCanBlock(accountId) == -1L){
                ControllerEffects.makeSystem(ControllerAccounts.getAccount(accountId)!!, API.EFFECT_INDEX_VAHTER, System.currentTimeMillis() + 1000L * 60 * 60 * 24, API.EFFECT_COMMENT_TAG_REJECTED)
            }
        }
    }

    private val MAX_COUNT = 10
    private val BLOCK_TIME = 1000L * 60 * 60 * 24
    private val cash = HashMap<Long, ArrayList<Long>>()

    fun addAdminPublicationBlock(accountId: Long) {
        synchronized(this) {
            if(!cash.containsKey(accountId)) cash[accountId] = ArrayList()
            cash[accountId]!!.add(System.currentTimeMillis())

            if(cash[accountId]!!.size >= MAX_COUNT){
                while (cash[accountId]!!.isNotEmpty()){
                    if(cash[accountId]!![0] < System.currentTimeMillis() - BLOCK_TIME){
                        cash[accountId]!!.removeAt(0)
                    }else{
                        break
                    }
                }
                if(cash[accountId]!!.size >= MAX_COUNT){
                    if(isCanBlock(accountId) == -1L){
                        ControllerEffects.makeSystem(ControllerAccounts.getAccount(accountId)!!, API.EFFECT_INDEX_VAHTER, System.currentTimeMillis() + 1000L * 60 * 60, API.EFFECT_COMMENT_TAG_TOO_MANY)
                    }
                }
            }


        }
    }



}