package com.dzen.campfire.api

import com.dzen.campfire.api.models.images.ImageRef

object ApiResources {
    private fun staticImage(name: String) = ImageRef("${API.S3_ROOT}/static/$name")

    //  New Year
    val IMAGE_NEW_YEAR_DEER_1 = staticImage("IMAGE_NEW_YEAR_DEER_1")
    val IMAGE_NEW_YEAR_DEER_2 = staticImage("IMAGE_NEW_YEAR_DEER_2")
    val IMAGE_NEW_YEAR_DEER_3 = staticImage("IMAGE_NEW_YEAR_DEER_3")
    val IMAGE_NEW_YEAR_DEER_4 = staticImage("IMAGE_NEW_YEAR_DEER_4")
    val IMAGE_NEW_YEAR_DEER_5 = staticImage("IMAGE_NEW_YEAR_DEER_5")
    val IMAGE_NEW_YEAR_ELF_1 = staticImage("IMAGE_NEW_YEAR_ELF_1")
    val IMAGE_NEW_YEAR_ELF_2 = staticImage("IMAGE_NEW_YEAR_ELF_2")
    val IMAGE_NEW_YEAR_ELF_3 = staticImage("IMAGE_NEW_YEAR_ELF_3")
    val IMAGE_NEW_YEAR_ELF_4 = staticImage("IMAGE_NEW_YEAR_ELF_4")
    val IMAGE_NEW_YEAR_KID_1 = staticImage("IMAGE_NEW_YEAR_KID_1")
    val IMAGE_NEW_YEAR_KID_2 = staticImage("IMAGE_NEW_YEAR_KID_2")
    val IMAGE_NEW_YEAR_KID_3 = staticImage("IMAGE_NEW_YEAR_KID_3")
    val IMAGE_NEW_YEAR_KID_4 = staticImage("IMAGE_NEW_YEAR_KID_4")
    val IMAGE_NEW_YEAR_KID_5 = staticImage("IMAGE_NEW_YEAR_KID_5")
    val IMAGE_NEW_YEAR_KID_6 = staticImage("IMAGE_NEW_YEAR_KID_6")
    val IMAGE_NEW_YEAR_KID_7 = staticImage("IMAGE_NEW_YEAR_KID_7")
    val IMAGE_NEW_YEAR_KID_8 = staticImage("IMAGE_NEW_YEAR_KID_8")
    val IMAGE_NEW_YEAR_KID_9 = staticImage("IMAGE_NEW_YEAR_KID_9")
    val IMAGE_NEW_YEAR_KID_10 = staticImage("IMAGE_NEW_YEAR_KID_10")
    val IMAGE_NEW_YEAR_PIG = staticImage("IMAGE_NEW_YEAR_PIG")
    val IMAGE_NEW_YEAR_SANTA = staticImage("IMAGE_NEW_YEAR_SANTA")
    val IMAGE_NEW_YEAR_LIGHT_GIF = staticImage("IMAGE_NEW_YEAR_LIGHT_GIF")

    //  Achievements
    val IMAGE_ICHI_1_1 = staticImage("IMAGE_ICHI_1_1")
    val IMAGE_ICHI_1_2 = staticImage("IMAGE_ICHI_1_2")
    val IMAGE_ICHI_1_3 = staticImage("IMAGE_ICHI_1_3")
    val IMAGE_ICHI_1_4 = staticImage("IMAGE_ICHI_1_4")
    val IMAGE_ICHI_1_5 = staticImage("IMAGE_ICHI_1_5")
    val IMAGE_ICHI_2_1 = staticImage("IMAGE_ICHI_2_1")
    val IMAGE_ICHI_2_2 = staticImage("IMAGE_ICHI_2_2")
    val IMAGE_ICHI_2_3 = staticImage("IMAGE_ICHI_2_3")
    val IMAGE_ICHI_2_4 = staticImage("IMAGE_ICHI_2_4")
    val IMAGE_ICHI_3_1 = staticImage("IMAGE_ICHI_3_1")
    val IMAGE_ICHI_3_2 = staticImage("IMAGE_ICHI_3_2")
    val IMAGE_ICHI_3_3 = staticImage("IMAGE_ICHI_3_3")
    val IMAGE_ICHI_4_1 = staticImage("IMAGE_ICHI_4_1")
    val IMAGE_ICHI_4_2 = staticImage("IMAGE_ICHI_4_2")
    val IMAGE_ICHI_4_3 = staticImage("IMAGE_ICHI_4_3")
    val IMAGE_ICHI_4_4 = staticImage("IMAGE_ICHI_4_4")
    val IMAGE_ICHI_4_5 = staticImage("IMAGE_ICHI_4_5")
    val IMAGE_ICHI_5_1 = staticImage("IMAGE_ICHI_5_1")
    val IMAGE_ICHI_5_2 = staticImage("IMAGE_ICHI_5_2")
    val IMAGE_ICHI_5_3 = staticImage("IMAGE_ICHI_5_3")
    val IMAGE_ICHI_6 = staticImage("IMAGE_ICHI_6")
    val IMAGE_ICHI_7 = staticImage("IMAGE_ICHI_7")
    val IMAGE_ICHI_8 = staticImage("IMAGE_ICHI_8")
    val IMAGE_ICHI_9 = staticImage("IMAGE_ICHI_9")
    val IMAGE_ICHI_10 = staticImage("IMAGE_ICHI_10")
    val IMAGE_ICHI_11 = staticImage("IMAGE_ICHI_11")
    val IMAGE_ICHI_12 = staticImage("IMAGE_ICHI_12")
    val IMAGE_ICHI_13 = staticImage("IMAGE_ICHI_13")
    val IMAGE_ICHI_15_1 = staticImage("IMAGE_ICHI_15_1")
    val IMAGE_ICHI_15_2 = staticImage("IMAGE_ICHI_15_2")
    val IMAGE_ICHI_15_3 = staticImage("IMAGE_ICHI_15_3")
    val IMAGE_ICHI_16_1 = staticImage("IMAGE_ICHI_16_1")
    val IMAGE_ICHI_16_2 = staticImage("IMAGE_ICHI_16_2")
    val IMAGE_ICHI_16_3 = staticImage("IMAGE_ICHI_16_3")
    val IMAGE_ICHI_17 = staticImage("IMAGE_ICHI_17")
    val IMAGE_ICHI_18 = staticImage("IMAGE_ICHI_18")
    val IMAGE_ICHI_19_1 = staticImage("IMAGE_ICHI_19_1")
    val IMAGE_ICHI_19_2 = staticImage("IMAGE_ICHI_19_2")
    val IMAGE_ICHI_19_3 = staticImage("IMAGE_ICHI_19_3")
    val IMAGE_ICHI_20_1 = staticImage("IMAGE_ICHI_20_1")
    val IMAGE_ICHI_20_2 = staticImage("IMAGE_ICHI_20_2")
    val IMAGE_ICHI_20_3 = staticImage("IMAGE_ICHI_20_3")
    val IMAGE_ICHI_21 = staticImage("IMAGE_ICHI_21")
    val IMAGE_ICHI_22 = staticImage("IMAGE_ICHI_22")
    val IMAGE_ICHI_23 = staticImage("IMAGE_ICHI_23")
    val IMAGE_ICHI_24 = staticImage("IMAGE_ICHI_24")

    //  Background
    val IMAGE_BACKGROUND_1 = staticImage("IMAGE_BACKGROUND_1")
    val IMAGE_BACKGROUND_2 = staticImage("IMAGE_BACKGROUND_2")
    val IMAGE_BACKGROUND_3 = staticImage("IMAGE_BACKGROUND_3")
    val IMAGE_BACKGROUND_4 = staticImage("IMAGE_BACKGROUND_4")
    val IMAGE_BACKGROUND_5 = staticImage("IMAGE_BACKGROUND_5")
    val IMAGE_BACKGROUND_6 = staticImage("IMAGE_BACKGROUND_6")
    val IMAGE_BACKGROUND_7 = staticImage("IMAGE_BACKGROUND_7")
    val IMAGE_BACKGROUND_8 = staticImage("IMAGE_BACKGROUND_8")
    val IMAGE_BACKGROUND_9 = staticImage("IMAGE_BACKGROUND_9")
    val IMAGE_BACKGROUND_10 = staticImage("IMAGE_BACKGROUND_10")
    val IMAGE_BACKGROUND_11 = staticImage("IMAGE_BACKGROUND_11")
    val IMAGE_BACKGROUND_12 = staticImage("IMAGE_BACKGROUND_12")
    val IMAGE_BACKGROUND_13 = staticImage("IMAGE_BACKGROUND_13")
    val IMAGE_BACKGROUND_14 = staticImage("IMAGE_BACKGROUND_14")
    val IMAGE_BACKGROUND_15 = staticImage("IMAGE_BACKGROUND_15")
    val IMAGE_BACKGROUND_16 = staticImage("IMAGE_BACKGROUND_16")
    val IMAGE_BACKGROUND_17 = staticImage("IMAGE_BACKGROUND_17")
    val IMAGE_BACKGROUND_18 = staticImage("IMAGE_BACKGROUND_18")
    val IMAGE_BACKGROUND_19 = staticImage("IMAGE_BACKGROUND_19")
    val IMAGE_BACKGROUND_20 = staticImage("IMAGE_BACKGROUND_20")
    val IMAGE_BACKGROUND_21 = staticImage("IMAGE_BACKGROUND_21")
    val IMAGE_BACKGROUND_22 = staticImage("IMAGE_BACKGROUND_22")
    val IMAGE_BACKGROUND_23 = staticImage("IMAGE_BACKGROUND_23")
    val IMAGE_BACKGROUND_24 = staticImage("IMAGE_BACKGROUND_24")
    val IMAGE_BACKGROUND_25 = staticImage("IMAGE_BACKGROUND_25")
    val IMAGE_BACKGROUND_26 = staticImage("IMAGE_BACKGROUND_26")
    val IMAGE_BACKGROUND_27 = staticImage("IMAGE_BACKGROUND_27")
    val IMAGE_BACKGROUND_28 = staticImage("IMAGE_BACKGROUND_28")
    val IMAGE_BACKGROUND_29 = staticImage("IMAGE_BACKGROUND_29")
    val IMAGE_BACKGROUND_30 = staticImage("IMAGE_BACKGROUND_30")
    val IMAGE_BACKGROUND_31 = staticImage("IMAGE_BACKGROUND_31")

    //  Background Level
    val IMAGE_BACKGROUND_LEVEL_1 = staticImage("IMAGE_BACKGROUND_LEVEL_1")
    val IMAGE_BACKGROUND_LEVEL_2 = staticImage("IMAGE_BACKGROUND_LEVEL_2")
    val IMAGE_BACKGROUND_LEVEL_3 = staticImage("IMAGE_BACKGROUND_LEVEL_3")
    val IMAGE_BACKGROUND_LEVEL_4 = staticImage("IMAGE_BACKGROUND_LEVEL_4")
    val IMAGE_BACKGROUND_LEVEL_5 = staticImage("IMAGE_BACKGROUND_LEVEL_5")
    val IMAGE_BACKGROUND_LEVEL_6 = staticImage("IMAGE_BACKGROUND_LEVEL_6")
    val IMAGE_BACKGROUND_LEVEL_7 = staticImage("IMAGE_BACKGROUND_LEVEL_7")
    val IMAGE_BACKGROUND_LEVEL_8 = staticImage("IMAGE_BACKGROUND_LEVEL_8")
    val IMAGE_BACKGROUND_LEVEL_9 = staticImage("IMAGE_BACKGROUND_LEVEL_9")
    val IMAGE_BACKGROUND_LEVEL_10 = staticImage("IMAGE_BACKGROUND_LEVEL_10")
    val IMAGE_BACKGROUND_LEVEL_11 = staticImage("IMAGE_BACKGROUND_LEVEL_11")
    val IMAGE_BACKGROUND_LEVEL_12 = staticImage("IMAGE_BACKGROUND_LEVEL_12")
    val IMAGE_BACKGROUND_LEVEL_13 = staticImage("IMAGE_BACKGROUND_LEVEL_13")
    val IMAGE_BACKGROUND_LEVEL_14 = staticImage("IMAGE_BACKGROUND_LEVEL_14")
    val IMAGE_BACKGROUND_LEVEL_15 = staticImage("IMAGE_BACKGROUND_LEVEL_15")
    val IMAGE_BACKGROUND_LEVEL_16 = staticImage("IMAGE_BACKGROUND_LEVEL_16")
    val IMAGE_BACKGROUND_LEVEL_17 = staticImage("IMAGE_BACKGROUND_LEVEL_17")
    val IMAGE_BACKGROUND_LEVEL_18 = staticImage("IMAGE_BACKGROUND_LEVEL_18")
    val IMAGE_BACKGROUND_LEVEL_19 = staticImage("IMAGE_BACKGROUND_LEVEL_19")
    val IMAGE_BACKGROUND_LEVEL_20 = staticImage("IMAGE_BACKGROUND_LEVEL_20")

    //  Emoji
    val EMOJI_1 = staticImage("EMOJI_1")
    val EMOJI_2 = staticImage("EMOJI_2")
    val EMOJI_3 = staticImage("EMOJI_3")
    val EMOJI_4 = staticImage("EMOJI_4")
    val EMOJI_5 = staticImage("EMOJI_5")
    val EMOJI_6 = staticImage("EMOJI_6")
    val EMOJI_7 = staticImage("EMOJI_7")

    //  Campfire images
    val CAMPFIRE_IMAGE_1 = staticImage("CAMPFIRE_IMAGE_1")
    val CAMPFIRE_IMAGE_2 = staticImage("CAMPFIRE_IMAGE_2")
    val CAMPFIRE_IMAGE_3 = staticImage("CAMPFIRE_IMAGE_3")
    val CAMPFIRE_IMAGE_4 = staticImage("CAMPFIRE_IMAGE_4")

    //  Avatars
    val AVATAR_1 = staticImage("AVATAR_1")
    val AVATAR_2 = staticImage("AVATAR_2")
    val AVATAR_3 = staticImage("AVATAR_3")
    val AVATAR_4 = staticImage("AVATAR_4")
    val AVATAR_5 = staticImage("AVATAR_5")
    val AVATAR_6 = staticImage("AVATAR_6")

    //  Flags
    val FLAG_DE = staticImage("FLAG_DE")
    val FLAG_EN = staticImage("FLAG_EN")
    val FLAG_FR = staticImage("FLAG_FR")
    val FLAG_IT = staticImage("FLAG_IT")
    val FLAG_PL = staticImage("FLAG_PL")
    val FLAG_PT = staticImage("FLAG_PT")
    val FLAG_RU = staticImage("FLAG_RU")
    val FLAG_UK = staticImage("FLAG_UK")
    val FLAG_WORLD = staticImage("FLAG_WORLD")

    //  Icons
    val ICON_ANIME_BLACK = staticImage("ICON_ANIME_BLACK")
    val ICON_ANIME_WHITE = staticImage("ICON_ANIME_WHITE")
    val ICON_APPSTORE_BLACK = staticImage("ICON_APPSTORE_BLACK")
    val ICON_APPSTORE_WHITE = staticImage("ICON_APPSTORE_WHITE")
    val ICON_BANK_CARD = staticImage("ICON_BANK_CARD")
    val ICON_CAMPFIRE = staticImage("ICON_CAMPFIRE")
    val ICON_DISCORD_BLACK = staticImage("ICON_DISCORD_BLACK")
    val ICON_DISCORD_WHITE = staticImage("ICON_DISCORD_WHITE")
    val ICON_GAMES_BLACK = staticImage("ICON_GAMES_BLACK")
    val ICON_GAMES_WHITE = staticImage("ICON_GAMES_WHITE")
    val ICON_GOOGLE_PLAY_BLACK = staticImage("ICON_GOOGLE_PLAY_BLACK")
    val ICON_GOOGLE_PLAY_WHITE = staticImage("ICON_GOOGLE_PLAY_WHITE")
    val ICON_PHONE = staticImage("ICON_PHONE")
    val ICON_STEAM_BLACK = staticImage("ICON_STEAM_BLACK")
    val ICON_STEAM_WHITE = staticImage("ICON_STEAM_WHITE")
    val ICON_TWITTER_BLACK = staticImage("ICON_TWITTER_BLACK")
    val ICON_TWITTER_WHITE = staticImage("ICON_TWITTER_WHITE")
    val ICON_VKONTAKTE_BLACK = staticImage("ICON_VKONTAKTE_BLACK")
    val ICON_VKONTAKTE_WHITE = staticImage("ICON_VKONTAKTE_WHITE")
    val ICON_WIKI_BLACK = staticImage("ICON_WIKI_BLACK")
    val ICON_WIKI_WHITE = staticImage("ICON_WIKI_WHITE")
    val ICON_YANDEX_DENGI = staticImage("ICON_YANDEX_DENGI")
    val ICON_YOUTUBE_BLACK = staticImage("ICON_YOUTUBE_BLACK")
    val ICON_YOUTUBE_WHITE = staticImage("ICON_YOUTUBE_WHITE")

    //  Developers
    val DEVELOPER_SIT = staticImage("DEVELOPER_SIT")
    val DEVELOPER_NIKI = staticImage("DEVELOPER_NIKI")
    val DEVELOPER_ZEON = staticImage("DEVELOPER_ZEON")
    val DEVELOPER_EGOR = staticImage("DEVELOPER_EGOR")
    val DEVELOPER_SAYNOK = staticImage("DEVELOPER_SAYNOK")
    val DEVELOPER_TURBO = staticImage("DEVELOPER_TURBO")
    val DEVELOPER_ZYMIXX = staticImage("DEVELOPER_ZYMIXX")

    // Epic Quest
    val QUEST_NEW_YEAR_1 = staticImage("QUEST_NEW_YEAR_1")
    val QUEST_NEW_YEAR_2 = staticImage("QUEST_NEW_YEAR_2")
    val QUEST_NEW_YEAR_3 = staticImage("QUEST_NEW_YEAR_3")
    val QUEST_NEW_YEAR_4 = staticImage("QUEST_NEW_YEAR_4")
    val QUEST_NEW_YEAR_5 = staticImage("QUEST_NEW_YEAR_5")
    val QUEST_NEW_YEAR_6 = staticImage("QUEST_NEW_YEAR_6")
    val QUEST_NEW_YEAR_7 = staticImage("QUEST_NEW_YEAR_7")
    val QUEST_NEW_YEAR_8 = staticImage("QUEST_NEW_YEAR_8")
    val QUEST_NEW_YEAR_9 = staticImage("QUEST_NEW_YEAR_9")
    val QUEST_NEW_YEAR_10 = staticImage("QUEST_NEW_YEAR_10")

    val QUEST_TANK_1 = staticImage("QUEST_TANK_1")
    val QUEST_TANK_2 = staticImage("QUEST_TANK_2")
    val QUEST_TANK_3 = staticImage("QUEST_TANK_3")
    val QUEST_TANK_4 = staticImage("QUEST_TANK_4")
    val QUEST_TANK_5 = staticImage("QUEST_TANK_5")
    val QUEST_TANK_6 = staticImage("QUEST_TANK_6")
    val QUEST_TANK_7 = staticImage("QUEST_TANK_7")
    val QUEST_TANK_8 = staticImage("QUEST_TANK_8")
    val QUEST_TANK_9 = staticImage("QUEST_TANK_9")
    val QUEST_TANK_10 = staticImage("QUEST_TANK_10")
    val QUEST_TANK_11 = staticImage("QUEST_TANK_11")
    val QUEST_TANK_12 = staticImage("QUEST_TANK_12")
    val QUEST_TANK_13 = staticImage("QUEST_TANK_13")
    val QUEST_TANK_14 = staticImage("QUEST_TANK_14")
    val QUEST_TANK_15 = staticImage("QUEST_TANK_15")
    val QUEST_TANK_16 = staticImage("QUEST_TANK_16")
    val QUEST_TANK_17 = staticImage("QUEST_TANK_17")
    val QUEST_TANK_18 = staticImage("QUEST_TANK_18")
    val QUEST_TANK_19 = staticImage("QUEST_TANK_19")
    val QUEST_TANK_20 = staticImage("QUEST_TANK_20")
    val QUEST_TANK_21 = staticImage("QUEST_TANK_21")
    val QUEST_TANK_22 = staticImage("QUEST_TANK_22")
    val QUEST_TANK_23 = staticImage("QUEST_TANK_23")


    // == Lists ==

    val ARRAY_ACHI = arrayOf(
        IMAGE_ICHI_1_1,
        IMAGE_ICHI_1_2,
        IMAGE_ICHI_1_3,
        IMAGE_ICHI_1_4,
        IMAGE_ICHI_1_5,
        IMAGE_ICHI_2_1,
        IMAGE_ICHI_2_2,
        IMAGE_ICHI_2_3,
        IMAGE_ICHI_2_4,
        IMAGE_ICHI_3_1,
        IMAGE_ICHI_3_2,
        IMAGE_ICHI_3_3,
        IMAGE_ICHI_4_1,
        IMAGE_ICHI_4_2,
        IMAGE_ICHI_4_3,
        IMAGE_ICHI_4_4,
        IMAGE_ICHI_4_5,
        IMAGE_ICHI_5_1,
        IMAGE_ICHI_5_2,
        IMAGE_ICHI_5_3,
        IMAGE_ICHI_6,
        IMAGE_ICHI_7,
        IMAGE_ICHI_8,
        IMAGE_ICHI_9,
        IMAGE_ICHI_10,
        IMAGE_ICHI_11,
        IMAGE_ICHI_12,
        IMAGE_ICHI_13,
        IMAGE_ICHI_15_1,
        IMAGE_ICHI_15_2,
        IMAGE_ICHI_15_3,
        IMAGE_ICHI_16_1,
        IMAGE_ICHI_16_2,
        IMAGE_ICHI_16_3,
        IMAGE_ICHI_17,
        IMAGE_ICHI_18,
        IMAGE_ICHI_19_1,
        IMAGE_ICHI_19_2,
        IMAGE_ICHI_19_3,
        IMAGE_ICHI_20_1,
        IMAGE_ICHI_20_2,
        IMAGE_ICHI_20_3,
        IMAGE_ICHI_21,
        IMAGE_ICHI_22,
        IMAGE_ICHI_23,
        IMAGE_ICHI_24
    )

    val ARRAY_NEW_YEAR = arrayOf(
        IMAGE_NEW_YEAR_DEER_1,
        IMAGE_NEW_YEAR_DEER_2,
        IMAGE_NEW_YEAR_DEER_3,
        IMAGE_NEW_YEAR_DEER_4,
        IMAGE_NEW_YEAR_DEER_5,
        IMAGE_NEW_YEAR_ELF_1,
        IMAGE_NEW_YEAR_ELF_2,
        IMAGE_NEW_YEAR_ELF_3,
        IMAGE_NEW_YEAR_ELF_4,
        IMAGE_NEW_YEAR_KID_1,
        IMAGE_NEW_YEAR_KID_2,
        IMAGE_NEW_YEAR_KID_3,
        IMAGE_NEW_YEAR_KID_4,
        IMAGE_NEW_YEAR_KID_5,
        IMAGE_NEW_YEAR_KID_6,
        IMAGE_NEW_YEAR_KID_7,
        IMAGE_NEW_YEAR_KID_8,
        IMAGE_NEW_YEAR_KID_9,
        IMAGE_NEW_YEAR_KID_10,
        IMAGE_NEW_YEAR_PIG,
        IMAGE_NEW_YEAR_SANTA,
        IMAGE_NEW_YEAR_LIGHT_GIF
    )

    val ARRAY_BACKGROUND = arrayOf(
        IMAGE_BACKGROUND_1,
        IMAGE_BACKGROUND_2,
        IMAGE_BACKGROUND_3,
        IMAGE_BACKGROUND_4,
        IMAGE_BACKGROUND_5,
        IMAGE_BACKGROUND_6,
        IMAGE_BACKGROUND_7,
        IMAGE_BACKGROUND_8,
        IMAGE_BACKGROUND_9,
        IMAGE_BACKGROUND_10,
        IMAGE_BACKGROUND_11,
        IMAGE_BACKGROUND_12,
        IMAGE_BACKGROUND_13,
        IMAGE_BACKGROUND_14,
        IMAGE_BACKGROUND_15,
        IMAGE_BACKGROUND_16,
        IMAGE_BACKGROUND_17,
        IMAGE_BACKGROUND_18,
        IMAGE_BACKGROUND_19,
        IMAGE_BACKGROUND_20,
        IMAGE_BACKGROUND_21,
        IMAGE_BACKGROUND_22,
        IMAGE_BACKGROUND_23,
        IMAGE_BACKGROUND_24,
        IMAGE_BACKGROUND_25,
        IMAGE_BACKGROUND_26,
        IMAGE_BACKGROUND_27,
        IMAGE_BACKGROUND_28,
        IMAGE_BACKGROUND_29,
        IMAGE_BACKGROUND_30,
        IMAGE_BACKGROUND_31,
    )

    val ARRAY_BACKGROUND_LEVEL = arrayOf(
        IMAGE_BACKGROUND_LEVEL_1,
        IMAGE_BACKGROUND_LEVEL_2,
        IMAGE_BACKGROUND_LEVEL_3,
        IMAGE_BACKGROUND_LEVEL_4,
        IMAGE_BACKGROUND_LEVEL_5,
        IMAGE_BACKGROUND_LEVEL_6,
        IMAGE_BACKGROUND_LEVEL_7,
        IMAGE_BACKGROUND_LEVEL_8,
        IMAGE_BACKGROUND_LEVEL_9,
        IMAGE_BACKGROUND_LEVEL_10,
        IMAGE_BACKGROUND_LEVEL_11,
        IMAGE_BACKGROUND_LEVEL_12,
        IMAGE_BACKGROUND_LEVEL_13,
        IMAGE_BACKGROUND_LEVEL_14,
        IMAGE_BACKGROUND_LEVEL_15,
        IMAGE_BACKGROUND_LEVEL_16,
        IMAGE_BACKGROUND_LEVEL_17,
        IMAGE_BACKGROUND_LEVEL_18,
        IMAGE_BACKGROUND_LEVEL_19,
        IMAGE_BACKGROUND_LEVEL_20,
    )

    val ARRAY_EMOJI = arrayOf(
        EMOJI_1,
        EMOJI_2,
        EMOJI_3,
        EMOJI_4,
        EMOJI_5,
        EMOJI_6,
        EMOJI_7
    )

    val ARRAY_CAMPFIRE_IMAGE = arrayOf(
        CAMPFIRE_IMAGE_1,
        CAMPFIRE_IMAGE_2,
        CAMPFIRE_IMAGE_3,
        CAMPFIRE_IMAGE_4
    )

    val ARRAY_AVATAR = arrayOf(
        AVATAR_1,
        AVATAR_2,
        AVATAR_3,
        AVATAR_4,
        AVATAR_5,
        AVATAR_6
    )

    val ARRAY_FLAG = arrayOf(
        FLAG_DE,
        FLAG_EN,
        FLAG_FR,
        FLAG_IT,
        FLAG_PL,
        FLAG_PT,
        FLAG_RU,
        FLAG_UK,
        FLAG_WORLD
    )

    val ARRAY_ICON = arrayOf(
        ICON_ANIME_BLACK,
        ICON_ANIME_WHITE,
        ICON_APPSTORE_BLACK,
        ICON_APPSTORE_WHITE,
        ICON_BANK_CARD,
        ICON_CAMPFIRE,
        ICON_DISCORD_BLACK,
        ICON_DISCORD_WHITE,
        ICON_GAMES_BLACK,
        ICON_GAMES_WHITE,
        ICON_GOOGLE_PLAY_BLACK,
        ICON_GOOGLE_PLAY_WHITE,
        ICON_PHONE,
        ICON_STEAM_BLACK,
        ICON_STEAM_WHITE,
        ICON_TWITTER_BLACK,
        ICON_TWITTER_WHITE,
        ICON_VKONTAKTE_BLACK,
        ICON_VKONTAKTE_WHITE,
        ICON_WIKI_BLACK,
        ICON_WIKI_WHITE,
        ICON_YANDEX_DENGI,
        ICON_YOUTUBE_BLACK,
        ICON_YOUTUBE_WHITE
    )

    val ARRAY_DEVELOPER = arrayOf(
        DEVELOPER_ZEON,
        DEVELOPER_EGOR,
        DEVELOPER_SAYNOK,
        DEVELOPER_TURBO,
        DEVELOPER_ZYMIXX
    )

    val ARRAY_QUEST_NEW_YEAR = arrayOf(
        QUEST_NEW_YEAR_1,
        QUEST_NEW_YEAR_2,
        QUEST_NEW_YEAR_3,
        QUEST_NEW_YEAR_4,
        QUEST_NEW_YEAR_5,
        QUEST_NEW_YEAR_6,
        QUEST_NEW_YEAR_7,
        QUEST_NEW_YEAR_8,
        QUEST_NEW_YEAR_9,
        QUEST_NEW_YEAR_10
    )
}
