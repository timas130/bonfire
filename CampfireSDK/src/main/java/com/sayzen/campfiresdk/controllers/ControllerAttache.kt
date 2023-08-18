package com.sayzen.campfiresdk.controllers

import android.net.Uri
import com.dzen.campfire.api.API_TRANSLATE
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.AttacheAgent
import com.sayzen.campfiresdk.screens.chat.SChat
import com.sayzen.campfiresdk.screens.chat.SChats
import com.sayzen.campfiresdk.screens.fandoms.search.SFandomsSearch
import com.sayzen.campfiresdk.screens.post.create.SPostCreate
import com.sayzen.campfiresdk.screens.post.drafts.SDrafts
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsBitmap
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.splash.SplashMenu
import com.sup.dev.java.tools.ToolsText
import com.sup.dev.java.tools.ToolsThreads

object ControllerAttache {

    fun onAttache(text: String?, image: Uri?) {
        ToolsThreads.main(1000) { //  Для того чтоб не попасть на смену экранов при запуске приложения

            val activity = SupAndroid.activity
            if (activity != null) {
                val splash = activity.getToSplash()?.splash
                if (splash is AttacheAgent && splash.attacheAgentIsActive()) {
                    parseAttache(text, image, splash, false)
                    return@main
                }
            }
            val screen = Navigator.getCurrent()
            if(screen is AttacheAgent && screen.attacheAgentIsActive()){
                parseAttache(text, image, screen, false)
                return@main
            }
            SplashMenu()
                    .add(t(API_TRANSLATE.app_create_post)) {
                        SFandomsSearch.instance(Navigator.TO) { fandom ->
                            val screen = SPostCreate(fandom.id, fandom.languageId, fandom.name, fandom.imageId, null, SPostCreate.PostParams(), false)
                            Navigator.to(screen)
                            parseAttache(text, image, screen, false)
                        }
                    }
                    .add(t(API_TRANSLATE.app_add_into_draft)) {
                        Navigator.to(SDrafts { SPostCreate.instance(it.id, Navigator.TO) { screen -> parseAttache(text, image, screen, false) } })
                    }
                    .add(t(API_TRANSLATE.app_add_to_message)) {
                        Navigator.to(SChats { SChat.instance(it.tag, 0, true, Navigator.TO) { screen -> parseAttache(text, image, screen, false) } })
                    }
                    .add(t(API_TRANSLATE.app_fast_post_to, ControllerSettings.fastPublicationFandomName)) {
                        SPostCreate.instance(ControllerSettings.fastPublicationFandomId, ControllerSettings.fastPublicationFandomLanguageId, SPostCreate.PostParams(), { screen -> parseAttache(text, image, screen, true) }, Navigator.TO)
                    }
                    .asSheetShow()
        }
    }

    fun parseAttache(text: String?, image: Uri?, screen: AttacheAgent, postAfterAdd: Boolean) {
        if (text != null) {
            if (ToolsText.isWebLink(text)) {
                val dialog = ToolsView.showProgressDialog()
                ToolsThreads.thread {
                    val bitmap = ToolsBitmap.getFromURL(text)
                    ToolsThreads.main {
                        dialog.hide()
                        if (bitmap != null) screen.attacheImage(bitmap, postAfterAdd)
                        else screen.attacheText(text, postAfterAdd)
                    }
                }

            } else {
                screen.attacheText(text, postAfterAdd)
            }
        } else if (image != null) screen.attacheImage(image, postAfterAdd)
    }


}