package com.sayzen.campfiresdk.screens.fandoms.moderation.moderators.privilege

import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.lvl.LvlInfoModeration
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.app.CampfireConstants
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.views.cards.CardDividerTitle
import com.sup.dev.android.views.screens.SRecycler
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapter

class SPrivilege(
        private val fandomId: Long,
        private val languageId: Long
) : SRecycler() {

    private val adapterSub: RecyclerCardAdapter = RecyclerCardAdapter()
    private var ignoreLine: Boolean = false

    init {
        disableShadows()
        disableNavigation()

        adapterSub.add(CardInfo(fandomId, languageId))
        for(i in CampfireConstants.LVLS)  if(i.lvl is LvlInfoModeration) addLvl(CardKarmaLvl(fandomId, languageId, i))

        vRecycler.adapter = adapterSub

        setTitle(t(API_TRANSLATE.app_privilege))
    }

    private fun addLvl(card: CardKarmaLvl) {
        if (!ignoreLine && !ControllerApi.can(fandomId, languageId, card.moderateInfo.lvl) && !adapterSub.contains(CardDividerTitle::class)) {
            if (!adapterSub.contains(CardKarmaLvl::class))
                ignoreLine = true
            else
                adapterSub.add(CardDividerTitle().setText(t(API_TRANSLATE.achi_you_are_here)))
        }
        adapterSub.add(card)
    }
}