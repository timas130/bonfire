package com.sayzen.campfiresdk.models.cards.post_pages

import android.view.View
import android.widget.ImageView
import com.dzen.campfire.api.models.publications.PagesContainer
import com.dzen.campfire.api.models.publications.post.PageVideo
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerYoutube
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.views.views.ViewIcon

class CardPageVideo(
        pagesContainer: PagesContainer?,
        page: PageVideo
) : CardPage(R.layout.card_page_video, pagesContainer, page) {

    override fun bindView(view: View) {
        super.bindView(view)
        val page = this.page as PageVideo
        val vImage: ImageView = view.findViewById(R.id.vImage)
        val vPlay: ViewIcon = view.findViewById(R.id.vPlay)

        if (clickable) vImage.setOnClickListener { ControllerYoutube.play(page.videoId) }
        else vImage.setOnClickListener(null)
        vPlay.setOnClickListener { ControllerYoutube.play(page.videoId) }

        vImage.isFocusable = false
        vImage.isClickable = clickable
        vImage.isFocusableInTouchMode = false

        ImageLoader.load(page.imageId).size(page.w, page.h).into(vImage)
    }

    override fun notifyItem() {
        val page = this.page as PageVideo
        ImageLoader.load(page.imageId).size(page.w, page.h).intoCash()
    }

}
