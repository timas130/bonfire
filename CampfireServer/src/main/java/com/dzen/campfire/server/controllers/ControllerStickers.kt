package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.server.optimizers.OptimizerStickersCount
import com.dzen.campfire.server.tables.TAccounts
import com.dzen.campfire.server.tables.TCollisions
import com.sup.dev.java.libs.debug.info
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.Sql
import com.sup.dev.java_pc.sql.SqlQueryRemove
import com.sup.dev.java_pc.sql.SqlQuerySelect

object ControllerStickers {

    fun getStickersPacksCount(accountId:Long):Long{
       return Database.select("ControllerStickers select_stickers_count", SqlQuerySelect(TCollisions.NAME, Sql.COUNT)
                .where(TCollisions.collision_type, "=", API.COLLISION_ACCOUNT_STICKERPACKS)
                .where(TCollisions.owner_id, "=", accountId)).nextLongOrZero()
    }

    fun getStickersCount(accountId:Long):Long{
       return Database.select("ControllerStickers select_stickers_count", SqlQuerySelect(TCollisions.NAME, Sql.COUNT)
                .where(TCollisions.collision_type, "=", API.COLLISION_ACCOUNT_STICKERS)
                .where(TCollisions.owner_id, "=", accountId)).nextLongOrZero()
    }

    fun getStickersPacksIds(accountId:Long):Array<Long>{
        val selectIds = SqlQuerySelect(TCollisions.NAME, TCollisions.collision_id)
                .where(TCollisions.collision_type, "=", API.COLLISION_ACCOUNT_STICKERPACKS)
                .where(TCollisions.owner_id, "=", accountId)

        val vIds = Database.select("ControllerStickers getStickersPacksIds", selectIds)
        val ids = Array<Long>(vIds.rowsCount) { vIds.next() }

        return ids
    }

    fun getStickersIds(accountId:Long):Array<Long>{
        val selectIds = SqlQuerySelect(TCollisions.NAME, TCollisions.collision_id)
                .where(TCollisions.collision_type, "=", API.COLLISION_ACCOUNT_STICKERS)
                .where(TCollisions.owner_id, "=", accountId)

        val vIds = Database.select("ControllerStickers getStickersIds", selectIds)
        val ids = Array<Long>(vIds.rowsCount) { vIds.next() }

        return ids
    }

    fun removeCollisionsStickersPack(id:Long){
        Database.remove("ControllerStickers removeCollisionsStickersPack", SqlQueryRemove(TCollisions.NAME)
                .where(TCollisions.collision_id, "=",id)
                .where(TCollisions.collision_type, "=", API.COLLISION_ACCOUNT_STICKERPACKS))
    }

    fun removeCollisionsSticker(id:Long){
        val v = Database.select("ControllerStickers removeCollisionsSticker select", SqlQuerySelect(TCollisions.NAME, TCollisions.owner_id)
                .where(TCollisions.collision_id, "=",id)
                .where(TCollisions.collision_type, "=", API.COLLISION_ACCOUNT_STICKERS))
        while (v.hasNext()){
            OptimizerStickersCount.decrement(v.nextLongOrZero())
        }
        Database.remove("ControllerStickers removeCollisionsSticker", SqlQueryRemove(TCollisions.NAME)
                .where(TCollisions.collision_id, "=",id)
                .where(TCollisions.collision_type, "=", API.COLLISION_ACCOUNT_STICKERS))
    }

    fun removeDeadStickersCollisions(){
        val v = Database.select("ControllerStickers removeDeadStickersCollisions getAccounts", SqlQuerySelect(TAccounts.NAME, TAccounts.id))
        var x = 0
        while (v.hasNext()) {
            removeDeadStickersCollisions(v.next())
            x++
            info("$x / ${v.rowsCount}")
        }
    }

    fun removeDeadStickersCollisions(accountId:Long){
        val stickersPacksIds = getStickersPacksIds(accountId)
        for (i in stickersPacksIds) {
            val publication = ControllerPublications.getPublication(i, 1)
            if(publication == null || publication.status != API.STATUS_PUBLIC) removeCollisionsStickersPack(i)
        }
        val stickersIds = getStickersIds(accountId)
        for (i in stickersIds) {
            val publication = ControllerPublications.getPublication(i, 1)
            if(publication == null || publication.status != API.STATUS_PUBLIC) removeCollisionsSticker(i)
        }
    }

}