package com.sayzen.campfiresdk.models.cards.post_pages

import android.view.View
import android.widget.TextView
import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.models.publications.PagesContainer

import com.dzen.campfire.api.models.publications.post.PageImages
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.controllers.ControllerPost
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.views.views.ViewImagesContainer
import com.sup.dev.android.views.views.ViewText

class CardPageImages(
        pagesContainer: PagesContainer?,
        page: PageImages
) : CardPage(R.layout.card_page_images, pagesContainer, page) {

    override fun bindView(view: View) {
        super.bindView(view)
        val vTextEmpty: TextView = view.findViewById(R.id.vTextEmpty)
        val vTitle: ViewText = view.findViewById(R.id.vTitle)
        val vImagesContainer: ViewImagesContainer = view.findViewById(R.id.vImagesContainer)
        vTitle.setTextIsSelectable(clickable)

        vTextEmpty.text = t(API_TRANSLATE.post_page_images_empty)

        vTitle.text = (page as PageImages).title
        vTitle.visibility = if ((page as PageImages).title.isEmpty()) View.GONE else View.VISIBLE
        vTextEmpty.visibility = if ((page as PageImages).imagesIds.isEmpty()) View.VISIBLE else View.GONE
        vImagesContainer.clear()
        for (i in (page as PageImages).imagesIds.indices) {
            vImagesContainer.add(
                    ImageLoader.load((page as PageImages).imagesMiniIds[i])
                            .fullImageLoader(ImageLoader.load((page as PageImages).imagesIds[i]))
                            .size(
                                    if(i >= (page as PageImages).imagesMiniSizesW.size) 500 else (page as PageImages).imagesMiniSizesW[i],
                                    if(i >= (page as PageImages).imagesMiniSizesH.size) 500 else (page as PageImages).imagesMiniSizesH[i]
                            ),
                    {
                        if (pagesContainer != null) {
                            ControllerPost.toImagesScreen(pagesContainer, (page as PageImages).imagesIds[i])
                        } else {
                            vImagesContainer.toImageView(ImageLoader.load((page as PageImages).imagesMiniIds[i]))
                        }
                    })
        }

        ControllerLinks.makeLinkable(vTitle)
    }


    override fun onDetachView() {
        if (getView() == null) return
        val vImagesSwipe: ViewImagesContainer = getView()!!.findViewById(R.id.vImagesContainer)
        vImagesSwipe.clear()
    }

    override fun notifyItem() {
        for (i in 0 until (page as PageImages).imagesIds.size) {
            ImageLoader.load((page as PageImages).imagesIds[i]).intoCash()
        }
    }
}
