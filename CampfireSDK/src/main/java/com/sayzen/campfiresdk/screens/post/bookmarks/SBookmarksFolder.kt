package com.sayzen.campfiresdk.screens.post.bookmarks

import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.BookmarksFolder
import com.dzen.campfire.api.requests.bookmarks.RBookmarksGetAll
import com.sayzen.campfiresdk.models.cards.CardPublication
import com.dzen.campfire.api.models.publications.Publication
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.models.events.publications.EventPublicationBookmarkChange
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.bookmarks.EventBookmarkFolderChanged
import com.sayzen.campfiresdk.models.events.bookmarks.EventBookmarkFolderRemove
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.splash.SplashAlert
import com.sup.dev.android.views.splash.SplashField
import com.sup.dev.android.views.splash.SplashMenu
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsCollections

class SBookmarksFolder constructor(
        val folder: BookmarksFolder
) : SLoadingRecycler<CardPublication, Publication>() {

    private val eventBus = EventBus
            .subscribe(EventPublicationBookmarkChange::class) { this.onEventUnitBookmarkChange(it) }
            .subscribe(EventBookmarkFolderRemove::class) { if (folder.id == it.folderId) Navigator.remove(this) }
            .subscribe(EventBookmarkFolderChanged::class) { if (folder.id == it.folder.id) this.folder.name = it.folder.name; setTitle(folder.name) }

    init {
        vScreenRoot!!.setBackgroundColor(ToolsResources.getBackgroundColor(context))
        setTitle(folder.name)
        setTextEmpty(t(API_TRANSLATE.bookmarks_empty_folder))
        setTextProgress(t(API_TRANSLATE.bookmarks_loading))
        setBackgroundImage(API_RESOURCES.IMAGE_BACKGROUND_1)

        adapter.setBottomLoader { onLoad, cards ->
            RBookmarksGetAll(cards.size.toLong(), "", ControllerCampfireSDK.ROOT_FANDOM_ID, 0, folder.id, emptyArray())
                    .onComplete { r -> onLoad.invoke(r.publications) }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }

        addToolbarIcon(R.drawable.ic_more_vert_white_24dp) {
            SplashMenu()
                    .add(t(API_TRANSLATE.app_change)) { change(folder) }
                    .add(t(API_TRANSLATE.app_remove)) { remove(folder) }
                    .asPopupShow(it)
        }
    }

    override fun classOfCard() = CardPublication::class

    override fun map(item: Publication) = CardPublication.instance(item, vRecycler)

    private fun change(folder: BookmarksFolder) {
        SplashField()
                .setHint(t(API_TRANSLATE.app_name_s))
                .setText(folder.name)
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .setMax(API.BOOKMARKS_FOLDERS_NAME_MAX.toInt())
                .setOnEnter(t(API_TRANSLATE.app_change)) { w, text ->

                    folder.name = text
                    for (f in ControllerSettings.bookmarksFolders) if (f.id == folder.id) f.name = text
                    ControllerSettings.onSettingsUpdated()
                    EventBus.post(EventBookmarkFolderChanged(folder))
                    ToolsToast.show(t(API_TRANSLATE.app_done))
                }
                .asSheetShow()
    }

    private fun remove(folder: BookmarksFolder) {
        SplashAlert()
                .setText(t(API_TRANSLATE.bookmarks_folder_remove_alert))
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .setOnEnter(t(API_TRANSLATE.app_remove)) {
                    ControllerSettings.bookmarksFolders = ToolsCollections.removeIf(ControllerSettings.bookmarksFolders) { it.id == folder.id }
                    EventBus.post(EventBookmarkFolderRemove(folder.id))
                    ToolsToast.show(t(API_TRANSLATE.app_done))
                }
                .asSheetShow()
    }

    //
    //  EventBus
    //

    private fun onEventUnitBookmarkChange(e: EventPublicationBookmarkChange) {
        for (c in adapter.get(CardPublication::class)) if (c.xPublication.publication.id == e.publicationId) adapter.remove(c)
    }

}
