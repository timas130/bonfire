package com.sayzen.campfiresdk.screens.fandoms.view

import android.view.View
import com.dzen.campfire.api.API_TRANSLATE
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.fandom.EventFandomRemoveModerator
import com.sayzen.campfiresdk.screens.activities.user_activities.SRelayRacesList
import com.sayzen.campfiresdk.screens.fandoms.STags
import com.sayzen.campfiresdk.screens.fandoms.chats.SFandomChatsList
import com.sayzen.campfiresdk.screens.fandoms.moderation.moderators.SModeration
import com.sayzen.campfiresdk.screens.fandoms.rating.SRating
import com.sayzen.campfiresdk.screens.fandoms.rubrics.SRubricsList
import com.sayzen.campfiresdk.screens.wiki.SWikiList
import com.sayzen.campfiresdk.support.adapters.XFandom
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.settings.SettingsMini
import com.sup.dev.android.views.views.layouts.LayoutCorned
import com.sup.dev.java.libs.eventBus.EventBus

class CardButtons(
        private val xFandom: XFandom
) : Card(R.layout.screen_fandom_card_buttons) {

    private val eventBus = EventBus
            .subscribe(EventFandomRemoveModerator::class) { onEventFandomRemoveModerator(it) }

    override fun bindView(view: View) {
        super.bindView(view)
        val vLayoutCorned: LayoutCorned = view.findViewById(R.id.vLayoutCorned)
        val vChatsButton: SettingsMini = view.findViewById(R.id.vChatsButton)
        val vTagsButton: SettingsMini = view.findViewById(R.id.vTagsButton)
        val vModerationButton: SettingsMini = view.findViewById(R.id.vModerationButton)
        val vSubscribersButton: SettingsMini = view.findViewById(R.id.vSubscribersButton)
        val vWikiButton: SettingsMini = view.findViewById(R.id.vWikiButton)
        val vRubricButton: SettingsMini = view.findViewById(R.id.vRubricButton)
        val vRelayRaces: SettingsMini = view.findViewById(R.id.vRelayRaces)

        vChatsButton.setTitle(t(API_TRANSLATE.app_chats))
        vTagsButton.setTitle(t(API_TRANSLATE.app_tags))
        vModerationButton.setTitle(t(API_TRANSLATE.app_moderation))
        vSubscribersButton.setTitle(t(API_TRANSLATE.app_users))
        vRubricButton.setTitle(t(API_TRANSLATE.app_rubrics))
        vWikiButton.setTitle(t(API_TRANSLATE.app_wiki))
        vRelayRaces.setTitle(t(API_TRANSLATE.app_relay_races))

        vLayoutCorned.makeSoftware()
        vChatsButton.setOnClickListener { Navigator.to(SFandomChatsList(xFandom.getId(), xFandom.getLanguageId())) }
        vTagsButton.setOnClickListener { STags.instance(xFandom.getId(), xFandom.getLanguageId(), Navigator.TO) }
        vModerationButton.setOnClickListener { Navigator.to(SModeration(xFandom.getId(), xFandom.getLanguageId())) }
        vSubscribersButton.setOnClickListener { Navigator.to(SRating(xFandom.getId(), xFandom.getLanguageId())) }
        vWikiButton.setOnClickListener { Navigator.to(SWikiList(xFandom.getId(), xFandom.getLanguageId(), 0, "")) }
        vRubricButton.setOnClickListener { Navigator.to(SRubricsList(xFandom.getId(), xFandom.getLanguageId(), 0, true)) }
        vRelayRaces.setOnClickListener { Navigator.to(SRelayRacesList(xFandom.getId(), xFandom.getLanguageId())) }
    }

    //
    //  EventBus
    //

    private fun onEventFandomRemoveModerator(e: EventFandomRemoveModerator) {
        if (e.fandomId == xFandom.getId() && e.languageId == xFandom.getLanguageId()) {
            update()
        }
    }

}
