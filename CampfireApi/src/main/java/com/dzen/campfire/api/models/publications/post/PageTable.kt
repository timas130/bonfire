package com.dzen.campfire.api.models.publications.post

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.images.ImageHolder
import com.dzen.campfire.api.models.images.ImageHolderReceiver
import com.dzen.campfire.api.models.images.ImageRef
import com.dzen.campfire.api.tools.client.Request
import com.dzen.campfire.api.tools.server.IControllerResources
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.libs.json.JsonParsable

class PageTable : Page() {

    companion object {
        const val CELL_TYPE_TEXT = 1L
        const val CELL_TYPE_IMAGE = 2L
    }

    var title = ""
    var columnsCount = 0
    var rowsCount = 0
    var cells: Array<Cell> = emptyArray()

    override fun getType() = API.PAGE_TYPE_TABLE

    override fun json(inp: Boolean, json: Json): Json {
        columnsCount = json.m(inp, "columnsCount", columnsCount)
        rowsCount = json.m(inp, "rowsCount", rowsCount)
        title = json.m(inp, "title", title)
        cells = json.m(inp, "cells", cells)
        return super.json(inp, json)
    }

    override fun addInsertData(request: Request<*>) {
        for (c in cells) if (c.insertImage != null) c.insertImageIndex = request.addDataOutput(c.insertImage)
    }

    override fun restoreInsertData(request: Request<*>, offset: Int): Int {
        var count = 0
        for (c in cells) if (c.insertImageIndex != -1) {
            c.insertImage = request.dataOutput[c.insertImageIndex]
            c.insertImageIndex = -1
            count++
        }
        return count
    }

    override fun duplicateResources(res: IControllerResources, unitId: Long) {
        for (c in cells) {
            if (c.imageId > 0) {
                c.imageId = res.put(res.get(c.imageId), unitId)
            }
        }
    }

    override fun fillResourcesList(list: ArrayList<Long>) {
        for (c in cells) {
            if (c.imageId > 0) {
                list.add(c.imageId)
            }
        }
    }

    fun getCell(rowIndex: Int, columnIndex: Int): Cell? {
        for (c in cells) {
            if (c.rowIndex == rowIndex && c.columnIndex == columnIndex) {
                return c
            }
        }
        return null
    }

    override fun isRemoveOnChange(): Boolean = false

    override fun prepareForServer(page: Page) {
        page as PageTable
        columnsCount = page.columnsCount
        rowsCount = page.rowsCount
        title = page.title
        cells = page.cells
    }

    override fun fillImageRefs(receiver: ImageHolderReceiver) {
        super.fillImageRefs(receiver)
        for (c in cells) {
            c.fillImageRefs(receiver)
        }
    }

    class Cell : JsonParsable, ImageHolder {
        var rowIndex = 0
        var columnIndex = 0
        var text = ""
        var image = ImageRef()
        @Deprecated("use ImageRefs")
        var imageId = 0L
        var type = 0L

        var insertImage: ByteArray? = null
        var insertImageIndex = -1

        override fun json(inp: Boolean, json: Json): Json {
            rowIndex = json.m(inp, "rowIndex", rowIndex)
            columnIndex = json.m(inp, "columnIndex", columnIndex)
            type = json.m(inp, "type", type)
            image = json.m(inp, "image", image)
            imageId = json.m(inp, "imageId", imageId)
            text = json.m(inp, "text", text)
            insertImageIndex = json.m(inp, "insertImageIndex", insertImageIndex)
            return json
        }

        override fun fillImageRefs(receiver: ImageHolderReceiver) {
            receiver.add(image, imageId)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Cell

            if (rowIndex != other.rowIndex) return false
            if (columnIndex != other.columnIndex) return false
            if (text != other.text) return false
            if (image != other.image) return false
            if (imageId != other.imageId) return false
            if (type != other.type) return false
            if (insertImage != null) {
                if (other.insertImage == null) return false
                if (!insertImage.contentEquals(other.insertImage)) return false
            } else if (other.insertImage != null) return false
            if (insertImageIndex != other.insertImageIndex) return false

            return true
        }

        override fun hashCode(): Int {
            var result = rowIndex
            result = 31 * result + columnIndex
            result = 31 * result + text.hashCode()
            result = 31 * result + image.hashCode()
            result = 31 * result + imageId.hashCode()
            result = 31 * result + type.hashCode()
            result = 31 * result + (insertImage?.contentHashCode() ?: 0)
            result = 31 * result + insertImageIndex
            return result
        }

        companion object {
            val Empty = Cell().apply {
                type = CELL_TYPE_TEXT
                text = ""
            }
        }
    }
}
