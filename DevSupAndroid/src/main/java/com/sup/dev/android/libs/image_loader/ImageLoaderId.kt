package com.sup.dev.android.libs.image_loader

class ImageLoaderId(val imageId: Long, var pwd: String? = null) : ImageLink() {

    companion object {
        var loader: (Long, String?) -> ByteArray? = { _, _ -> throw RuntimeException("You must set your own loader!") }
    }

    override fun equalsTo(imageLoader: ImageLink): Boolean {
        return imageId == (imageLoader as ImageLoaderId).imageId
    }

    override fun getKeyOfImage() = "imgId_${imageId}"

    override fun load(): ByteArray? {
        if (imageId < 1) return null
        return loader.invoke(imageId, pwd)
    }

    override fun copyLocal() = ImageLoaderId(imageId)

}