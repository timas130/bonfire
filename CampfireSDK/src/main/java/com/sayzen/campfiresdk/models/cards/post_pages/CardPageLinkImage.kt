package com.sayzen.campfiresdk.models.cards.post_pages

import android.view.View
import android.widget.ImageView
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.PagesContainer

import com.dzen.campfire.api.models.publications.post.PageLinkImage
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsToast

class CardPageLinkImage(
        pagesContainer: PagesContainer?,
        page: PageLinkImage
) : CardPage(R.layout.card_page_link_image, pagesContainer, page) {

    override fun bindView(view: View) {
        super.bindView(view)

        val vImage: ImageView = view.findViewById(R.id.vImage)
        val vTouch: View = view.findViewById(R.id.vTouch)

        ImageLoader.load((page as PageLinkImage).imageId).into(vImage)

        if (clickable){
            vTouch.setOnClickListener { ControllerLinks.openLink((page as PageLinkImage).link) }
            vTouch.setOnLongClickListener {
                ToolsAndroid.setToClipboard((page as PageLinkImage).link)
                ToolsToast.show(t(API_TRANSLATE.app_copied))
                true
            }
        }
        else {
            vTouch.setOnClickListener(null)
            vTouch.setOnLongClickListener(null)
        }

        vTouch.isFocusable = false
        vTouch.isClickable = clickable
        vTouch.isFocusableInTouchMode = false


    }


    override fun notifyItem() {
        ImageLoader.load((page as PageLinkImage).imageId).intoCash()
    }
}