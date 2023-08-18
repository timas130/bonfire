package com.sayzen.campfiresdk.screens.activities.administration.votes

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.admins.MAdminVote
import com.dzen.campfire.api.requests.admins.RAdminVoteAccept
import com.dzen.campfire.api.requests.admins.RAdminVoteCancel
import com.dzen.campfire.api.requests.admins.RAdminVoteGet
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.*
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sayzen.campfiresdk.support.adapters.XAccount
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.screens.SLoading
import com.sup.dev.android.views.views.ViewAvatar
import com.sup.dev.android.views.views.ViewButton
import com.sup.dev.android.views.views.ViewText

class SAdminVotes : SLoading(R.layout.screen_admin_votes) {

    val vAvatar: ViewAvatar = findViewById(R.id.vAvatar)
    val vAvatarText:TextView = findViewById(R.id.vAvatarText)
    val vActionText:ViewText = findViewById(R.id.vActionText)
    val vCommentText:ViewText = findViewById(R.id.vCommentText)
    val vCancel: ViewButton = findViewById(R.id.vCancel)
    val vAccept: ViewButton = findViewById(R.id.vAccept)
    var xAccount:XAccount? = null
    var m:MAdminVote? = null

    init {
        activityRootBackground = ToolsResources.getColorAttr(R.attr.colorPrimary)
        disableNavigation()
        reload()
        setTextEmpty(t(API_TRANSLATE.screen_admin_votes))

        vCancel.text = t(API_TRANSLATE.app_reject)
        vAccept.text = t(API_TRANSLATE.app_confirm)

        vCancel.setOnClickListener { cancel() }
        vAccept.setOnClickListener { accept() }
    }

    fun reload(){
        m = null;
        reset()
        setState(State.PROGRESS)

        RAdminVoteGet()
                .onComplete {
                    m = it.mAdminVote
                    reset()
                }
                .onError {
                    setState(State.ERROR)
                }
                .send(api)

    }

    fun accept(){
        ApiRequestsSupporter.executeProgressDialog(
            RAdminVoteAccept(m!!.id)
        ) { r ->
            ToolsToast.show(t(API_TRANSLATE.app_done))
            reload()
        }
    }

    fun cancel(){
        ControllerApi.moderation(
            t(API_TRANSLATE.screen_admin_votes_cancel_title),
            t(API_TRANSLATE.app_reject),
            { RAdminVoteCancel(m!!.id, it) },
            {
                ToolsToast.show(t(API_TRANSLATE.app_done))
                reload()
            })
    }


    fun reset(){
        setState(if(m == null)State.EMPTY else State.NONE)
        vAvatar.visibility = if(m == null) View.GONE else View.VISIBLE
        vAvatarText.visibility = if(m == null) View.GONE else View.VISIBLE
        vActionText.visibility = if(m == null) View.GONE else View.VISIBLE
        vCommentText.visibility = if(m == null) View.GONE else View.VISIBLE
        vCancel.visibility = if(m == null) View.GONE else View.VISIBLE
        vAccept.visibility = if(m == null) View.GONE else View.VISIBLE
        if(m != null) {
            xAccount = XAccount().setAccount(m!!.adminAccount)
            xAccount?.setView(vAvatar)
            xAccount?.setView(vAvatarText)
            vActionText.text = ControllerAdminVote.getActionText(m!!)
            vCommentText.text = ControllerAdminVote.getCommentText(m!!)
            ControllerLinks.makeLinkable(vActionText)
            ControllerLinks.makeLinkable(vCommentText)
        }
    }

    override fun onReloadClicked() {
        reload()
    }


}