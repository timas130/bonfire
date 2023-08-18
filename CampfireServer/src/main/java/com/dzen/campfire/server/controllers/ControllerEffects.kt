package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.models.account.MAccountEffect
import com.dzen.campfire.api.models.notifications.account.NotificationEffectAdd
import com.dzen.campfire.api.models.notifications.account.NotificationEffectRemove
import com.dzen.campfire.api.models.publications.events_admins.ApiEventAdminEffectAdd
import com.dzen.campfire.api.models.publications.events_admins.ApiEventAdminEffectRemove
import com.dzen.campfire.api.models.publications.events_user.ApiEventUserEffectAdd
import com.dzen.campfire.api.models.publications.events_user.ApiEventUserEffectRemove
import com.dzen.campfire.server.optimizers.OptimizerEffects
import com.dzen.campfire.server.tables.TAccountsEffects
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.ResultRows
import com.sup.dev.java_pc.sql.SqlQueryRemove
import com.sup.dev.java_pc.sql.SqlQuerySelect

object ControllerEffects {



    fun get(effectId:Long):MAccountEffect?{
        val v = Database.select("ControllerEffects.get", instanceSelect()
                .where(TAccountsEffects.id, "=", effectId)
        )
        if(v.isEmpty) return null
        return parseSelect(v)[0]
    }

    fun instanceSelect() = SqlQuerySelect(TAccountsEffects.NAME,
            TAccountsEffects.id,
            TAccountsEffects.account_id,
            TAccountsEffects.date_create,
            TAccountsEffects.date_end,
            TAccountsEffects.comment,
            TAccountsEffects.effect_index,
            TAccountsEffects.from_account_name,
            TAccountsEffects.effect_tag,
            TAccountsEffects.effect_comment_tag)


    fun parseSelect(v: ResultRows): Array<MAccountEffect> {
        val list = ArrayList<MAccountEffect>()
        while (v.hasNext()) {
            val m = MAccountEffect()

            m.id = v.next()
            m.accountId = v.next()
            m.dateCreate = v.next()
            m.dateEnd = v.next()
            m.comment = v.next()
            m.effectIndex = v.next()
            m.fromAccountName = v.next()
            m.tag = v.next()
            m.commentTag = v.next()

            list.add(m)
        }

        return list.toTypedArray()
    }

    fun makeSystem(account:Account, effectIndex:Long, dateEnd:Long, commentTag:Long=0) = make(account, effectIndex, dateEnd,  "",  API.EFFECT_TAG_SOURCE_SYSTEM, Account(), commentTag)

    fun makeAdmin(account:Account, effectIndex:Long, dateEnd:Long, comment:String, fromAccount:Account) :MAccountEffect{
        val m = make(account, effectIndex, dateEnd, comment, 0, fromAccount, 0)
        ControllerPublications.event(ApiEventAdminEffectAdd(fromAccount.id, fromAccount.name, fromAccount.imageId, fromAccount.sex, account.id, account.name, account.imageId, account.sex, comment, m), fromAccount.id)
        return m
    }

    fun make(account:Account, effectIndex:Long, dateEnd:Long, comment:String, tag:Long, fromAccount:Account, commentTag:Long):MAccountEffect{
        val m = MAccountEffect()
        m.accountId = account.id
        m.dateCreate = System.currentTimeMillis()
        m.dateEnd = dateEnd
        m.comment = comment
        m.effectIndex = effectIndex
        m.fromAccountName = fromAccount.name
        m.tag = tag
        m.commentTag = commentTag

        m.id = put(m)

        OptimizerEffects.add(m)
        ControllerNotifications.push(account.id, NotificationEffectAdd(m, fromAccount.name, fromAccount.sex, fromAccount.imageId))
        ControllerPublications.event(ApiEventUserEffectAdd(account.id, account.name, account.imageId, account.sex, fromAccount.id, fromAccount.name, fromAccount.imageId, fromAccount.sex, comment, m), account.id)


        return m
    }

    fun remove(account:Account, m:MAccountEffect, comment: String, fromAccount:Account){

        Database.remove("ControllerEffects.remove", SqlQueryRemove(TAccountsEffects.NAME)
                .where(TAccountsEffects.id, "=", m.id)
        )

        OptimizerEffects.remove(m.accountId, m.id)
        ControllerNotifications.push(m.accountId, NotificationEffectRemove(m.id, m.effectIndex, comment, fromAccount.name, fromAccount.sex, fromAccount.imageId))
        ControllerPublications.event(ApiEventUserEffectRemove(account.id, account.name, account.imageId, account.sex, fromAccount.id, fromAccount.name, fromAccount.imageId, fromAccount.sex, comment, m.id, m.effectIndex), account.id)
        ControllerPublications.event(ApiEventAdminEffectRemove(fromAccount.id, fromAccount.name, fromAccount.imageId, fromAccount.sex, account.id, account.name, account.imageId, account.sex, comment, m.id, m.effectIndex), fromAccount.id)

    }

    fun put(m:MAccountEffect):Long{
        return Database.insert("ControllerEffects.put", TAccountsEffects.NAME,
                TAccountsEffects.account_id, m.accountId,
                TAccountsEffects.date_create, m.dateCreate,
                TAccountsEffects.date_end, m.dateEnd,
                TAccountsEffects.comment,m.comment,
                TAccountsEffects.effect_index,m.effectIndex,
                TAccountsEffects.from_account_name, m.fromAccountName,
                TAccountsEffects.effect_tag, m.tag,
                TAccountsEffects.effect_comment_tag, m.commentTag
        )
    }


}