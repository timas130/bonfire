package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.models.fandoms.Rubric
import com.dzen.campfire.api.models.notifications.rubrics.NotificationRubricsKarmaCofChanged
import com.dzen.campfire.server.tables.TRubrics
import com.dzen.campfire.server.tables.TPublications
import com.sup.dev.java.classes.collections.AnyArray
import com.sup.dev.java.tools.ToolsDate
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.ResultRows
import com.sup.dev.java_pc.sql.Sql
import com.sup.dev.java_pc.sql.SqlQuerySelect

object ControllerRubrics {

    fun instanceSelect() = SqlQuerySelect(TRubrics.NAME,
            TRubrics.id,
            TRubrics.name,
            TRubrics.date_create,
            TRubrics.karma_cof,
            TRubrics.status,
            TRubrics.status_change_date,
            TRubrics.creator_id,

            TRubrics.owner_id,
            TRubrics.OWNER_LVL,
            TRubrics.OWNER_LAST_ONLINE_TIME,
            TRubrics.OWNER_NAME,
            TRubrics.OWNER_IMAGE_ID,
            TRubrics.OWNER_SEX,
            TRubrics.OWNER_KARMA_30,

            TRubrics.fandom_id,
            TRubrics.language_id,
            TRubrics.FANDOM_NAME,
            TRubrics.FANDOM_IMAGE_ID,
            TRubrics.FANDOM_CLOSED,
            TRubrics.FANDOM_KARMA_COF
    )

    fun parseSelect(v: ResultRows): Array<Rubric> {
        val list = ArrayList<Rubric>()
        while (v.hasNext()) {
            val rubric = Rubric()
            rubric.id = v.next()
            rubric.name = v.next()
            rubric.dateCreate = v.next()
            rubric.karmaCof = v.next()
            rubric.status = v.next()
            rubric.statusChangeDate = v.next()
            rubric.creatorId = v.next()

            rubric.owner = ControllerAccounts.instance(v)

            rubric.fandom = Fandom(v.next(), v.next(), v.next(), v.next(), v.nextLongOrZero()==1L, v.next())

            list.add(rubric)
        }


        return list.toTypedArray()
    }

    fun getWaitForPostRubricsIds(accountId: Long): Array<Long> {
        val v = Database.select("ControllerActivities getRubricsCount select 1", SqlQuerySelect(TRubrics.NAME, TRubrics.id)
                .where(TRubrics.owner_id, "=", accountId)
                .where(TRubrics.status, "=", API.STATUS_PUBLIC)
                .where(TRubrics.NOTIFICATION_ON(accountId), "=", 1)
        )
        if (v.isEmpty) return emptyArray()

        val list = ArrayList<Long>()
        while (v.hasNext()) {
            val rubricId: Long = v.next()
            if (isWaitForPostRubricsIds(accountId, rubricId)) list.add(rubricId)
        }

        return list.toTypedArray()
    }

    fun isWaitForPostRubricsIds(accountId: Long, rubricId: Long): Boolean {
        val count = Database.select("ControllerActivities getRubricsCount select 2", SqlQuerySelect(TPublications.NAME, Sql.COUNT)
                .where(TPublications.creator_id, "=", accountId)
                .where(TPublications.status, "=", API.STATUS_PUBLIC)
                .where(TPublications.date_create, ">", System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 7 - (1000L * 60 * 60 * 4))
                .where(TPublications.tag_6, "=", rubricId)
        ).nextLongOrZero()

        return count <= 0
    }

    fun getRubric(id: Long): Rubric? {
        val array = parseSelect(Database.select("ControllerRubrics getRubric", instanceSelect().where(TRubrics.id, "=", id)))
        if (array.isEmpty()) return null
        return array[0]
    }

    operator fun get(id: Long, vararg columns: String): AnyArray {
        val query = SqlQuerySelect(TRubrics.NAME, *columns)
        query.where(TRubrics.id, "=", id)
        return Database.select("ControllerRubrics.get", query).values
    }

    fun updateCof() {

        val rubricsV = Database.select("ControllerRubrics updateCof select 1", SqlQuerySelect(TRubrics.NAME, TRubrics.id, TRubrics.karma_cof, TRubrics.owner_id, TRubrics.name, TRubrics.fandom_id, TRubrics.language_id, TRubrics.FANDOM_IMAGE_ID)
                .where(TRubrics.status, "=", API.STATUS_PUBLIC)
        )

        while (rubricsV.hasNext()) {
            val id: Long = rubricsV.next()
            val oldKarmaCof: Long = rubricsV.next()
            val ownerId: Long = rubricsV.next()
            val name: String = rubricsV.next()
            val fandomId: Long = rubricsV.next()
            val languageId: Long = rubricsV.next()
            val fandomImageId: Long = rubricsV.next()

            var newKarmaCof: Long = oldKarmaCof

            val publicationV = Database.select("ControllerRubrics updateCof select 2", SqlQuerySelect(TPublications.NAME, TPublications.id)
                    .where(TPublications.status, "=", API.STATUS_PUBLIC)
                    .where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_POST)
                    .where(TPublications.date_create, ">", ToolsDate.getStartOfDay() - 1000L * 60 * 60 * 24 * 7)
                    .where(TPublications.tag_6, "=", id)
                    .where(TPublications.karma_count, ">=", API.RUBRIC_KARMA_BOUND)
                    .count(1))

            newKarmaCof += if (publicationV.isEmpty) -API.RUBRIC_COF_STEP_DOWN else API.RUBRIC_COF_STEP_UP

            if (newKarmaCof < API.RUBRIC_COF_MIN) newKarmaCof = API.RUBRIC_COF_MIN
            if (newKarmaCof > API.RUBRIC_COF_MAX) newKarmaCof = API.RUBRIC_COF_MAX

            if (oldKarmaCof == newKarmaCof) continue

            ControllerOptimizer.setRubricKarmaCof(id, newKarmaCof)
            ControllerNotifications.push(ownerId, NotificationRubricsKarmaCofChanged(id, name, newKarmaCof, newKarmaCof - oldKarmaCof, fandomImageId, fandomId, languageId))
        }

    }
}