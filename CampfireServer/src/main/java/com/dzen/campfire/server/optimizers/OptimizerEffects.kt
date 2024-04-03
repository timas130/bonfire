package com.dzen.campfire.server.optimizers

import com.dzen.campfire.api.models.account.MAccountEffect
import com.dzen.campfire.server.controllers.ControllerEffects
import com.dzen.campfire.server.tables.TAccountsEffects
import com.sup.dev.java_pc.sql.Database
import java.util.concurrent.ConcurrentHashMap

object OptimizerEffects {
    private val cache = ConcurrentHashMap<Long, Array<MAccountEffect>>()

    fun get(accountId: Long, effectIndex: Long): MAccountEffect? {
        val effects = get(accountId)
        for (i in effects) {
            if (i.effectIndex == effectIndex && i.dateEnd > System.currentTimeMillis()) {
                return i
            }
        }
        return null
    }

    fun get(accountId: Long): Array<MAccountEffect> {
        var list = cache[accountId]
        if (list == null) {
            val v = Database.select(
                "OptimizerEffects", ControllerEffects.instanceSelect()
                    .where(TAccountsEffects.account_id, "=", accountId)
                    .where(TAccountsEffects.date_end, ">", System.currentTimeMillis())
                    .sort(TAccountsEffects.date_create, true)
            )

            list = ControllerEffects.parseSelect(v)

            cache[accountId] = list
        }
        return list
    }

    fun add(effect: MAccountEffect) {
        cache.compute(effect.accountId) { _, effects ->
            val time = System.currentTimeMillis()
            val newList = effects
                ?.filter { it.dateEnd < time }
                ?.toMutableList()
                ?: mutableListOf()

            newList.add(effect)

            newList.toTypedArray()
        }
    }

    fun remove(accountId: Long, effectId: Long) {
        cache.compute(accountId) { _, effects ->
            effects?.filter { it.id == effectId }?.toTypedArray()
        }
    }
}
