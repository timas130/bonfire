package com.dzen.campfire.api.models.publications.post

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.images.ImageHolderReceiver
import com.dzen.campfire.api.models.images.ImageRef
import com.dzen.campfire.api.tools.client.Request
import com.sup.dev.java.libs.json.Json

class PageImages : Page() {
    var title = ""
    var images = emptyArray<ImageRef>()
    var imagesMini = emptyArray<ImageRef>()

    @Deprecated("use ImageRefs")
    var imagesIds: Array<Long> = emptyArray()

    @Deprecated("use ImageRefs")
    var imagesMiniIds: Array<Long> = emptyArray()

    @Deprecated("use ImageRefs")
    var imagesMiniSizesW: Array<Int> = emptyArray()

    @Deprecated("use ImageRefs")
    var imagesMiniSizesH: Array<Int> = emptyArray()
    var insertImages: Array<ByteArray?> = emptyArray()
    var insertImagesMini: Array<ByteArray?> = emptyArray()

    var imagesCount = 0

    var replacePageIndex = -1
    var removePageIndex = -1

    override fun getType() = API.PAGE_TYPE_IMAGES

    override fun isRemoveOnChange() = false

    override fun prepareForServer(page: Page) {
        page as PageImages
        title = page.title
        insertImages = page.insertImages
        insertImagesMini = page.insertImagesMini
        removePageIndex = page.removePageIndex
        replacePageIndex = page.replacePageIndex
    }

    override fun fillResourcesList(list: ArrayList<Long>) {
        for (i in imagesIds) list.add(i)
        for (i in imagesMiniIds) list.add(i)
    }

    override fun addInsertData(request: Request<*>) {
        for (i in insertImages) request.addDataOutput(i)
        for (i in insertImagesMini) request.addDataOutput(i)
        imagesCount = insertImages.size
    }

    override fun restoreInsertData(request: Request<*>, offset: Int): Int {
        insertImages = Array(imagesCount) { request.dataOutput[it + offset] }
        insertImagesMini = Array(imagesCount) { request.dataOutput[it + offset + imagesCount] }
        return imagesCount / 2
    }

    override fun json(inp: Boolean, json: Json): Json {
        title = json.m(inp, "title", title)
        images = json.m(inp, "images", images)
        imagesMini = json.m(inp, "imagesMini", imagesMini)
        imagesIds = json.m(inp, "imagesIds", imagesIds)
        imagesMiniIds = json.m(inp, "imagesMiniIds", imagesMiniIds)
        imagesMiniSizesW = json.m(inp, "imagesMiniSizesW", imagesMiniSizesW)
        imagesMiniSizesH = json.m(inp, "imagesMiniSizesH", imagesMiniSizesH)
        removePageIndex = json.m(inp, "removePageIndex", removePageIndex)
        replacePageIndex = json.m(inp, "replacePageIndex", replacePageIndex)
        imagesCount = json.m(inp, "imagesCount", imagesCount)
        return super.json(inp, json)
    }

    override fun fillImageRefs(receiver: ImageHolderReceiver) {
        super.fillImageRefs(receiver)

        val size = imagesIds.size.coerceAtLeast(images.size)
        if (images.isEmpty()) {
            images = Array(size) { ImageRef() }
        }
        if (imagesMini.isEmpty()) {
            imagesMini = Array(size) { ImageRef() }
        }

        for (i in 0 until size) {
            receiver.add(images[i], imagesIds[i])
            receiver.add(
                imagesMini[i],
                imagesMiniIds[i],
                imagesMiniSizesW.getOrNull(i) ?: 500,
                imagesMiniSizesH.getOrNull(i) ?: 500
            )
        }
    }
}
