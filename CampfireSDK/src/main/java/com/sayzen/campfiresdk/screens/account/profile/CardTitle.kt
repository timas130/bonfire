package com.sayzen.campfiresdk.screens.account.profile

import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.requests.accounts.RAccountsChangeAvatar
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.support.adapters.XAccount
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.account.*
import com.sayzen.campfiresdk.screens.activities.support.SDonate
import com.sayzen.campfiresdk.support.ApiRequestsSupporter
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.*
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.screens.SCrop
import com.sup.dev.android.views.views.ViewChip
import com.sup.dev.android.views.splash.SplashChooseImage
import com.sup.dev.android.views.splash.SplashProgressTransparent
import com.sup.dev.java.libs.eventBus.EventBus
import com.sup.dev.java.tools.ToolsBytes
import com.sup.dev.java.tools.ToolsText
import com.sup.dev.java.tools.ToolsThreads

abstract class CardTitle(
        private val xAccount: XAccount,
        layout: Int
) : Card(layout) {

    override fun bindView(view: View) {
        super.bindView(view)
        val vSponsor: ViewChip = view.findViewById(R.id.vSponsor)

        vSponsor.setOnClickListener { SDonate.instance(xAccount.getId(), Navigator.TO) }
        vSponsor.text = t(API_TRANSLATE.app_sponsor) + " ${ToolsText.numToStringRoundAndTrim(xAccount.getSponsor() / 100.0, 2)} \u20BD"
        vSponsor.visibility = if(xAccount.getSponsor() > 0L && ControllerApi.account.getLevel() >= 200) VISIBLE else GONE
    }

    fun onChangeAvatarClicked() {
        SplashChooseImage()
                .setOnSelected { _, bytes, _ ->

                    ToolsThreads.thread {

                        val bitmap = ToolsBitmap.decode(bytes)
                        if (bitmap == null) {
                            ToolsToast.show(t(API_TRANSLATE.error_cant_load_image))
                            return@thread
                        }

                        ToolsThreads.main {

                            val isGif = ControllerApi.can(API.LVL_CAN_CHANGE_AVATAR_GIF) && ToolsBytes.isGif(bytes)
                            val cropSize = if (isGif) API.ACCOUNT_IMG_SIDE_GIF else API.ACCOUNT_IMG_SIDE

                            Navigator.to(SCrop(bitmap, cropSize, cropSize) { _, b2, x, y, w, h ->
                                if (isGif) {

                                    val d = ToolsView.showProgressDialog()
                                    ToolsThreads.thread {
                                        val bytesSized = ToolsGif.resize(bytes, API.ACCOUNT_IMG_SIDE_GIF, API.ACCOUNT_IMG_SIDE_GIF, x, y, w, h, true)

                                        ToolsThreads.main {
                                            if (bytesSized.size > API.ACCOUNT_IMG_WEIGHT_GIF) {
                                                d.hide()
                                                ToolsToast.show(t(API_TRANSLATE.error_too_long_file))
                                            } else {
                                                changeAvatarNow(d, bytesSized)
                                            }
                                        }
                                    }

                                } else {
                                    val d = ToolsView.showProgressDialog()
                                    ControllerApi.toBytes(b2, API.ACCOUNT_IMG_WEIGHT, API.ACCOUNT_IMG_SIDE, API.ACCOUNT_IMG_SIDE, true) {
                                        if (it == null) d.hide()
                                        else changeAvatarNow(d, it)
                                    }
                                }
                            })

                        }


                    }


                }
                .asSheetShow()
    }

    open fun updateDateCreate() {

    }

    private fun changeAvatarNow(dialog: SplashProgressTransparent, bytes: ByteArray) {
        ApiRequestsSupporter.executeProgressDialog(dialog, RAccountsChangeAvatar(bytes)) { _ ->
            ImageLoader.clear(xAccount.getImageId())
            EventBus.post(EventAccountChanged(xAccount.getId(), xAccount.getName(), xAccount.getImageId()))
        }
    }


}
