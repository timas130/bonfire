package com.sup.dev.android.libs.image_loader

import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.tools.ToolsNetwork
import com.sup.dev.java.tools.ToolsThreads

class ImageLoaderUrl(
        private val url: String
) : ImageLink() {

    private var tryCount = 5


    override fun equalsTo(imageLoader: ImageLink): Boolean {
        return url == (imageLoader as ImageLoaderUrl).url
    }

    override fun getKeyOfImage() = "url_${url}"

    override fun load(): ByteArray? {
        return load(tryCount)
    }

    private fun load(tryCount:Int): ByteArray?{
        if(tryCount < 0) {
            return null
        }
        try {
            return ToolsNetwork.getBytesFromURL(url)!!
        } catch (e: Exception) {
            err(e)
            if(tryCount > 1) {
                ToolsThreads.sleep(1000)
                return load(tryCount - 1)
            }else{
                return null
            }
        }
    }

    override fun copyLocal() = ImageLoaderUrl(url)

    fun setTryCount(tryCount:Int):ImageLoaderUrl{
        this.tryCount = tryCount
        return this
    }
}