package sh.sit.bonfire.images

import android.content.Context
import android.os.Build
import coil.Coil
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import sh.sit.bonfire.networking.OkHttpController

object ImagesController {
    fun init(context: Context) {
        Coil.setImageLoader {
            ImageLoader.Builder(context)
                .okHttpClient(OkHttpController.getClient(context))
                // zero idea how to control cache on minio
                .respectCacheHeaders(false)
                .memoryCache {
                    MemoryCache.Builder(context)
                        .maxSizeBytes(50 * 1024)
                        .build()
                }
                .diskCache {
                    DiskCache.Builder()
                        .directory(context.cacheDir.resolve("dildos"))
                        .maxSizePercent(0.02)
                        .build()
                }
                .components {
                    if (Build.VERSION.SDK_INT >= 28) {
                        add(ImageDecoderDecoder.Factory())
                    } else {
                        add(GifDecoder.Factory())
                    }
                }
                .build()
        }
    }
}
