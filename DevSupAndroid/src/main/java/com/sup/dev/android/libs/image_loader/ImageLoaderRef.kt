package com.sup.dev.android.libs.image_loader

class ImageLoaderRef(
    url: String,
    private val id: Long,
) : ImageLoaderUrl(url) {
    companion object;

    override fun copyLocal(): ImageLink {
        return ImageLoaderRef(url, id)
    }

    override fun equalsTo(imageLoader: ImageLink): Boolean {
        return super.equalsTo(imageLoader) && (imageLoader as ImageLoaderRef).id == id
    }

    override fun getKeyOfImage(): String {
        return if (id > 0) {
            "ref_$id"
        } else {
            "ref_static_$url"
        }
    }
}
