package com.sayzen.campfiresdk.screens.fandoms.view

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.account.Account
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerSettings
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.screens.account.search.SAccountSearch
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.cards.CardMenu
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.android.views.splash.SplashCheckBoxes

class CardFilters(
        private val onChange: () -> Unit
) : Card(R.layout.screen_account_card_filters) {
    var account: Account? = null

    override fun bindView(view: View) {
        super.bindView(view)
        val vFilters: ViewIcon = view.findViewById(R.id.vFilters)
        val vText: TextView = view.findViewById(R.id.vText)

        vFilters.setImageResource(if (account != null) R.drawable.ic_clear_white_24dp else R.drawable.ic_tune_white_24dp)
        vText.text = t(API_TRANSLATE.app_publications)

        vFilters.setOnClickListener {
            if (account != null) {
                account = null
                onChange()
                update()
                return@setOnClickListener
            }

            val fandomFilterModerationsPostsOld = ControllerSettings.fandomFilterModerationsPosts
            val fandomFilterOnlyImportantOld = ControllerSettings.fandomFilterOnlyImportant
            val fandomFilterAdministrationsOld = ControllerSettings.fandomFilterAdministrations
            val fandomFilterModerationsOld = ControllerSettings.fandomFilterModerations
            val fandomFilterModerationsBlocksOld = ControllerSettings.fandomFilterModerationsBlocks

            var fandomFilterModerationsPostsNew = fandomFilterModerationsPostsOld
            var fandomFilterOnlyImportantNew = fandomFilterOnlyImportantOld
            var fandomFilterAdministrationsNew = fandomFilterAdministrationsOld
            var fandomFilterModerationsNew = fandomFilterModerationsOld
            var fandomFilterModerationsBlocksNew = fandomFilterModerationsBlocksOld

            val splash = SplashCheckBoxes()
                    .add(t(API_TRANSLATE.filter_posts)).checked(ControllerSettings.fandomFilterModerationsPosts).onChange { fandomFilterModerationsPostsNew = it.isChecked }
                    .add(t(API_TRANSLATE.filter_only_important)).checked(ControllerSettings.fandomFilterOnlyImportant).onChange { fandomFilterOnlyImportantNew = it.isChecked }
                    .add(t(API_TRANSLATE.filter_events)).checked(ControllerSettings.fandomFilterAdministrations).onChange { fandomFilterAdministrationsNew = it.isChecked }
                    .add(t(API_TRANSLATE.filter_moderations)).checked(ControllerSettings.fandomFilterModerations).onChange { fandomFilterModerationsNew = it.isChecked }
                    .add(t(API_TRANSLATE.filter_moderations_block)).checked(ControllerSettings.fandomFilterModerationsBlocks).onChange { fandomFilterModerationsBlocksNew = it.isChecked }
                    .setOnCancel(t(API_TRANSLATE.app_cancel))
                    .setOnEnter(t(API_TRANSLATE.app_save)) {

                        ControllerSettings.fandomFilterModerationsPosts = fandomFilterModerationsPostsNew
                        ControllerSettings.fandomFilterOnlyImportant = fandomFilterOnlyImportantNew
                        ControllerSettings.fandomFilterAdministrations = fandomFilterAdministrationsNew
                        ControllerSettings.fandomFilterModerations = fandomFilterModerationsNew
                        ControllerSettings.fandomFilterModerationsBlocks = fandomFilterModerationsBlocksNew

                        if (fandomFilterModerationsPostsOld != ControllerSettings.fandomFilterModerationsPosts
                                || fandomFilterOnlyImportantOld != ControllerSettings.fandomFilterOnlyImportant
                                || fandomFilterAdministrationsOld != ControllerSettings.fandomFilterAdministrations
                                || fandomFilterModerationsOld != ControllerSettings.fandomFilterModerations
                                || fandomFilterModerationsBlocksOld != ControllerSettings.fandomFilterModerationsBlocks
                        ) {
                            onChange.invoke()
                        }
                    }

            val accountCard = CardMenu()
                    .setText(t(API_TRANSLATE.app_choose_user))
                    .setOnClick {
                        Navigator.to(SAccountSearch(showMyAccount = true) {
                            account = it
                            onChange()
                            update()
                        })
                    }
            val viewAccount = accountCard.instanceView(splash.view as ViewGroup)
            accountCard.bindCardView(viewAccount)
            splash.addView(viewAccount)

            splash.asSheetShow()
        }
    }
}
