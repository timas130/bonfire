package com.dzen.campfire.screens.hello

import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.dzen.campfire.R
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.ApiResources
import com.dzen.campfire.api.models.images.ImageRef
import com.dzen.campfire.api.requests.accounts.RAccountsChangeAvatar
import com.sayzen.campfiresdk.controllers.ControllerApi
import com.sayzen.campfiresdk.controllers.api
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.events.account.EventAccountChanged
import com.sayzen.campfiresdk.support.clear
import com.sayzen.campfiresdk.support.load
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.ViewAvatar
import com.sup.dev.java.libs.eventBus.EventBus

class Hello_Avatars(
    val screen: SCampfireHello,
    val demoMode: Boolean
) {

    val view: View = ToolsView.inflate(screen.vContainer, R.layout.screen_campfire_hello_avatars)
    val vTitle: TextView = view.findViewById(R.id.vTitle)
    val vNext: Button = view.findViewById(R.id.vNext)
    val vSkip: TextView = view.findViewById(R.id.vSkip)

    val vAvatar1: ViewAvatar = view.findViewById(R.id.vAvatar1)
    val vAvatar2: ViewAvatar = view.findViewById(R.id.vAvatar2)
    val vAvatar3: ViewAvatar = view.findViewById(R.id.vAvatar3)
    val vAvatar4: ViewAvatar = view.findViewById(R.id.vAvatar4)
    val vAvatar5: ViewAvatar = view.findViewById(R.id.vAvatar5)
    val vAvatar6: ViewAvatar = view.findViewById(R.id.vAvatar6)
    val vAvatar1_frame: ViewAvatar = view.findViewById(R.id.vAvatar1_frame)
    val vAvatar2_frame: ViewAvatar = view.findViewById(R.id.vAvatar2_frame)
    val vAvatar3_frame: ViewAvatar = view.findViewById(R.id.vAvatar3_frame)
    val vAvatar4_frame: ViewAvatar = view.findViewById(R.id.vAvatar4_frame)
    val vAvatar5_frame: ViewAvatar = view.findViewById(R.id.vAvatar5_frame)
    val vAvatar6_frame: ViewAvatar = view.findViewById(R.id.vAvatar6_frame)

    var currentAvatar = ImageRef()

    init {

        vNext.text = t(API_TRANSLATE.app_continue)
        vSkip.text = t(API_TRANSLATE.app_skip)
        vTitle.text = t(API_TRANSLATE.into_hello_avatars)

        ImageLoader.load(ApiResources.AVATAR_1).into(vAvatar1)
        ImageLoader.load(ApiResources.AVATAR_2).into(vAvatar2)
        ImageLoader.load(ApiResources.AVATAR_3).into(vAvatar3)
        ImageLoader.load(ApiResources.AVATAR_4).into(vAvatar4)
        ImageLoader.load(ApiResources.AVATAR_5).into(vAvatar5)
        ImageLoader.load(ApiResources.AVATAR_6).into(vAvatar6)

        vAvatar1.setOnClickListener { setAvatar(ApiResources.AVATAR_1, vAvatar1_frame) }
        vAvatar2.setOnClickListener { setAvatar(ApiResources.AVATAR_2, vAvatar2_frame) }
        vAvatar3.setOnClickListener { setAvatar(ApiResources.AVATAR_3, vAvatar3_frame) }
        vAvatar4.setOnClickListener { setAvatar(ApiResources.AVATAR_4, vAvatar4_frame) }
        vAvatar5.setOnClickListener { setAvatar(ApiResources.AVATAR_5, vAvatar5_frame) }
        vAvatar6.setOnClickListener { setAvatar(ApiResources.AVATAR_6, vAvatar6_frame) }

        updateFinishEnabled()

        vSkip.setOnClickListener { screen.toNextScreen() }
        vNext.setOnClickListener { send() }
    }

    private fun setAvatar(avatar: ImageRef, vFrame: ViewAvatar) {
        currentAvatar = avatar

        vAvatar1_frame.vImageView.setImageDrawable(null)
        vAvatar2_frame.vImageView.setImageDrawable(null)
        vAvatar3_frame.vImageView.setImageDrawable(null)
        vAvatar4_frame.vImageView.setImageDrawable(null)
        vAvatar5_frame.vImageView.setImageDrawable(null)
        vAvatar6_frame.vImageView.setImageDrawable(null)

        vFrame.vImageView.setImageDrawable(ColorDrawable(ToolsResources.getColor(R.color.focus)))

        updateFinishEnabled()
    }

    private fun updateFinishEnabled() {
        vNext.isEnabled = currentAvatar.isNotEmpty()
    }

    private fun send() {
        if (demoMode || currentAvatar.isEmpty()) {
            screen.toNextScreen()
        } else {
            val vLoading = ToolsView.showProgressDialog()
            ImageLoader.load(currentAvatar).intoBitmap {
                ControllerApi.toBytes(it, API.ACCOUNT_IMG_WEIGHT, API.ACCOUNT_IMG_SIDE, API.ACCOUNT_IMG_SIDE, true) {
                    if (it == null) {
                        vLoading.hide()
                        ToolsToast.show(t(API_TRANSLATE.error_unknown))
                    } else {
                        RAccountsChangeAvatar(it)
                            .onComplete {
                                ImageLoader.clear(ControllerApi.account.getImage())
                                EventBus.post(
                                    EventAccountChanged(
                                        ControllerApi.account.getId(),
                                        ControllerApi.account.getName(),
                                        ControllerApi.account.getImage()
                                    )
                                )
                                screen.toNextScreen()
                            }
                            .onError { ToolsToast.show(t(API_TRANSLATE.error_unknown)) }
                            .onFinish { vLoading.hide() }
                            .send(api)
                    }
                }
            }
        }
    }


}
