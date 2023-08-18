package com.sayzen.campfiresdk.controllers

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.requests.fandoms.*
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.models.events.fandom.EventFandomCategoryChanged
import com.sayzen.campfiresdk.models.events.fandom.EventFandomChanged
import com.sayzen.campfiresdk.models.events.fandom.EventFandomClose
import com.sayzen.campfiresdk.models.events.fandom.EventFandomRemove
import com.sayzen.campfiresdk.screens.fandoms.view.SplashSubscription
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sayzen.campfiresdk.support.adapters.XFandom
import com.sup.dev.android.libs.image_loader.ImageLoaderId
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.*
import com.sup.dev.android.views.screens.SCrop
import com.sup.dev.android.views.splash.*
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsBytes
import com.sup.dev.java.tools.ToolsCollections
import com.sup.dev.java.tools.ToolsText
import com.sup.dev.java.tools.ToolsThreads

object ControllerFandoms {

    fun showPopupMenu(xFandom: XFandom, view: View, x: Float = 0f, y: Float = 0f, onLanguage: ((Long) -> Unit)? = null) {
        SplashMenu()
                .add(t(API_TRANSLATE.app_copy_link)) { ToolsAndroid.setToClipboard(xFandom.linkToWithLanguage());ToolsToast.show(t(API_TRANSLATE.app_copied)) }
                .add(t(API_TRANSLATE.app_subscription)) { showSubscription(xFandom) }
                .add(t(API_TRANSLATE.settings_black_list)) { ControllerCampfireSDK.switchToBlackListFandom(xFandom.getId()) }
                .add(t(API_TRANSLATE.fandom_language_choose)) { chooseLanguage(onLanguage!!) }.condition(onLanguage != null)
                .spoiler(t(API_TRANSLATE.app_moderator))
                .add(t(API_TRANSLATE.profile_change_background)) { changeTitleImage(xFandom) }.condition(ControllerApi.can(xFandom.getId(), xFandom.getLanguageId(), API.LVL_MODERATOR_FANDOM_IMAGE)).backgroundRes(R.color.blue_700).textColorRes(R.color.white)
                .spoiler(t(API_TRANSLATE.app_admin))
                .add(t(API_TRANSLATE.profile_change_avatar)) { changeImage(xFandom) }.condition(ControllerApi.can(API.LVL_ADMIN_FANDOM_AVATAR)).backgroundRes(R.color.red_700).textColorRes(R.color.white)
                .add(t(API_TRANSLATE.fandoms_menu_change_category)) { changeCategory(xFandom) }.condition(ControllerApi.can(API.LVL_ADMIN_FANDOM_CATEGORY)).backgroundRes(R.color.red_700).textColorRes(R.color.white)
                .add(t(API_TRANSLATE.fandoms_menu_rename)) { rename(xFandom) }.condition(ControllerApi.can(API.LVL_ADMIN_FANDOM_NAME)).backgroundRes(R.color.red_700).textColorRes(R.color.white)
                .add(if (xFandom.getFandom().closed) t(API_TRANSLATE.app_open) else t(API_TRANSLATE.app_close)) { close(xFandom) }.condition(ControllerApi.can(API.LVL_ADMIN_FANDOM_CLOSE)).backgroundRes(R.color.red_700).textColorRes(R.color.white)
                .add(t(API_TRANSLATE.app_remove)) { remove(xFandom) }.condition(ControllerApi.can(API.LVL_ADMIN_FANDOM_REMOVE)).backgroundRes(R.color.red_700).textColorRes(R.color.white)
                .asPopupShow(view, x, y)
    }

    private fun chooseLanguage(onLanguage: (Long) -> Unit) {
        val splash = SplashMenu()
        for (language in API.LANGUAGES) {
            splash.add(language.name) {
                onLanguage(language.id)
            }
        }
        splash.asSheetShow()
    }

    private fun showSubscription(xFandom: XFandom) {

        ControllerStoryQuest.incrQuest(API.QUEST_STORY_FANDOM)

        ApiRequestsSupporter.executeProgressDialog(RFandomsGetSubscribtion(xFandom.getId(), xFandom.getLanguageId())) { r ->
            SplashSubscription(xFandom.getId(), xFandom.getLanguageId(), r.subscriptionType, r.notifyImportant).asSheetShow()
        }


    }


    private fun changeTitleImage(xFandom: XFandom) {
        SplashChooseImage()
                .setOnSelected { _, bytes, _ ->

                    ToolsThreads.thread {

                        val bitmap = ToolsBitmap.decode(bytes)
                        if (bitmap == null) {
                            ToolsToast.show(t(API_TRANSLATE.error_cant_load_image))
                            return@thread
                        }

                        ToolsThreads.main {


                            val isGif = ToolsBytes.isGif(bytes)
                            val cropSizeW = if (isGif) API.FANDOM_TITLE_IMG_GIF_W else API.FANDOM_TITLE_IMG_W
                            val cropSizeH = if (isGif) API.FANDOM_TITLE_IMG_GIF_H else API.FANDOM_TITLE_IMG_H

                            Navigator.to(SCrop(bitmap, cropSizeW, cropSizeH) { _, b2, x, y, w, h ->
                                if (isGif) {

                                    val d = ToolsView.showProgressDialog()
                                    ToolsThreads.thread {
                                        val bytesSized = ToolsGif.resize(bytes, API.FANDOM_TITLE_IMG_GIF_W, API.FANDOM_TITLE_IMG_GIF_H, x, y, w, h, true)

                                        ToolsThreads.main {
                                            if (bytesSized.size > API.FANDOM_TITLE_IMG_GIF_WEIGHT) {
                                                d.hide()
                                                ToolsToast.show(t(API_TRANSLATE.error_too_long_file))
                                            } else {
                                                ControllerApi.toBytes(b2, API.FANDOM_TITLE_IMG_WEIGHT, API.FANDOM_TITLE_IMG_GIF_W, API.FANDOM_TITLE_IMG_GIF_H, true) {
                                                    if (it == null) d.hide()
                                                    else changeTitleImageNow(xFandom, d, it, bytesSized)
                                                }
                                            }
                                        }
                                    }

                                } else {
                                    val d = ToolsView.showProgressDialog()
                                    ControllerApi.toBytes(b2, API.FANDOM_TITLE_IMG_WEIGHT, API.FANDOM_TITLE_IMG_W, API.FANDOM_TITLE_IMG_H, true) {
                                        if (it == null) d.hide()
                                        else changeTitleImageNow(xFandom, d, it, null)
                                    }
                                }
                            })
                        }
                    }
                }
                .asSheetShow()
    }

    private fun changeTitleImageNow(xFandom: XFandom, dialog: Splash, image: ByteArray, imageGif: ByteArray?) {
        dialog.hide()
        ToolsThreads.main {
            SplashField().setHint(t(API_TRANSLATE.moderation_widget_comment))
                    .setOnCancel(t(API_TRANSLATE.app_cancel))
                    .setMin(API.MODERATION_COMMENT_MIN_L)
                    .setMax(API.MODERATION_COMMENT_MAX_L)
                    .setOnEnter(t(API_TRANSLATE.app_change)) { ww, comment ->
                        ApiRequestsSupporter.executeEnabled(ww, RFandomsModerationChangeImageTitle(xFandom.getId(), xFandom.getLanguageId(), image, imageGif, comment)) { r ->
                            ImageLoaderId(xFandom.getImageTitleId()).clear()
                            EventBus.post(EventFandomChanged(xFandom.getId(), "", -1, r.imageId, r.imageGifId))
                            ToolsToast.show(t(API_TRANSLATE.app_done))
                        }
                    }
                    .asSheetShow()
        }
    }

    private fun changeCategory(xFandom: XFandom) {
        val wMenu = SplashMenu()
        for (c in CampfireConstants.CATEGORIES) {
            if (c.index != xFandom.getFandom().creatorId) {
                wMenu.add(c.name).onClick {
                    SplashField().setHint(t(API_TRANSLATE.moderation_widget_comment))
                            .setOnCancel(t(API_TRANSLATE.app_cancel))
                            .setMin(API.MODERATION_COMMENT_MIN_L)
                            .setMax(API.MODERATION_COMMENT_MAX_L)
                            .addChecker(t(API_TRANSLATE.error_use_english)) { ToolsText.isOnly(it, API.ENGLISH) }
                            .setOnEnter(t(API_TRANSLATE.app_change)) { w, comment ->
                                ApiRequestsSupporter.executeEnabled(w, RFandomsAdminChangeCategory(xFandom.getId(), c.index, comment)) {
                                    EventBus.post(EventFandomCategoryChanged(xFandom.getId(), c.index))
                                    ToolsToast.show(t(API_TRANSLATE.app_done))
                                }
                            }
                            .asSheetShow()
                }
            }
        }
        wMenu.asSheetShow()
    }

    private fun close(xFandom: XFandom) {
        val closed = xFandom.isClosed()
        ControllerApi.moderation(
                if (closed) t(API_TRANSLATE.app_open) else t(API_TRANSLATE.app_close),
                if (closed) t(API_TRANSLATE.app_open) else t(API_TRANSLATE.app_close),
                { RFandomsAdminClose(xFandom.getId(), !closed, it) },
                {
                    EventBus.post(EventFandomClose(xFandom.getId(), !closed))
                    ToolsToast.show(t(API_TRANSLATE.app_done))
                })
    }

    private fun remove(xFandom: XFandom) {
        SplashField()
                .setHint(t(API_TRANSLATE.moderation_widget_comment))
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .setMin(API.MODERATION_COMMENT_MIN_L)
                .setMax(API.MODERATION_COMMENT_MAX_L)
                .addChecker(t(API_TRANSLATE.error_use_english)) { ToolsText.isOnly(it, API.ENGLISH) }
                .setOnEnter(t(API_TRANSLATE.app_remove)) { _, comment ->
                    ApiRequestsSupporter.executeEnabledConfirm(t(API_TRANSLATE.fandom_remove_confirm), t(API_TRANSLATE.app_remove), RFandomsAdminRemove(xFandom.getId(), comment)) {
                        EventBus.post(EventFandomRemove(xFandom.getId()))
                        ToolsToast.show(t(API_TRANSLATE.app_done))
                    }
                            .onApiError(RFandomsAdminRemove.ERROR_ALREADY){
                                EventBus.post(EventFandomRemove(xFandom.getId()))
                                ToolsToast.show(t(API_TRANSLATE.fandom_remove_error_already))
                            }
                }
                .asSheetShow()
    }


    private fun rename(xFandom: XFandom) {
        SplashFieldTwo()
                .setTitle(t(API_TRANSLATE.fandoms_menu_rename))
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .setText_1(xFandom.getName())
                .setHint_1(t(API_TRANSLATE.app_name_s))
                .setLinesCount_1(1)
                .addChecker_1(t(API_TRANSLATE.error_use_english)) { ToolsText.isOnly(it, API.ENGLISH) }
                .setMin_1(1)
                .setMax_1(API.FANDOM_NAME_MAX)
                .setMin_2(API.MODERATION_COMMENT_MIN_L)
                .setMax_2(API.MODERATION_COMMENT_MAX_L)
                .setHint_2(t(API_TRANSLATE.comments_hint))
                .addChecker_2(t(API_TRANSLATE.error_use_english)) { ToolsText.isOnly(it, API.ENGLISH) }
                .setOnEnter(t(API_TRANSLATE.app_rename)) { _, name, comment ->
                    ApiRequestsSupporter.executeEnabledConfirm(t(API_TRANSLATE.fandoms_menu_rename_confirm), t(API_TRANSLATE.fandoms_menu_rename), RFandomsAdminChangeName(xFandom.getId(), name, comment)) {
                        EventBus.post(EventFandomChanged(xFandom.getId(), name))
                        ToolsToast.show(t(API_TRANSLATE.app_done))
                    }
                }
                .asSheetShow()
    }

    private fun changeImage(xFandom: XFandom) {
        SplashChooseImage()
                .setOnSelectedBitmap { _, bitmap ->
                    Navigator.to(SCrop(bitmap, API.FANDOM_IMG_SIDE, API.FANDOM_IMG_SIDE) { _, b, _, _, _, _ ->
                        SplashField()
                                .setHint(t(API_TRANSLATE.moderation_widget_comment))
                                .setOnCancel(t(API_TRANSLATE.app_cancel))
                                .setMin(API.MODERATION_COMMENT_MIN_L)
                                .setMax(API.MODERATION_COMMENT_MAX_L)
                                .setOnEnter(t(API_TRANSLATE.app_change)) { _, comment ->
                                    val dialog = ToolsView.showProgressDialog()
                                    ToolsThreads.thread {
                                        val image = ToolsBitmap.toBytes(ToolsBitmap.resize(b, API.FANDOM_IMG_SIDE, API.FANDOM_IMG_SIDE), API.FANDOM_IMG_WEIGHT)
                                        ToolsThreads.main {
                                            ApiRequestsSupporter.executeProgressDialog(dialog, RFandomsAdminChangeImage(xFandom.getId(), image, comment)) { _ ->
                                                ImageLoaderId(xFandom.getImageId()).clear()
                                                EventBus.post(EventFandomChanged(xFandom.getId(), xFandom.getName()))
                                                ToolsToast.show(t(API_TRANSLATE.app_done))
                                            }
                                        }
                                    }
                                }
                                .asSheetShow()
                    })
                }
                .asSheetShow()
    }

    fun showAlertIfNeed(screen: Screen, fandomId: Long, isPost: Boolean) {

        if (!ControllerCampfireSDK.ENABLE_CLOSE_FANDOM_ALERT) return

        if (ControllerSettings.fandomNSFW.contains(fandomId)) return

        val w = SplashAlert()
                .setTopTitleText(t(API_TRANSLATE.app_attention))
                .setCancelable(false)
                .setTitleImageBackgroundRes(R.color.blue_700)
                .setChecker(t(API_TRANSLATE.message_closed_fandom_check))
                .setOnEnter(t(API_TRANSLATE.app_continue))
                .setOnCancel(t(API_TRANSLATE.app_cancel)) { Navigator.remove(screen) }
                .setOnChecker { if (it) ControllerSettings.fandomNSFW = ToolsCollections.add(fandomId, ControllerSettings.fandomNSFW) }

        if (isPost) {
            w.setText(t(API_TRANSLATE.message_closed_fandom_post))
        } else {
            w.setText(t(API_TRANSLATE.message_closed_fandom))
        }

        w.asSheetShow()
    }

}