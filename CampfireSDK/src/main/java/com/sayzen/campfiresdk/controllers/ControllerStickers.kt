package com.sayzen.campfiresdk.controllers

import android.util.LongSparseArray
import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.stickers.PublicationSticker
import com.dzen.campfire.api.models.publications.stickers.PublicationStickersPack
import com.dzen.campfire.api.requests.stickers.*
import com.dzen.campfire.api.requests.publications.RPublicationsRemove
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.events.account.EventAccountChanged
import com.sayzen.campfiresdk.models.events.publications.EventPublicationRemove
import com.sayzen.campfiresdk.models.events.stickers.*
import com.sayzen.campfiresdk.screens.account.stickers.SStickersPackCreate
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.splash.SplashMenu
import com.sup.dev.java.libs.eventBus.EventBus

object ControllerStickers {

    //
    //  Cash
    //

    private val eventBus = EventBus
            .subscribe(EventAccountChanged::class) { clearCash() }
            .subscribe(EventStickerCollectionChanged::class) { clearCash() }
            .subscribe(EventStickersPackCollectionChanged::class) { clearCash() }
            .subscribe(EventStickerCreate::class) { clearCash() }
            .subscribe(EventStickersPackChanged::class) { clearCash() }
            .subscribe(EventStickersPackCreate::class) { clearCash() }

    private var packs = emptyArray<PublicationStickersPack>()
    private val stickers = LongSparseArray<Array<PublicationSticker>>()

    fun clearCash() {
        packs = emptyArray()
        stickers.clear()
    }

    fun getPacks(onComplete: (Array<PublicationStickersPack>) -> Unit, onError: () -> Unit) {
        if (packs.isNotEmpty()) onComplete.invoke(packs)
        else getPacksFromServer(onComplete, onError)
    }

    fun getPacksFromServer(onComplete: (Array<PublicationStickersPack>) -> Unit, onError: () -> Unit) {
        RStickersPacksGetAllByAccount(ControllerApi.account.getId(), 0, Integer.MAX_VALUE)
                .onComplete { r ->
                    packs = r.stickersPacks
                    onComplete.invoke(r.stickersPacks)
                }
                .onError { onError.invoke() }
                .send(api)
    }

    fun getStickers(onComplete: (Array<PublicationSticker>) -> Unit, onError: () -> Unit) {
        getStickersFromServer(onComplete, onError)
    }

    fun getStickersFromServer(onComplete: (Array<PublicationSticker>) -> Unit, onError: () -> Unit) {
        RStickersGetAllByAccount(ControllerApi.account.getId())
                .onComplete { r ->
                    onComplete.invoke(r.stickers)
                }
                .onError { onError.invoke() }
                .send(api)
    }

    fun getStickersFavorite(onComplete: (Array<PublicationSticker>) -> Unit, onError: () -> Unit) {
        if (stickers[0] != null) onComplete.invoke(stickers[0])
        else getStickersFavoriteFromServer(onComplete, onError)
    }

    fun getStickersFavoriteFromServer(onComplete: (Array<PublicationSticker>) -> Unit, onError: () -> Unit) {
        RStickersGetAllFavorite(ControllerApi.account.getId())
                .onComplete { r ->
                    onComplete.invoke(r.stickers)
                }
                .onError { onError.invoke() }
                .send(api)
    }

    fun getStickers(packsId: Long, onComplete: (Array<PublicationSticker>) -> Unit, onError: () -> Unit) {
        if (stickers[packsId] != null) onComplete.invoke(stickers[packsId])
        else getStickersFromServer(packsId, onComplete, onError)
    }

    fun getStickersFromServer(packsId: Long, onComplete: (Array<PublicationSticker>) -> Unit, onError: () -> Unit) {
        RStickersGetAllByPackId(packsId)
                .onComplete { r ->
                    stickers.put(packsId, r.stickers)
                    onComplete.invoke(r.stickers)
                }
                .onError { onError.invoke() }
                .send(api)
    }

    //
    //  Stickers
    //

    fun showStickerPackPopup(view: View, x: Float, y: Float, publication: PublicationStickersPack) {
        ApiRequestsSupporter.executeProgressDialog(RStickersPackCollectionCheck(publication.id)) { r ->
            showStickerPackPopup(view, x, y, publication, r.inCollection)
        }
    }

    fun showStickerPackPopup(view: View, x: Float, y: Float, publication: PublicationStickersPack, inCollection: Boolean) {
        SplashMenu()
                .add(t(API_TRANSLATE.app_copy_link)) { ToolsAndroid.setToClipboard(ControllerLinks.linkToStickersPack(publication.id)); ToolsToast.show(t(API_TRANSLATE.app_copied)) }
                .add(t(API_TRANSLATE.publication_menu_comments_watch)) { ControllerPublications.changeWatchComments(publication.id) }.condition(publication.isPublic)
                .add(t(API_TRANSLATE.app_change)) { Navigator.to(SStickersPackCreate(publication)) }.condition(publication.creator.id == ControllerApi.account.getId())
                .add(t(API_TRANSLATE.app_remove)) { removeStickersPack(publication.id) }.condition(publication.creator.id == ControllerApi.account.getId())
                .add(t(API_TRANSLATE.app_report)) { ControllerApi.reportPublication(publication.id, t(API_TRANSLATE.stickers_packs_report_confirm), t(API_TRANSLATE.stickers_packs_error_gone)) }.condition(publication.creator.id != ControllerApi.account.getId())
                .add(if (inCollection) t(API_TRANSLATE.app_collection_remove) else t(API_TRANSLATE.app_collection_add)) { switchStickerPackCollection(publication, inCollection) }.condition(publication.status == API.STATUS_PUBLIC)
                .add(t(API_TRANSLATE.app_clear_reports)) { ControllerPublications.clearReports(publication) }.backgroundRes(R.color.red_700).textColorRes(R.color.white).condition(ControllerPost.ENABLED_CLEAR_REPORTS && ControllerApi.can(API.LVL_ADMIN_MODER) && publication.reportsCount > 0 && publication.creator.id != ControllerApi.account.getId())
                .add(t(API_TRANSLATE.app_block)) { ControllerPublications.block(publication) }.backgroundRes(R.color.red_700).textColorRes(R.color.white).condition(ControllerPost.ENABLED_BLOCK && ControllerApi.can(API.LVL_ADMIN_MODER) && publication.creator.id != ControllerApi.account.getId())
                .asPopupShow(view, x, y)
    }

    fun switchStickerPackCollection(publication: PublicationStickersPack, inCollection: Boolean) {
        if (inCollection) {
            ApiRequestsSupporter.executeEnabledConfirm(t(API_TRANSLATE.sticker_remove), t(API_TRANSLATE.app_remove), RStickersPackCollectionRemove(publication.id)) {
                EventBus.post(EventStickersPackCollectionChanged(publication, false))
                ToolsToast.show(t(API_TRANSLATE.stickers_message_remove_from_collection_pack))
            }
        } else {
            ApiRequestsSupporter.executeEnabledConfirm(t(API_TRANSLATE.sticker_add), t(API_TRANSLATE.app_add), RStickersPackCollectionAdd(publication.id)) {
                EventBus.post(EventStickersPackCollectionChanged(publication, true))
                ToolsToast.show(t(API_TRANSLATE.stickers_message_add_to_collection_pack))
                ControllerStoryQuest.incrQuest(API.QUEST_STORY_STICKERS)
            }.onApiError(RStickersPackCollectionAdd.E_TOO_MANY) {
                ToolsToast.show(t(API_TRANSLATE.stickers_message_too_many_paskc))
            }
        }
    }

    fun removeStickersPack(publicationId: Long) {
        ApiRequestsSupporter.executeEnabledConfirm(t(API_TRANSLATE.stickers_packs_remove_confirm), t(API_TRANSLATE.app_remove), RPublicationsRemove(publicationId)) {
            EventBus.post(EventPublicationRemove(publicationId))
            ToolsToast.show(t(API_TRANSLATE.app_done))
        }
    }

    fun showStickerPopup(view: View, x: Float, y: Float, publication: PublicationSticker) {
        SplashMenu()
                .add(t(API_TRANSLATE.app_copy_link)) { ToolsAndroid.setToClipboard(ControllerLinks.linkToSticker(publication.id)); ToolsToast.show(t(API_TRANSLATE.app_copied)) }
                .add(t(API_TRANSLATE.app_remove)) { removeSticker(publication.id) }.condition(publication.creator.id == ControllerApi.account.getId())
                .add(t(API_TRANSLATE.app_report)) { ControllerApi.reportPublication(publication.id, t(API_TRANSLATE.stickers_report_confirm), t(API_TRANSLATE.sticker_error_gone)) }.condition(publication.creator.id != ControllerApi.account.getId())
                .add(t(API_TRANSLATE.app_favorite)) { switchStickerCollection(publication) }.condition(publication.status == API.STATUS_PUBLIC)
                .add(t(API_TRANSLATE.app_clear_reports)) { ControllerPublications.clearReports(publication) }.backgroundRes(R.color.red_700).textColorRes(R.color.white).condition(ControllerPost.ENABLED_CLEAR_REPORTS && ControllerApi.can(API.LVL_ADMIN_MODER) && publication.reportsCount > 0 && publication.creator.id != ControllerApi.account.getId())
                .add(t(API_TRANSLATE.app_block)) { ControllerPublications.block(publication) }.backgroundRes(R.color.red_700).textColorRes(R.color.white).condition(ControllerPost.ENABLED_BLOCK && ControllerApi.can(API.LVL_ADMIN_MODER) && publication.creator.id != ControllerApi.account.getId())
                .asPopupShow(view, x, y)
    }

    fun switchStickerCollection(publication: PublicationSticker) {
        ApiRequestsSupporter.executeProgressDialog(RStickerCollectionCheck(publication.id)) { r ->
            if (r.inCollection) {
                ApiRequestsSupporter.executeEnabledConfirm(t(API_TRANSLATE.sticker_remove_favorites), t(API_TRANSLATE.app_remove), RStickerCollectionRemove(publication.id)) {
                    EventBus.post(EventStickerCollectionChanged(publication, false))
                    ToolsToast.show(t(API_TRANSLATE.stickers_message_remove_from_collection))
                }
            } else {
                ApiRequestsSupporter.executeEnabledConfirm(t(API_TRANSLATE.sticker_add_favorites), t(API_TRANSLATE.app_add), RStickerCollectionAdd(publication.id)) {
                    EventBus.post(EventStickerCollectionChanged(publication, true))
                    ToolsToast.show(t(API_TRANSLATE.stickers_message_add_to_collection))
                }
            }
        }
    }

    fun removeSticker(publicationId: Long) {
        ApiRequestsSupporter.executeEnabledConfirm(t(API_TRANSLATE.stickers_remove_confirm), t(API_TRANSLATE.app_remove), RPublicationsRemove(publicationId)) {
            EventBus.post(EventPublicationRemove(publicationId))
            ToolsToast.show(t(API_TRANSLATE.app_done))
        }
    }


}