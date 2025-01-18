package com.sayzen.campfiresdk.controllers

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.PublicationComment
import com.dzen.campfire.api.requests.publications.RPublicationsReactionAdd
import com.dzen.campfire.api.requests.publications.RPublicationsReactionRemove
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.models.events.publications.EventCommentRemove
import com.sayzen.campfiresdk.models.events.publications.EventPublicationReactionAdd
import com.sayzen.campfiresdk.models.events.publications.EventPublicationReactionRemove
import com.sayzen.campfiresdk.screens.post.history.SPublicationHistory
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sayzen.campfiresdk.support.load
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.splash.SplashMenu
import com.sup.dev.android.views.views.ViewIcon
import com.sup.dev.java.libs.eventBus.EventBus

object ControllerComment {
    fun showMenu(targetView: View, x: Float, y: Float, publication: PublicationComment) {
        val vMenuReactions = FrameLayout(targetView.context)
        val vMenuReactionsLinear = LinearLayout(targetView.context)
        vMenuReactionsLinear.orientation = LinearLayout.HORIZONTAL
        vMenuReactions.addView(vMenuReactionsLinear)
        (vMenuReactionsLinear.layoutParams as FrameLayout.LayoutParams).gravity = Gravity.CENTER
        (vMenuReactionsLinear.layoutParams as FrameLayout.LayoutParams).topMargin = ToolsView.dpToPx(8).toInt()
        vMenuReactionsLinear.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        vMenuReactionsLinear.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT

        val w = SplashMenu()
            .addTitleView(vMenuReactions)
            .add(t(API_TRANSLATE.app_copy_link)) {
                ToolsAndroid.setToClipboard(ControllerLinks.linkToComment(publication));ToolsToast.show(
                t(API_TRANSLATE.app_copied)
            )
            }
            .groupCondition(ControllerApi.isCurrentAccount(publication.creator.id))
            .add(t(API_TRANSLATE.app_remove)) {
                ControllerApi.removePublication(
                    publication.id,
                    t(API_TRANSLATE.comment_remove_confirm),
                    t(API_TRANSLATE.comment_error_gone)
                ) { EventBus.post(EventCommentRemove(publication.id, publication.parentPublicationId)) }
            }
            .clearGroupCondition()
            .add(t(API_TRANSLATE.app_copy)) {
                ToolsAndroid.setToClipboard(publication.text);ToolsToast.show(
                t(
                    API_TRANSLATE.app_copied
                )
            )
            }
            .add(t(API_TRANSLATE.app_history)) { Navigator.to(SPublicationHistory(publication.id)) }
            .condition(ControllerPost.ENABLED_HISTORY)
            .groupCondition(!ControllerApi.isCurrentAccount(publication.creator.id))
            .add(t(API_TRANSLATE.app_report)) {
                ControllerApi.reportPublication(
                    publication.id,
                    t(API_TRANSLATE.comment_report_confirm),
                    t(API_TRANSLATE.comment_error_gone)
                )
            }
            .spoiler(t(API_TRANSLATE.app_moderator))
            .add(t(API_TRANSLATE.app_clear_reports)) {
                ControllerApi.clearReportsPublication(
                    publication.id,
                    publication.publicationType
                )
            }.backgroundRes(R.color.blue_700).textColorRes(R.color.white).condition(
                ControllerApi.can(
                    publication.fandom.id,
                    publication.fandom.languageId,
                    API.LVL_MODERATOR_BLOCK
                ) && publication.reportsCount > 0
            )
            .add(t(API_TRANSLATE.app_block)) { ControllerPublications.block(publication) }
            .backgroundRes(R.color.blue_700).textColorRes(R.color.white)
            .condition(ControllerApi.can(publication.fandom.id, publication.fandom.languageId, API.LVL_MODERATOR_BLOCK))
            .clearGroupCondition()
            .spoiler(t(API_TRANSLATE.app_protoadmin))
            .add(t(API_TRANSLATE.app_restore)) { ControllerPublications.restoreDeepBlock(publication.id) }
            .backgroundRes(R.color.orange_700).textColorRes(R.color.white)
            .condition(ControllerApi.can(API.LVL_PROTOADMIN) && publication.status == API.STATUS_DEEP_BLOCKED)
            .asPopupShow(targetView, x, y)


        val p = ToolsView.dpToPx(4).toInt()
        for (i in API.REACTIONS.indices) {
            val v: ViewIcon = ToolsView.inflate(vMenuReactionsLinear, R.layout.z_icon_18)
            v.setPadding(p, p, p, p)
            v.setOnClickListener { sendReaction(publication, i.toLong()); w?.hide(); }
            vMenuReactionsLinear.addView(v)
            ImageLoader.load(API.REACTIONS[i]).into(v)
        }
    }

    fun sendReaction(publication: PublicationComment, reactionIndex: Long) {
        ApiRequestsSupporter.executeProgressDialog(
            RPublicationsReactionAdd(
                publication.id,
                reactionIndex
            )
        ) { _ ->
            ToolsToast.show(t(API_TRANSLATE.app_done))
            EventBus.post(EventPublicationReactionAdd(publication.id, reactionIndex))
        }
            .onApiError(API.ERROR_ALREADY) { ToolsToast.show(t(API_TRANSLATE.app_done)) }
            .onApiError(API.ERROR_GONE) { ToolsToast.show(t(API_TRANSLATE.comment_error_gone)) }
    }

    fun removeReaction(publication: PublicationComment, reactionIndex: Long) {
        ApiRequestsSupporter.executeProgressDialog(
            RPublicationsReactionRemove(
                publication.id,
                reactionIndex
            )
        ) { _ ->
            ToolsToast.show(t(API_TRANSLATE.app_done))
            EventBus.post(EventPublicationReactionRemove(publication.id, reactionIndex))
        }
            .onApiError(API.ERROR_GONE) { ToolsToast.show(t(API_TRANSLATE.comment_error_gone)) }
    }
}
