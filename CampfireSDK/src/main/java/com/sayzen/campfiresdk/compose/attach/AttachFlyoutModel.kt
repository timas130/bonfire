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
import com.sayzen.campfiresdk.GifSearchSuggestionsQuery
import com.sayzen.campfiresdk.compose.util.combineStates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.closeQuietly
import sh.sit.bonfire.auth.apollo
import java.io.File

internal class AttachFlyoutModel(application: Application) : AndroidViewModel(application) {
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

    private var _trendingGifQueries = MutableStateFlow<List<String>?>(null)
    private val _gifSearchSuggestions = MutableStateFlow<Pair<String, List<String>>?>(null)

    private val _gifQuery = MutableStateFlow("")
    val gifQuery = _gifQuery.asStateFlow()

    private var gifTabStarted: Boolean = false

    val gifSearchModel = GifSearchModel(application, gifQuery)

    val searchSuggestions = gifQuery
        .combineStates(_gifSearchSuggestions) { query, suggestions ->
            if (query.isBlank()) {
                Unit
            } else {
                suggestions
            }
        }
        .combineStates(_trendingGifQueries) { suggestionsOrUnit, trending ->
            if (suggestionsOrUnit is Unit) {
                Pair("", trending)
            } else {
                @Suppress("UNCHECKED_CAST")
                suggestionsOrUnit as Pair<String, List<String>>?
            }
        }

    fun setGifQuery(query: String) {
        _gifQuery.value = query
    }

    @OptIn(FlowPreview::class)
    private fun startGifSuggestions() {
        viewModelScope.launch {
            gifQuery
                .sample(300)
                .distinctUntilChanged()
                .filterNot { it.isBlank() }
                .collectLatest { query ->
                    try {
                        val resp = apollo.query(GifSearchSuggestionsQuery(query))
                            .execute()
                            .dataAssertNoErrors

                        _gifSearchSuggestions.emit(Pair(query, resp.gifSearchSuggestions))
                    } catch (_: Exception) {
                    }
                }
        }
    }

    private fun loadTrendingGifQueries() {
        viewModelScope.launch {
            if (_trendingGifQueries.value != null) return@launch
            try {
                val resp = apollo.query(GifSearchSuggestionsQuery(""))
                    .execute()
                    .dataAssertNoErrors

                _trendingGifQueries.emit(resp.gifSearchSuggestions)
            } catch (_: Exception) {
            }
        }
    }

    enum class ScrollMarker {
        Recent,
        Favorite,
        Trending,
        Search,
    }

    private val _gifScrollMarker = MutableStateFlow(ScrollMarker.Recent)
    val gifScrollMarker = _gifScrollMarker.asStateFlow()

    fun setGifScrollMarker(scrollMarker: ScrollMarker) {
        _gifScrollMarker.value = scrollMarker
    }
    fun scrollToGifMarker(scrollMarker: ScrollMarker) {

    }

    // == other public apis ==

    fun switchTab(tab: Tab, userAction: Boolean = false) {
        _activeTab.value = tab

        when (tab) {
            Tab.Gallery -> {
                loadGallery()
            }

            Tab.Gif -> {
                if (!gifTabStarted) {
                    gifTabStarted = true
                    startGifSuggestions()
                    loadTrendingGifQueries()
                    gifSearchModel.start()
                }
            }

            Tab.Stickers -> {}
        }

        if (userAction) {
            PostHog.capture("attach flyout tab switched", properties = mapOf("tab" to tab.name))

            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    datastore.edit {
                        it[lastActiveTab] = tab.name
                    }
                }
            }
        }
    }

    // constructor code is at the bottom to make sure all the properties
    // are initialised before calling init functions
    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                switchTab(Tab.valueOf(datastore.data.first()[lastActiveTab] ?: Tab.Gallery.toString()))
            }
        }
    }
}
