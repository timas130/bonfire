package com.sayzen.campfiresdk.screens.other.gallery

import android.view.View
import android.widget.ImageView
import com.dzen.campfire.api.models.images.ImageRef
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.screens.account.profile.SProfile
import com.sayzen.campfiresdk.support.load
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.screens.SImageView
import com.sup.dev.android.views.views.ViewChipMini

class CardImage(
    val screen: SGallery,
    val image: ImageRef,
    val userName: String?
) : Card(R.layout.screen_other_gallery_card_image) {

    override fun bindView(view: View) {
        super.bindView(view)

        val vImage: ImageView = view.findViewById(R.id.vImage)
        val vLabel: ViewChipMini = view.findViewById(R.id.vLabel)

        ImageLoader.load(image).size(ToolsView.dpToPx(128).toInt()).into(vImage)

        vImage.setOnClickListener {
            val all = screen.getAllImages()
            var myIndex = 0
            for (i in all.indices) if (all[i].image == image) myIndex = i
            Navigator.to(SImageView(myIndex, Array(all.size) { ImageLoader.load(all[it].image) }))
        }

        vLabel.visibility = if (userName == null) View.GONE else View.VISIBLE
        vLabel.setText(userName)

        if (userName != null) {
            vLabel.setOnClickListener {
                SProfile.instance(userName, Navigator.TO)
            }
        }
    }


}
