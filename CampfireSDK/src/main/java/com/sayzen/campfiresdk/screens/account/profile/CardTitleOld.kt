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
import com.sup.dev.android.views.views.ViewCircleImage
import com.sup.dev.java.tools.ToolsDate

class CardTitleOld(
        private val xAccount: XAccount
) : CardTitle(xAccount, R.layout.screen_account_card_title_old) {

    override fun bindView(view: View) {
        super.bindView(view)
        val vAvatar: ViewCircleImage = view.findViewById(R.id.vAvatar)
        val vStatus: TextView = view.findViewById(R.id.vStatus)
        val vName: TextView = view.findViewById(R.id.vName)

        xAccount.setView(vAvatar)
        xAccount.setView(vName)

        vAvatar.setOnClickListener {
            Navigator.to(SImageView(ImageLoader.load(xAccount.getImageId())))
        }
        vAvatar.setOnLongClickListener {
            Navigator.to(SImageView(ImageLoader.load(xAccount.getImageId())))
            true
        }

        if (xAccount.isBot()) {
            vStatus.text = t(API_TRANSLATE.app_bot)
            vStatus.setTextColor(ToolsResources.getColor(R.color.green_700))
        } else if (!xAccount.isOnline()) {
            vStatus.text = tCap(API_TRANSLATE.app_was_online, ToolsResources.sex(xAccount.getSex(), t(API_TRANSLATE.he_was), t(API_TRANSLATE.she_was)), ToolsDate.dateToString(xAccount.getLastOnlineTime()))
            vStatus.setTextColor(ToolsResources.getColor(R.color.grey_500))
        } else {
            vStatus.text = t(API_TRANSLATE.app_online)
            vStatus.setTextColor(ToolsResources.getColor(R.color.green_700))
        }
    }


}
