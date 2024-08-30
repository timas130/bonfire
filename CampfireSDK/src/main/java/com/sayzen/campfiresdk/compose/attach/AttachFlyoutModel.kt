package com.sayzen.campfiresdk.compose.attach

import android.app.Application
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.provider.MediaStore
import android.provider.MediaStore.Images.ImageColumns
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dzen.campfire.api.models.publications.stickers.PublicationSticker
import com.mr0xf00.easycrop.ImageCropper
import com.mr0xf00.easycrop.crop
import com.posthog.PostHog
import com.sayzen.campfiresdk.*
import com.sayzen.campfiresdk.compose.util.combineStates
import com.sayzen.campfiresdk.compose.util.mapState
import com.sayzen.campfiresdk.fragment.AttachGifItem
import com.sayzen.campfiresdk.fragment.FavouriteGifs
import com.sup.dev.android.tools.ToolsPermission
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.internal.closeQuietly
import sh.sit.bonfire.auth.AuthController
import sh.sit.bonfire.auth.apollo
import sh.sit.bonfire.auth.optimisticUpdatesExt
import java.io.File

interface AttachFlyoutDelegate {
    fun onSelectedImage(file: File)
    fun onSelectedImages(files: List<File>)
    fun onSelectedGif(gif: AttachGifItem)
    fun onSelectedSticker(sticker: PublicationSticker)

    object Stub : AttachFlyoutDelegate {
        override fun onSelectedImage(file: File) {
        }

        override fun onSelectedImages(files: List<File>) {
        }

        override fun onSelectedGif(gif: AttachGifItem) {
        }

        override fun onSelectedSticker(sticker: PublicationSticker) {
        }
    }
}

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

    val pagerScrollAllowed = MutableStateFlow(true)

    // == gallery ==

    private var _galleryFilter = MutableStateFlow<Album?>(null)
    val galleryFilter = _galleryFilter.asStateFlow()

    private var galleryCursor: Cursor? = null
    private var galleryVersion = MutableStateFlow<String?>(null)

    // using ArrayList instead of mutableListOf is justified
    // since we use its api to reserve capacity
    private val galleryCache = ArrayList<GalleryImage?>()

    private val _galleryPermissionGranted = MutableStateFlow(true)
    val galleryPermissionGranted = _galleryPermissionGranted.asStateFlow()

    private fun loadGallery(force: Boolean = false) {
        val context = getApplication<Application>().applicationContext

        if (!ToolsPermission.hasReadPermission()) {
            ToolsPermission.requestReadPermission(
                onGranted = {
                    _galleryPermissionGranted.value = true
                    loadGallery(force = true)
                },
                onPermissionRestriction = {
                    _galleryPermissionGranted.value = false
                }
            )
            return
        }

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
                "${ImageColumns.BUCKET_ID} = ?",
                arrayOf(galleryFilter.id.toString())
            )
        } else {
            Pair(null, null)
        }

        galleryCache.clear()
        galleryCursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(ImageColumns._ID, ImageColumns.DATA, ImageColumns.WIDTH, ImageColumns.HEIGHT),
            selection.first,
            selection.second,
            ImageColumns.DATE_MODIFIED + " DESC"
        ) ?: return
        galleryVersion.value = MediaStore.getVersion(context)
    }

    data class GalleryImage(
        val id: Int,
        val file: File,
        val width: Int,
        val height: Int,
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
            width = cursor.getInt(2),
            height = cursor.getInt(3),
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
                ImageColumns._ID,
                ImageColumns.DATA,
                ImageColumns.BUCKET_ID,
                ImageColumns.BUCKET_DISPLAY_NAME,
                ImageColumns.WIDTH,
                ImageColumns.HEIGHT,
            ),
            null,
            null,
            ImageColumns.DATE_MODIFIED + " DESC"
        ) ?: return

        val albumMap = hashMapOf<Int, Album>()

        while (cursor.moveToNext()) {
            val id = cursor.getInt(0)
            val path = cursor.getString(1)
            val albumId = cursor.getInt(2)
            val albumName = cursor.getString(3)
            val width = cursor.getInt(4)
            val height = cursor.getInt(5)

            albumMap.compute(albumId) { _, album ->
                if (album != null) {
                    album.elements++
                    album
                } else {
                    Album(
                        id = albumId,
                        name = albumName,
                        elements = 1,
                        preview = GalleryImage(id = id, file = File(path), width = width, height = height)
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

    private val _selectedImages = MutableStateFlow<List<GalleryImage>>(emptyList())
    val selectedImages = _selectedImages.asStateFlow()

    fun selectedImageIndex(id: Int): StateFlow<Int> =
        selectedImages.mapState { images ->
            images.indexOfFirst { it.id == id }
        }

    fun setImageSelected(image: GalleryImage, selected: Boolean) {
        _selectedImages.update { images ->
            if (selected) {
                if (images.any { it.id == image.id }) return@update images
                images + image
            } else {
                images.filterNot { it.id == image.id }
            }
        }
    }

    private val _openedImage = MutableStateFlow<GalleryImage?>(null)
    val openedImage = _openedImage.asStateFlow()

    val imageCropper = ImageCropper()
    var croppingJob: Job? = null

    fun openImage(image: GalleryImage) {
        _openedImage.value = image
    }
    fun closeImage() {
        _openedImage.value = null

        croppingJob?.cancel()
        croppingJob = null
    }

    fun startImageCrop() {
        croppingJob?.cancel()
        croppingJob = viewModelScope.launch {
            val openedImage = _openedImage.value ?: return@launch
            imageCropper.crop(openedImage.file)
        }
    }

    private val _alteredImages = MutableStateFlow<Map<Int, Bitmap>>(mapOf())
    val alteredImages = _alteredImages.asStateFlow()

    // == gif ==

    private var _trendingGifQueries = MutableStateFlow<List<String>?>(null)
    private val _gifSearchSuggestions = MutableStateFlow<Pair<String, List<String>>?>(null)

    private val _gifQuery = MutableStateFlow("")
    val gifQuery = _gifQuery.asStateFlow()

    private var gifTabStarted: Boolean = false

    private val _recentGifs = MutableStateFlow<List<AttachGifItem>?>(null)
    val recentGifs = _recentGifs.asStateFlow()
        .mapState { it?.subList(0, it.size.coerceAtMost(9)) }

    val gifFavouritesModel = GifFavouritesModel(application)
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

    private fun loadGifTab() {
        viewModelScope.launch {
            if (_trendingGifQueries.value != null) return@launch
            try {
                val resp = apollo.query(AttachGifTabQuery())
                    .execute()
                    .dataAssertNoErrors

                _recentGifs.emit(resp.recentGifs.map { it.attachGifItem })
                _trendingGifQueries.emit(resp.gifSearchSuggestions)
            } catch (_: Exception) {
            }
        }
    }

    fun shareGif(gif: AttachGifItem) {
        MainScope().launch {
            _recentGifs.update { recentGifs ->
                if (recentGifs?.any { it.id == gif.id } == true) {
                    recentGifs
                } else {
                    listOf(gif) + (recentGifs ?: emptyList())
                }
            }

            try {
                apollo.mutation(ShareGifMutation(_gifQuery.value, gif.id))
                    .execute()
                    .dataAssertNoErrors
            } catch (_: Exception) {
            }
        }

        // STOPSHIP: delegate
    }

    fun isGifInFavourites(id: String): Boolean {
        return gifFavouritesModel.items.value?.any { it.node.id == id } == true
    }

    fun setGifFavourite(gif: AttachGifItem, favourite: Boolean): Job {
        return viewModelScope.launch {
            closeGifPopup()

            fun AttachGifItem.toEdge(): FavouriteGifs.Edge {
                return FavouriteGifs.Edge(
                    cursor = id,
                    node = FavouriteGifs.Node(
                        id = id,
                        attachGifItem = this,
                        __typename = "GifItem"
                    )
                )
            }

            try {
                val userId = AuthController.currentUserState.value!!.id
                val currentFavouriteGifs = gifFavouritesModel.queryResponse.value.takeIf { it?.data != null }

                if (favourite) {
                    apollo.mutation(AddGifToFavouritesMutation(gif.id))
                        .optimisticUpdatesExt(currentFavouriteGifs?.let {
                            AddGifToFavouritesMutation.Data(
                                addGifToFavourites = AddGifToFavouritesMutation.AddGifToFavourites(
                                    id = userId,
                                    favouriteGifs = AddGifToFavouritesMutation.FavouriteGifs(
                                        __typename = "GifItemConnection",
                                        favouriteGifs = it.data!!.me.favouriteGifs.favouriteGifs.copy(
                                            edges = listOf(gif.toEdge())
                                                    + it.data!!.me.favouriteGifs.favouriteGifs.edges
                                        )
                                    ),
                                    __typename = "User"
                                )
                            )
                        })
                        .execute()
                } else {
                    apollo.mutation(RemoveGifFromFavouritesMutation(gif.id))
                        .optimisticUpdatesExt(currentFavouriteGifs?.let { favouriteGifs ->
                            RemoveGifFromFavouritesMutation.Data(
                                removeGifFromFavourites = RemoveGifFromFavouritesMutation.RemoveGifFromFavourites(
                                    id = userId,
                                    favouriteGifs = RemoveGifFromFavouritesMutation.FavouriteGifs(
                                        __typename = "GifItemConnection",
                                        favouriteGifs = favouriteGifs.data!!.me.favouriteGifs.favouriteGifs.copy(
                                            edges = favouriteGifs.data!!.me.favouriteGifs.favouriteGifs.edges
                                                .filterNot { it.node.id == gif.id }
                                        )
                                    ),
                                    __typename = "User"
                                )
                            )
                        })
                        .execute()
                }
            } catch (_: Exception) {
            }
        }
    }

    private val _activeGifPopup = MutableStateFlow<Pair<String, AttachGifItem>?>(null)
    val activeGifPopup = _activeGifPopup.asStateFlow()

    fun openGifPopup(parentKey: String, item: AttachGifItem) {
        _activeGifPopup.value = Pair(parentKey, item)
    }

    fun closeGifPopup() {
        _activeGifPopup.value = null
    }

    enum class ScrollMarker {
        Recent,
        Favourite,
        Trending,
        Search,
    }

    private val _gifScrollMarker = MutableStateFlow(ScrollMarker.Recent)
    val gifScrollMarker = _gifScrollMarker.asStateFlow()

    private val _gifScrollMarkerRequest = MutableStateFlow<ScrollMarker?>(null)
    val gifScrollMarkerRequest = _gifScrollMarkerRequest.asStateFlow()

    fun setGifScrollMarker(scrollMarker: ScrollMarker) {
        _gifScrollMarker.value = scrollMarker
        _gifScrollMarkerRequest.value = null
    }
    fun scrollToGifMarker(scrollMarker: ScrollMarker) {
        _gifScrollMarkerRequest.value = scrollMarker
    }

    // == stickers ==

    private val _recentStickers = MutableStateFlow<List<PublicationSticker>?>(null)
    val recentStickers = _recentStickers.asStateFlow()

    private fun loadRecentStickers() {

    }

    fun shareSticker(sticker: PublicationSticker) {

    }

    // == other public apis ==

    fun switchTab(tab: Tab, userAction: Boolean = false) {
        _activeTab.value = tab

        if (!userAction) return

        when (tab) {
            Tab.Gallery -> {
                loadGallery()
            }

            Tab.Gif -> {
                if (!gifTabStarted) {
                    gifTabStarted = true

                    startGifSuggestions()
                    loadGifTab()
                    gifFavouritesModel.start()
                    gifSearchModel.start()
                }
            }

            Tab.Stickers -> {}
        }

        PostHog.capture("attach flyout tab switched", properties = mapOf("tab" to tab.name))

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                datastore.edit {
                    it[lastActiveTab] = tab.name
                }
            }
        }
    }

    fun onClosed() {
        _selectedImages.value = emptyList()

        closeGifPopup()
        closeImage()
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
