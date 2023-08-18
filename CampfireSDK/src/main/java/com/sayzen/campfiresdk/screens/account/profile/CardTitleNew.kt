package com.sayzen.campfiresdk.screens.account.profile

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.API_TRANSLATE
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.support.adapters.XAccount
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.controllers.tCap
import com.sayzen.campfiresdk.controllers.tPlural
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.*
import com.sup.dev.android.views.screens.SImageView
import com.sup.dev.android.views.views.ViewAvatarTitle
import com.sup.dev.java.tools.ToolsDate

class CardTitleNew(
        private val xAccount: XAccount
) : CardTitle(xAccount, R.layout.screen_account_card_title_new) {

    override fun bindView(view: View) {
        super.bindView(view)
        val vAvatar: ViewAvatarTitle = view.findViewById(R.id.vAvatar)

        xAccount.setView(vAvatar)

        vAvatar.vAvatar.setOnClickListener {
            Navigator.to(SImageView(ImageLoader.load(xAccount.getImageId())))
        }
        vAvatar.vAvatar.setOnLongClickListener {
            Navigator.to(SImageView(ImageLoader.load(xAccount.getImageId())))
            true
        }
        vAvatar.setOnClickListener {}
        vAvatar.setOnLongClickListener { true }

        if (xAccount.isBot()) {
            vAvatar.setSubtitle(t(API_TRANSLATE.app_bot))
            vAvatar.setSubtitleColor(ToolsResources.getColor(R.color.green_700))
        } else if (!xAccount.isOnline()) {
            vAvatar.setSubtitle(tCap(API_TRANSLATE.app_was_online, ToolsResources.sex(xAccount.getSex(), t(API_TRANSLATE.he_was), t(API_TRANSLATE.she_was)), ToolsDate.dateToString(xAccount.getLastOnlineTime())))
            vAvatar.setSubtitleColor(ToolsResources.getColor(R.color.grey_500))
        } else {
            vAvatar.setSubtitle(t(API_TRANSLATE.app_online))
            vAvatar.setSubtitleColor(ToolsResources.getColor(R.color.green_700))
        }

        updateDateCreate()
    }

    override fun updateDateCreate() {
        val view = getView() ?: return
        val vDate: TextView = view.findViewById(R.id.vDate)

        if(xAccount.getDateAccountCreated() > 0) {
            val days = ((ControllerApi.currentTime() - xAccount.getDateAccountCreated()) / (1000L * 60 * 60 * 24)) + 1
            vDate.text = "$days ${tPlural(days.toInt(), API_TRANSLATE.days_count)} " + t(API_TRANSLATE.app_wits_us)
        }else{
            vDate.text = ""
        }

    }


}
