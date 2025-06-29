package com.sayzen.campfiresdk.screens.fandoms

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.ApiResources
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerAppUpdate
import com.sayzen.campfiresdk.controllers.ControllerAppUpdate.UpdateStatus
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.project.EventAppUpdateAvailable
import com.sayzen.campfiresdk.support.load
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.views.cards.Card
import com.sup.dev.java.libs.eventBus.EventBus

class CardUpdate : Card(R.layout.screen_fandom_card_update) {
    private val eventBus = EventBus
            .subscribe(EventAppUpdateAvailable::class) { update() }

    override fun bindView(view: View) {
        super.bindView(view)

        val vContainer: View = view.findViewById(R.id.vContainer)
        val vUpdate: Button = view.findViewById(R.id.vUpdate)
        val vImage: ImageView = view.findViewById(R.id.vImage)
        val vIntroUpdate: TextView = view.findViewById(R.id.vIntroUpdate)

        vIntroUpdate.text = t(API_TRANSLATE.intro_update)
        vUpdate.isEnabled = true
        vUpdate.text = when (ControllerAppUpdate.updateStatus) {
            UpdateStatus.DOWNLOADING -> {
                vUpdate.isEnabled = false
                t(API_TRANSLATE.app_update_downloading)
            }
            UpdateStatus.DOWNLOADED -> t(API_TRANSLATE.app_update_complete)
            else -> t(API_TRANSLATE.app_update)
        }

        vContainer.visibility = if (ControllerAppUpdate.isUpdateUnavailable()) View.GONE else View.VISIBLE

        ImageLoader.load(ApiResources.IMAGE_BACKGROUND_LEVEL_9).noHolder().into(vImage)
        vUpdate.setOnClickListener {
            if (ControllerAppUpdate.isUpdateAvailable()) {
                ControllerAppUpdate.startUpdate()
            } else if (ControllerAppUpdate.isUpdateDownloaded()) {
                ControllerAppUpdate.completeUpdate()
            }
        }
    }
}
