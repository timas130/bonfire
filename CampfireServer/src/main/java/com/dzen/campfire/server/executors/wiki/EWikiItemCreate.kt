package com.dzen.campfire.server.executors.wiki

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.wiki.WikiTitle
import com.dzen.campfire.api.requests.wiki.RWikiItemCreate
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java.tools.ToolsCollections
import com.sup.dev.java.tools.ToolsText
import com.sup.dev.java_pc.tools.ToolsImage

class EWikiItemCreate : RWikiItemCreate(0, 0, WikiTitle(), null, null) {

    val newItem = WikiTitle()
    val languagesIds = ArrayList<Long>()

    override fun check() {

        languagesIds.add(API.getLanguage("en").id)
        for(i in item.translates){
            if(i.name.isEmpty()) continue
            if(i.name.length > API.WIKI_NAME_MAX) throw ApiException(API.ERROR_ACCESS)
            if(i.languageCode == "en") throw ApiException(API.ERROR_ACCESS)
            val id = API.getLanguage(i.languageCode).id
            if(languagesIds.contains(id)) throw ApiException(API.ERROR_ACCESS)
            languagesIds.add(id)

            val t = WikiTitle.Translate()
            t.name = i.name
            t.languageCode = i.languageCode
            newItem.translates = ToolsCollections.add(t, newItem.translates)
        }

        val permissionGranted = languagesIds.any { ControllerFandom.can(apiAccount, fandomId, it, API.LVL_MODERATOR_WIKI_EDIT) }
        if (!permissionGranted) throw ApiException(API.ERROR_ACCESS)

        if(ToolsImage.isGIF(imageMini!!)){
            if (imageMini!!.size > API.WIKI_IMG_WEIGHT_GIF) throw ApiException(API.ERROR_ACCESS, " "+ imageMini!!.size + " > " + API.WIKI_IMG_WEIGHT_GIF)
            if (!ToolsImage.checkImageMaxScaleUnknownType(imageMini!!, API.WIKI_IMG_SIDE_GIF, API.WIKI_IMG_SIDE_GIF, true, true, true)) throw ApiException(API.ERROR_ACCESS)
        }else{
            if (imageMini!!.size > API.WIKI_IMG_WEIGHT) throw ApiException(API.ERROR_ACCESS, " "+ imageMini!!.size + " > " + API.WIKI_IMG_WEIGHT)
            if (!ToolsImage.checkImageMaxScaleUnknownType(imageMini!!, API.WIKI_IMG_SIDE, API.WIKI_IMG_SIDE, true, true, true)) throw ApiException(API.ERROR_ACCESS)
        }

        if(ToolsImage.isGIF(imageBig!!)){
            if (imageBig!!.size > API.WIKI_TITLE_IMG_GIF_WEIGHT) throw ApiException(API.ERROR_ACCESS, " "+ imageBig!!.size + " > " + API.WIKI_TITLE_IMG_GIF_WEIGHT)
            if (!ToolsImage.checkImageMaxScaleUnknownType(imageBig!!, API.WIKI_TITLE_IMG_GIF_W, API.WIKI_TITLE_IMG_GIF_H, true, true, true)) throw ApiException(API.ERROR_ACCESS)
        }else{
            if (imageBig!!.size > API.WIKI_TITLE_IMG_WEIGHT) throw ApiException(API.ERROR_ACCESS, " "+ imageBig!!.size + " > " + API.WIKI_TITLE_IMG_WEIGHT)
            if (!ToolsImage.checkImageMaxScaleUnknownType(imageBig!!, API.WIKI_TITLE_IMG_W, API.WIKI_TITLE_IMG_H, true, true, true)) throw ApiException(API.ERROR_ACCESS)
        }

        if(item.name.isEmpty()) throw ApiException(API.ERROR_ACCESS)
        if (!ToolsText.checkStringChars(item.name, API.ENGLISH, false)) throw ApiException(API.ERROR_ACCESS)
        newItem.name = item.name

        if (item.itemType != API.WIKI_TYPE_ARTICLE && item.itemType != API.WIKI_TYPE_SECION) throw ApiException(API.ERROR_ACCESS)
        newItem.itemType = item.itemType

        if(parentItemId > 0) {
            val parentItem = ControllerWiki.getTitlesByItemId(parentItemId)
            if(parentItem == null)  throw ApiException(API.ERROR_GONE)
            if(parentItem.wikiStatus != API.STATUS_PUBLIC) throw ApiException(API.ERROR_ACCESS)
            if(parentItem.itemType != API.WIKI_TYPE_SECION) throw ApiException(API.ERROR_ACCESS)
            newItem.parentItemId = parentItemId
        }
    }


    override fun execute(): Response {

        newItem.fandomId = fandomId
        newItem.imageId = ControllerResources.put(imageMini, API.RESOURCES_PUBLICATION_WIKI)
        newItem.imageBigId = ControllerResources.put(imageBig, API.RESOURCES_PUBLICATION_WIKI)
        newItem.dateCreate = System.currentTimeMillis()
        newItem.changeDate = newItem.dateCreate

        ControllerWiki.insert(newItem, apiAccount.id)

        ControllerSubThread.inSub("EWikiItemCreate viceroy"){
            for(i in API.LANGUAGES) ControllerAchievements.addAchievementWithCheck(
                ControllerViceroy.getViceroyId(fandomId, i.id),
                API.ACHI_VICEROY_WIKI_COUNT
            )
        }

        return Response(newItem)
    }


}
