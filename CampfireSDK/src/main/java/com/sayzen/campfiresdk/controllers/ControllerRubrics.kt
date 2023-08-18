package com.sayzen.campfiresdk.controllers

import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.fandoms.Rubric
import com.dzen.campfire.api.requests.rubrics.*
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.events.rubrics.EventRubricChangeName
import com.sayzen.campfiresdk.models.events.rubrics.EventRubricChangeOwner
import com.sayzen.campfiresdk.models.events.rubrics.EventRubricRemove
import com.sayzen.campfiresdk.screens.account.search.SAccountSearch
import com.sayzen.campfiresdk.screens.fandoms.search.SFandomsSearch
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.splash.SplashField
import com.sup.dev.android.views.splash.SplashMenu
import com.sup.dev.java.libs.eventBus.EventBus

object ControllerRubrics {

    fun instanceMenu(rubric: Rubric) = SplashMenu()
            .add(t(API_TRANSLATE.app_copy_link)) { ToolsAndroid.setToClipboard(ControllerLinks.linkToRubric(rubric.id));ToolsToast.show(t(API_TRANSLATE.app_copied)) }
            .spoiler(t(API_TRANSLATE.app_additional), condition = ControllerApi.isCurrentAccount(rubric.owner.id)) { w, card ->
                card.setProgress(true)
                ApiRequestsSupporter.execute(RRubricsGetParams(rubric.id)) { r ->
                    card.setProgress(false)
                    w.spoiler(card)
                            .add(if (r.isNotification) t(API_TRANSLATE.rubric_notification_on) else t(API_TRANSLATE.rubric_notification_off)) { changeNotifications(rubric) }.backgroundRes(R.color.focus)
                            .finishItemBuilding()
                }
            }
            .groupCondition(ControllerApi.can(rubric.fandom.id, rubric.fandom.languageId, API.LVL_MODERATOR_RUBRIC))
            .spoiler(t(API_TRANSLATE.app_moderator))
            .add(t(API_TRANSLATE.app_change_naming)) { edit(rubric) }.backgroundRes(R.color.blue_700).textColorRes(R.color.white)
            .add(t(API_TRANSLATE.app_remove)) { removeRubric(rubric) }.backgroundRes(R.color.blue_700).textColorRes(R.color.white)
            .add(t(API_TRANSLATE.rubric_change_owner)) { changeOwner(rubric) }.backgroundRes(R.color.blue_700).textColorRes(R.color.white)
            .spoiler(t(API_TRANSLATE.app_admin))
            .add(t(API_TRANSLATE.rubric_move_fandom)) { changeFandom(rubric) }.condition(ControllerApi.can(rubric.fandom.id, rubric.fandom.languageId, API.LVL_ADMIN_MOVE_RUBRIC)).backgroundRes(R.color.red_500).textColorRes(R.color.white)

    private fun changeNotifications(rubric: Rubric){
        ApiRequestsSupporter.executeProgressDialog(RRubricsChangeNotifications(rubric.id)) { r ->
            ControllerActivities.setRubricsCount(r.rubricsCount)
            if (r.isNotification) ToolsToast.show(t(API_TRANSLATE.rubric_notification_on_message))
            else ToolsToast.show(t(API_TRANSLATE.rubric_notification_off_message))
        }
    }

    private fun edit(rubric: Rubric) {
        SplashField()
                .setTitle(t(API_TRANSLATE.app_change_naming))
                .setMin(API.RUBRIC_NAME_MIN)
                .setMax(API.RUBRIC_NAME_MAX)
                .setOnCancel(t(API_TRANSLATE.app_cancel))
                .setOnEnter(t(API_TRANSLATE.app_change)) { w, newName ->
                    ControllerApi.moderation(t(API_TRANSLATE.app_change_naming), t(API_TRANSLATE.app_change), { RRubricsModerChangeName(rubric.id, newName, it) }) { r ->
                        EventBus.post(EventRubricChangeName(rubric.id, newName))
                        ToolsToast.show(t(API_TRANSLATE.app_done))
                    }
                }
                .asSheetShow()
    }

    private fun removeRubric(rubric: Rubric) {
        ControllerApi.moderation(t(API_TRANSLATE.app_remove), t(API_TRANSLATE.app_remove), { RRubricsModerRemove(rubric.id, it) }) { r ->
            EventBus.post(EventRubricRemove(rubric.id))
            ToolsToast.show(t(API_TRANSLATE.app_done))
        }
    }

    private fun changeOwner(rubric: Rubric) {
        Navigator.to(SAccountSearch(false, true) { account ->
            ControllerApi.moderation(t(API_TRANSLATE.rubric_change_owner), t(API_TRANSLATE.app_change), { RRubricsModerChangeOwner(rubric.id, account.id, it) }) { r ->
                EventBus.post(EventRubricChangeOwner(rubric.id, r.rubric.owner))
                ToolsToast.show(t(API_TRANSLATE.app_done))
            }
        })
    }

    private fun changeFandom(rubric: Rubric) {
        SFandomsSearch.instance(Navigator.TO, backWhenSelect = true) { fandom ->
            if (rubric.owner.id != ControllerApi.account.getId()) {
                ControllerApi.moderation(
                    t(API_TRANSLATE.rubric_move_fandom),
                    t(API_TRANSLATE.app_move),
                    { RRubricsMoveFandom(rubric.id, fandom.id, fandom.languageId, it) }
                ) {
                    ToolsToast.show(t(API_TRANSLATE.app_done))
                }
            } else {
                ApiRequestsSupporter.executeProgressDialog(
                    RRubricsMoveFandom(rubric.id, fandom.id, fandom.languageId, "")
                ) { _ ->
                    ToolsToast.show(t(API_TRANSLATE.app_done))
                }.onApiError(RRubricsMoveFandom.E_SAME_FANDOM) {
                    ToolsToast.show(t(API_TRANSLATE.error_same_fandom))
                }
            }
        }
    }
}
