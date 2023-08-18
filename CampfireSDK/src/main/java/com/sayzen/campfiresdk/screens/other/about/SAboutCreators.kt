package com.sayzen.campfiresdk.screens.other.about

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.screens.account.profile.SProfile
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.tools.ToolsIntent
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.splash.SplashMenu

class SAboutCreators : Screen(R.layout.screen_other_abount_creators){

    private val vCopyLink:View = findViewById(R.id.vCopyLink)

    private val vMoreZeon: View = findViewById(R.id.vMoreZeon)
    private val vMoreSaynok: View = findViewById(R.id.vMoreSaynok)
    private val vMoreEgor: View = findViewById(R.id.vMoreEgor)
    private val vMoreTurbo: View = findViewById(R.id.vMoreTurbo)
    private val vMoreZYMixx: View = findViewById(R.id.vMoreZYMixx)

    private val vPhotoZeon: ViewAvatarTitle = findViewById(R.id.vPhotoZeon)
    private val vPhotoSaynok: ViewAvatarTitle = findViewById(R.id.vPhotoSaynok)
    private val vPhotoEgor: ViewAvatarTitle = findViewById(R.id.vPhotoEgor)
    private val vPhotoTurbo: ViewAvatarTitle = findViewById(R.id.vPhotoTurbo)
    private val vPhotoZYMixx: ViewAvatarTitle = findViewById(R.id.vPhotoZYMixx)


    init {
        setTitle(t(API_TRANSLATE.about_creators))
        disableNavigation()

        vPhotoZeon.setTitle(t(API_TRANSLATE.about_creators_zeon))
        vPhotoZeon.setSubtitle(t(API_TRANSLATE.about_creators_zeon_subtitle))

        vPhotoSaynok.setTitle(t(API_TRANSLATE.about_creators_saynok))
        vPhotoSaynok.setSubtitle(t(API_TRANSLATE.about_creators_saynok_subtitle))

        vPhotoEgor.setTitle(t(API_TRANSLATE.about_creators_egor))
        vPhotoEgor.setSubtitle(t(API_TRANSLATE.about_creators_egor_subtitle))

        vPhotoTurbo.setTitle(t(API_TRANSLATE.about_creators_turbo))
        vPhotoTurbo.setSubtitle(t(API_TRANSLATE.about_creators_turbo_subtitle))

        vPhotoZYMixx.setTitle(t(API_TRANSLATE.about_creators_zymixx))
        vPhotoZYMixx.setSubtitle(t(API_TRANSLATE.about_creators_zymixx_subtitle))

        vMoreZeon.setOnClickListener {
            SplashMenu()
                    .add(t(API_TRANSLATE.app_campfire)){SProfile.instance(1, Navigator.TO)  }
                    .add(t(API_TRANSLATE.app_email)){ToolsIntent.startMail("zeooon@ya.ru")  }
                    .add(t(API_TRANSLATE.app_vkontakte)){ControllerLinks.openLink("https://vk.com/zeooon")   }
                    .asSheetShow()
        }

        vMoreSaynok.setOnClickListener {
            SplashMenu()
                    .add(t(API_TRANSLATE.app_campfire)){SProfile.instance(2720, Navigator.TO)  }
                    .add(t(API_TRANSLATE.app_email)){ToolsIntent.startMail("saynokdeveloper@gmail.com")  }
                    .add(t(API_TRANSLATE.app_vkontakte)){ControllerLinks.openLink("https://vk.com/saynok")   }
                    .asSheetShow()
        }

        vMoreEgor.setOnClickListener {
            SplashMenu()
                    .add(t(API_TRANSLATE.app_campfire)){SProfile.instance(9447, Navigator.TO)  }
                    .add(t(API_TRANSLATE.app_email)){ToolsIntent.startMail("georgepro036@gmail.com")  }
                    .add(t(API_TRANSLATE.app_vkontakte)){ControllerLinks.openLink("https://vk.com/id216069359")   }
                    .asSheetShow()
        }

        vMoreTurbo.setOnClickListener {
            SplashMenu()
                    .add(t(API_TRANSLATE.app_campfire)){SProfile.instance(8083, Navigator.TO)  }
                    .add(t(API_TRANSLATE.app_email)){ToolsIntent.startMail("turboRO99@gmail.com")  }
                    .add(t(API_TRANSLATE.app_vkontakte)){ControllerLinks.openLink("https://vk.com/turboa99")   }
                    .asSheetShow()
        }

        vMoreZYMixx.setOnClickListener {
            SplashMenu()
                    .add(t(API_TRANSLATE.app_campfire)){SProfile.instance(6236, Navigator.TO)  }
                    .add(t(API_TRANSLATE.app_email)){ToolsIntent.startMail("zymiix@gmail.com")  }
                    .add(t(API_TRANSLATE.app_vkontakte)){ControllerLinks.openLink("https://vk.com/zymixx")   }
                    .asSheetShow()
        }

        vCopyLink.setOnClickListener {
            ToolsAndroid.setToClipboard(API.LINK_CREATORS.asWeb())
            ToolsToast.show(t(API_TRANSLATE.app_copied))
        }

        ImageLoader.load(API_RESOURCES.DEVELOPER_ZEON).into(vPhotoZeon)
        ImageLoader.load(API_RESOURCES.DEVELOPER_SAYNOK).into(vPhotoSaynok)
        ImageLoader.load(API_RESOURCES.DEVELOPER_EGOR).into(vPhotoEgor)
        ImageLoader.load(API_RESOURCES.DEVELOPER_TURBO).into(vPhotoTurbo)
        ImageLoader.load(API_RESOURCES.DEVELOPER_ZYMIXX).into(vPhotoZYMixx)

        ControllerLinks.makeLinkable(vPhotoZeon.vTitle)
        ControllerLinks.makeLinkable(vPhotoSaynok.vTitle)
        ControllerLinks.makeLinkable(vPhotoEgor.vTitle)
        ControllerLinks.makeLinkable(vPhotoTurbo.vTitle)
        ControllerLinks.makeLinkable(vPhotoZYMixx.vTitle)
    }

}