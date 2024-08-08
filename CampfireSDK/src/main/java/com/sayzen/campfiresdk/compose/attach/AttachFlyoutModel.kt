package com.sayzen.campfiresdk.compose.attach

import android.app.Application
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.posthog.PostHog
import com.sayzen.campfiresdk.compose.util.combineStates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.closeQuietly
import java.io.File

class AttachFlyoutModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private val Context.attachDatastore by preferencesDataStore("attach")

        private val lastActiveTab = stringPreferencesKey("lastActiveTab")
    }

    enum class Tab {
        Gallery,
        Gif,
        Stickers,
    }

    private val datastore = getApplication<Application>().applicationContext.attachDatastore

    private val _activeTab = MutableStateFlow(Tab.Gallery)
    val activeTab = _activeTab.asStateFlow()

    // == gallery ==

    private var _galleryFilter = MutableStateFlow<Album?>(null)
    val galleryFilter = _galleryFilter.asStateFlow()

    private var galleryCursor: Cursor? = null
    private var galleryVersion = MutableStateFlow<String?>(null)

    // using ArrayList instead of mutableListOf is justified
    // since we use its api to reserve capacity
    private val galleryCache = ArrayList<GalleryImage?>()

    private fun loadGallery(force: Boolean = false) {
        val context = getApplication<Application>().applicationContext

        galleryCursor?.let {
            val currentVersion = MediaStore.getVersion(context)
            if (currentVersion == galleryVersion.value && !force) {
                // don't reload if no changes occurred
                return
            }
        }

        val contentResolver = context.contentResolver

        val galleryFilter = this._galleryFilter.value
        val selection = if (galleryFilter != null) {
            Pair(
                "${MediaStore.Images.ImageColumns.BUCKET_ID} = ?",
                arrayOf(galleryFilter.id.toString())
            )
        } else {
            Pair(null, null)
        }

        galleryCache.clear()
        galleryCursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA),
            selection.first,
            selection.second,
            MediaStore.Images.ImageColumns.DATE_MODIFIED + " DESC"
        ) ?: return
        galleryVersion.value = MediaStore.getVersion(context)
    }

    data class GalleryImage(
        val id: Int,
        val file: File,
    )

    val totalGalleryImages = galleryVersion.asStateFlow()
        .combineStates(galleryFilter) { _, _ -> galleryCursor?.count ?: 0 }
    fun getGalleryImage(offset: Int): GalleryImage? {
        val cached = galleryCache.getOrNull(offset)
        if (cached != null) {
            return cached
        }

        val cursor = galleryCursor!!

        if (offset >= cursor.count) return null
        cursor.moveToPosition(offset)
        val galleryImage = GalleryImage(
            id = cursor.getInt(0),
            file = File(cursor.getString(1)),
        )

        galleryCache.ensureCapacity(offset + 1)
        while (galleryCache.size <= offset) {
            galleryCache.add(null)
        }
        galleryCache[offset] = galleryImage

        return galleryImage
    }

    sealed interface AlbumLike
    data class AllAlbums(val elements: Int) : AlbumLike
    data class Album(val id: Int, val name: String, var elements: Int, val preview: GalleryImage) : AlbumLike

    private val _galleryAlbums = MutableStateFlow<List<AlbumLike>?>(null)
    val galleryAlbums = _galleryAlbums.asStateFlow()

    fun loadGalleryAlbums() {
        if (_galleryAlbums.value != null) return

        PostHog.capture("attach flyout gallery albums loaded")

        val context = getApplication<Application>().applicationContext
        val contentResolver = context.contentResolver

        val cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.BUCKET_ID,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
            ),
            null,
            null,
            MediaStore.Images.ImageColumns.DATE_MODIFIED + " DESC"
        ) ?: return

        val albumMap = hashMapOf<Int, Album>()

        while (cursor.moveToNext()) {
            val id = cursor.getInt(0)
            val path = cursor.getString(1)
            val albumId = cursor.getInt(2)
            val albumName = cursor.getString(3)

            albumMap.compute(albumId) { _, album ->
                if (album != null) {
                    album.elements++
                    album
                } else {
                    Album(
                        id = albumId,
                        name = albumName,
                        elements = 1,
                        preview = GalleryImage(id = id, file = File(path))
                    )
                }
            }
        }

        val allAlbums = AllAlbums(cursor.count)

        cursor.close()

        val albums = mutableListOf<AlbumLike>(allAlbums)
        albums.addAll(albumMap.values.sortedByDescending { it.preview.id })

        _galleryAlbums.value = albums
    }

    fun setGalleryFilter(album: AlbumLike) {
        PostHog.capture("attach flyout gallery filter set", properties = mapOf("all" to (album is AllAlbums)))

        _galleryFilter.value = album as? Album
        loadGallery(force = true)
    }

    override fun onCleared() {
        super.onCleared()
        galleryCursor?.closeQuietly()
    }

    // == gif ==



    // == other public apis ==

    fun switchTab(tab: Tab) {
        PostHog.capture("attach flyout tab switched", properties = mapOf("tab" to tab.name))

        _activeTab.value = tab
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                datastore.edit {
                    it[lastActiveTab] = tab.name
                }
            }
        }
    }

    // constructor code is at the bottom to make sure all the properties
    // are initialised before calling loadGallery()
    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _activeTab.emit(Tab.valueOf(datastore.data.first()[lastActiveTab] ?: Tab.Gallery.toString()))
            }
        }
        loadGallery()
    }
}
