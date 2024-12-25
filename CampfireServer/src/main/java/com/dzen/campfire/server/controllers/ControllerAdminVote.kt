package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.admins.*
import com.dzen.campfire.server.admin_votes.*

object ControllerAdminVote {

    val NEED_VOTES = 3

    val map = HashMap<Long, MAdminVote>()

    fun getCount(accountId:Long) : Long {
        var count = 0L
        synchronized(map){
            for(m in map.values)
                if(!m.votes.contains(accountId))
                    count++

        }
        return count
    }

    fun getForAccount(accountId:Long) : MAdminVote? {
        synchronized(map){
            val list = ArrayList<MAdminVote>()
            for(m in map.values)
                if(!m.votes.contains(accountId))
                    list.add(m)
            if (list.isNotEmpty()) {
                return list.random()
            }
        }
        return null
    }

    fun getById(voteId:Long) : MAdminVote? {
        var m:MAdminVote?
        synchronized(map){ m = map[voteId] }
        return m
    }

    fun voteAccept(id:Long, accountId:Long){
        var mForParse:MAdminVote? = null
        synchronized(map){
            val m = map[id] ?: return
            m.votes.add(accountId)
            if(API.PROTOADMINS.contains(accountId) || m.votes.size >= NEED_VOTES){
                map.remove(id)
                mForParse = m   //  Чтобы выйти из synchronized
            }
        }
        if(mForParse != null) parseAccept(mForParse!!)
    }

    fun voteCancel(cancelAdminAccountId:Long, id:Long){
        var mForParse:MAdminVote?
        synchronized(map){
            val m = map[id] ?: return
            map.remove(id)
            mForParse = m  //  Чтобы выйти из synchronized
        }
        if(mForParse != null) parseCancel(mForParse!!, cancelAdminAccountId)
    }

    fun addAction(m:MAdminVote){
        synchronized(map){
            var id = System.nanoTime()
            while (map[id] != null){
                id++
            }
            m.id = id
            map[id] = m
        }
    }

    fun parseAccept(m:MAdminVote){
        if(m is MAdminVoteAccountRecountAchi) PAdminVoteAccountRecountAchi().accept(m)
        if(m is MAdminVoteAccountChangeName) PAdminVoteAccountChangeName().accept(m)
        if(m is MAdminVoteAccountEffect) PAdminVoteAccountEffect().accept(m)
        if(m is MAdminVoteAccountRecountKarma) PAdminVoteAccountRecountKarma().accept(m)
        if(m is MAdminVoteAccountRemoveAvatar) PAdminVoteAccountRemoveAvatar().accept(m)
        if(m is MAdminVoteAccountRemoveBackground) PAdminVoteAccountRemoveBackground().accept(m)
        if(m is MAdminVoteAccountRemoveName) PAdminVoteAccountRemoveName().accept(m)
        if(m is MAdminVoteAccountRemoveReports) PAdminVoteAccountRemoveReports().accept(m)
        if(m is MAdminVoteAccountRemoveStatus) PAdminVoteAccountRemoveStatus().accept(m)
        if(m is MAdminVoteFandomRemove) PAdminVoteFandomRemove().accept(m)
    }

    fun parseCancel(m:MAdminVote, cancelAdminAccountId:Long){
        if(m is MAdminVoteAccountPunish) PAdminVoteAccountPunish().cancel(m, cancelAdminAccountId)
    }


}
