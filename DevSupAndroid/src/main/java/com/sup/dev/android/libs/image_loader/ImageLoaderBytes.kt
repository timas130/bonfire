package com.sup.dev.android.libs.image_loader

class ImageLoaderBytes(
        private val bytes: ByteArray
) : ImageLink() {

    override fun equalsTo(imageLoader: ImageLink): Boolean {
        return bytes.hashCode() == (imageLoader as ImageLoaderBytes).bytes.hashCode()
    }

    override fun getKeyOfImage() = "bytes_${bytes.hashCode()}"

    override fun load(): ByteArray {
        return bytes
    }

    override fun copyLocal() = ImageLoaderBytes(bytes)
}