package com.sayzen.campfiresdk.screens.chat.create

import android.view.View
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.chat.ChatMember
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.support.adapters.XAccount
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.controllers.tCap
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.android.views.views.layouts.LayoutCorned

class CardChatMember(
        val chatMember: ChatMember,
        val myLvl: Long
) : Card(R.layout.screen_chat_create_card_user) {

    private val xAccount = XAccount().setAccount(chatMember.account).setOnChanged{ update() }
    var newLevel = 0L

    override fun bindView(view: View) {
        super.bindView(view)

        val vAvatar: ViewAvatarTitle = view.findViewById(R.id.vAvatar)
        val vRemove: View = view.findViewById(R.id.vRemove)
        val vUser: View = view.findViewById(R.id.vUser)
        val vModer: View = view.findViewById(R.id.vModer)
        val vAdmin: View = view.findViewById(R.id.vAdmin)

        xAccount.setView(vAvatar)
        vAvatar.isClickable = false
        vAvatar.vAvatar.isClickable = false

        vUser.setOnClickListener { newLevel = API.CHAT_MEMBER_LVL_USER;updateIcons() }
        vModer.setOnClickListener { newLevel = API.CHAT_MEMBER_LVL_MODERATOR;updateIcons() }
        vAdmin.setOnClickListener { newLevel = API.CHAT_MEMBER_LVL_ADMIN;updateIcons() }

        var canRemove = false
        if(myLvl == API.CHAT_MEMBER_LVL_ADMIN) canRemove = true
        if(myLvl == API.CHAT_MEMBER_LVL_MODERATOR && chatMember.memberLvl == API.CHAT_MEMBER_LVL_USER)  canRemove = true
        if(chatMember.memberLvl == API.CHAT_MEMBER_LVL_USER && chatMember.memberOwner == ControllerApi.account.getId())  canRemove = true
        if(xAccount.getId() == ControllerApi.account.getId()) canRemove = false

        vRemove.visibility = if (canRemove) View.VISIBLE else View.INVISIBLE

        vAvatar.setSubtitle("")
        if (chatMember.memberStatus == API.CHAT_MEMBER_STATUS_LEAVE) vAvatar.setSubtitle(tCap(API_TRANSLATE.chat_member_label_leave, ToolsResources.sex(xAccount.getSex(), t(API_TRANSLATE.he_leave), t(API_TRANSLATE.she_leave))))

        vRemove.setOnClickListener {
            adapter.remove(this)
        }

        updateIcons()
    }

    private fun updateIcons() {
        if (getView() == null) return
        val vUser: View = getView()!!.findViewById(R.id.vUser)
        val vModer: LayoutCorned = getView()!!.findViewById(R.id.vModer)
        val vAdmin: View = getView()!!.findViewById(R.id.vAdmin)

        vUser.setBackgroundColor(ToolsResources.getColor(if (getLvl() == API.CHAT_MEMBER_LVL_USER) R.color.green_700 else R.color.focus_dark))
        vModer.setBackgroundColor(ToolsResources.getColor(if (getLvl() == API.CHAT_MEMBER_LVL_MODERATOR) R.color.blue_700 else R.color.focus_dark))
        vAdmin.setBackgroundColor(ToolsResources.getColor(if (getLvl() == API.CHAT_MEMBER_LVL_ADMIN) R.color.red_700 else R.color.focus_dark))

        vUser.invalidate()
        vModer.invalidate()
        vAdmin.invalidate()

        if (myLvl == API.CHAT_MEMBER_LVL_ADMIN && xAccount.getId() != ControllerApi.account.getId()) {
            vUser.visibility = View.VISIBLE
            vModer.visibility = View.VISIBLE
            vAdmin.visibility = View.VISIBLE
        } else {
            vUser.visibility = if (getLvl() == API.CHAT_MEMBER_LVL_USER) View.VISIBLE else View.GONE
            vModer.visibility = if (getLvl() == API.CHAT_MEMBER_LVL_MODERATOR) View.VISIBLE else View.GONE
            vAdmin.visibility = if (getLvl() == API.CHAT_MEMBER_LVL_ADMIN) View.VISIBLE else View.GONE
        }

    }

    private fun getLvl() = if(newLevel > 0) newLevel else chatMember.memberLvl

}