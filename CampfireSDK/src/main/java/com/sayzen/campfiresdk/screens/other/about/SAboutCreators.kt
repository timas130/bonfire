package com.sayzen.campfiresdk.screens.other.about

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.ApiResources
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.screens.account.profile.SProfile
import com.sayzen.campfiresdk.support.load
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.tools.ToolsIntent
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.splash.SplashMenu

class SAboutCreators : Screen(R.layout.screen_other_abount_creators) {
    private val vCopyLink: View = findViewById(R.id.vCopyLink)

    private val vMoreSit: View = findViewById(R.id.vMoreSit)
    private val vMoreNiki: View = findViewById(R.id.vMoreNiki)
    private val vMoreZeon: View = findViewById(R.id.vMoreZeon)
    private val vMoreSaynok: View = findViewById(R.id.vMoreSaynok)
    private val vMoreEgor: View = findViewById(R.id.vMoreEgor)

    private val vPhotoSit: ViewAvatarTitle = findViewById(R.id.vPhotoSit)
    private val vPhotoNiki: ViewAvatarTitle = findViewById(R.id.vPhotoNiki)
    private val vPhotoZeon: ViewAvatarTitle = findViewById(R.id.vPhotoZeon)
    private val vPhotoSaynok: ViewAvatarTitle = findViewById(R.id.vPhotoSaynok)
    private val vPhotoEgor: ViewAvatarTitle = findViewById(R.id.vPhotoEgor)

    init {
        setTitle(t(API_TRANSLATE.about_creators))
        disableNavigation()

        vPhotoSit.setTitle(t(API_TRANSLATE.about_creators_sit))
        vPhotoSit.setSubtitle(t(API_TRANSLATE.about_creators_sit_subtitle))

        vPhotoNiki.setTitle(t(API_TRANSLATE.about_creators_niki))
        vPhotoNiki.setSubtitle(t(API_TRANSLATE.about_creators_niki_subtitle))

        vPhotoZeon.setTitle(t(API_TRANSLATE.about_creators_zeon))
        vPhotoZeon.setSubtitle(t(API_TRANSLATE.about_creators_zeon_subtitle))

        vPhotoSaynok.setTitle(t(API_TRANSLATE.about_creators_saynok))
        vPhotoSaynok.setSubtitle(t(API_TRANSLATE.about_creators_saynok_subtitle))

        vPhotoEgor.setTitle(t(API_TRANSLATE.about_creators_egor))
        vPhotoEgor.setSubtitle(t(API_TRANSLATE.about_creators_egor_subtitle))

        vMoreSit.setOnClickListener {
            SplashMenu()
                .add(t(API_TRANSLATE.app_campfire)) { SProfile.instance(1, Navigator.TO) }
                .add(t(API_TRANSLATE.app_email)) { ToolsIntent.startMail("me@bonfire.moe") }
                .add(t(API_TRANSLATE.app_site)) { ToolsIntent.openLink("https://sit.sh") }
                .asSheetShow()
        }

        vMoreNiki.setOnClickListener {
            SplashMenu()
                .add(t(API_TRANSLATE.app_campfire)) { SProfile.instance(12, Navigator.TO) }
                .asSheetShow()
        }

        vMoreZeon.setOnClickListener {
            SplashMenu()
                .add(t(API_TRANSLATE.app_email)) { ToolsIntent.startMail("zeooon@ya.ru")  }
                .add(t(API_TRANSLATE.app_vkontakte)) { ControllerLinks.openLink("https://vk.com/zeooon") }
                .asSheetShow()
        }

        vMoreSaynok.setOnClickListener {
            SplashMenu()
                .add(t(API_TRANSLATE.app_email)) { ToolsIntent.startMail("saynokdeveloper@gmail.com") }
                .add(t(API_TRANSLATE.app_vkontakte)) { ControllerLinks.openLink("https://vk.com/saynok") }
                .asSheetShow()
        }

        vMoreEgor.setOnClickListener {
            SplashMenu()
                .add(t(API_TRANSLATE.app_email)) { ToolsIntent.startMail("georgepro036@gmail.com") }
                .add(t(API_TRANSLATE.app_vkontakte)) { ControllerLinks.openLink("https://vk.com/id216069359") }
                .asSheetShow()
        }

        vCopyLink.setOnClickListener {
            ToolsAndroid.setToClipboard(API.LINK_CREATORS.asWeb())
            ToolsToast.show(t(API_TRANSLATE.app_copied))
        }

        ImageLoader.load(ApiResources.DEVELOPER_SIT).into(vPhotoSit)
        ImageLoader.load(ApiResources.DEVELOPER_NIKI).into(vPhotoNiki)
        ImageLoader.load(ApiResources.DEVELOPER_ZEON).into(vPhotoZeon)
        ImageLoader.load(ApiResources.DEVELOPER_SAYNOK).into(vPhotoSaynok)
        ImageLoader.load(ApiResources.DEVELOPER_EGOR).into(vPhotoEgor)

        ControllerLinks.makeLinkable(vPhotoSit.vTitle)
        ControllerLinks.makeLinkable(vPhotoNiki.vTitle)
        ControllerLinks.makeLinkable(vPhotoZeon.vTitle)
        ControllerLinks.makeLinkable(vPhotoSaynok.vTitle)
        ControllerLinks.makeLinkable(vPhotoEgor.vTitle)
    }

}
