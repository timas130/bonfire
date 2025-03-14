package com.sayzen.campfiresdk.screens.activities

import android.view.View
import android.view.ViewGroup
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerActivities
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.ControllerHoliday
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.activities.EventActivitiesAdminCountChanged
import com.sayzen.campfiresdk.screens.activities.administration.block.SAdministrationBlock
import com.sayzen.campfiresdk.screens.activities.administration.fandoms.SAdministrationFandoms
import com.sayzen.campfiresdk.screens.activities.administration.reports.SAdministrationReports
import com.sayzen.campfiresdk.screens.activities.administration.reports.SAdministrationUserReports
import com.sayzen.campfiresdk.screens.activities.administration.votes.SAdminVotes
import com.sayzen.campfiresdk.screens.activities.quests.SQuestsList
import com.sayzen.campfiresdk.screens.activities.support.SDonate
import com.sayzen.campfiresdk.screens.activities.user_activities.SRelayRacesList
import com.sayzen.campfiresdk.screens.administation.SAdministrationDeepBlocked
import com.sayzen.campfiresdk.screens.fandoms.rubrics.SRubricsList
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.settings.Settings
import com.sup.dev.android.views.views.ViewChip
import com.sup.dev.java.libs.eventBus.EventBus

class SActivities : Screen(R.layout.screen_activities) {

    private val eventBus = EventBus
            .subscribe(EventActivitiesAdminCountChanged::class) { updateCounters() }

    private val vContainer: ViewGroup = findViewById(R.id.vContainer)
    private val vRelayRace: Settings = findViewById(R.id.vRelayRace)
    private val vRubrics: Settings = findViewById(R.id.vRubrics)
    private val vSupport: Settings = findViewById(R.id.vSupport)
    private val vTitleAdmins: Settings = findViewById(R.id.vTitleAdmins)
    private val vFandoms: Settings = findViewById(R.id.vFandoms)
    private val vUserReports: Settings = findViewById(R.id.vUserReports)
    private val vReports: Settings = findViewById(R.id.vReports)
    private val vBlock: Settings = findViewById(R.id.vBlock)
    private val vTitleProtoadmins: Settings = findViewById(R.id.vTitleProtoadmins)
    private val vDebug: Settings = findViewById(R.id.vDebug)
    private val vDeepBlocks: Settings = findViewById(R.id.vDeepBlocks)
    private val vQuest: Settings = findViewById(R.id.vQuest)
    private val vAdminVotes: Settings = findViewById(R.id.vAdminVotes)

    private val vRelayRaceChip = ViewChip.instanceMini(vRelayRace, "")
    private val vRubricsChip = ViewChip.instanceMini(vRubrics, "")
    private val vFandomsChip = ViewChip.instanceMini(vFandoms, "")
    private val vUserReportsChip = ViewChip.instanceMini(vUserReports, "")
    private val vReportsChip = ViewChip.instanceMini(vReports, "")
    private val vBlockChip = ViewChip.instanceMini(vBlock, "")
    private val vAdminVotesChip = ViewChip.instanceMini(vAdminVotes, "")

    init {
        disableNavigation()
        disableShadows()
        setTitle(t(API_TRANSLATE.app_activities))

        vFandoms.setTitle(t(API_TRANSLATE.administration_fandoms))
        vUserReports.setTitle(t(API_TRANSLATE.administration_user_reports))
        vDebug.setTitle(t(API_TRANSLATE.administration_debug))
        vDeepBlocks.setTitle(t(API_TRANSLATE.protoadin_profile_blocked))
        vSupport.setTitle(t(API_TRANSLATE.activities_support))
        vQuest.setTitle(t(API_TRANSLATE.activities_new_year_quest))
        vReports.setTitle(t(API_TRANSLATE.moderation_screen_reports))
        vRelayRace.setTitle(t(API_TRANSLATE.app_relay_races))
        vRubrics.setTitle(t(API_TRANSLATE.app_rubrics))
        vTitleAdmins.setTitle(t(API_TRANSLATE.app_admin))
        vBlock.setTitle(t(API_TRANSLATE.app_block_title))
        vTitleProtoadmins.setTitle(t(API_TRANSLATE.app_protoadmin))
        vAdminVotes.setTitle(t(API_TRANSLATE.translates_title_administration))

        vQuest.setOnClickListener { Navigator.to(SQuestsList()) }
        vRelayRace.setOnClickListener { Navigator.to(SRelayRacesList()) }
        vRubrics.setOnClickListener { Navigator.to(SRubricsList(0, 0, ControllerApi.account.getId(), true)) }
        vFandoms.setOnClickListener { SAdministrationFandoms.instance(Navigator.TO) }
        vUserReports.setOnClickListener { Navigator.to(SAdministrationUserReports()) }
        vReports.setOnClickListener { Navigator.to(SAdministrationReports()) }
        vBlock.setOnClickListener { Navigator.to(SAdministrationBlock()) }
        vSupport.setOnClickListener { SDonate.instance(Navigator.TO) }
        vDebug.setOnClickListener { Navigator.to(SQuestsList()) }
        vDeepBlocks.setOnClickListener { Navigator.to(SAdministrationDeepBlocked(0)) }
        vAdminVotes.setOnClickListener {  Navigator.to(SAdminVotes())  }

        if(ControllerApi.getLanguage().code != "ru" || !ControllerHoliday.isNewYear()) vQuest.visibility = View.GONE

        if(ControllerApi.account.getLevel() >= 200) {
            vSupport.visibility = View.VISIBLE
        }else{
            vSupport.visibility = View.GONE
        }

        vTitleProtoadmins.visibility = if (ControllerApi.isProtoadmin()) View.VISIBLE else View.GONE
        //vDebug.visibility = if (ControllerApi.isProtoadmin()) View.VISIBLE else View.GONE
        vDeepBlocks.visibility = if (ControllerApi.isProtoadmin()) View.VISIBLE else View.GONE

        vRelayRaceChip.setBackground(ToolsResources.getSecondaryColor(context))
        vRubricsChip.setBackground(ToolsResources.getSecondaryColor(context))

        vRelayRace.setSubView(vRelayRaceChip)
        vRubrics.setSubView(vRubricsChip)
        vFandoms.setSubView(vFandomsChip)
        vUserReports.setSubView(vUserReportsChip)
        vReports.setSubView(vReportsChip)
        vBlock.setSubView(vBlockChip)
        vAdminVotes.setSubView(vAdminVotesChip)

        if (!ControllerApi.can(API.LVL_ADMIN_BAN)) {
            vUserReports.isEnabled = false
            vUserReports.setSubtitle(t(API_TRANSLATE.activities_low_lvl) + ". (${t(API_TRANSLATE.app_level)} ${API.LVL_ADMIN_BAN.lvl/100f}, ${t(API_TRANSLATE.app_karma)} ${API.LVL_ADMIN_BAN.karmaCount/100})")
            vUserReportsChip.visibility = View.GONE
        } else {
            vUserReportsChip.visibility = View.VISIBLE
        }

        if (!ControllerApi.can(API.LVL_ADMIN_BAN)) {
            vAdminVotes.isEnabled = false
            vAdminVotes.setSubtitle(t(API_TRANSLATE.activities_low_lvl) + ". (${t(API_TRANSLATE.app_level)} ${API.LVL_ADMIN_BAN.lvl/100f}, ${t(API_TRANSLATE.app_karma)} ${API.LVL_ADMIN_BAN.karmaCount/100})")
            vAdminVotesChip.visibility = View.GONE
        } else {
            vAdminVotesChip.visibility = View.VISIBLE
        }

        if (!ControllerApi.can(API.LVL_ADMIN_MODER)) {
            vReports.isEnabled = false
            vReports.setSubtitle(t(API_TRANSLATE.activities_low_lvl) + ". (${t(API_TRANSLATE.app_level)} ${API.LVL_ADMIN_MODER.lvl/100f}, ${t(API_TRANSLATE.app_karma)} ${API.LVL_ADMIN_MODER.karmaCount/100})")
            vReportsChip.visibility = View.GONE
        } else {
            vReportsChip.visibility = View.VISIBLE
        }

        if (!ControllerApi.can(API.LVL_ADMIN_FANDOMS_ACCEPT)) {
            vFandoms.isEnabled = false
            vFandoms.setSubtitle(t(API_TRANSLATE.activities_low_lvl) + ". (${t(API_TRANSLATE.app_level)} ${API.LVL_ADMIN_FANDOMS_ACCEPT.lvl/100f}, ${t(API_TRANSLATE.app_karma)} ${API.LVL_ADMIN_FANDOMS_ACCEPT.karmaCount/100})")
            vFandomsChip.visibility = View.GONE
        } else {
            vFandomsChip.visibility = View.VISIBLE
        }

        if (!ControllerApi.can(API.LVL_ADMIN_FANDOM_ADMIN)) {
            vBlock.isEnabled = false
            vBlock.setSubtitle(t(API_TRANSLATE.activities_low_lvl) + ". (${t(API_TRANSLATE.app_level)} ${API.LVL_ADMIN_FANDOM_ADMIN.lvl/100f}, ${t(API_TRANSLATE.app_karma)} ${API.LVL_ADMIN_FANDOM_ADMIN.karmaCount/100})")
            vBlockChip.visibility = View.GONE
        } else {
            vBlockChip.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        ControllerActivities.reloadActivities()
    }

    private fun updateCounters() {
        if (ControllerActivities.isAdministrationLoadInProgress()) {
            vRelayRaceChip.text = ""
            vRubricsChip.text = ""
            if (vFandomsChip.visibility == View.VISIBLE) vFandomsChip.text = "-"
            if (vUserReportsChip.visibility == View.VISIBLE) vUserReportsChip.text = "-"
            if (vReportsChip.visibility == View.VISIBLE) vReportsChip.text = "-"
            if (vBlockChip.visibility == View.VISIBLE) vBlockChip.text = "-"
            if (vAdminVotesChip.visibility == View.VISIBLE) vAdminVotesChip.text = "-"
        } else {
            vRelayRaceChip.text = "${if(ControllerActivities.getRelayRacesCount() == 0L) "" else ControllerActivities.getRelayRacesCount()}"
            vRubricsChip.text =  "${if(ControllerActivities.getRubricsCount() == 0L) "" else ControllerActivities.getRubricsCount()}"
            if (vFandomsChip.visibility == View.VISIBLE) vFandomsChip.text = "${ControllerActivities.getSuggestedFandomsCount()}"
            if (vUserReportsChip.visibility == View.VISIBLE) vUserReportsChip.text = "${ControllerActivities.getReportsUserCount()}"
            if (vReportsChip.visibility == View.VISIBLE) vReportsChip.text = "${ControllerActivities.getReportsCount()}"
            if (vBlockChip.visibility == View.VISIBLE) vBlockChip.text = "${ControllerActivities.getBlocksCount()}"
            if (vAdminVotesChip.visibility == View.VISIBLE) vAdminVotesChip.text = "${ControllerActivities.getAdminVoteCount()}"
        }
    }
}
