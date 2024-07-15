package com.sayzen.campfiresdk.support

import android.view.View
import android.widget.ImageView
import com.dzen.campfire.api.models.images.ImageRef
import com.sup.dev.android.libs.image_loader.ImageLink
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.image_loader.ImageLoaderRef
import com.sup.dev.java.tools.ToolsThreads

fun ImageLoaderRef.Companion.create(ref: ImageRef): ImageLoaderRef {
    return ImageLoaderRef(ref.url, ref.imageId)
}

fun ImageLoader.load(ref: ImageRef) = ImageLoaderRef.create(ref)
    .size(ref.width, ref.height)

fun ImageLoader.clear(ref: ImageRef) = ImageLoaderRef.create(ref)
    .clear()

fun ImageLoader.loadGif(
    image: ImageRef,
    gif: ImageRef,
    vImage: ImageView,
    vGifProgressBar: View? = null,
    onInit: (ImageLink) -> Unit = {}
) {
    if (gif.isEmpty()) {
        // only base image
        ToolsThreads.main { vGifProgressBar?.visibility = View.INVISIBLE }
        val load = load(image)
        onInit.invoke(load)
        load.into(vImage)
    } else if (image.isNotEmpty()) {
        // base image and gif
        val load = load(image)
        onInit.invoke(load)
        load.into(vImage) {
            val loadGif = load(gif)
            onInit.invoke(loadGif)
            loadGif.holder(vImage.drawable).into(vImage, vGifProgressBar)
        }
    } else {
        // only gif
        val load = load(gif)
        onInit.invoke(load)
        load.holder(vImage.drawable).into(vImage, vGifProgressBar)
    }
}
