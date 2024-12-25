package com.dzen.campfire.screens.hello

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.dzen.campfire.R
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.ApiResources
import com.dzen.campfire.api.models.images.ImageRef
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.support.load
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.views.ViewButton

class Hello_Info(
        private val image: ImageRef,
        private val title: Long,
        private val text: Long,
        val screen: SCampfireHello
) {

    companion object{
        fun instance1(screen: SCampfireHello) = Hello_Info(ApiResources.IMAGE_BACKGROUND_13, API_TRANSLATE.hello_1_title, API_TRANSLATE.hello_1_text, screen)
        fun instance2(screen: SCampfireHello) = Hello_Info(ApiResources.IMAGE_BACKGROUND_9,  API_TRANSLATE.hello_2_title, API_TRANSLATE.hello_2_text, screen)
        fun instance3(screen: SCampfireHello) = Hello_Info(ApiResources.IMAGE_BACKGROUND_3,  API_TRANSLATE.hello_3_title, API_TRANSLATE.hello_3_text, screen)
        fun instance4(screen: SCampfireHello) = Hello_Info(ApiResources.IMAGE_BACKGROUND_14, API_TRANSLATE.hello_4_title, API_TRANSLATE.hello_4_text, screen)
    }

    val view: View = ToolsView.inflate(screen.vContainer, R.layout.screen_campfire_hello_info)
    val vImage: ImageView = view.findViewById(R.id.vImage)
    val vTitle: TextView = view.findViewById(R.id.vTitle)
    val vText: TextView = view.findViewById(R.id.vText)
    val vNext: ViewButton = view.findViewById(R.id.vNext)

    init {

        ImageLoader.load(image).noHolder().into(vImage)
        vTitle.text = t(title)
        vText.text = t(text)
        vNext.text = t(API_TRANSLATE.app_continue)

        view.setOnClickListener { screen.toNextScreen() }
        vNext.setOnClickListener { screen.toNextScreen() }
    }


}
