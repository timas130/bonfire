package com.sayzen.campfiresdk.screens.other.gallery

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_RESOURCES
import com.dzen.campfire.api.API_TRANSLATE
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

    private val array_bg = cardsOf(API_RESOURCES.ARRAY_BACKGROUND)
    private val array_newYear = cardsOf(API_RESOURCES.ARRAY_NEW_YEAR)
    private val array_achi = cardsOf(API_RESOURCES.ARRAY_ACHI)
    private val array_level = cardsOf(API_RESOURCES.ARRAY_BACKGROUND_LEVEL)
    private val array_emoji = cardsOf(API_RESOURCES.ARRAY_EMOJI)
    private val array_other = cardsOf(ToolsCollections.merge(API_RESOURCES.ARRAY_CAMPFIRE_IMAGE, API_RESOURCES.ARRAY_AVATAR))
    private val array_flags = cardsOf(API_RESOURCES.ARRAY_FLAG)
    private val array_icons = cardsOf(API_RESOURCES.ARRAY_ICON)
    private val array_developers = cardsOf(API_RESOURCES.ARRAY_DEVELOPER)
    private val array_newyearQuest = cardsOf(API_RESOURCES.ARRAY_QUEST_NEW_YEAR)
    private val array_all = arrayOf(*array_bg, *array_level, *array_achi, *array_newYear, *array_emoji, *array_other, *array_flags, *array_icons, *array_developers, *array_newyearQuest)

    init {
        disableNavigation()
        setTitle(t(API_TRANSLATE.app_gallery))

        vCopyLink.setOnClickListener {
            ToolsAndroid.setToClipboard(API.LINK_RULES_GALLERY.asWeb())
            ToolsToast.show(t(API_TRANSLATE.app_copied))
        }

        vRecycler.layoutManager = LinearLayoutManager(context)
        vRecycler.adapter = adapter

        fill(t(API_TRANSLATE.gallery_achi_title),t(API_TRANSLATE.gallery_achi_text), array_achi, 5)
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
        adapter.add(CardTitle(title,text))
        for (i in array.indices step count) adapter.add(CardContainer(this, i, array, count))
    }

    fun getAllImages() = array_all

    private fun cardsOf(array: Array<Long>) = Array(array.size) { CardImage(this, array[it], getLabel(array[it])) }

    private fun getLabel(imageId: Long): String? {

        if (imageId == API_RESOURCES.IMAGE_BACKGROUND_26
                || imageId == API_RESOURCES.IMAGE_BACKGROUND_23
                || imageId == API_RESOURCES.CAMPFIRE_IMAGE_4
                || imageId == API_RESOURCES.IMAGE_BACKGROUND_27
                || imageId == API_RESOURCES.IMAGE_BACKGROUND_28
                || imageId == API_RESOURCES.IMAGE_BACKGROUND_29
                || imageId == API_RESOURCES.IMAGE_BACKGROUND_30
                || imageId == API_RESOURCES.AVATAR_1
                || imageId == API_RESOURCES.AVATAR_2
                || imageId == API_RESOURCES.AVATAR_3
                || imageId == API_RESOURCES.AVATAR_4
                || imageId == API_RESOURCES.AVATAR_5
                || imageId == API_RESOURCES.AVATAR_6
                || imageId == API_RESOURCES.IMAGE_BACKGROUND_LEVEL_16
                || imageId == API_RESOURCES.IMAGE_BACKGROUND_LEVEL_17
                || imageId == API_RESOURCES.IMAGE_BACKGROUND_LEVEL_18
                || imageId == API_RESOURCES.IMAGE_BACKGROUND_LEVEL_19
                || imageId == API_RESOURCES.IMAGE_BACKGROUND_LEVEL_20
        ) return "NikiTank"

        if (imageId == API_RESOURCES.DEVELOPER_ZEON) return "Zeon"
        if (imageId == API_RESOURCES.DEVELOPER_EGOR) return "GeorgePro"
        if (imageId == API_RESOURCES.DEVELOPER_SAYNOK) return "Saynok"
        if (imageId == API_RESOURCES.DEVELOPER_TURBO) return "TurboA99"
        if (imageId == API_RESOURCES.DEVELOPER_ZYMIXX) return "ZYMixx"

        return null;

    }

}