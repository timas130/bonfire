package com.sayzen.campfiresdk.screens.account.profile

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.account.AccountLinks
import com.dzen.campfire.api.requests.accounts.*
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.*
import com.sayzen.campfiresdk.models.events.account.EventAccountBioChangedAge
import com.sayzen.campfiresdk.models.events.account.EventAccountBioChangedDescription
import com.sayzen.campfiresdk.models.events.account.EventAccountBioChangedLinks
import com.sayzen.campfiresdk.models.events.account.EventAccountBioChangedSex
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sayzen.campfiresdk.support.adapters.XAccount
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.settings.Settings
import com.sup.dev.android.views.splash.*
import com.sup.dev.android.views.views.ViewText
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsText

class CardBio(
        private var xAccount: XAccount
) : Card(R.layout.screen_account_card_bio) {

    private val eventBus = EventBus
            .subscribe(EventAccountBioChangedAge::class) { onEventAccountBioChangedAge(it) }
            .subscribe(EventAccountBioChangedDescription::class) { onEventAccountBioChangedDescription(it) }
            .subscribe(EventAccountBioChangedLinks::class) { onEventAccountBioChangedLinks(it) }
            .subscribe(EventAccountBioChangedSex::class) { onEventAccountBioChangedSex(it) }

    private var loaded = false
    private var age = 0L
    private var description = ""
    private var links = AccountLinks()

    override fun bindView(view: View) {
        super.bindView(view)
        val vSex: TextView = view.findViewById(R.id.vSex)
        val vAge: TextView = view.findViewById(R.id.vAge)
        val vDescription: ViewText = view.findViewById(R.id.vInfo)
        val vDescriptionChange: View = view.findViewById(R.id.vDescriptionChange)
        val vLinksContainer: ViewGroup = view.findViewById(R.id.vLinksContainer)
        val vAddLink: Settings = view.findViewById(R.id.vAddLink)

        vAddLink.setTitle(t(API_TRANSLATE.profile_link_add))
        view.visibility = if (loaded) View.VISIBLE else View.GONE

        vSex.text = t(API_TRANSLATE.profile_appeal, if (xAccount.getSex() == 0L) tCap(API_TRANSLATE.he) else tCap(API_TRANSLATE.she))
        vAge.text = t(API_TRANSLATE.profile_age, if (age == 0L) t(API_TRANSLATE.profile_age_not_set) else age)
        vDescription.text = if (description.isEmpty()) t(API_TRANSLATE.profile_bio_empty) else description

        if (ControllerApi.isCurrentAccount(xAccount.getId())) {
            vSex.setOnClickListener { onSexClicked() }
            vAge.setOnClickListener { onAgeClicked() }
            vDescriptionChange.visibility = View.VISIBLE
            vDescriptionChange.setOnClickListener { onDescriptionClicked() }
            vAddLink.setOnClickListener { onChangeLinkClicked(links.getEmptyIndex(), t(API_TRANSLATE.app_create)) }
            vAddLink.visibility = if (links.count() == API.ACCOUNT_LINK_MAX) View.GONE else View.VISIBLE
        } else {
            vSex.setOnClickListener(null)
            vAge.setOnClickListener(null)
            vDescriptionChange.visibility = View.GONE
            vDescriptionChange.setOnClickListener(null)
            vAddLink.setOnClickListener(null)
            vAddLink.visibility = View.GONE

            if (description.isNotEmpty() && ControllerApi.can(API.LVL_ADMIN_USER_REMOVE_DESCRIPTION)) {
                vDescriptionChange.visibility = View.VISIBLE
                vDescriptionChange.setOnClickListener { onAdminRemoveDescriptionClicked() }
            }

        }

        ControllerLinks.makeLinkable(vDescription)

        vLinksContainer.visibility = if (links.count() > 0) View.VISIBLE else View.GONE
        vLinksContainer.removeAllViews()

        for (i in 0 until links.links.size) {
            val link = links.links[i]
            if (link.a1.isEmpty() || link.a2.isEmpty()) continue
            val v: View = ToolsView.inflate(R.layout.screen_account_card_bio_view_link)
            val vTitle: TextView = v.findViewById(R.id.vTitle)
            val vUrl: TextView = v.findViewById(R.id.vUrl)

            vTitle.text = link.a1
            vUrl.text = link.a2

            v.setOnClickListener { ControllerLinks.openLink(link.a2) }

            val w = SplashMenu()
                    .add(t(API_TRANSLATE.app_copy)) {
                        ToolsAndroid.setToClipboard(link.a2)
                        ToolsToast.show(t(API_TRANSLATE.app_copied))
                    }
                    .add(t(API_TRANSLATE.app_change)) { onChangeLinkClicked(i, t(API_TRANSLATE.app_change), link.a1, link.a2) }.condition(ControllerApi.isCurrentAccount(xAccount.getId()))
                    .add(t(API_TRANSLATE.app_remove)) { onRemoveLinkClicked(i) }.condition(ControllerApi.isCurrentAccount(xAccount.getId()))
                    .add(t(API_TRANSLATE.app_remove)) { onAdminRemoveLinkClicked(i) }.backgroundRes(R.color.red_700).textColorRes(R.color.white).condition(!ControllerApi.isCurrentAccount(xAccount.getId()) && ControllerApi.can(API.LVL_ADMIN_USER_REMOVE_LINK))

            v.setOnLongClickListener {
                w.asSheetShow()
                true
            }

            vLinksContainer.addView(v)
        }
    }

    fun setInfo(age: Long, description: String, links: AccountLinks) {
        loaded = true
        this.age = age
        this.description = description
        this.links = links
        update()
    }

    //
    //  Clicks
    //

    private fun onSexClicked() {
        SplashMenu()
                .add(t(API_TRANSLATE.he)) { ControllerCampfireSDK.setSex(0) { ToolsToast.show(t(API_TRANSLATE.app_done)) } }
                .add(t(API_TRANSLATE.she)) { ControllerCampfireSDK.setSex(1) { ToolsToast.show(t(API_TRANSLATE.app_done)) } }
                .asSheetShow()
    }

    private fun onAgeClicked() {
        SplashAge(age) { w, age ->
            setAge(w, age)
        }.asSheetShow()
    }

    private fun onDescriptionClicked() {
        SplashField()
                .setAutoHideOnEnter(false)
                .setHint(t(API_TRANSLATE.app_description))
                .setText(description)
                .setMax(API.ACCOUNT_DESCRIPTION_MAX_L)
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .setOnEnter(t(API_TRANSLATE.app_change)) { w, t -> setDescription(w, t) }
                .asSheetShow()
    }

    private fun onChangeLinkClicked(index: Int, enterText: String, title: String = "", url: String = "") {
        SplashFieldTwo()
                .setHint_1(t(API_TRANSLATE.app_naming))
                .setMax_1(API.ACCOUNT_LINK_TITLE_MAX_L)
                .setMin_1(1)
                .setText_1(title)
                .setLinesCount_1(1)
                .setHint_2(t(API_TRANSLATE.app_link))
                .addChecker_2 { ToolsText.isWebLink(it) }
                .setMin_2(2)
                .setText_2(url)
                .setMax_2(API.ACCOUNT_LINK_URL_MAX_L)
                .setLinesCount_2(1)
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .setOnEnter(enterText) { w, titleV, urlV -> setLink(w, index, titleV, urlV) }
                .asSheetShow()
    }

    private fun onRemoveLinkClicked(index: Int) {
        SplashAlert()
                .setText(t(API_TRANSLATE.app_remove_link))
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .setOnEnter(t(API_TRANSLATE.app_remove)) { w -> setLink(w, index, "", "") }
                .asSheetShow()
    }

    private fun onAdminRemoveDescriptionClicked() {
        SplashField()
                .setTitle(t(API_TRANSLATE.profile_remove_description))
                .setHint(t(API_TRANSLATE.moderation_widget_comment))
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .setOnEnter(t(API_TRANSLATE.app_remove)) { w, comment -> adminRemoveDescription(w, comment) }
                .asSheetShow()
    }

    private fun onAdminRemoveLinkClicked(index: Int) {
        SplashField()
                .setTitle(t(API_TRANSLATE.app_remove_link))
                .setHint(t(API_TRANSLATE.moderation_widget_comment))
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .setOnEnter(t(API_TRANSLATE.app_remove)) { w, comment -> adminRemoveLink(w, index, comment) }
                .asSheetShow()
    }

    //
    //  Api
    //

    private fun setAge(splash: Splash, age: Long) {
        ApiRequestsSupporter.executeEnabled(splash, RAccountsBioSetAge(age)) {
            EventBus.post(EventAccountBioChangedAge(xAccount.getId(), age))
            ToolsToast.show(t(API_TRANSLATE.app_done))
        }
    }

    private fun setDescription(splash: Splash, description: String) {
        ApiRequestsSupporter.executeEnabled(splash, RAccountsBioSetDescription(description)) {
            EventBus.post(EventAccountBioChangedDescription(xAccount.getId(), description))
            ToolsToast.show(t(API_TRANSLATE.app_done))
        }
    }

    private fun setLink(splash: Splash, index: Int, description: String, url: String) {
        ApiRequestsSupporter.executeEnabled(splash, RAccountsBioSetLink(index, description, url)) {
            links.set(index, description, url)
            EventBus.post(EventAccountBioChangedLinks(xAccount.getId(), links))
            ToolsToast.show(t(API_TRANSLATE.app_done))
        }
    }

    private fun adminRemoveDescription(splash: Splash, comment: String) {
        ApiRequestsSupporter.executeEnabled(splash, RAccountsAdminRemoveDescription(xAccount.getId(), comment)) {
            EventBus.post(EventAccountBioChangedDescription(xAccount.getId(), ""))
            ToolsToast.show(t(API_TRANSLATE.app_done))
        }
    }

    private fun adminRemoveLink(splash: Splash, index: Int, comment: String) {
        ApiRequestsSupporter.executeEnabled(splash, RAccountsAdminRemoveLink(xAccount.getId(), index, comment)) {
            links.set(index, "", "")
            EventBus.post(EventAccountBioChangedLinks(xAccount.getId(), links))
            ToolsToast.show(t(API_TRANSLATE.app_done))
        }
    }

    //
    //  EventBus
    //

    private fun onEventAccountBioChangedAge(e: EventAccountBioChangedAge) {
        if (e.accountId == xAccount.getId()) {
            age = e.age
            update()
        }
    }

    private fun onEventAccountBioChangedDescription(e: EventAccountBioChangedDescription) {
        if (e.accountId == xAccount.getId()) {
            description = e.description
            update()
        }
    }

    private fun onEventAccountBioChangedLinks(e: EventAccountBioChangedLinks) {
        if (e.accountId == xAccount.getId()) {
            links = e.links
            update()
        }
    }

    private fun onEventAccountBioChangedSex(e: EventAccountBioChangedSex) {
        if (e.accountId == xAccount.getId()) {
            update()
        }
    }

}