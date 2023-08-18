package com.sayzen.campfiresdk.controllers

import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.admins.*
import com.sup.dev.java.libs.debug.log
import com.sup.dev.java.tools.ToolsDate

object ControllerAdminVote {

    fun getActionText(m: MAdminVote, html:Boolean=true):String{
        if(m is MAdminVoteAccountRecountAchi) return t(API_TRANSLATE.screen_admin_votes_info_account_recount_achi, ControllerLinks.linkToAccount(m.targetAccount.name))
        if(m is MAdminVoteAccountChangeName) return t(API_TRANSLATE.screen_admin_votes_info_account_change_name, ControllerLinks.linkToAccount(m.targetAccount.name), m.newName)
        if(m is MAdminVoteAccountEffect) return t(API_TRANSLATE.screen_admin_votes_info_account_effect, ControllerLinks.linkToAccount(m.targetAccount.name), ControllerEffects.getTitle(m.effectIndex), ToolsDate.dateToString(m.dateEnd))
        if(m is MAdminVoteAccountPunish){
            if(m.banTime > 0)return t(API_TRANSLATE.screen_admin_votes_info_account_punish_bam, ControllerLinks.linkToAccount(m.targetAccount.name), ToolsDate.dateToString(m.banEndDate))
            else return t(API_TRANSLATE.screen_admin_votes_info_account_punish_warn, ControllerLinks.linkToAccount(m.targetAccount.name))
        }
        if(m is MAdminVoteAccountRecountKarma) return t(API_TRANSLATE.screen_admin_votes_info_account_recount_karma, ControllerLinks.linkToAccount(m.targetAccount.name))
        if(m is MAdminVoteAccountRemoveAvatar) return t(API_TRANSLATE.screen_admin_votes_info_account_remove_avatar, ControllerLinks.linkToAccount(m.targetAccount.name))
        if(m is MAdminVoteAccountRemoveBackground) return t(API_TRANSLATE.screen_admin_votes_info_account_remove_background, ControllerLinks.linkToAccount(m.targetAccount.name))
        if(m is MAdminVoteAccountRemoveName) return t(API_TRANSLATE.screen_admin_votes_info_account_remove_name, ControllerLinks.linkToAccount(m.targetAccount.name))
        if(m is MAdminVoteAccountRemoveReports) return t(API_TRANSLATE.screen_admin_votes_info_account_remove_reports, ControllerLinks.linkToAccount(m.targetAccount.name))
        if(m is MAdminVoteAccountRemoveStatus) return t(API_TRANSLATE.screen_admin_votes_info_account_remove_status, ControllerLinks.linkToAccount(m.targetAccount.name))
        if(m is MAdminVoteFandomRemove) return t(API_TRANSLATE.screen_admin_votes_info_fandom_remove, if(html) "[${m.targetFandom.name}]${ControllerLinks.linkToFandom(m.targetFandom.id)}" else m.targetFandom.name)
        return t(API_TRANSLATE.error_of_loading)
    }

    fun getCommentText(m: MAdminVote):String{
        return t(API_TRANSLATE.app_comment)+": " + m.comment
    }

}