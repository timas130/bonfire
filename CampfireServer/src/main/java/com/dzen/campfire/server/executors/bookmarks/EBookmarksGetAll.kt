package com.dzen.campfire.server.executors.bookmarks

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.Publication
import com.dzen.campfire.api.requests.bookmarks.RBookmarksGetAll
import com.dzen.campfire.server.controllers.ControllerPublications
import com.dzen.campfire.server.tables.TPublications
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.tables.TCollisions
import com.sup.dev.java.classes.items.Item2
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryRemove
import com.sup.dev.java_pc.sql.SqlQuerySelect
import com.sup.dev.java_pc.sql.SqlWhere
import java.util.*
import kotlin.collections.ArrayList

class EBookmarksGetAll : RBookmarksGetAll(0, "", 0, 0, 0, emptyArray()) {

    @Throws(ApiException::class)
    override fun check() {

    }

    override fun execute(): Response {

        val collisionsSelect = SqlQuerySelect(TCollisions.NAME, TCollisions.owner_id, TCollisions.collision_date_create)
                .where(TCollisions.collision_type, "=", API.COLLISION_BOOKMARK)
                .where(TCollisions.collision_id, "=", apiAccount.id)
                .sort(TCollisions.collision_date_create, false)
                .offset_count(offset, COUNT)

        if(folderId > 0) collisionsSelect.where(TCollisions.value_1, "=", folderId)
        else if(foldersIds.isNotEmpty()) collisionsSelect.where(SqlWhere.WhereIN(TCollisions.value_1, true, foldersIds))

        val v = Database.select("EPublicationsBookmarksGetAll select_1", collisionsSelect)

        if (v.isEmpty) return Response(emptyArray())

        val items = Array(v.rowsCount) { Item2(v.next<Long>(), v.next<Long>()) }
        val ids = Array(v.rowsCount) { items[it].a1 }

        val select = ControllerPublications.instanceSelect(apiAccount.id)
                .where(TPublications.status, "=", API.STATUS_PUBLIC)
                .where(SqlWhere.WhereIN(TPublications.id, ids))

        if (projectKey.isNotEmpty()) select.whereValue(TPublications.tag_s_1, "=", projectKey)
        if (fandomId != 0L) select.where(TPublications.fandom_id, "=", fandomId)
        if (languageId != 0L) select.where(TPublications.fandom_id, "=", languageId)

        val publications = ControllerPublications.parseSelect(Database.select("EPublicationsBookmarksGetAll select_2", select))

        val list = ArrayList<Publication>()
        for (i in publications) {
            list.add(i)
        }

        val wasNotFoundIds = ArrayList<Long>()
        for (id in ids) {
            var found = false
            for (p in publications) if (p.id == id) {
                found = true
                break
            }
            if (!found) wasNotFoundIds.add(id)
        }

        if (wasNotFoundIds.isNotEmpty()) {
            Database.remove("EPublicationsBookmarksGetAll remove", SqlQueryRemove(TCollisions.NAME)
                    .where(TCollisions.collision_type, "=", API.COLLISION_BOOKMARK)
                    .where(TCollisions.collision_id, "=", apiAccount.id)
                    .where(SqlWhere.WhereIN(TCollisions.owner_id, wasNotFoundIds))
            )
        }

        list.sortWith(Comparator { u1, u2 ->

            var u1Date = 0L
            var u2Date = 0L

            for (i in items) {
                if (i.a1 == u1.id) u1Date = i.a2
                if (i.a1 == u2.id) u2Date = i.a2
                if (u1Date != 0L && u2Date != 0L) break
            }

            (u2Date - u1Date).toInt()
        })

        ControllerPublications.loadSpecDataForPosts(apiAccount.id, publications)

        return Response(list.toTypedArray())
    }

}
