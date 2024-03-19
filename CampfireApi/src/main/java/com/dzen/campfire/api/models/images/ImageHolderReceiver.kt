package com.dzen.campfire.api.models.images

interface ImageHolderReceiver {
    fun add(imageRef: ImageRef, legacyId: Long? = null, width: Int? = 0, height: Int? = 0)
}
