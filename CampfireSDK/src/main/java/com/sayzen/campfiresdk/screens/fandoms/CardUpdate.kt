package com.sayzen.campfiresdk.screens.fandoms

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.sayzen.campfiresdk.BuildConfig
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.project.EventApiVersionChanged
import com.sayzen.devsupandroidgoogle.ToolsInAppUpdates
import com.sayzen.devsupandroidgoogle.events.EventInAppUpdatesChanged
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.tools.ToolsIntent
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.cards.Card
import com.sup.dev.java.libs.eventBus.EventBus

class CardUpdate : Card(R.layout.screen_fandom_card_update) {

    private val eventBus = EventBus
            .subscribe(EventApiVersionChanged::class) { update() }
            .subscribe(EventInAppUpdatesChanged::class) { update() }

    override fun bindView(view: View) {
        super.bindView(view)

        val vContainer:View = view.findViewById(R.id.vContainer)
        val vUpdate: TextView = view.findViewById(R.id.vUpdate)
        val vImage: ImageView = view.findViewById(R.id.vImage)
        val vIntroUpdate: TextView = view.findViewById(R.id.vIntroUpdate)
        val vIntroUpdateSub: TextView = view.findViewById(R.id.vIntroUpdateSub)

        vIntroUpdate.text = t(API_TRANSLATE.intro_update)
        vIntroUpdateSub.text = t(API_TRANSLATE.intro_update_sub)
        vUpdate.text = t(API_TRANSLATE.app_update)

        vContainer.visibility = if (ControllerApi.isOldVersion()) View.VISIBLE else View.GONE

        ImageLoader.load(API_RESOURCES.IMAGE_BACKGROUND_LEVEL_9).noHolder().into(vImage)
        vUpdate.setOnClickListener { ToolsIntent.startPlayMarket(SupAndroid.appId) }

        when {
            ToolsInAppUpdates.isNone() -> vUpdate.text = t(API_TRANSLATE.app_checking) + "..."
            ToolsInAppUpdates.isNotAvailable() -> vUpdate.text = t(API_TRANSLATE.app_not_available_yet) + "..."
            ToolsInAppUpdates.isAvailable() -> vUpdate.text = t(API_TRANSLATE.app_update)
            ToolsInAppUpdates.isApiNotAvailable() -> vUpdate.text = t(API_TRANSLATE.app_update)
        }

        if (ControllerApi.isOldVersion()){
            ToolsInAppUpdates.start()
        }

    }

}
