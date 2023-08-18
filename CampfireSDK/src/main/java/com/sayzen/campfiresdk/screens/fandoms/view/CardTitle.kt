package com.sayzen.campfiresdk.screens.fandoms.view

import android.view.View
import android.widget.Button
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.requests.fandoms.RFandomsGetSubscribtion
import com.dzen.campfire.api.requests.fandoms.RFandomsSubscribeChange
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.support.adapters.XFandom
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerCampfireSDK
import com.sayzen.campfiresdk.controllers.ControllerStoryQuest
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.fandom.EventFandomCategoryChanged
import com.sayzen.campfiresdk.models.events.fandom.EventFandomSubscribe
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.libs.image_loader.ImageLoaderId
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.screens.SImageView
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.java.libs.eventBus.EventBus

class CardTitle(
        val xFandom: XFandom,
        var category: Long
) : Card(R.layout.screen_fandom_card_title) {

    private var subscriptionType: Long = 0
    private var loaded = false

    private val eventBus = EventBus
            .subscribe(EventFandomCategoryChanged::class) { onEventFandomCategoryChanged(it) }
            .subscribe(EventFandomSubscribe::class) {
                if (xFandom.getId() == it.fandomId && xFandom.getLanguageId() == it.languageId) {
                    subscriptionType = it.subscriptionType
                    updateSubscription()
                }
            }

    override fun bindView(view: View) {
        super.bindView(view)

        updateAvatar()
        updateSubscription()
    }

    fun updateSubscription() {
        val view = getView() ?: return

        val vSubscription: ViewIcon = view.findViewById(R.id.vSubscription)

        vSubscription.visibility = if (loaded) View.VISIBLE else View.INVISIBLE
        if (subscriptionType == API.PUBLICATION_IMPORTANT_NONE)
            vSubscription.setFilter(ToolsResources.getColor(R.color.white))
        else
            vSubscription.setFilter(ToolsResources.getColor(R.color.orange_700))


        vSubscription.setOnClickListener {
            if (!loaded) {
                ToolsToast.show(t(API_TRANSLATE.fandom_loading_in_profess))
                return@setOnClickListener
            }
            ControllerStoryQuest.incrQuest(API.QUEST_STORY_FANDOM)
            val type = if (subscriptionType == API.PUBLICATION_IMPORTANT_NONE) API.PUBLICATION_IMPORTANT_DEFAULT else API.PUBLICATION_IMPORTANT_NONE
            ApiRequestsSupporter.executeProgressDialog(RFandomsSubscribeChange(xFandom.getId(), xFandom.getLanguageId(), type, true)) { _ ->
                EventBus.post(EventFandomSubscribe(xFandom.getId(), xFandom.getLanguageId(), type, true))
                if (type != API.PUBLICATION_IMPORTANT_NONE) ControllerApi.setHasFandomSubscribes(true)
                ToolsToast.show(t(API_TRANSLATE.app_done))
            }
        }
    }

    fun updateAvatar() {
        val view = getView() ?: return
        val vAvatar: ViewAvatarTitle = view.findViewById(R.id.vAvatar)
        xFandom.setView(vAvatar)
        vAvatar.vAvatar.setOnClickListener { Navigator.to(SImageView(ImageLoaderId(xFandom.getImageId()))) }
    }

    fun setParams(subscriptionType: Long) {
        this.subscriptionType = subscriptionType
        loaded = true
        updateSubscription()
    }

    //
    //  EventBus
    //

    private fun onEventFandomCategoryChanged(e: EventFandomCategoryChanged) {
        if (xFandom.getId() == e.fandomId) {
            category = e.newCategory
            update()
        }
    }

}