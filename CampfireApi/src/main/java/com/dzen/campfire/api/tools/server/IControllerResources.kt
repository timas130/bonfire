package com.dzen.campfire.api.tools.server

import com.dzen.campfire.api.models.images.ImageHolderReceiver

interface IControllerResources : ImageHolderReceiver {
    fun put(resource: ByteArray?, publicationId: Long, pwd: String = ""): Long
    fun get(resourceId: Long): ByteArray

    fun getPublicUrl(resourceId: Long): String
}
