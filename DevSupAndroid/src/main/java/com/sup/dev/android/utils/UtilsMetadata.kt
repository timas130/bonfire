package com.sup.dev.android.utils

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Build
import androidx.annotation.RequiresApi
import com.sup.dev.android.tools.ToolsBitmap
import com.sup.dev.java.libs.debug.info

class UtilsMetadata(var retriever: MediaMetadataRetriever? = null) {

    constructor (path: String) : this(wrap(path))

    fun parse(path: String) {

        val retriever = wrap(path)

        info("getEmbeddedPicture", retriever.getEmbeddedPicture())
        info("getFrameAtTime", retriever.getFrameAtTime())
        info("CD_TRACK_NUMBER", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER))
        info("ALBUM", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM))
        info("ARTIST", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST))
        info("AUTHOR", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR))
        info("COMPOSER", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPOSER))
        info("DATE", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE))
        info("GENRE", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE))
        info("TITLE", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE))
        info("YEAR", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR))
        info("DURATION", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))
        info("NUM_TRACKS", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_NUM_TRACKS))
        info("WRITER", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_WRITER))
        info("MIMETYPE", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE))
        info("ALBUMARTIST", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST))
        info("DISC_NUMBER", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER))
        info("COMPILATION", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPILATION))
        info("HAS_AUDIO", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO))
        info("HAS_VIDEO", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO))
        info("VIDEO_WIDTH", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH))
        info("VIDEO_HEIGHT", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT))
        info("BITRATE", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE))
        info("LOCATION", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_LOCATION))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            info("VIDEO_ROTATION", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            info("CAPTURE_FRAMERATE", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE))

    }

    //
    //  Inited
    //

    fun getPreview(): Bitmap? {
        val embeddedPicture = retriever!!.getEmbeddedPicture()
        return if (embeddedPicture != null) ToolsBitmap.decode(embeddedPicture) else retriever!!.getFrameAtTime()

    }

    fun getVideoWidth(): Int {
        return Integer.parseInt(retriever!!.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH))
    }

    fun getVideoHeight(): Int {
        return Integer.parseInt(retriever!!.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT))
    }

    fun getDurationMs(): Int {
        return Integer.parseInt(retriever!!.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?:"0")
    }

    fun getTrackCount(): Int {
        return Integer.parseInt(retriever!!.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER))
    }

    fun getMimeType(): String {
        return retriever!!.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)!!
    }

    fun hasAudio(): Boolean {
        return retriever!!.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO)?.toLowerCase() == "yes"
    }

    fun hasVideo(): Boolean {
        return retriever!!.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO)?.toLowerCase() == "yes"
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    fun getVideoRotation(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) Integer.parseInt(retriever!!.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)) else 0
    }
    //
    //  Static
    //

    companion object {
        private fun wrap(path: String): MediaMetadataRetriever {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(path)
            return retriever
        }

        fun getPreview(path: String): Bitmap? {
            val retriever = wrap(path)

            val embeddedPicture = retriever.embeddedPicture
            return if (embeddedPicture != null) ToolsBitmap.decode(embeddedPicture) else retriever.frameAtTime

        }

        fun getVideoWidth(path: String): Int {
            return Integer.parseInt(wrap(path).extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH))
        }

        fun getVideoHeight(path: String): Int {
            return Integer.parseInt(wrap(path).extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT))
        }

        fun getDurationMs(path: String): Int {
            return Integer.parseInt(wrap(path).extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))
        }

        fun getTrackCount(path: String): Int {
            return Integer.parseInt(wrap(path).extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER))
        }

        fun getMimeType(path: String): String {
            return wrap(path).extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)!!
        }

        fun hasAudio(path: String): Boolean {
            return wrap(path).extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO)?.toLowerCase() == "yes"
        }

        fun hasVideo(path: String): Boolean {
            return wrap(path).extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO)?.toLowerCase() == "yes"
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
        fun getVideoRotation(path: String): Int {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) Integer.parseInt(wrap(path).extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)) else 0
        }

    }


}
