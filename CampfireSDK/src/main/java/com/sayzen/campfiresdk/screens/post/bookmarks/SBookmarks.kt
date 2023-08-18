package com.sayzen.campfiresdk.screens.post.bookmarks

import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.BookmarksFolder
import com.dzen.campfire.api.requests.bookmarks.RBookmarksGetAll
import com.sayzen.campfiresdk.models.cards.CardPublication
import com.dzen.campfire.api.models.publications.Publication
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.*
import com.sayzen.campfiresdk.models.events.publications.EventPublicationBookmarkChange
import com.sayzen.campfiresdk.models.events.bookmarks.EventBookmarkFolderCreate
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.screens.SLoadingRecycler
import com.sup.dev.android.views.splash.SplashField
import com.sup.dev.android.views.splash.SplashMenu
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsCollections

class SBookmarks constructor() : SLoadingRecycler<CardPublication, Publication>() {

    private val eventBus = EventBus
            .subscribe(EventPublicationBookmarkChange::class) { this.onEventUnitBookmarkChange(it) }

    init {
        vScreenRoot!!.setBackgroundColor(ToolsResources.getBackgroundColor(context))
        setTitle(t(API_TRANSLATE.app_bookmarks))
        setTextEmpty(t(API_TRANSLATE.bookmarks_empty))
        setTextProgress(t(API_TRANSLATE.bookmarks_loading))
        setBackgroundImage(API_RESOURCES.IMAGE_BACKGROUND_1)

        ControllerStoryQuest.incrQuest(API.QUEST_STORY_BOOKMARKS_SCREEN)

        adapter.setBottomLoader { onLoad, cards ->

            val foldersIds = Array(ControllerSettings.bookmarksFolders.size) { ControllerSettings.bookmarksFolders[it].id }

            RBookmarksGetAll(cards.size.toLong(), "", ControllerCampfireSDK.ROOT_FANDOM_ID, 0, 0, foldersIds)
                    .onComplete { r -> onLoad.invoke(r.publications) }
                    .onNetworkError { onLoad.invoke(null) }
                    .send(api)
        }

        addToolbarIcon(R.drawable.ic_more_vert_white_24dp){
            val w = SplashMenu()
            w.add(t(API_TRANSLATE.bookmarks_create_folder)){create()}
            for (f in ControllerSettings.bookmarksFolders) w.add(f.name){ Navigator.to(SBookmarksFolder(f)) }
            w.asPopupShow(it)
        }

    }

    override fun classOfCard() = CardPublication::class

    override fun map(item: Publication) = CardPublication.instance(item, vRecycler)

    private fun create(){
        if (ControllerSettings.bookmarksFolders.size > API.BOOKMARKS_FOLDERS_MAX) {
            ToolsToast.show(t(API_TRANSLATE.bookmarks_folder_error_max))
            return
        }
        SplashField()
                .setHint(t(API_TRANSLATE.app_name_s))
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .setMax(API.BOOKMARKS_FOLDERS_NAME_MAX.toInt())
                .setOnEnter(t(API_TRANSLATE.app_create)) { w, text ->
                    val folder = BookmarksFolder()
                    folder.name =text
                    folder.id = System.currentTimeMillis()
                    ControllerSettings.bookmarksFolders = ToolsCollections.add(folder, ControllerSettings.bookmarksFolders)
                    EventBus.post(EventBookmarkFolderCreate(folder))
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
