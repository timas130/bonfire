package com.sayzen.campfiresdk.screens.post.view

import android.widget.ImageView
import android.widget.TextView
import com.dzen.campfire.api.ApiResources
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.compose.auth.SetBirthdayScreen
import com.sayzen.campfiresdk.support.load
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsResources
import com.sup.dev.android.views.views.ViewButton
import sh.sit.bonfire.auth.AuthController

class SAlertNsfw(private val onProceed: () -> Unit) : Screen(R.layout.screen_alert_nsfw) {
    private val vText: TextView = findViewById(R.id.vText)
    private val vAction: ViewButton = findViewById(R.id.vAction)
    private val vAction2: ViewButton = findViewById(R.id.vAction2)
    private val vImage: ImageView = findViewById(R.id.vImage)

    init {
        isNavigationVisible = false
        isNavigationAllowed = false
        isNavigationAnimation = false
        navigationBarColor = ToolsResources.getColorAttr(com.sup.dev.android.R.attr.colorPrimarySurface)
        statusBarColor = ToolsResources.getColorAttr(com.sup.dev.android.R.attr.colorPrimarySurface)

        vText.text = context.getString(R.string.nsfw_warning_full)

        vAction.text = context.getString(R.string.back)
        vAction.setOnClickListener {
            Navigator.remove(this)
        }

        ImageLoader.load(ApiResources.IMAGE_BACKGROUND_14).into(vImage)

        update()
    }

    fun update() {
        if (AuthController.currentUserState.value?.birthday == null) {
            vAction2.setText(R.string.nsfw_verify_age)
            vAction2.setOnClickListener {
                Navigator.to(SetBirthdayScreen(onBirthdaySet = {
                    update()
                }))
            }
        } else if (AuthController.currentUserState.value?.nsfwAllowed == true) {
            vAction2.setText(R.string.nsfw_open)
            vAction2.setOnClickListener {
                Navigator.remove(this)
                onProceed()
            }
        } else {
            vAction2.visibility = GONE
        }
    }
}
