package com.sayzen.campfiresdk.screens.other.gallery

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.ApiResources
import com.dzen.campfire.api.models.images.ImageRef
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t
import com.sup.dev.android.libs.screens.Screen
import com.sup.dev.android.tools.ToolsAndroid
import com.sup.dev.android.tools.ToolsToast
import com.sup.dev.android.views.support.adapters.recycler_view.RecyclerCardAdapter
import com.sup.dev.java.tools.ToolsCollections

class SGallery : Screen(R.layout.screen_other_gallery) {

    private val vCopyLink: View = findViewById(R.id.vCopyLink)
    private val vRecycler: RecyclerView = findViewById(R.id.vRecycler)

    private val adapter = RecyclerCardAdapter()

    private val array_bg = cardsOf(ApiResources.ARRAY_BACKGROUND)
    private val array_newYear = cardsOf(ApiResources.ARRAY_NEW_YEAR)
    private val array_achi = cardsOf(ApiResources.ARRAY_ACHI)
    private val array_level = cardsOf(ApiResources.ARRAY_BACKGROUND_LEVEL)
    private val array_emoji = cardsOf(ApiResources.ARRAY_EMOJI)
    private val array_other =
        cardsOf(ToolsCollections.merge(ApiResources.ARRAY_CAMPFIRE_IMAGE, ApiResources.ARRAY_AVATAR))
    private val array_flags = cardsOf(ApiResources.ARRAY_FLAG)
    private val array_icons = cardsOf(ApiResources.ARRAY_ICON)
    private val array_developers = cardsOf(ApiResources.ARRAY_DEVELOPER)
    private val array_newyearQuest = cardsOf(ApiResources.ARRAY_QUEST_NEW_YEAR)
    private val array_all = arrayOf(
        *array_bg,
        *array_level,
        *array_achi,
        *array_newYear,
        *array_emoji,
        *array_other,
        *array_flags,
        *array_icons,
        *array_developers,
        *array_newyearQuest
    )

    init {
        disableNavigation()
        setTitle(t(API_TRANSLATE.app_gallery))

        vCopyLink.setOnClickListener {
            ToolsAndroid.setToClipboard(API.LINK_RULES_GALLERY.asWeb())
            ToolsToast.show(t(API_TRANSLATE.app_copied))
        }

        vRecycler.layoutManager = LinearLayoutManager(context)
        vRecycler.adapter = adapter

        fill(t(API_TRANSLATE.gallery_achi_title), t(API_TRANSLATE.gallery_achi_text), array_achi, 5)
        fill(t(API_TRANSLATE.gallery_level_title), t(API_TRANSLATE.gallery_level_text), array_level, 3)
        fill(t(API_TRANSLATE.gallery_bg_title), t(API_TRANSLATE.gallery_bg_title), array_bg, 3)
        fill(t(API_TRANSLATE.gallery_emoji_title), t(API_TRANSLATE.gallery_emoji_text), array_emoji, 7)
        fill(t(API_TRANSLATE.gallery_other_title), t(API_TRANSLATE.gallery_other_text), array_other, 3)
        fill(t(API_TRANSLATE.gallery_flags_title), t(API_TRANSLATE.gallery_flags_text), array_flags, 5)
        fill(t(API_TRANSLATE.gallery_icons_title), t(API_TRANSLATE.gallery_icons_text), array_icons, 5)
        fill(t(API_TRANSLATE.gallery_developers_title), t(API_TRANSLATE.gallery_developers_text), array_developers, 3)
        fill(t(API_TRANSLATE.gallery_ny_title), t(API_TRANSLATE.gallery_ny_text), array_newYear, 4)
        fill(t(API_TRANSLATE.gallery_nyq_title), t(API_TRANSLATE.gallery_nyq_text), array_newyearQuest, 2)
    }

    private fun fill(title: String, text: String, array: Array<CardImage>, count: Int) {
        adapter.add(CardTitle(title, text))
        for (i in array.indices step count) adapter.add(CardContainer(this, i, array, count))
    }

    fun getAllImages() = array_all

    private fun cardsOf(array: Array<ImageRef>) = Array(array.size) {
        CardImage(this, array[it], getLabel(array[it]))
    }

    private fun getLabel(image: ImageRef): String? {
        if (image == ApiResources.IMAGE_BACKGROUND_26
            || image == ApiResources.IMAGE_BACKGROUND_23
            || image == ApiResources.CAMPFIRE_IMAGE_4
            || image == ApiResources.IMAGE_BACKGROUND_27
            || image == ApiResources.IMAGE_BACKGROUND_28
            || image == ApiResources.IMAGE_BACKGROUND_29
            || image == ApiResources.IMAGE_BACKGROUND_30
            || image == ApiResources.AVATAR_1
            || image == ApiResources.AVATAR_2
            || image == ApiResources.AVATAR_3
            || image == ApiResources.AVATAR_4
            || image == ApiResources.AVATAR_5
            || image == ApiResources.AVATAR_6
            || image == ApiResources.IMAGE_BACKGROUND_LEVEL_16
            || image == ApiResources.IMAGE_BACKGROUND_LEVEL_17
            || image == ApiResources.IMAGE_BACKGROUND_LEVEL_18
            || image == ApiResources.IMAGE_BACKGROUND_LEVEL_19
            || image == ApiResources.IMAGE_BACKGROUND_LEVEL_20
        ) return "NikiTank"

        if (image == ApiResources.DEVELOPER_ZEON) return "Zeon"
        if (image == ApiResources.DEVELOPER_EGOR) return "GeorgePro"
        if (image == ApiResources.DEVELOPER_SAYNOK) return "Saynok"
        if (image == ApiResources.DEVELOPER_TURBO) return "TurboA99"
        if (image == ApiResources.DEVELOPER_ZYMIXX) return "ZYMixx"

        return null;

    }

}
