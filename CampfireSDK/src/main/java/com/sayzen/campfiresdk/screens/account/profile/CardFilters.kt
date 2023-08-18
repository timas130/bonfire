package com.sayzen.campfiresdk.screens.account.profile

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.dzen.campfire.api.API_TRANSLATE
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.screens.fandoms.search.SFandomsSearch
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.cards.CardMenu
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.android.views.splash.SplashCheckBoxes

class CardFilters(
        private val onChange: () -> Unit
) : Card(R.layout.screen_account_card_filters) {

    var fandomId = 0L
    var fandomLanguageId = 0L
    var fandomName = ""

    override fun bindView(view: View) {
        super.bindView(view)
        val vFilters: ViewIcon = view.findViewById(R.id.vFilters)
        val vText: TextView = view.findViewById(R.id.vText)

        vFilters.setImageResource(if(fandomId > 0) R.drawable.ic_clear_white_24dp else R.drawable.ic_tune_white_24dp)
        vText.text = if (fandomName.isEmpty()) t(API_TRANSLATE.app_publications) else fandomName

        vFilters.setOnClickListener {

            if(fandomId > 0){
                fandomId = 0L
                fandomLanguageId = 0L
                fandomName = ""
                onChange.invoke()
                update()
                return@setOnClickListener
            }


            val eventsOld = ControllerSettings.profileFilterEvents
            val postOld = ControllerSettings.profileFilterPosts
            val commentOld = ControllerSettings.profileFilterComments
            val chatMessagesOld = ControllerSettings.profileFilterChatMessages
            val moderationsOld = ControllerSettings.profileFilterModerations
            val stickersOld = ControllerSettings.profileFilterStickers

            var eventsNew = eventsOld
            var postNew = postOld
            var commentNew = commentOld
            var chatMessagesNew = chatMessagesOld
            var moderationsNew = moderationsOld
            var stickersNew = stickersOld

            val widget = SplashCheckBoxes()
                    .add(t(API_TRANSLATE.filter_events)).checked(ControllerSettings.profileFilterEvents).onChange { eventsNew = it.isChecked }
                    .add(t(API_TRANSLATE.filter_posts)).checked(ControllerSettings.profileFilterPosts).onChange { postNew = it.isChecked }
                    .add(t(API_TRANSLATE.filter_comment)).checked(ControllerSettings.profileFilterComments).onChange { commentNew = it.isChecked }
                    .add(t(API_TRANSLATE.filter_chat_messages)).checked(ControllerSettings.profileFilterChatMessages).onChange { chatMessagesNew = it.isChecked }
                    .add(t(API_TRANSLATE.filter_moderations)).checked(ControllerSettings.profileFilterModerations).onChange { moderationsNew = it.isChecked }
                    .add(t(API_TRANSLATE.app_stickers)).checked(ControllerSettings.profileFilterStickers).onChange { stickersNew = it.isChecked }
                    .setOnCancel(t(API_TRANSLATE.app_cancel))
                    .setOnEnter(t(API_TRANSLATE.app_save)) {

                        ControllerSettings.profileFilterEvents = eventsNew
                        ControllerSettings.profileFilterPosts = postNew
                        ControllerSettings.profileFilterComments = commentNew
                        ControllerSettings.profileFilterChatMessages = chatMessagesNew
                        ControllerSettings.profileFilterModerations = moderationsNew
                        ControllerSettings.profileFilterStickers = stickersNew

                        if (eventsOld != ControllerSettings.profileFilterEvents
                                || postOld != ControllerSettings.profileFilterPosts
                                || commentOld != ControllerSettings.profileFilterComments
                                || chatMessagesOld != ControllerSettings.profileFilterChatMessages
                                || moderationsOld != ControllerSettings.profileFilterModerations
                                || stickersOld != ControllerSettings.profileFilterStickers
                        ) {
                            if (!ControllerSettings.profileFilterEvents
                                    && !ControllerSettings.profileFilterPosts
                                    && !ControllerSettings.profileFilterComments
                                    && !ControllerSettings.profileFilterChatMessages
                                    && !ControllerSettings.profileFilterModerations
                                    && !ControllerSettings.profileFilterStickers
                            ) {
                                ControllerSettings.profileFilterEvents = true
                                ControllerSettings.profileFilterPosts = true
                                ControllerSettings.profileFilterComments = true
                                ControllerSettings.profileFilterChatMessages = true
                                ControllerSettings.profileFilterModerations = true
                                ControllerSettings.profileFilterStickers = true
                            }
                            onChange.invoke()
                        }
                    }


            val cardFandom = CardMenu()
            cardFandom.setText(t(API_TRANSLATE.app_choose_fandom))
            cardFandom.setOnClick {
                SFandomsSearch.instance(Navigator.TO, true) {
                    fandomId = it.id
                    fandomName = it.name
                    fandomLanguageId = it.languageId
                    onChange.invoke()
                    update()
                }
            }
            val viewFandom = cardFandom.instanceView(widget.view as ViewGroup)
            cardFandom.bindCardView(viewFandom)
            widget.addView(viewFandom)

            widget.asSheetShow()
        }
    }

}