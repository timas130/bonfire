package com.dzen.campfire.api.models.publications.post

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.images.ImageHolder
import com.dzen.campfire.api.models.images.ImageHolderReceiver
import com.dzen.campfire.api.tools.client.Request
import com.dzen.campfire.api.tools.server.IControllerResources
import com.sup.dev.java.libs.json.Json
import com.sup.dev.java.libs.json.JsonPolimorf


sealed class Page : JsonPolimorf, ImageHolder {
    // @ client internal
    var index = 0

    override fun json(inp: Boolean, json: Json): Json {
        if (inp) json.put(J_PAGE_TYPE, getType())
        return json
    }

    abstract fun getType(): Long

    open fun addInsertData(request: Request<*>) {

    }

    open fun restoreInsertData(request: Request<*>, offset: Int): Int {
        return 0
    }

    open fun isRemoveOnChange() = true

    abstract fun fillResourcesList(list: ArrayList<Long>)

    override fun fillImageRefs(receiver: ImageHolderReceiver) {

    }

    open fun duplicateResources(res: IControllerResources, unitId: Long) {}

    open fun prepareForServer(page: Page) {

    }

    open fun copyChangeData(page: Page) {

    }

    companion object {

        private val J_PAGE_TYPE = "J_PAGE_TYPE"

        //
        //  Static
        //

        @JvmStatic
        fun instance(pageType: Long): Page {
            return when (pageType) {
                API.PAGE_TYPE_TEXT -> PageText()
                API.PAGE_TYPE_IMAGE -> PageImage()
                API.PAGE_TYPE_IMAGES -> PageImages()
                API.PAGE_TYPE_LINK -> PageLink()
                API.PAGE_TYPE_QUOTE -> PageQuote()
                API.PAGE_TYPE_SPOILER -> PageSpoiler()
                API.PAGE_TYPE_POLLING -> PagePolling()
                API.PAGE_TYPE_VIDEO -> PageVideo()
                API.PAGE_TYPE_TABLE -> PageTable()
                API.PAGE_TYPE_DOWNLOAD -> PageDownload()
                API.PAGE_TYPE_CAMPFIRE_OBJECT -> PageCampfireObject()
                API.PAGE_TYPE_USER_ACTIVITY -> PageUserActivity()
                API.PAGE_TYPE_LINK_IMAGE -> PageLinkImage()
                API.PAGE_TYPE_CODE -> PageCode()
                else -> PageText().apply { text = "[null]" }
            }
        }

        @JvmStatic
        fun instance(json: Json): Page {
            val type = json.get<Long>(J_PAGE_TYPE)!!

            val page = instance(type)
            page.json(false, json)

            return page
        }
    }
}
