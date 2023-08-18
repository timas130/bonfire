package com.sayzen.campfiresdk.models.cards.post_pages

import android.view.View
import android.widget.ImageView
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.PagesContainer
import com.dzen.campfire.api.models.publications.post.PageImage
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerPost
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.views.screens.SImageView

class CardPageImage(
        pagesContainer: PagesContainer?,
        page: PageImage
) : CardPage(R.layout.card_page_image, pagesContainer, page) {

    override fun getChangeMenuItemText() = t(API_TRANSLATE.app_crop)

    override fun bindView(view: View) {
        super.bindView(view)
        val page = this.page as PageImage
        val vImage: ImageView = view.findViewById(R.id.vImage)
        val vGifProgressBar: View = view.findViewById(R.id.vGifProgressBar)

        vGifProgressBar.visibility = View.GONE

        if (clickable) vImage.setOnClickListener { onImageClicked() }
        else vImage.setOnClickListener(null)

        vImage.isFocusable = false
        vImage.isClickable = clickable
        vImage.isFocusableInTouchMode = false

        ImageLoader.loadGif(page.imageId, page.gifId, vImage, vGifProgressBar){
            it.size(page.w, page.h)
        }
    }

    private fun onImageClicked() {
        if (pagesContainer != null) {
            val page = this.page as PageImage
            ControllerPost.toImagesScreen(pagesContainer, page.imageId)
        } else {
            Navigator.to(SImageView(ImageLoader.load((page as PageImage).getMainImageId())))
        }
    }

    override fun notifyItem() {
        ImageLoader.load((page as PageImage).imageId).size((page as PageImage).w, (page as PageImage).h).intoCash()
    }

}
