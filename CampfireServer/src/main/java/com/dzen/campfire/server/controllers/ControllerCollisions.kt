package com.dzen.campfire.server.controllers

import com.dzen.campfire.server.tables.TCollisions
import com.sup.dev.java_pc.sql.*

object ControllerCollisions {

    fun getCollisionSum(ownerId: Long, collisionType: Long): Long {
        val select = SqlQuerySelect(TCollisions.NAME, "SUM(" + TCollisions.collision_id + ")")
        select.where(TCollisions.owner_id, "=", ownerId)
        select.where(TCollisions.collision_type, "=", collisionType)
        val v = Database.select("ControllerCollisions.getCollisionSum", select)
        if (v.isEmpty) return 0
        val n = v.next<Any>()
        return java.lang.Long.parseLong(n.toString() + "")

    }

    fun removeById(id: Long) {
        val remove = SqlQueryRemove(TCollisions.NAME)
        remove.where(TCollisions.id, "=", id)
        Database.remove("ControllerCollisions.removeById", remove)
    }

    fun removeCollisionsValue1(ownerId: Long, collisionId: Long, value1: Long, collisionType: Long) {
        val remove = SqlQueryRemove(TCollisions.NAME)
        remove.where(TCollisions.owner_id, "=", ownerId)
        remove.where(TCollisions.collision_id, "=", collisionId)
        remove.where(TCollisions.value_1, "=", value1)
        remove.where(TCollisions.collision_type, "=", collisionType)
        Database.remove("ControllerCollisions.removeCollisionsValue1", remove)
    }

    fun removeCollisions(ownerId: Long, collisionType: Long) {
        val remove = SqlQueryRemove(TCollisions.NAME)
        remove.where(TCollisions.owner_id, "=", ownerId)
        remove.where(TCollisions.collision_type, "=", collisionType)
        Database.remove("ControllerCollisions.removeCollisions_1", remove)
    }

    fun removeCollisions(ownerId: Long, collisionId: Long, collisionType: Long) {
        val remove = SqlQueryRemove(TCollisions.NAME)
        remove.where(TCollisions.owner_id, "=", ownerId)
        remove.where(TCollisions.collision_type, "=", collisionType)
        remove.where(TCollisions.collision_id, "=", collisionId)
        Database.remove("ControllerCollisions.removeCollisions_2", remove)
    }

    fun removeCollisions(ownerId: Long, collisionId: Long, collisionSubId: Long, collisionType: Long) {
        val remove = SqlQueryRemove(TCollisions.NAME)
        remove.where(TCollisions.owner_id, "=", ownerId)
        remove.where(TCollisions.collision_type, "=", collisionType)
        remove.where(TCollisions.collision_id, "=", collisionId)
        remove.where(TCollisions.collision_sub_id, "=", collisionSubId)
        Database.remove("ControllerCollisions.removeCollisions_3", remove)
    }

    fun getCollisionsOwnerIds(collisionId: Long, collisionType: Long) =
            getCollisionsOwnerIds(collisionId, null, collisionType)

    fun getCollisionsOwnerIds(collisionId: Long, collisionSybId: Long?, collisionType: Long): Array<Long> {
        val query = SqlQuerySelect(TCollisions.NAME, TCollisions.owner_id)
        query.where(TCollisions.collision_id, "=", collisionId)
        query.where(TCollisions.collision_type, "=", collisionType)
        if (collisionSybId != null) query.where(TCollisions.collision_sub_id, "=", collisionSybId)
        val v = Database.select("ControllerCollisions.getCollisionsOwnerIds", query)
        return Array(v.rowsCount) { v.next<Long>() }
    }

    fun getCollisionsCount(ownerId: Long, collisionType: Long): Long {
        val query = SqlQuerySelect(TCollisions.NAME, "COUNT(*)")
        query.where(TCollisions.owner_id, "=", ownerId)
        query.where(TCollisions.collision_type, "=", collisionType)
        return Database.select("ControllerCollisions.getCollisionsCount", query).next<Long>()
    }

    fun getCollisionsCount(ownerId: Long, collisionId: Long, collisionType: Long): Long {
        val query = SqlQuerySelect(TCollisions.NAME, "COUNT(*)")
        query.where(TCollisions.owner_id, "=", ownerId)
        query.where(TCollisions.collision_id, "=", collisionId)
        query.where(TCollisions.collision_type, "=", collisionType)
        return Database.select("ControllerCollisions.getCollisionsCount", query).next<Long>()
    }

    fun checkCollisionExist(ownerId: Long, collisionType: Long): Boolean {
        return checkCollisionExist(ownerId, null, null, collisionType)
    }

    fun checkCollisionExist(ownerId: Long, collisionId: Long?, collisionType: Long): Boolean {
        return checkCollisionExist(ownerId, collisionId, null, collisionType)
    }

    fun checkCollisionExist(ownerId: Long, collisionId: Long?, collisionSubId: Long?, collisionType: Long): Boolean {
        return checkCollisionExist(ownerId, collisionId, collisionSubId, collisionType, null, null, null, null, null, null)
    }

    fun checkCollisionExist(
            ownerId: Long,
            collisionId: Long?,
            collisionSubId: Long?,
            collisionType: Long,
            colisionDate: Long?,
            value1: Long?,
            value2: String?,
            value3: Long?,
            value4: Long?,
            value5: Long?
    ): Boolean {
        val query = SqlQuerySelect(TCollisions.NAME, TCollisions.id)
                .where(TCollisions.owner_id, "=", ownerId)
                .where(TCollisions.collision_type, "=", collisionType)
        if (collisionId != null) query.where(TCollisions.collision_id, "=", collisionId)
        if (collisionSubId != null) query.where(TCollisions.collision_sub_id, "=", collisionSubId)
        if (colisionDate != null) query.where(TCollisions.collision_date_create, "=", colisionDate)
        if (value1 != null) query.where(TCollisions.value_1, "=", value1)
        if (value2 != null) query.whereValue(TCollisions.value_2, "=", value2)
        if (value3 != null) query.where(TCollisions.value_3, "=", value3)
        if (value4 != null) query.where(TCollisions.value_4, "=", value4)
        if (value5 != null) query.where(TCollisions.value_5, "=", value5)

        return !Database.select("ControllerCollisions.checkCollisionExist", query).isEmpty
    }

    fun incrementCollisionIdOrCreate(
            ownerId: Long,
            collisionType: Long,
            incrValue: Long
    ) {
        if (checkCollisionExist(ownerId, collisionType)) {
            val update = SqlQueryUpdate(TCollisions.NAME)
            update.where(TCollisions.owner_id, "=", ownerId)
            update.where(TCollisions.collision_type, "=", collisionType)
            update.update(TCollisions.collision_id, TCollisions.collision_id + "+(" + incrValue + ")")
            Database.update("ControllerCollisions.incrementCollisionIdOrCreate", update)
        } else {
            putCollision(
                    ownerId,
                    incrValue,
                    collisionType
            )
        }
    }

    fun incrementCollisionValueOrCreate(
            ownerId: Long,
            collisionId: Long,
            collisionSubId: Long,
            collisionType: Long,
            incrValue: Long
    ) {
        if (checkCollisionExist(ownerId, collisionId, collisionSubId, collisionType)) {
            val update = SqlQueryUpdate(TCollisions.NAME)
            update.where(TCollisions.owner_id, "=", ownerId)
            update.where(TCollisions.collision_id, "=", collisionId)
            update.where(TCollisions.collision_sub_id, "=", collisionSubId)
            update.where(TCollisions.collision_type, "=", collisionType)
            update.update(TCollisions.value_1, TCollisions.value_1 + "+(" + incrValue + ")")
            Database.update("ControllerCollisions.incrementCollisionValueOrCreate", update)
        } else {
            putCollisionValue1(
                    ownerId,
                    collisionId,
                    collisionSubId,
                    collisionType,
                    System.currentTimeMillis(),
                    incrValue
            )
        }
    }

    fun getCollisionDate(ownerId: Long, collisionId: Long, collisionType: Long): Long {
        return getCollisionDate(ownerId, collisionId, 0, collisionType, 0)
    }

    fun getCollisionDate(ownerId: Long, collisionId: Long, collisionSubId: Long, collisionType: Long): Long {
        return getCollisionDate(ownerId, collisionId, collisionSubId, collisionType, 0)
    }

    fun getCollisionDate(
            ownerId: Long,
            collisionId: Long,
            collisionSubId: Long,
            collisionType: Long,
            value1: Long
    ): Long {
        val query = SqlQuerySelect(TCollisions.NAME, TCollisions.collision_date_create)
        query.where(TCollisions.owner_id, "=", ownerId)
        query.where(TCollisions.collision_type, "=", collisionType)
        query.where(TCollisions.collision_id, "=", collisionId)
        if (collisionSubId != 0L) query.where(TCollisions.collision_sub_id, "=", collisionSubId)
        if (value1 != 0L) query.where(TCollisions.value_1, "=", value1)
        query.sort(TCollisions.collision_date_create, false)
        val select = Database.select("ControllerCollisions.getCollisionDate", query)
        return if (select.isEmpty) 0 else select.next()
    }


    //
    //  Get Arrays
    //

    fun getCollisions(ownerId: Long, collisionType: Long): Array<Long> {
        val query = SqlQuerySelect(TCollisions.NAME, TCollisions.collision_id)
                .where(TCollisions.owner_id, "=", ownerId)
                .where(TCollisions.collision_type, "=", collisionType)
        val v = Database.select("ControllerCollisions.getCollisions", query)
        return Array(v.rowsCount) { v.next<Long>() }
    }

    fun getCollisionsValue1(ownerId: Long, collisionType: Long) = getCollisionsValue1(ownerId, 0, collisionType)

    fun getCollisionsValue1(ownerId: Long, collisionId: Long, collisionType: Long) =
            getCollisionsValue1(ownerId, collisionId, 0, collisionType)

    fun getCollisionsValue1(ownerId: Long, collisionId: Long, collisionSubId: Long, collisionType: Long): Array<Long> {
        val query = SqlQuerySelect(TCollisions.NAME, TCollisions.value_1)
                .where(TCollisions.owner_id, "=", ownerId)
                .where(TCollisions.collision_type, "=", collisionType)
        if (collisionId != 0L) query.where(TCollisions.collision_id, "=", collisionId)
        if (collisionSubId != 0L) query.where(TCollisions.collision_sub_id, "=", collisionSubId)
        val v = Database.select("ControllerCollisions.getCollisionsValue1", query)
        return Array(v.rowsCount) { v.next<Long>() }
    }

    fun getCollisionsValue2(ownerId: Long, collisionType: Long) = getCollisionsValue2(ownerId, 0, collisionType)

    fun getCollisionsValue2(ownerId: Long, collisionId: Long, collisionType: Long) =
            getCollisionsValue2(ownerId, collisionId, 0, collisionType)

    fun getCollisionsValue2(
            ownerId: Long,
            collisionId: Long,
            collisionSubId: Long,
            collisionType: Long
    ): Array<String> {
        val query = SqlQuerySelect(TCollisions.NAME, TCollisions.value_2)
        query.where(TCollisions.owner_id, "=", ownerId)
        query.where(TCollisions.collision_type, "=", collisionType)
        if (collisionId != 0L) query.where(TCollisions.collision_id, "=", collisionId)
        if (collisionSubId != 0L) query.where(TCollisions.collision_sub_id, "=", collisionSubId)
        val v = Database.select("ControllerCollisions.getCollisionsValue2", query)
        return Array(v.rowsCount) { v.next<String>() }
    }

    fun getCollisionsValue3(ownerId: Long, collisionType: Long) = getCollisionsValue3(ownerId, 0, collisionType)

    fun getCollisionsValue3(ownerId: Long, collisionId: Long, collisionType: Long) =
            getCollisionsValue3(ownerId, collisionId, 0, collisionType)

    fun getCollisionsValue3(ownerId: Long, collisionId: Long, collisionSubId: Long, collisionType: Long): Array<Long> {
        val query = SqlQuerySelect(TCollisions.NAME, TCollisions.value_3)
        query.where(TCollisions.owner_id, "=", ownerId)
        query.where(TCollisions.collision_type, "=", collisionType)
        if (collisionId != 0L) query.where(TCollisions.collision_id, "=", collisionId)
        if (collisionSubId != 0L) query.where(TCollisions.collision_sub_id, "=", collisionSubId)
        val v = Database.select("ControllerCollisions.getCollisionsValue3", query)
        return Array(v.rowsCount) { v.next<Long>() }
    }

    fun getCollisionValue4(
            ownerId: Long,
            collisionId: Long?,
            collisionSubId: Long?,
            collisionType: Long,
            colisionDate: Long?,
            value1: Long?,
            value2: String?,
            value3: Long?
    ): Long {
        val array = getCollisionsValue4(
                ownerId,
                collisionId,
                collisionSubId,
                collisionType,
                colisionDate,
                value1,
                value2,
                value3
        )
        return if (array.isEmpty()) 0 else array[0]
    }

    fun getCollisionValue5(
            ownerId: Long,
            collisionId: Long?,
            collisionSubId: Long?,
            collisionType: Long,
            colisionDate: Long?,
            value1: Long?,
            value2: String?,
            value3: Long?,
            value4: Long?
    ): Long {
        val array = getCollisionsValue5(
                ownerId,
                collisionId,
                collisionSubId,
                collisionType,
                colisionDate,
                value1,
                value2,
                value3,
                value4
        )
        return if (array.isEmpty()) 0 else array[0]
    }

    fun getCollisionsValue4(
            ownerId: Long,
            collisionId: Long?,
            collisionSubId: Long?,
            collisionType: Long,
            colisionDate: Long?,
            value1: Long?,
            value2: String?,
            value3: Long?
    ): Array<Long> {
        val query = SqlQuerySelect(TCollisions.NAME, TCollisions.value_4)
        query.where(TCollisions.owner_id, "=", ownerId)
        query.where(TCollisions.collision_type, "=", collisionType)
        if (collisionId != null) query.where(TCollisions.collision_id, "=", collisionId)
        if (collisionSubId != null) query.where(TCollisions.collision_sub_id, "=", collisionSubId)
        if (colisionDate != null) query.where(TCollisions.collision_date_create, "=", colisionDate)
        if (value1 != null) query.where(TCollisions.value_1, "=", value1)
        if (value2 != null) query.whereValue(TCollisions.value_2, "=", value2)
        if (value3 != null) query.where(TCollisions.value_3, "=", value3)
        val v = Database.select("ControllerCollisions.getCollisionsValue4", query)
        return Array(v.rowsCount) { v.next<Long>() }
    }

    fun getCollisionsValue5(
            ownerId: Long,
            collisionId: Long?,
            collisionSubId: Long?,
            collisionType: Long,
            colisionDate: Long?,
            value1: Long?,
            value2: String?,
            value3: Long?,
            value4: Long?
    ): Array<Long> {
        val query = SqlQuerySelect(TCollisions.NAME, TCollisions.value_5)
        query.where(TCollisions.owner_id, "=", ownerId)
        query.where(TCollisions.collision_type, "=", collisionType)
        if (collisionId != null) query.where(TCollisions.collision_id, "=", collisionId)
        if (collisionSubId != null) query.where(TCollisions.collision_sub_id, "=", collisionSubId)
        if (colisionDate != null) query.where(TCollisions.collision_date_create, "=", colisionDate)
        if (value1 != null) query.where(TCollisions.value_1, "=", value1)
        if (value2 != null) query.whereValue(TCollisions.value_2, "=", value2)
        if (value3 != null) query.where(TCollisions.value_3, "=", value3)
        if (value4 != null) query.where(TCollisions.value_4, "=", value4)
        val v = Database.select("ControllerCollisions.getCollisionsValue5", query)
        return Array(v.rowsCount) { v.next<Long>() }
    }

    //
    //  Get
    //

    fun getCollision(ownerId: Long, collisionType: Long, def: Long = -1): Long {
        val array = getCollisions(ownerId, collisionType)
        return if (array.isEmpty()) def else array[0]
    }

    fun getCollisionNullable(ownerId: Long, collisionType: Long, def: Long? = null): Long? {
        val array = getCollisions(ownerId, collisionType)
        return if (array.isEmpty()) def else array[0]
    }

    fun getCollisionValue1(ownerId: Long, collisionType: Long) = getCollisionValue1(ownerId, 0, collisionType)

    fun getCollisionValue1(ownerId: Long, collisionId: Long, collisionType: Long) =
            getCollisionValue1(ownerId, collisionId, 0, collisionType)

    fun getCollisionValue1(ownerId: Long, collisionId: Long, collisionSubId: Long, collisionType: Long) = getCollisionValue1OrDef(ownerId, collisionId, collisionSubId, collisionType, 0L)

    fun getCollisionValue1OrDef(
            ownerId: Long,
            collisionId: Long,
            collisionSubId: Long,
            collisionType: Long,
            def: Long
    ): Long {
        val array = getCollisionsValue1(ownerId, collisionId, collisionSubId, collisionType)
        return if (array.isEmpty()) def else array[0]
    }

    fun getCollisionValue2(ownerId: Long, collisionType: Long) = getCollisionValue2(ownerId, 0, collisionType)

    fun getCollisionValue2(ownerId: Long, collisionId: Long, collisionType: Long) =
            getCollisionValue2(ownerId, collisionId, 0, collisionType)

    fun getCollisionValue2(ownerId: Long, collisionId: Long, collisionSubId: Long, collisionType: Long): String {
        val array = getCollisionsValue2(ownerId, collisionId, collisionSubId, collisionType)
        return if (array.isEmpty()) "" else array[0]
    }

    //
    //  Update or Create
    //

    fun updateOrCreateValue1(ownerId: Long, collisionType: Long, value1: Long) {
        updateOrCreate(ownerId, null, null, collisionType, null, value1, null)
    }

    fun updateOrCreateValue1(ownerId: Long, collisionId: Long, collisionType: Long, value1: Long) {
        updateOrCreate(ownerId, collisionId, null, collisionType, null, value1, null)
    }

    fun updateOrCreateValue2(ownerId: Long, collisionType: Long, value2: String) {
        updateOrCreate(ownerId, null, null, collisionType, null, null, value2)
    }

    fun updateOrCreateValue2(ownerId: Long, collisionId: Long, collisionType: Long, value2: String) {
        updateOrCreate(ownerId, collisionId, null, collisionType, null, null, value2)
    }

    fun updateOrCreate(ownerId: Long, collisionId: Long, collisionType: Long) {
        if (checkCollisionExist(ownerId, collisionType)) {
            update(ownerId, collisionId, collisionType)
        } else {
            putCollision(ownerId, collisionId, collisionType)
        }
    }
    fun update(ownerId: Long, collisionId: Long, collisionType: Long) {
        Database.update("ControllerCollisions.updateOrCreate_1", SqlQueryUpdate(TCollisions.NAME)
                .where(TCollisions.owner_id, "=", ownerId)
                .where(TCollisions.collision_type, "=", collisionType)
                .update(TCollisions.collision_id, collisionId))
    }

    fun updateOrCreate(ownerId: Long, collisionType: Long, value1: Long, value2: String?) {
        updateOrCreate(ownerId, null, null, collisionType, null, value1, value2)
    }

    fun updateOrCreate(ownerId: Long, collisionId: Long, collisionType: Long, value1: Long, value2: String?) {
        updateOrCreate(ownerId, collisionId, null, collisionType, null, value1, value2)
    }

    fun updateOrCreate(
            ownerId: Long,
            collisionId: Long,
            collisionSubId: Long,
            collisionType: Long,
            value1: Long,
            value2: String?
    ) {
        updateOrCreate(ownerId, collisionId, collisionSubId, collisionType, null, value1, value2)
    }

    fun updateOrCreate(
            ownerId: Long,
            collisionId: Long?,
            collisionSubId: Long?,
            collisionType: Long,
            date: Long?,
            value1: Long?,
            value2: String?
    ) {
        updateOrCreate(ownerId, collisionId, collisionSubId, collisionType, date, value1, value2, null, null, null)
    }

    fun updateOrCreate(
            ownerId: Long,
            collisionId: Long?,
            collisionSubId: Long?,
            collisionType: Long,
            date: Long?,
            value1: Long?,
            value2: String?,
            value3: Long?,
            value4: Long?,
            value5: Long?
    ) {
        if (checkCollisionExist(ownerId, collisionId, collisionSubId, collisionType)) {
            val update = SqlQueryUpdate(TCollisions.NAME)
            update.where(TCollisions.owner_id, "=", ownerId)
            update.where(TCollisions.collision_type, "=", collisionType)
            if (collisionId != null) update.where(TCollisions.collision_id, "=", collisionId)
            if (collisionSubId != null) update.where(TCollisions.collision_sub_id, "=", collisionSubId)
            if (date != null) update.update(TCollisions.collision_date_create, date)
            if (value1 != null) update.update(TCollisions.value_1, value1)
            if (value2 != null) update.updateValue(TCollisions.value_2, value2)
            if (value3 != null) update.update(TCollisions.value_3, value3)
            if (value4 != null) update.update(TCollisions.value_4, value4)
            if (value5 != null) update.update(TCollisions.value_5, value5)

            Database.update("ControllerCollisions.updateOrCreate_2", update)
        } else {
            putCollision(ownerId, collisionId, collisionSubId, collisionType, date, value1, value2, value3, value4, value5)
        }
    }


    fun updateOrCreateValue4(
            ownerId: Long,
            collisionId: Long?,
            collisionSubId: Long?,
            collisionType: Long,
            collisionDate: Long?,
            value1: Long?,
            value2: String?,
            value3: Long?,
            value4: Long,
            value5: Long?
    ) {
        if (checkCollisionExist(ownerId, collisionId, collisionSubId, collisionType, collisionDate, value1, value2, value3, null, value5)) {
            val update = SqlQueryUpdate(TCollisions.NAME)
            update.where(TCollisions.owner_id, "=", ownerId)
            update.where(TCollisions.collision_type, "=", collisionType)
            if (collisionId != null) update.where(TCollisions.collision_id, "=", collisionId)
            if (collisionSubId != null) update.where(TCollisions.collision_sub_id, "=", collisionSubId)
            if (collisionDate != null) update.where(TCollisions.collision_date_create, "=", collisionDate)
            if (value1 != null) update.where(TCollisions.value_1, "=", value1)
            if (value2 != null) update.whereValue(TCollisions.value_2, "=", value2)
            if (value3 != null) update.where(TCollisions.value_3, "=", value3)
            if (value5 != null) update.where(TCollisions.value_5, "=", value5)

            update.update(TCollisions.value_4, value4)

            Database.update("ControllerCollisions.updateOrCreateValue4", update)
        } else {
            putCollision(ownerId, collisionId, collisionSubId, collisionType, collisionDate, value1, value2, value3, value4, value5)
        }
    }


    fun updateOrCreateValue5(
            ownerId: Long,
            collisionId: Long?,
            collisionSubId: Long?,
            collisionType: Long,
            collisionDate: Long?,
            value1: Long?,
            value2: String?,
            value3: Long?,
            value4: Long?,
            value5: Long
    ) {
        if (checkCollisionExist(ownerId, collisionId, collisionSubId, collisionType, collisionDate, value1, value2, value3, value4, null)) {
            val update = SqlQueryUpdate(TCollisions.NAME)
            update.where(TCollisions.owner_id, "=", ownerId)
            update.where(TCollisions.collision_type, "=", collisionType)
            if (collisionId != null) update.where(TCollisions.collision_id, "=", collisionId)
            if (collisionSubId != null) update.where(TCollisions.collision_sub_id, "=", collisionSubId)
            if (collisionDate != null) update.where(TCollisions.collision_date_create, "=", collisionDate)
            if (value1 != null) update.where(TCollisions.value_1, "=", value1)
            if (value2 != null) update.whereValue(TCollisions.value_2, "=", value2)
            if (value3 != null) update.where(TCollisions.value_3, "=", value3)
            if (value4 != null) update.where(TCollisions.value_4, "=", value4)

            update.update(TCollisions.value_5, value5)

            Database.update("ControllerCollisions.updateOrCreateValue5", update)
        } else {
            putCollision(ownerId, collisionId, collisionSubId, collisionType, collisionDate, value1, value2, value3, value4, value5)
        }
    }


    //
    //  Put
    //


    fun putCollisionWithCheck(ownerId: Long, collisionId: Long, collisionType: Long) =
            putCollisionWithCheck(ownerId, collisionId, null, collisionType)

    fun putCollisionWithCheck(ownerId: Long, collisionId: Long, collisionSubId: Long?, collisionType: Long) =
            putCollisionWithCheck(ownerId, collisionId, collisionSubId, collisionType, null, null, null)

    fun putCollisionWithCheck(
            ownerId: Long,
            collisionId: Long,
            collisionSubId: Long?,
            collisionType: Long,
            collisionDate: Long?,
            value1: Long?,
            value2: String?
    ): Boolean {
        if (!checkCollisionExist(ownerId, collisionId, collisionSubId, collisionType)) {
            putCollision(ownerId, collisionId, collisionSubId, collisionType, collisionDate, value1, value2)
            return true
        }
        return false
    }

    fun putCollision(ownerId: Long, collisionType: Long): Long {
        return putCollision(ownerId, null, null, collisionType, System.currentTimeMillis(), null, null)
    }

    fun putCollision(ownerId: Long, collisionId: Long, collisionType: Long): Long {
        return putCollision(ownerId, collisionId, null, collisionType, System.currentTimeMillis(), null, null)
    }

    fun putCollisionDate(ownerId: Long, collisionId: Long, collisionType: Long, collisionDate: Long): Long {
        return putCollision(ownerId, collisionId, null, collisionType, collisionDate, null, null)
    }

    fun putCollision(
            ownerId: Long,
            collisionId: Long?,
            collisionSubId: Long?,
            collisionType: Long,
            collisionDate: Long?,
            value1: Long?,
            value2: String?
    ): Long {
        return putCollision(ownerId, collisionId, collisionSubId, collisionType, collisionDate, value1, value2, null, null, null)
    }

    fun putCollision(
            ownerId: Long,
            collisionId: Long?,
            collisionSubId: Long?,
            collisionType: Long,
            collisionDate: Long?,
            value1: Long?,
            value2: String?,
            value3: Long?,
            value4: Long?,
            value5: Long?
    ): Long {
        val insert = SqlQueryInsert(TCollisions.NAME)
        insert.put(TCollisions.owner_id, ownerId)
        insert.put(TCollisions.collision_type, collisionType)
        if (collisionId != null) insert.put(TCollisions.collision_id, collisionId)
        if (collisionSubId != null) insert.put(TCollisions.collision_sub_id, collisionSubId)
        if (collisionDate != null) insert.put(TCollisions.collision_date_create, collisionDate)
        if (value1 != null) insert.put(TCollisions.value_1, value1)
        if (value2 != null) insert.putValue(TCollisions.value_2, value2)
        if (value3 != null) insert.put(TCollisions.value_3, value3)
        if (value4 != null) insert.put(TCollisions.value_4, value4)
        if (value5 != null) insert.put(TCollisions.value_5, value5)

        return Database.insert("ControllerCollisions.putCollision", insert)
    }

    //
    //  Put Array
    //

    fun putCollisions(ownerId: Long, collisionIds: Array<Long>, collisionType: Long) {
        for (collisionId in collisionIds) putCollision(ownerId, collisionId, collisionType)
    }

    fun putCollisionsValue2(ownerId: Long, collisionId: Long, collisionType: Long, values: Array<String>) {
        for (v in values) putCollisionValue2(ownerId, collisionId, collisionType, v)
    }

    //
    //  Put Values
    //

    fun putCollisionValue1(ownerId: Long, collisionType: Long, value1: Long) {
        putCollision(ownerId, null, null, collisionType, System.currentTimeMillis(), value1, null)
    }

    fun putCollisionValue1(ownerId: Long, collisionId: Long, collisionType: Long, value1: Long) {
        putCollision(ownerId, collisionId, null, collisionType, null, value1, null)
    }

    fun putCollisionValue1(ownerId: Long, collisionId: Long, collisionSubId: Long, collisionType: Long, value1: Long) {
        putCollision(ownerId, collisionId, collisionSubId, collisionType, System.currentTimeMillis(), value1, null)
    }

    fun putCollisionValue1(
            ownerId: Long,
            collisionId: Long,
            collisionSubId: Long,
            collisionType: Long,
            collisionDate: Long,
            value1: Long
    ) {
        putCollision(ownerId, collisionId, collisionSubId, collisionType, collisionDate, value1, null)
    }

    fun putCollisionValue2(ownerId: Long, collisionType: Long, value2: String) {
        putCollision(ownerId, null, null, collisionType, System.currentTimeMillis(), null, value2)
    }

    fun putCollisionValue2(ownerId: Long, collisionId: Long, collisionType: Long, value2: String) {
        putCollision(ownerId, collisionId, null, collisionType, System.currentTimeMillis(), null, value2)
    }


}