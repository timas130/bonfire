package com.dzen.campfire.server.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.publications.history.HistoryMultilingual
import com.dzen.campfire.api.models.publications.history.HistoryNotMultolingual
import com.dzen.campfire.api.models.publications.post.*
import com.dzen.campfire.api.tools.ApiAccount
import com.dzen.campfire.server.tables.TCollisions
import com.dzen.campfire.server.tables.TPublications
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java.tools.ToolsBytes
import com.sup.dev.java.tools.ToolsCollections
import com.sup.dev.java.tools.ToolsText
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryRemove
import com.sup.dev.java_pc.sql.SqlQueryUpdate
import com.sup.dev.java_pc.tools.ToolsImage


object ControllerPost {

    fun setMultilingual(publication: PublicationPost, multilingual: Boolean) {
        if (multilingual) {

            ControllerPublications.removeCollisions(publication.id, API.COLLISION_TAG)

            Database.update("ControllerPost.setMultilingual[$multilingual]", SqlQueryUpdate(TPublications.NAME)
                    .where(TPublications.id, "=", publication.id)
                    .update(TPublications.language_id, -1L)
                    .update(TPublications.fandom_key, "'${publication.fandom.id}--1-${publication.important}'")
                    .update(TPublications.tag_5, publication.fandom.languageId)
            )


        } else {

            Database.update("ControllerPost.setMultilingual[$multilingual]", SqlQueryUpdate(TPublications.NAME)
                    .where(TPublications.id, "=", publication.id)
                    .update(TPublications.fandom_key, "'${publication.fandom.id}-${publication.tag_5}-${publication.important}'")
                    .update(TPublications.language_id, publication.tag_5)
            )


        }
    }


    fun publish(publicationId: Long, willNotify: Long, creatorId: Long) {
        Database.update("EPostPendingPublish", SqlQueryUpdate(TPublications.NAME)
                .where(TPublications.id, "=", publicationId)
                .update(TPublications.status, API.STATUS_PUBLIC)
                .update(TPublications.date_create, System.currentTimeMillis())
        )

        if (willNotify == 2L) {
            ControllerPublications.notifyFollowers(creatorId, publicationId)
        }
    }

    fun remove(publication: PublicationPost) {
        Database.remove("ControllerPost.remove", SqlQueryRemove(TPublications.NAME)
                .where(TPublications.id, "=", publication.id))

        for (p in publication.pages) removePage(p)
    }

    @Throws(ApiException::class)
    fun checkPage(page: Page?, eBadPage: String, isChange: Boolean) {

        if (page == null) throw ApiException(eBadPage, "Page is null")

        when (page) {
            is PageText -> {
                page.text = ControllerCensor.cens(page.text)
                if (page.size == PageText.SIZE_0 && page.text.length > API.PAGE_TEXT_MAX_L || page.size == PageText.SIZE_1 && page.text.length > API.PAGE_TEXT_TITLE_MAX_L)
                    throw ApiException(eBadPage, "PageText. Bad text size [${page.text.length}]")
            }
            is PageImage -> {
                if (page.insertBytes == null) throw ApiException(eBadPage, "PageImage. Bad insertBytes. Bytes is NULL")
                if (page.insertBytes!!.size > API.PAGE_IMAGE_WEIGHT) throw ApiException(eBadPage, "PageImage. Bad insertBytes. Weight ${page.insertBytes!!.size} > ${API.PAGE_IMAGE_WEIGHT}")
                if (page.insertGifBytes != null && page.insertGifBytes!!.size > API.PAGE_IMAGE_GIF_WEIGHT) throw ApiException(eBadPage, "PageImage. Gif size too big")
                val scale = ToolsImage.getImgScaleUnknownType(page.insertBytes!!, true, false, true)
                page.w = scale[0]
                page.h = scale[1]
                val size = if (page.gifId == 0L) API.PAGE_IMAGE_SIDE else API.PAGE_IMAGE_SIDE_GIF
                if (page.w > size || page.h > size) throw ApiException(eBadPage, "PageImage. Bad sides " + page.w + " " + page.h)
            }
            is PageLinkImage -> {
                if (!isChange && page.insertBytes == null) throw ApiException(eBadPage, "PageLinkImage. Bad insertBytes. Bytes is NULL")
                if (page.insertBytes != null) {
                    if (page.insertBytes!!.size > API.PAGE_LINK_IMAGE_WEIGHT) throw ApiException(eBadPage, "PageLinkImage. Bad insertBytes. Weight ${page.insertBytes!!.size} > ${API.PAGE_LINK_IMAGE_WEIGHT}")
                    val scale = ToolsImage.getImgScaleUnknownType(page.insertBytes!!, true, false, true)
                    if (scale[0] != API.PAGE_LINK_IMAGE_W || scale[1] > API.PAGE_LINK_IMAGE_H) throw ApiException(eBadPage, "PageLinkImage. Bad sides " + scale[0] + " " + scale[1])
                }
                if (page.link.length > API.PAGE_LINK_WEB_MAX_L) throw ApiException(eBadPage, "PageLinkImage. Too big link.")
            }
            is PageVideo -> {
                if (page.insertBytes == null || page.insertBytes!!.size > API.PAGE_VIDEO_IMAGE_WEIGHT) throw ApiException(eBadPage, "PageVideo. Bad insertBytes")
                val scale = ToolsImage.getImgScaleUnknownType(page.insertBytes!!, true, false, true)
                page.w = scale[0]
                page.h = scale[1]
                if (page.w > API.PAGE_VIDEO_IMAGE_SIDE || page.h > API.PAGE_VIDEO_IMAGE_SIDE) throw ApiException(eBadPage, "PageVideo. Bad sides " + page.w + " " + page.h)
            }
            is PageLink -> {
                page.name = ControllerCensor.cens(page.name)
                if (page.name.isEmpty() || page.name.length > API.PAGE_LINK_NAME_MAX_L) throw ApiException(eBadPage, "PageLink. Bad name")
                if (page.link.isEmpty() || page.link.length > API.PAGE_LINK_WEB_MAX_L) throw ApiException(eBadPage, "PageLink. Bad link")
                if (!ToolsText.isWebLink(page.link)) throw ApiException(eBadPage, "PageLink. IS not web link")
            }
            is PageQuote -> {
                page.text = ControllerCensor.cens(page.text)
                page.author = ControllerCensor.cens(page.author)
                if (page.text.isEmpty() || page.text.length > API.PAGE_QUOTE_TEXT_MAX_L) throw ApiException(eBadPage, "PageQuote. Bad text")
                if (page.author.length > API.PAGE_QUOTE_AUTHOR_MAX_L) throw ApiException(eBadPage, "PageQuote. Bad author")
            }
            is PageSpoiler -> {
                if (page.name != null) page.name = ControllerCensor.cens(page.name!!)
                if (page.name == null || page.name!!.isEmpty() || page.name!!.length > API.PAGE_LINK_SPOILER_NAME_MAX_L) throw ApiException(eBadPage, "PageSpoiler. Bad name")
                if (page.count < 1 || page.count > API.PAGE_LINK_SPOILER_MAX) throw ApiException(eBadPage, "PageSpoiler. Bad count")
            }
            is PagePolling -> {
                page.title = ControllerCensor.cens(page.title)
                if (page.title.length > API.PAGE_IMAGES_TITLE_MAX) throw ApiException(eBadPage, "PagePolling. Bad title")
                if (page.options.size > API.PAGE_POLLING_OPTION_MAX_COUNT) throw ApiException(eBadPage, "PagePolling. Bad count")
                if (page.minLevel < 0) throw ApiException(eBadPage, "PagePolling. Bad min lvl")
                if (page.minKarma < 0) throw ApiException(eBadPage, "PagePolling. Bad min karma")
                for (i in page.options.indices) {
                    page.options[i] = ControllerCensor.cens(page.options[i])
                    if (page.options[i].length > API.PAGE_POLLING_OPTION_MAX_TEXT) throw ApiException(eBadPage, "PagePolling. Bad option size")
                }
                if (page.blacklist.size > API.PAGE_POLLING_BLACKLIST_MAX) throw ApiException(eBadPage, "PagePolling. Too many blacklisted users")
            }
            is PageImages -> {
                page.title = ControllerCensor.cens(page.title)
                if (page.title.length > API.PAGE_IMAGES_TITLE_MAX) throw ApiException(eBadPage, "PageImages. Bad title")
                if (page.imagesIds.size + page.insertImages.size > API.PAGE_IMAGES_MAX_COUNT) throw ApiException(eBadPage, "PageImages. Bad insertImages count")
                if (page.imagesMiniIds.size + page.insertImagesMini.size > API.PAGE_IMAGES_MAX_COUNT) throw ApiException(eBadPage, "PageImages. Bad insertImagesMini count")
                if (page.insertImages.size != page.insertImagesMini.size) throw ApiException(eBadPage, "PageImages. Arrays has not equals size")
                if (page.replacePageIndex != -1 && page.insertImages.size != 1) throw ApiException(eBadPage, "PageImages. Can't replace Image. Insert array size must bedf 1.")

                for (i in page.insertImages) {
                    if (i == null) throw ApiException(eBadPage, "PageImages. Bad image")
                    if (!ToolsBytes.isGif(i) && i.size > API.PAGE_IMAGES_WEIGHT) throw ApiException(eBadPage, "PageImages. Bad image")
                    if (ToolsBytes.isGif(i) && i.size > API.PAGE_IMAGES_WEIGHT_GIF) throw ApiException(eBadPage, "PageImages. Bad image")
                    val scale = ToolsImage.getImgScaleUnknownType(i, true, true, true)
                    val w = scale[0]
                    val h = scale[1]
                    if (!ToolsBytes.isGif(i) && (w > API.PAGE_IMAGES_SIDE || h > API.PAGE_IMAGES_SIDE)) throw ApiException(eBadPage, "PageImages. Bad  image sides $w $h")
                    if (ToolsBytes.isGif(i) && (w > API.PAGE_IMAGES_SIDE_GIF || h > API.PAGE_IMAGES_SIDE_GIF)) throw ApiException(eBadPage, "PageImages. Bad  image sides $w $h")
                }

                for (i in page.insertImagesMini) {
                    if (i == null) throw ApiException(eBadPage, "PageImages. Bad imageMini")
                    if (!ToolsBytes.isGif(i) && i.size > API.PAGE_IMAGES_MINI_WEIGHT) throw ApiException(eBadPage, "PageImages. Bad imageMini")
                    if (ToolsBytes.isGif(i) && i.size > API.PAGE_IMAGES_WEIGHT_GIF) throw ApiException(eBadPage, "PageImages. Bad imageMini")
                    val scale = ToolsImage.getImgScaleUnknownType(i, true, true, true)
                    val w = scale[0]
                    val h = scale[1]
                    if (!ToolsBytes.isGif(i) && (w > API.PAGE_IMAGES_MINI_SIDE || h > API.PAGE_IMAGES_MINI_SIDE)) throw ApiException(eBadPage, "PageImages. Bad imageMini sides $w $h")
                    if (ToolsBytes.isGif(i) && (w > API.PAGE_IMAGES_SIDE_GIF || h > API.PAGE_IMAGES_SIDE_GIF)) throw ApiException(eBadPage, "PageImages. Bad imageMini sides $w $h")
                }

            }
            is PageTable -> {
                page.title = ControllerCensor.cens(page.title)
                if (page.title.length > API.PAGE_TABLE_TITLE_MAX) throw ApiException(eBadPage, "PageTable. Bad title")
                if (page.columnsCount > API.PAGE_TABLE_MAX_COLUMNS) throw ApiException(eBadPage, "PageTable. Bad max columns")
                if (page.rowsCount > API.PAGE_TABLE_MAX_ROWS) throw ApiException(eBadPage, "PageTable. Bad max rows")
                if (page.cells.size > (page.columnsCount * page.rowsCount)) throw ApiException(eBadPage, "PageTable. Bad cells count")

                for (c in page.cells) {
                    if (c.type == PageTable.CELL_TYPE_TEXT) {
                        c.text = ControllerCensor.cens(c.text)
                        if (c.text.length > API.PAGE_TABLE_MAX_TEXT_SIZE) throw ApiException(eBadPage, "PageTable. Bad text size")
                        if (c.insertImage != null) throw ApiException(eBadPage, "PageTable. Text cell can't add image")
                    } else if (c.type == PageTable.CELL_TYPE_IMAGE) {
                        if (c.insertImage != null) {
                            if (c.insertImage == null) throw ApiException(eBadPage, "PageTable. Insert iamge is null")
                            if (!ToolsBytes.isGif(c.insertImage) && c.insertImage!!.size > API.PAGE_TABLE_MAX_IMAGE_WEIGHT) throw ApiException(eBadPage, "PageTable. Bad image")
                            if (ToolsBytes.isGif(c.insertImage) && c.insertImage!!.size > API.PAGE_TABLE_MAX_IMAGE_WEIGHT_GIF) throw ApiException(eBadPage, "PageTable. Bad image")
                            val scale = ToolsImage.getImgScaleUnknownType(c.insertImage!!, true, true, true)
                            val w = scale[0]
                            val h = scale[1]
                            if (!ToolsBytes.isGif(c.insertImage) && (w > API.PAGE_TABLE_MAX_IMAGE_SIDE || h > API.PAGE_TABLE_MAX_IMAGE_SIDE)) throw ApiException(eBadPage, "PageTable. Bad  image sides $w $h")
                            if (ToolsBytes.isGif(c.insertImage) && (w > API.PAGE_TABLE_MAX_IMAGE_SIDE_GIF || h > API.PAGE_TABLE_MAX_IMAGE_SIDE_GIF)) throw ApiException(eBadPage, "PageTable. Bad  image sides $w $h")
                        }
                    } else {
                        throw ApiException(eBadPage, "PageTable. Bad insert type")
                    }
                }
            }
            is PageDownload -> {
                page.title = ControllerCensor.cens(page.title)
                if (page.title.length > API.PAGE_DOWNLOAD_TITLE_MAX) throw ApiException(eBadPage, "PageDownload. Bad title size")
                if (page.patch.length > API.PAGE_DOWNLOAD_TITLE_MAX) throw ApiException(eBadPage, "PageDownload. Bad patch size")
                if (page.insertBytes!!.size > API.PAGE_DOWNLOAD_SIZE_MAX) throw ApiException(eBadPage, "PageDownload. Bad max file size")
            }
            is PageCampfireObject -> {
                if (page.link.length > API.PAGE_CAMPFIRE_OBJECT_LINK_MAX) throw ApiException(eBadPage, "PageCampfireObject. Bad link size")
            }
            is PageCode -> {
                page.code = ControllerCensor.cens(page.code, "%s", "*")
                if (page.code.length > API.PAGE_TEXT_MAX_L)
                    throw ApiException(eBadPage, "PageCode. Bad code length [${page.code.length} > ${API.PAGE_TEXT_MAX_L}]")
            }
            else -> {
                throw ApiException(eBadPage, "Unknown page type [${page.getType()}]")
            }
        }

    }

    fun insertPage(page: Page, publicationId: Long) {

        when (page) {
            is PageImage -> {
                page.imageId = ControllerResources.put(page.insertBytes!!, publicationId)
                if (page.insertGifBytes != null) page.gifId = ControllerResources.put(page.insertGifBytes!!, publicationId)
                page.insertBytes = null
                page.insertGifBytes = null
            }
            is PageLinkImage -> {
                if(page.insertBytes != null) {
                    page.imageId = ControllerResources.put(page.insertBytes!!, publicationId)
                    page.insertBytes = null
                }
            }
            is PageVideo -> {
                page.imageId = ControllerResources.put(page.insertBytes!!, publicationId)
                page.insertBytes = null
            }
            is PagePolling -> {
                page.pollingId = System.nanoTime()
            }
            is PageImages -> {
                if (page.replacePageIndex != -1) {
                    ControllerResources.remove(page.imagesIds[page.replacePageIndex])
                    ControllerResources.remove(page.imagesMiniIds[page.replacePageIndex])
                }
                var x = 0
                page.imagesIds = Array(page.imagesIds.size + if (page.replacePageIndex == -1) page.insertImages.size else 0) {
                    if (it >= page.imagesIds.size || it == page.replacePageIndex) ControllerResources.put(page.insertImages[x++], publicationId)
                    else page.imagesIds[it]
                }

                x = 0
                page.imagesMiniIds = Array(page.imagesMiniIds.size + if (page.replacePageIndex == -1) page.insertImages.size else 0) {
                    if (it >= page.imagesMiniIds.size || it == page.replacePageIndex) {
                        val scales = ToolsImage.getImgScaleUnknownType(page.insertImagesMini[x]!!, true, true, true)
                        page.imagesMiniSizesW = ToolsCollections.add(scales[0], page.imagesMiniSizesW)
                        page.imagesMiniSizesH = ToolsCollections.add(scales[1], page.imagesMiniSizesH)
                        ControllerResources.put(page.insertImagesMini[x++], publicationId)
                    } else page.imagesMiniIds[it]
                }
                if (page.removePageIndex != -1) {
                    ControllerResources.remove(page.imagesIds[page.removePageIndex])
                    ControllerResources.remove(page.imagesMiniIds[page.removePageIndex])
                    page.imagesIds = ToolsCollections.remove(page.removePageIndex, page.imagesIds)
                    page.imagesMiniIds = ToolsCollections.remove(page.removePageIndex, page.imagesMiniIds)
                    page.imagesMiniSizesW = ToolsCollections.remove(page.removePageIndex, page.imagesMiniSizesW)
                    page.imagesMiniSizesH = ToolsCollections.remove(page.removePageIndex, page.imagesMiniSizesH)
                }
                page.removePageIndex = -1
                page.replacePageIndex = -1
            }
            is PageTable -> {
                val newCells = ArrayList<PageTable.Cell>()
                for (c in page.cells) {
                    if (c.rowIndex >= page.rowsCount || c.columnIndex >= page.columnsCount) {
                        if (c.imageId > 0) ControllerResources.remove(c.imageId)
                    } else {
                        newCells.add(c)
                    }
                }
                page.cells = newCells.toTypedArray()
                for (c in page.cells) {
                    if (c.insertImage != null) {
                        if (c.imageId > 0) ControllerResources.remove(c.imageId)

                        c.imageId = ControllerResources.put(c.insertImage!!, publicationId)
                        c.insertImage = null
                    }
                }
            }
            is PageDownload -> {
                page.resourceId = ControllerResources.put(page.insertBytes!!, publicationId)
                page.size = page.insertBytes!!.size.toLong()
                page.insertBytes = null
            }
        }

    }

    fun removePage(page: Page) {
        when (page) {
            is PageImage -> {
                ControllerResources.remove(page.imageId)
                if (page.gifId != 0L) ControllerResources.remove(page.gifId)
            }
            is PageLinkImage -> {
                ControllerResources.remove(page.imageId)
            }
            is PagePolling -> {
                if (page.pollingId != 0L) {
                    val remove = SqlQueryRemove(TCollisions.NAME)
                    remove.where(TCollisions.collision_id, "=", page.pollingId)
                    remove.where(TCollisions.collision_type, "=", API.COLLISION_PAGE_POLLING_VOTE)
                    Database.remove("ControllerPost.removePage", remove)
                }
            }
            is PageImages -> {
                for (i in page.imagesIds) ControllerResources.remove(i)
                for (i in page.imagesMiniIds) ControllerResources.remove(i)
            }
            is PageTable -> {
                for (c in page.cells) {
                    if (c.type == PageTable.CELL_TYPE_IMAGE) ControllerResources.remove(c.imageId)
                }
            }
            is PageDownload -> {
                ControllerResources.remove(page.resourceId)
            }
        }

    }

}
