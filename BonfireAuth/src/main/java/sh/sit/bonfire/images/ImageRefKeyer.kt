package sh.sit.bonfire.images

import coil.key.Keyer
import coil.request.Options
import com.dzen.campfire.api.models.images.ImageRef

object ImageRefKeyer : Keyer<ImageRef> {
    fun key(data: ImageRef): String {
        return if (data.imageId > 0) {
            "ref:id:${data.imageId}"
        } else {
            "ref:url:${data.url}"
        }
    }

    override fun key(data: ImageRef, options: Options): String {
        return key(data)
    }
}
