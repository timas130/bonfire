package com.dzen.campfire.server.optimizers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.account.MAccountEffect
import com.dzen.campfire.server.controllers.ControllerEffects
import com.dzen.campfire.server.tables.TAccountsEffects
import com.sup.dev.java_pc.sql.Database

object OptimizerEffects {

    val cash = HashMap<Long, ArrayList<MAccountEffect>>()

    fun get(accountId:Long, effectIndex:Long):MAccountEffect?{
        val effects = get(accountId)
        for(i in effects){
            if(i.effectIndex == effectIndex && i.dateEnd > System.currentTimeMillis()){
                return i
            }
        }
        return null
    }

    fun get(accountId: Long): Array<MAccountEffect> {
        var list: ArrayList<MAccountEffect>?
        synchronized(cash) {
            list = cash[accountId]
        }
        if (list == null) {
            list = ArrayList()

            val v = Database.select("OptimizerEffects", ControllerEffects.instanceSelect()
                    .where(TAccountsEffects.account_id, "=", accountId)
                    .where(TAccountsEffects.date_end, ">", System.currentTimeMillis())
                    .sort(TAccountsEffects.date_create, true))

            val array = ControllerEffects.parseSelect(v)

            list!!.addAll(array)

            synchronized(cash) {
                cash[accountId] = list!!
            }
        }
        return list!!.toTypedArray()
    }

    fun add(mAccountEffect: MAccountEffect) {
        synchronized(OptimizerRatesCount.cash) {
            var list = cash[mAccountEffect.accountId]
            if (list == null) {
                list = ArrayList()
                cash[mAccountEffect.accountId] = list
            }
            val removeList = ArrayList<MAccountEffect>()
            for(i in list) if(i.dateEnd < System.currentTimeMillis()) removeList.add(i)
            list.removeAll(removeList)
            list.add(mAccountEffect)
        }
    }

    fun remove(accountId: Long, effectId: Long) {
        synchronized(OptimizerRatesCount.cash) {
            val list = cash[accountId] ?: return
            for (i in list) if (i.id == effectId) {
                list.remove(i)
                break
            }
        }
    }


}