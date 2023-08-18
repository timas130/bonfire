package com.dzen.campfire.server.executors.wiki

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.post.Page
import com.dzen.campfire.api.models.wiki.WikiPages
import com.dzen.campfire.api.requests.wiki.RWikiPageMove
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.controllers.ControllerWiki
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java.tools.ToolsMapper
import java.util.*

class EWikiPageMove : RWikiPageMove(0, 0, 0, 0) {

    private var wikiPagesOld = WikiPages()

    @Throws(ApiException::class)
    override fun check() {
        val wikiTitle = ControllerWiki.getTitlesByItemId(wikiItemId)
        if (wikiTitle == null) throw ApiException(API.ERROR_GONE)
        if (wikiTitle.wikiStatus != API.STATUS_PUBLIC) throw ApiException(E_BAD_STATUS)
        if (wikiTitle.itemType != API.WIKI_TYPE_ARTICLE) throw ApiException(E_BAD_TYPE)

        ControllerFandom.checkCan(apiAccount, wikiTitle.fandomId, languageId, API.LVL_MODERATOR_WIKI_EDIT)

        ControllerFandom.checkCan(apiAccount, wikiTitle.fandomId, languageId, API.LVL_MODERATOR_WIKI_EDIT)
        val wikiPagesOldX = ControllerWiki.getPagesByItemId_OnlyPublic(wikiItemId, languageId)
        if (wikiPagesOldX == null) throw ApiException(API.ERROR_GONE)
        wikiPagesOld = wikiPagesOldX

        val fandom = ControllerFandom.getFandom(wikiTitle.fandomId)
        if (fandom!!.status != API.STATUS_PUBLIC) throw ApiException(API.ERROR_ACCESS)

        if (pageIndex < 0 || wikiPagesOld.pages.size <= pageIndex) throw ApiException(E_BAD_PAGE_INDEX, "index [" + pageIndex + "] size[" + wikiPagesOld.pages.size + "]")
        if (targetIndex < 0 || wikiPagesOld.pages.size <= targetIndex) throw ApiException(E_BAD_PAGE_INDEX, "index [" + targetIndex + "] size[" + wikiPagesOld.pages.size + "]")
    }

    override fun execute(): Response {

        val wikiPagesNew = WikiPages()
        wikiPagesNew.itemId = wikiItemId
        wikiPagesNew.languageId = languageId
        wikiPagesNew.creatorId = apiAccount.id
        wikiPagesNew.changeDate = System.currentTimeMillis()
        wikiPagesNew.wikiStatus = API.STATUS_PUBLIC
        wikiPagesNew.eventType = API.WIKI_PAGES_EVENT_TYPE_CHANGE_LANGUAGE

        val list = ArrayList(listOf(*wikiPagesOld.pages))
        list.add(targetIndex, list.removeAt(pageIndex))
        val array = arrayOfNulls<Page>(list.size)
        for (i in list.indices) array[i] = list[i]
        wikiPagesNew.pages = ToolsMapper.asNonNull(array)

        ControllerWiki.insertPages(wikiPagesNew)

        return Response()
    }


}
