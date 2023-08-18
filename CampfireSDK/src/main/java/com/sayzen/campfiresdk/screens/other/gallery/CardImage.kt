package com.sayzen.campfiresdk.screens.other.gallery

import android.view.View
import android.widget.ImageView
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.ControllerLinks
import com.sayzen.campfiresdk.screens.account.profile.SProfile
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.image_loader.ImageLoaderId
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.libs.screens.navigator.Navigator
import com.sup.dev.android.tools.ToolsView
import com.sup.dev.android.views.cards.Card
import com.sup.dev.android.views.screens.SImageView
import com.sup.dev.android.views.views.ViewChip
import com.sup.dev.android.views.views.ViewChipMini
import com.sup.dev.android.views.views.ViewText

class CardImage(
        val screen: SGallery,
        val imageId: Long,
        val userName: String?
) : Card(R.layout.screen_other_gallery_card_image) {

    override fun bindView(view: View) {
        super.bindView(view)

        val vImage: ImageView = view.findViewById(R.id.vImage)
        val vLabel: ViewChipMini = view.findViewById(R.id.vLabel)

        ImageLoader.load(imageId).size(ToolsView.dpToPx(128).toInt()).into(vImage)

        vImage.setOnClickListener {
            val all = screen.getAllImages()
            var myIndex = 0
            for (i in all.indices) if (all[i].imageId == imageId) myIndex = i
            Navigator.to(SImageView(myIndex, Array(all.size) { ImageLoaderId(all[it].imageId) }))
        }

        vLabel.visibility = if (userName == null) View.GONE else View.VISIBLE
        vLabel.setText(userName)

        if(userName != null) {
            vLabel.setOnClickListener {
                SProfile.instance(userName, Navigator.TO)
            }
        }
    }


}