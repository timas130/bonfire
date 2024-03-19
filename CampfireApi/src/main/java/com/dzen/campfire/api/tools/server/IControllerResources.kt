package com.dzen.campfire.api.tools.server

interface IControllerResources {
    fun put(resource: ByteArray?, publicationId: Long, pwd: String = ""): Long
    fun get(resourceId: Long): ByteArray

    fun getPublicUrl(resourceId: Long): String
}
