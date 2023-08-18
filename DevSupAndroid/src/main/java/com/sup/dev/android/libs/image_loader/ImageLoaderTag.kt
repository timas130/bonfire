package com.sup.dev.android.libs.image_loader

class ImageLoaderTag(val imageTag: String) : ImageLink() {


    companion object {
        var loader: (String) -> ByteArray? = { throw RuntimeException("You must set your own loader!") }
    }

    override fun equalsTo(imageLoader: ImageLink): Boolean {
        return imageTag == (imageLoader as ImageLoaderTag).imageTag
    }

    override fun getKeyOfImage() = "imageTag_${imageTag}"

    override fun load(): ByteArray? {
        if (imageTag.isEmpty()) return null
        return loader.invoke(imageTag)
    }

    override fun copyLocal() = ImageLoaderTag(imageTag)

}