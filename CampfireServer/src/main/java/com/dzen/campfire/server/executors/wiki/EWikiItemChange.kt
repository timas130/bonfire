package com.dzen.campfire.server.executors.wiki

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.wiki.WikiTitle
import com.dzen.campfire.api.requests.wiki.RWikiItemChange
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.controllers.ControllerResources
import com.dzen.campfire.server.controllers.ControllerWiki
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java.tools.ToolsCollections
import com.sup.dev.java.tools.ToolsText
import com.sup.dev.java_pc.tools.ToolsImage

class EWikiItemChange : RWikiItemChange(WikiTitle(), 0, null, null) {

    val newItem = WikiTitle()
    val languagesIds = ArrayList<Long>()

    override fun check() {

        val oldItem = ControllerWiki.getTitlesByItemId(item.itemId)

        if(oldItem == null)  throw ApiException(API.ERROR_GONE)

        if(oldItem.fandomId != item.fandomId) throw ApiException(API.ERROR_ACCESS)
        if(oldItem.itemType != item.itemType) throw ApiException(API.ERROR_ACCESS)
        if(oldItem.dateCreate != item.dateCreate) throw ApiException(API.ERROR_ACCESS)
        if(oldItem.name != item.name) ControllerFandom.checkCan(apiAccount, item.fandomId, 1, API.LVL_MODERATOR_WIKI_EDIT)

        newItem.itemId = oldItem.itemId
        newItem.parentItemId = oldItem.parentItemId
        newItem.fandomId = oldItem.fandomId
        newItem.dateCreate = oldItem.dateCreate
        newItem.itemType = oldItem.itemType
        newItem.imageId = oldItem.imageId
        newItem.imageBigId = oldItem.imageBigId
        newItem.creatorId = apiAccount.id
        newItem.creatorName = apiAccount.name
        newItem.creatorImageId = apiAccount.imageId

        for(i in item.translates){
            val id = API.getLanguage(i.languageCode).id
            if(i.name.isEmpty()) {
                if(oldItem.getName(i.languageCode).isNotEmpty())languagesIds.add(id)
                continue
            }
            if(i.name.length > API.WIKI_NAME_MAX) throw ApiException(API.ERROR_ACCESS)
            if(i.languageCode == "en") throw ApiException(API.ERROR_ACCESS)
            if(languagesIds.contains(id)) throw ApiException(API.ERROR_ACCESS)
            if(newItem.getName(i.languageCode) != i.name)languagesIds.add(id)

            val t = WikiTitle.Translate()
            t.name = i.name
            t.languageCode = i.languageCode
            newItem.translates = ToolsCollections.add(t, newItem.translates)
        }

        for(i in languagesIds) {
            ControllerFandom.checkCan(apiAccount, item.fandomId, i, API.LVL_MODERATOR_WIKI_EDIT)
        }

        if(imageMini != null) {
            if (ToolsImage.isGIF(imageMini!!)) {
                if (imageMini!!.size > API.WIKI_IMG_WEIGHT_GIF) throw ApiException(API.ERROR_ACCESS, " " + imageMini!!.size + " > " + API.WIKI_IMG_WEIGHT_GIF)
                if (!ToolsImage.checkImageMaxScaleUnknownType(imageMini!!, API.WIKI_IMG_SIDE_GIF, API.WIKI_IMG_SIDE_GIF, true, true, true)) throw ApiException(API.ERROR_ACCESS)
            } else {
                if (imageMini!!.size > API.WIKI_IMG_WEIGHT) throw ApiException(API.ERROR_ACCESS, " " + imageMini!!.size + " > " + API.WIKI_IMG_WEIGHT)
                if (!ToolsImage.checkImageMaxScaleUnknownType(imageMini!!, API.WIKI_IMG_SIDE, API.WIKI_IMG_SIDE, true, true, true)) throw ApiException(API.ERROR_ACCESS)
            }
        }

        if(imageBig != null) {
            if (ToolsImage.isGIF(imageBig!!)) {
                if (imageBig!!.size > API.WIKI_TITLE_IMG_GIF_WEIGHT) throw ApiException(API.ERROR_ACCESS, " " + imageBig!!.size + " > " + API.WIKI_TITLE_IMG_GIF_WEIGHT)
                if (!ToolsImage.checkImageMaxScaleUnknownType(imageBig!!, API.WIKI_TITLE_IMG_GIF_W, API.WIKI_TITLE_IMG_GIF_H, true, true, true)) throw ApiException(API.ERROR_ACCESS)
            } else {
                if (imageBig!!.size > API.WIKI_TITLE_IMG_WEIGHT) throw ApiException(API.ERROR_ACCESS, " " + imageBig!!.size + " > " + API.WIKI_TITLE_IMG_WEIGHT)
                if (!ToolsImage.checkImageMaxScaleUnknownType(imageBig!!, API.WIKI_TITLE_IMG_W, API.WIKI_TITLE_IMG_H, true, true, true)) throw ApiException(API.ERROR_ACCESS)
            }
        }

        if(item.name.isEmpty()) throw ApiException(API.ERROR_ACCESS)
        if (!ToolsText.checkStringChars(item.name, API.ENGLISH, false)) throw ApiException(API.ERROR_ACCESS)
        newItem.name = item.name



        if(parentItemId > 0 && oldItem.parentItemId != parentItemId) {
            val parentItem = ControllerWiki.getTitlesByItemId(parentItemId)
            if(parentItem == null)  throw ApiException(API.ERROR_GONE)
            if(parentItem.wikiStatus != API.STATUS_PUBLIC) throw ApiException(API.ERROR_ACCESS)
            if(parentItem.itemType != API.WIKI_TYPE_SECION) throw ApiException(API.ERROR_ACCESS)
            newItem.parentItemId = parentItemId
        }
    }


    override fun execute(): Response {

        if(imageMini != null)newItem.imageId = ControllerResources.put(imageMini, API.RESOURCES_PUBLICATION_WIKI)
        if(imageBig != null)newItem.imageBigId = ControllerResources.put(imageBig, API.RESOURCES_PUBLICATION_WIKI)
        newItem.changeDate = System.currentTimeMillis()

        ControllerWiki.insertChanges(newItem)

        return Response(newItem)
    }


}