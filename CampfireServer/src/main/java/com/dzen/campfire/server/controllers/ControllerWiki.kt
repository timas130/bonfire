package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.wiki.WikiItem
import com.dzen.campfire.api.models.wiki.WikiPages
import com.dzen.campfire.api.models.wiki.WikiTitle
import com.dzen.campfire.server.tables.TWikiItems
import com.dzen.campfire.server.tables.TWikiPages
import com.dzen.campfire.server.tables.TWikiTitles
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java_pc.sql.*

object ControllerWiki {

    fun markAsRemoved(itemId: Long) {
        Database.update("ControllerWiki.markAsRemoved", SqlQueryUpdate(TWikiItems.NAME)
                .where(TWikiItems.id, "=", itemId)
                .where(TWikiItems.status, "=", API.STATUS_PUBLIC)
                .update(TWikiItems.status, API.STATUS_REMOVED)
        )
    }

    fun insert(item: WikiTitle, creatorId: Long) {
        item.itemId = Database.insert("ControllerWiki.insert", TWikiItems.NAME,
                TWikiItems.creator_id, creatorId,
                TWikiItems.date_create, item.dateCreate,
                TWikiItems.fandom_id, item.fandomId,
                TWikiItems.parent_item_id, item.parentItemId,
                TWikiItems.type, item.itemType,
                TWikiItems.status, API.STATUS_PUBLIC
        )

        insertChanges(item)
    }

    fun insertChanges(wikiTitle: WikiTitle) {
        wikiTitle.id = Database.insert("ControllerWiki.insertChanges", TWikiTitles.NAME,
                TWikiTitles.creator_id, wikiTitle.creatorId,
                TWikiTitles.date_create, wikiTitle.changeDate,
                TWikiTitles.item_data, wikiTitle.json(true, Json()),
                TWikiTitles.item_id, wikiTitle.itemId,
                TWikiTitles.parent_item_id, wikiTitle.parentItemId,
                TWikiTitles.fandom_id, wikiTitle.fandomId,
                TWikiTitles.wiki_status, API.STATUS_PUBLIC,
                TWikiTitles.type, wikiTitle.itemType
        )

        Database.update("ControllerWiki.insertChanges", SqlQueryUpdate(TWikiTitles.NAME)
                .where(TWikiTitles.item_id, "=", wikiTitle.itemId)
                .where(TWikiTitles.id, "<>", wikiTitle.id)
                .where(TWikiTitles.wiki_status, "=", API.STATUS_PUBLIC)
                .update(TWikiTitles.wiki_status, API.STATUS_ARCHIVE)
        )

    }

    fun insertPages(wikiPages: WikiPages) {
        wikiPages.id = Database.insert("ControllerWiki.insertPages insert", TWikiPages.NAME,
                TWikiPages.item_id, wikiPages.itemId,
                TWikiPages.item_data, wikiPages.json(true, Json()),
                TWikiPages.date_create, wikiPages.changeDate,
                TWikiPages.creator_id, wikiPages.creatorId,
                TWikiPages.language_id, wikiPages.languageId,
                TWikiPages.event_type, wikiPages.eventType,
                TWikiPages.wiki_status, wikiPages.wikiStatus

        )

        Database.update("ControllerWiki.insertPages update", SqlQueryUpdate(TWikiPages.NAME)
                .where(TWikiPages.item_id, "=", wikiPages.itemId)
                .where(TWikiPages.language_id, "=", wikiPages.languageId)
                .where(TWikiPages.id, "<>", wikiPages.id)
                .where(TWikiPages.wiki_status, "=", API.STATUS_PUBLIC)
                .update(TWikiPages.wiki_status, API.STATUS_ARCHIVE)
        )

    }

    fun restorePages(pages: WikiPages): Long {
        Database.update("ControllerWiki.restorePages update[0]", SqlQueryUpdate(TWikiPages.NAME)
                .where(TWikiPages.id, "=", pages.id)
                .update(TWikiPages.wiki_status, API.STATUS_ARCHIVE)
        )

        val newId = Database.select("ControllerWiki.restorePages select", SqlQuerySelect(TWikiPages.NAME, TWikiPages.id)
                .where(SqlWhere.WhereIN(TWikiPages.wiki_status, arrayOf(API.STATUS_PUBLIC, API.STATUS_ARCHIVE)))
                .where(TWikiPages.item_id, "=", pages.itemId)
                .where(TWikiPages.language_id, "=", pages.languageId)
                .sort(TWikiPages.date_create, false)
                .count(1)
        ).nextLongOrZero()

        Database.update("ControllerWiki.restorePages update[1]", SqlQueryUpdate(TWikiPages.NAME)
                .where(TWikiPages.id, "=", newId)
                .update(TWikiPages.wiki_status, API.STATUS_PUBLIC)
        )

        Database.update("ControllerWiki.restorePages update[2]", SqlQueryUpdate(TWikiPages.NAME)
                .where(TWikiPages.item_id, "=", pages.itemId)
                .where(TWikiPages.language_id, "=", pages.languageId)
                .where(TWikiPages.id, "<>", newId)
                .where(TWikiPages.wiki_status, "=", API.STATUS_PUBLIC)
                .update(TWikiPages.wiki_status, API.STATUS_ARCHIVE)
        )

        return newId
    }

    fun cancelPages(pages: WikiPages): Long {
        val newId = Database.select("ControllerWiki.cancelPages select", SqlQuerySelect(TWikiPages.NAME, TWikiPages.id)
                .where(SqlWhere.WhereIN(TWikiPages.wiki_status, arrayOf(API.STATUS_PUBLIC, API.STATUS_ARCHIVE)))
                .where(TWikiPages.id, "<>", pages.id)
                .where(TWikiPages.item_id, "=", pages.itemId)
                .where(TWikiPages.language_id, "=", pages.languageId)
                .sort(TWikiPages.date_create, false)
                .count(1)
        ).nextLongOrZero()

        if (newId < 1) return 0

        Database.update("ControllerWiki.cancelPages update", SqlQueryUpdate(TWikiPages.NAME)
                .where(TWikiPages.id, "=", pages.id)
                .update(TWikiPages.wiki_status, API.STATUS_REMOVED)
        )
        Database.update("ControllerWiki.cancelPages update", SqlQueryUpdate(TWikiPages.NAME)
                .where(TWikiPages.id, "=", newId)
                .update(TWikiPages.wiki_status, API.STATUS_PUBLIC)
        )

        return newId
    }


    //
    //  Item
    //

    fun getItem(id: Long): WikiItem? {
        val v = parseSelectItem(Database.select("ControllerWiki.getItem", instanceSelectItem().where(TWikiItems.id, "=", id)))
        return if (v.isEmpty()) null else v[0]
    }

    fun instanceSelectItem() = SqlQuerySelect(TWikiItems.NAME,
            TWikiItems.id,
            TWikiItems.status
    )

    fun parseSelectItem(v: ResultRows): Array<WikiItem> {
        val list = ArrayList<WikiItem>()
        while (v.hasNext()) {
            val item = WikiItem()
            item.id = v.next()
            item.status = v.next()
            list.add(item)
        }
        return list.toTypedArray()
    }

    //
    //  Title
    //

    fun getTitlesByItemId(itemId: Long): WikiTitle? {
        val v = parseSelectTitles(Database.select("ControllerWiki.getFromChangesByItemId",
                instanceSelectTitles()
                        .where(TWikiTitles.item_id, "=", itemId)
                        .sort(TWikiTitles.date_create, false)
                        .count(1)
        ))
        return if (v.isEmpty()) null else v[0]
    }

    fun instanceSelectTitles() = SqlQuerySelect(TWikiTitles.NAME,
            TWikiTitles.id,
            TWikiTitles.item_data,
            TWikiTitles.wiki_status,
            TWikiTitles.priority,
            TWikiTitles.fandom_id,
    )

    fun parseSelectTitles(v: ResultRows): Array<WikiTitle> {
        val list = ArrayList<WikiTitle>()
        while (v.hasNext()) {
            val item = WikiTitle()
            val id: Long = v.next()
            item.json(false, Json(v.next<String>()))
            item.wikiStatus = v.next()
            item.priority = v.next()
            item.fandomId = v.next()
            item.id = id    //  Важно! После json
            list.add(item)
        }
        return list.toTypedArray()
    }

    //
    //  Pages
    //

    fun getPagesByItemId_OnlyPublic(itemId: Long, languageId: Long): WikiPages? {
        val v = parseSelectPages(Database.select("ControllerWiki.getPagesByItemId", instanceSelectPages()
                .where(TWikiPages.item_id, "=", itemId)
                .where(TWikiPages.language_id, "=", languageId)
                .where(TWikiPages.wiki_status, "=", API.STATUS_PUBLIC)
                .where(TWikiPages.ITEM_STATUS, "=", API.STATUS_PUBLIC)
        ))
        return if (v.isEmpty()) null else v[0]
    }

    fun getPagesById(pagesId: Long): WikiPages? {
        val v = parseSelectPages(Database.select("ControllerWiki.getPagesById", instanceSelectPages()
                .where(TWikiPages.id, "=", pagesId)
        ))
        return if (v.isEmpty()) null else v[0]
    }

    fun getPagesByItemId(itemId: Long, languageId: Long): WikiPages? {
        val v = parseSelectPages(Database.select("ControllerWiki.getPagesByItemId", instanceSelectPages()
                .where(TWikiPages.item_id, "=", itemId)
                .where(TWikiPages.language_id, "=", languageId)
                .where(TWikiPages.wiki_status, "=", API.STATUS_PUBLIC)
        ))
        return if (v.isEmpty()) null else v[0]
    }

    fun instanceSelectPages() = SqlQuerySelect(TWikiPages.NAME,
            TWikiPages.item_data,
            TWikiPages.id,
            TWikiPages.item_id,
            TWikiPages.language_id,
            TWikiPages.wiki_status,
            TWikiPages.creator_id,
            TWikiPages.CREATOR_NAME,
            TWikiPages.CREATOR_IMAGE_ID
    )

    fun parseSelectPages(v: ResultRows): Array<WikiPages> {
        val list = ArrayList<WikiPages>()
        while (v.hasNext()) {
            val item = WikiPages()
            item.json(false, Json(v.next<String>()))
            item.id = v.next()
            item.itemId = v.next()
            item.languageId = v.next()
            item.wikiStatus = v.next()
            item.creatorId = v.next()
            item.creatorName = v.next()
            item.creatorImageId = v.next()
            list.add(item)
        }
        return list.toTypedArray()
    }

}