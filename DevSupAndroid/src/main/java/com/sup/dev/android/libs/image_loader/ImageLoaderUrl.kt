package com.sup.dev.android.libs.image_loader

import com.sup.dev.android.app.SupAndroid
import com.sup.dev.java.libs.debug.err
import okhttp3.Request
import sh.sit.bonfire.networking.OkHttpController

open class ImageLoaderUrl(
    protected val url: String
) : ImageLink() {
    override fun load(): ByteArray? {
        if (url.isEmpty()) return null
        val client = OkHttpController.getClient(SupAndroid.appContext!!)
        val request = Request.Builder()
            .url(url)
            .build()
        try {
            val resp = client
                .newCall(request)
                .execute()
            if (!resp.isSuccessful) {
                err("ImageLoaderUrl failed with status ${resp.code} ${resp.message}")
                err(resp.use { it.body!!.string() })
                return null
            }
            return resp.use {
                it.body!!.bytes()
            }
        } catch (e: Exception) {
            err("ImageLoaderUrl failed:")
            err(e)
            return null
        }
    }

    override fun copyLocal(): ImageLink {
        return ImageLoaderUrl(url)
    }

    override fun equalsTo(imageLoader: ImageLink): Boolean {
        return (imageLoader as? ImageLoaderUrl)?.url == url
    }

    override fun getKeyOfImage(): String {
        return "url_${url}"
    }
}
