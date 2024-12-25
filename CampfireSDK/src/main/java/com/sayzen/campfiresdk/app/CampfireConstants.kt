package com.sayzen.campfiresdk.app

import com.dzen.campfire.api.API
import com.dzen.campfire.api.API_TRANSLATE
import com.dzen.campfire.api.ApiResources
import com.dzen.campfire.api.models.AchievementInfo
import com.dzen.campfire.api.models.images.ImageRef
import com.sayzen.campfiresdk.R
import com.sayzen.campfiresdk.controllers.t
import com.sayzen.campfiresdk.models.objects.*
import com.sup.dev.java.tools.ToolsText

object CampfireConstants {

    const val GREEN = "388E3C"
    const val YELLOW = "FBC02D"
    const val RED = "D32F2F"

    const val RATE_TIME = 2000L
    const val VOTE_TIME = 2000L

    const val CHECK_RULES_ACCEPTED = "CHECK_RULES_ACCEPTED"

    private val RULES_USER_BODIES = API_TRANSLATE.getAllSame(Regex("^rules_users_\\d+$"))
    private val RULES_USER_TITLES = API_TRANSLATE.getAllSame(Regex("^rules_users_\\d+_title$"))
    val RULES_USER = Array(RULES_USER_TITLES.size) {
        return@Array Rule(RULES_USER_TITLES[it], RULES_USER_BODIES[it])
    }
    val RULES_MODER = arrayOf(
        API_TRANSLATE.rules_moderators_1,
        API_TRANSLATE.rules_moderators_2,
        API_TRANSLATE.rules_moderators_3,
        API_TRANSLATE.rules_moderators_4,
        API_TRANSLATE.rules_moderators_5,
    )

    val TEXT_ICONS = arrayOf(
        0/*Stub wikiTitle*/,
        R.drawable.ic_arrow_back_white_24dp,
        R.drawable.ic_menu_white_24dp,
        R.drawable.ic_keyboard_arrow_right_white_24dp,
        R.drawable.ic_keyboard_arrow_left_white_24dp,
        R.drawable.ic_file_download_white_24dp,
        R.drawable.ic_share_white_24dp,
        R.drawable.ic_keyboard_arrow_up_white_24dp,
        R.drawable.ic_keyboard_arrow_down_white_24dp,
        R.drawable.ic_content_copy_white_24dp,
        R.drawable.ic_folder_white_24dp,
        R.drawable.ic_insert_drive_file_white_24dp,
        R.drawable.ic_mic_white_24dp,
        R.drawable.ic_clear_white_24dp,
        R.drawable.ic_lock_white_24dp,
        R.drawable.ic_access_time_white_24dp,
        R.drawable.ic_account_balance_white_24dp,
        R.drawable.ic_account_box_white_24dp,
        R.drawable.ic_account_circle_white_24dp,
        R.drawable.ic_add_white_24dp,
        R.drawable.ic_alarm_white_24dp,
        R.drawable.ic_all_inclusive_white_24dp,
        R.drawable.ic_attach_file_white_24dp,
        R.drawable.ic_bookmark_white_24dp,
        R.drawable.ic_brush_white_24dp,
        R.drawable.ic_burst_mode_white_24dp,
        R.drawable.ic_cached_white_24dp,
        R.drawable.ic_check_box_white_24dp,
        R.drawable.minus,
        R.drawable.ic_code_white_24dp,
        R.drawable.ic_done_all_white_24dp,
        R.drawable.ic_done_white_24dp,
        R.drawable.ic_email_white_24dp,
        R.drawable.ic_exit_to_app_white_24dp,
        R.drawable.ic_favorite_white_24dp,
        R.drawable.ic_format_quote_white_24dp,
        R.drawable.ic_gavel_white_24dp,
        R.drawable.ic_group_white_24dp,
        R.drawable.ic_help_white_24dp,
        R.drawable.ic_info_outline_white_24dp,
        R.drawable.ic_insert_link_white_24dp,
        R.drawable.ic_insert_link_white_36dp,
        R.drawable.ic_insert_photo_white_24dp,
        R.drawable.ic_keyboard_arrow_down_white_24dp,
        R.drawable.ic_keyboard_arrow_up_white_24dp,
        R.drawable.ic_landscape_white_24dp,
        R.drawable.ic_language_white_24dp,
        R.drawable.ic_mode_comment_white_24dp,
        R.drawable.ic_mode_edit_white_24dp,
        R.drawable.ic_more_vert_white_24dp,
        R.drawable.ic_notifications_white_24dp,
        R.drawable.ic_person_white_24dp,
        R.drawable.ic_play_arrow_white_24dp,
        R.drawable.ic_reply_white_24dp,
        R.drawable.ic_rowing_white_24dp,
        R.drawable.ic_search_white_24dp,
        R.drawable.ic_security_white_24dp,
        R.drawable.ic_send_white_24dp,
        R.drawable.ic_settings_white_24dp,
        R.drawable.ic_share_white_24dp,
        R.drawable.ic_star_border_white_24dp,
        R.drawable.ic_star_white_24dp,
        R.drawable.ic_text_fields_white_24dp,
        R.drawable.ic_thumbs_up_down_white_24dp,
        R.drawable.ic_translate_white_24dp,
        R.drawable.ic_trending_flat_white_24dp,
        R.drawable.ic_trending_up_white_24dp,
        R.drawable.ic_tune_white_24dp,
        R.drawable.ic_widgets_white_24dp,
        R.drawable.ic_book_white_24dp,
        R.drawable.ic_pets_white_24dp,
        R.drawable.ic_directions_bike_white_24dp,
        R.drawable.ic_border_all_white_24dp,
        R.drawable.ic_border_left_white_24dp,
        R.drawable.ic_border_top_white_24dp,
        R.drawable.ic_border_right_white_24dp,
        R.drawable.ic_border_bottom_white_24dp,
        R.drawable.ic_not_interested_white_24dp,
        R.drawable.ic_wb_incandescent_white_24dp,
        R.drawable.ic_whatshot_white_24dp
    )

    val ACHIEVEMENTS = arrayOf(
        Achievement(API.ACHI_APP_SHARE, API_TRANSLATE.achi_share, R.color.red_500, true, ApiResources.IMAGE_ICHI_3_2),
        Achievement(
            API.ACHI_CONTENT_SHARE,
            API_TRANSLATE.achi_content_share,
            R.color.pink_500,
            false,
            ApiResources.IMAGE_ICHI_3_3
        ),
        Achievement(
            API.ACHI_ADD_RECRUITER,
            API_TRANSLATE.achi_add_recruiter,
            R.color.green_500,
            true,
            ApiResources.IMAGE_ICHI_1_2
        ),
        Achievement(
            API.ACHI_ENTERS,
            API_TRANSLATE.achi_enters,
            R.color.deep_purple_500,
            false,
            ApiResources.IMAGE_ICHI_15_2
        ),
        Achievement(
            API.ACHI_KARMA_COUNT,
            API_TRANSLATE.achi_karma_count,
            R.color.indigo_500,
            false,
            ApiResources.IMAGE_ICHI_20_2
        ),
        Achievement(
            API.ACHI_REFERRALS_COUNT,
            API_TRANSLATE.achi_referals_count,
            R.color.blue_500,
            false,
            ApiResources.IMAGE_ICHI_1_3
        ),
        Achievement(
            API.ACHI_RATES_COUNT,
            API_TRANSLATE.achi_rates_count,
            R.color.light_blue_500,
            false,
            ApiResources.IMAGE_ICHI_10
        ),
        Achievement(
            API.ACHI_POSTS_COUNT,
            API_TRANSLATE.achi_posts_count,
            R.color.cyan_900,
            false,
            ApiResources.IMAGE_ICHI_2_2
        ),
        Achievement(
            API.ACHI_POST_KARMA,
            API_TRANSLATE.achi_posts_karma_count,
            R.color.teal_500,
            false,
            ApiResources.IMAGE_ICHI_5_2
        ),
        Achievement(
            API.ACHI_COMMENTS_KARMA,
            API_TRANSLATE.achi_comments_karma_count,
            R.color.light_green_900,
            false,
            ApiResources.IMAGE_ICHI_4_1
        ),
        Achievement(
            API.ACHI_STICKERS_KARMA,
            API_TRANSLATE.achi_stickers_karma_count,
            R.color.lime_900,
            false,
            ApiResources.IMAGE_ICHI_4_2
        ),
        Achievement(
            API.ACHI_COMMENTS_COUNT,
            API_TRANSLATE.achi_comments_count,
            R.color.orange_500,
            false,
            ApiResources.IMAGE_ICHI_4_4
        ),
        Achievement(API.ACHI_LOGIN, API_TRANSLATE.achi_login, R.color.red_500, true, ApiResources.IMAGE_ICHI_17),
        Achievement(API.ACHI_CHAT, API_TRANSLATE.achi_chat, R.color.blue_500, false, ApiResources.IMAGE_ICHI_3_1),
        Achievement(
            API.ACHI_COMMENT,
            API_TRANSLATE.achi_comment,
            R.color.pink_500,
            false,
            ApiResources.IMAGE_ICHI_4_2
        ),
        Achievement(API.ACHI_ANSWER, API_TRANSLATE.achi_answer, R.color.teal_500, false, ApiResources.IMAGE_ICHI_4_3),
        Achievement(
            API.ACHI_RATE,
            API_TRANSLATE.achi_rate,
            R.color.light_blue_500,
            false,
            ApiResources.IMAGE_ICHI_1_5
        ),
        Achievement(
            API.ACHI_CHANGE_PUBLICATION,
            API_TRANSLATE.achi_change_publication,
            R.color.deep_purple_500,
            false,
            ApiResources.IMAGE_ICHI_2_3
        ),
        Achievement(
            API.ACHI_CHANGE_COMMENT,
            API_TRANSLATE.achi_change_comment,
            R.color.indigo_500,
            false,
            ApiResources.IMAGE_ICHI_4_5
        ),
        Achievement(
            API.ACHI_FIRST_POST,
            API_TRANSLATE.achi_first_post,
            R.color.cyan_900,
            false,
            ApiResources.IMAGE_ICHI_2_2
        ),
        Achievement(
            API.ACHI_SUBSCRIBE,
            API_TRANSLATE.achi_first_follow,
            R.color.teal_500,
            false,
            ApiResources.IMAGE_ICHI_5_1
        ),
        Achievement(
            API.ACHI_TAGS_SEARCH,
            API_TRANSLATE.achi_tags_search,
            R.color.pink_500,
            false,
            ApiResources.IMAGE_ICHI_9
        ),
        Achievement(
            API.ACHI_LANGUAGE,
            API_TRANSLATE.achi_language,
            R.color.cyan_900,
            false,
            ApiResources.IMAGE_ICHI_24
        ),
        Achievement(
            API.ACHI_TITLE_IMAGE,
            API_TRANSLATE.achi_title_image,
            R.color.blue_500,
            false,
            ApiResources.IMAGE_ICHI_8,
            arrayOf(ToolsText.numToStringRound(API.LVL_CAN_CHANGE_PROFILE_IMAGE.lvl / 100.0, 2))
        ),
        Achievement(
            API.ACHI_CREATE_TAG,
            API_TRANSLATE.achi_create_tag,
            R.color.teal_500,
            false,
            ApiResources.IMAGE_ICHI_12
        ),
        Achievement(API.ACHI_QUESTS, API_TRANSLATE.achi_quests, R.color.teal_500, false, ApiResources.IMAGE_ICHI_6),
        Achievement(
            API.ACHI_FANDOMS,
            API_TRANSLATE.achi_fandoms,
            R.color.orange_500,
            true,
            ApiResources.IMAGE_ICHI_19_1
        ),
        Achievement(
            API.ACHI_RULES_USER,
            API_TRANSLATE.achi_rules_user,
            R.color.teal_500,
            true,
            ApiResources.IMAGE_ICHI_21
        ),
        Achievement(
            API.ACHI_RULES_MODERATOR,
            API_TRANSLATE.achi_rules_moderator,
            R.color.cyan_900,
            true,
            ApiResources.IMAGE_ICHI_21
        ),
        Achievement(
            API.ACHI_FOLLOWERS,
            API_TRANSLATE.achi_followers,
            R.color.red_500,
            false,
            ApiResources.IMAGE_ICHI_5_3
        ),
        Achievement(
            API.ACHI_MODER_CHANGE_POST_TAGS,
            API_TRANSLATE.achi_moderators_tags,
            R.color.indigo_500,
            false,
            ApiResources.IMAGE_ICHI_5_3
        ),
        Achievement(API.ACHI_FIREWORKS, API_TRANSLATE.achi_50, R.color.indigo_500, false, ApiResources.IMAGE_ICHI_23),
        Achievement(API.ACHI_MAKE_MODER, API_TRANSLATE.achi_51, R.color.orange_500, false, ApiResources.IMAGE_ICHI_21),
        Achievement(API.ACHI_CREATE_CHAT, API_TRANSLATE.achi_52, R.color.pink_500, false, ApiResources.IMAGE_ICHI_13),
        Achievement(
            API.ACHI_REVIEW_MODER_ACTION,
            API_TRANSLATE.achi_53,
            R.color.cyan_900,
            false,
            ApiResources.IMAGE_ICHI_21
        ),
        Achievement(
            API.ACHI_ACCEPT_FANDOM,
            API_TRANSLATE.achi_54,
            R.color.teal_500,
            false,
            ApiResources.IMAGE_ICHI_21
        ),
        Achievement(
            API.ACHI_MODERATOR_COUNT,
            API_TRANSLATE.achi_55,
            R.color.teal_500,
            false,
            ApiResources.IMAGE_ICHI_21
        ),
        Achievement(
            API.ACHI_MODERATOR_ACTION_KARMA,
            API_TRANSLATE.achi_56,
            R.color.light_blue_500,
            false,
            ApiResources.IMAGE_ICHI_21
        ),
        Achievement(
            API.ACHI_KARMA_30,
            API_TRANSLATE.achi_57,
            R.color.deep_purple_500,
            false,
            ApiResources.IMAGE_ICHI_21
        ),
        Achievement(API.ACHI_UP_RATES, API_TRANSLATE.achi_58, R.color.indigo_500, false, ApiResources.IMAGE_ICHI_21),
        Achievement(
            API.ACHI_UP_RATES_OVER_DOWN,
            API_TRANSLATE.achi_59,
            R.color.cyan_900,
            false,
            ApiResources.IMAGE_ICHI_21
        ),
        Achievement(
            API.ACHI_CHAT_SUBSCRIBE,
            API_TRANSLATE.achi_60,
            R.color.teal_500,
            false,
            ApiResources.IMAGE_ICHI_21
        ),
        Achievement(
            API.ACHI_RELAY_RACE_FIRST_POST,
            API_TRANSLATE.achi_64,
            R.color.orange_500,
            false,
            ApiResources.IMAGE_ICHI_21
        ),
        Achievement(
            API.ACHI_RELAY_RACE_FIRST_NEXT_MEMBER,
            API_TRANSLATE.achi_65,
            R.color.cyan_900,
            false,
            ApiResources.IMAGE_ICHI_21
        ),
        Achievement(
            API.ACHI_RELAY_RACE_FIRST_CREATE,
            API_TRANSLATE.achi_66,
            R.color.teal_500,
            false,
            ApiResources.IMAGE_ICHI_21
        ),
        Achievement(
            API.ACHI_RELAY_RACE_POSTS_COUNT,
            API_TRANSLATE.achi_67,
            R.color.red_500,
            false,
            ApiResources.IMAGE_ICHI_21
        ),
        Achievement(
            API.ACHI_RELAY_RACE_MY_RACE_POSTS_COUNT,
            API_TRANSLATE.achi_68,
            R.color.indigo_500,
            false,
            ApiResources.IMAGE_ICHI_21
        ),
        Achievement(
            API.ACHI_VICEROY_ASSIGN,
            API_TRANSLATE.achi_69,
            R.color.blue_500,
            false,
            ApiResources.IMAGE_ICHI_21
        ),
        Achievement(
            API.ACHI_VICEROY_POSTS_COUNT,
            API_TRANSLATE.achi_70,
            R.color.indigo_500,
            false,
            ApiResources.IMAGE_ICHI_21
        ),
        Achievement(
            API.ACHI_VICEROY_WIKI_COUNT,
            API_TRANSLATE.achi_71,
            R.color.cyan_900,
            false,
            ApiResources.IMAGE_ICHI_21
        ),
        Achievement(
            API.ACHI_VICEROY_KARMA_COUNT,
            API_TRANSLATE.achi_72,
            R.color.red_800,
            false,
            ApiResources.IMAGE_ICHI_21
        ),
        Achievement(
            API.ACHI_VICEROY_SUBSCRIBERS_COUNT,
            API_TRANSLATE.achi_73,
            R.color.orange_500,
            false,
            ApiResources.IMAGE_ICHI_21
        ),
        Achievement(
            API.ACHI_VICEROY_LINK,
            API_TRANSLATE.achi_74,
            R.color.indigo_800,
            false,
            ApiResources.IMAGE_ICHI_21
        ),
        Achievement(
            API.ACHI_VICEROY_IMAGES,
            API_TRANSLATE.achi_75,
            R.color.cyan_700,
            false,
            ApiResources.IMAGE_ICHI_21
        ),
        Achievement(
            API.ACHI_VICEROY_DESCRIPTION,
            API_TRANSLATE.achi_76,
            R.color.red_500,
            false,
            ApiResources.IMAGE_ICHI_21
        ),
        Achievement(
            API.ACHI_QUEST_KARMA,
            API_TRANSLATE.achi_77,
            R.color.teal_a_400,
            false,
            ApiResources.IMAGE_ICHI_5_2
        ),
        Achievement(API.ACHI_BONUS, API_TRANSLATE.achi_78, R.color.indigo_800, false, ApiResources.IMAGE_ICHI_7),
    )

    val keyLevels = listOf(
        AppLevel(API.LVL_TRUSTED, API_TRANSLATE.lvl_trusted),
        AppLevel(API.LVL_EXPERIENCED, API_TRANSLATE.lvl_experienced),
        AppLevel(API.LVL_CURATOR, API_TRANSLATE.lvl_curator),
        AppLevel(API.LVL_MODERATOR, API_TRANSLATE.lvl_moderator),
        AppLevel(API.LVL_ADMINISTRATOR, API_TRANSLATE.lvl_administrator),
        AppLevel(API.LVL_SUPER_ADMINISTRATOR, API_TRANSLATE.lvl_super_administrator),
        AppLevel(API.LVL_EXPERT, API_TRANSLATE.lvl_expert),
        AppLevel(API.LVL_PROTOADMIN, API_TRANSLATE.lvl_protoadmin),
    )

    val LVLS = arrayOf(
        AppLevel(API.LVL_TRUSTED, API_TRANSLATE.lvl_trusted),
        AppLevel(API.LVL_CAN_CHANGE_PROFILE_IMAGE, API_TRANSLATE.lvl_app_profile_image),
        AppLevel(API.LVL_CAN_CHANGE_STATUS, API_TRANSLATE.lvl_app_status),
        AppLevel(API.LVL_CAN_MENTION, API_TRANSLATE.lvl_app_mention),
        AppLevel(API.LVL_CAN_CHANGE_AVATAR_GIF, API_TRANSLATE.lvl_avatar_gif),
        AppLevel(API.LVL_CAN_PIN_POST, API_TRANSLATE.lvl_pin_post),
        AppLevel(API.LVL_CREATE_STICKERS, API_TRANSLATE.lvl_create_stickers),
        AppLevel(API.LVL_CREATE_QUESTS, API_TRANSLATE.lvl_create_quests),
        AppLevel(API.LVL_EXPERIENCED, API_TRANSLATE.lvl_experienced),
        AppLevel(API.LVL_CAN_CHANGE_PROFILE_IMAGE_GIF, API_TRANSLATE.lvl_app_profile_image_gif),
        AppLevel(API.LVL_MODERATOR_POST_TAGS, API_TRANSLATE.lvl_moderate_post_tags),
        AppLevel(API.LVL_MODERATOR_WIKI_EDIT, API_TRANSLATE.lvl_wiki_edit),
        AppLevel(API.LVL_ANONYMOUS, API_TRANSLATE.lvl_anonymous),
        AppLevel(API.LVL_CURATOR, API_TRANSLATE.lvl_curator),
        AppLevel(API.LVL_MODERATOR_TRANSLATE, API_TRANSLATE.lvl_translate),
        AppLevel(API.LVL_MODERATOR_TO_DRAFTS, API_TRANSLATE.lvl_moderate_to_drafts),
        AppLevel(API.LVL_MODERATOR_BLOCK, API_TRANSLATE.lvl_moderate_block),
        AppLevel(API.LVL_MODERATOR_CLOSE_POST, API_TRANSLATE.lvl_moderate_close_post),
        AppLevel(API.LVL_MODERATOR_CHATS, API_TRANSLATE.lvl_moderate_chats),
        AppLevel(API.LVL_MODERATOR_PIN_POST, API_TRANSLATE.lvl_moderate_pin_post),
        AppLevel(API.LVL_MODERATOR_IMPORTANT, API_TRANSLATE.lvl_moderate_important),
        AppLevel(API.LVL_MODERATOR_RELAY_RACE, API_TRANSLATE.lvl_moderate_relay_race),
        AppLevel(API.LVL_MODERATOR_RUBRIC, API_TRANSLATE.lvl_moderate_rubrics),
        AppLevel(API.LVL_MODERATOR, API_TRANSLATE.lvl_moderator),
        AppLevel(API.LVL_MODERATOR_NAMES, API_TRANSLATE.lvl_moderate_names),
        AppLevel(API.LVL_MODERATOR_GALLERY, API_TRANSLATE.lvl_moderate_gallery),
        AppLevel(API.LVL_MODERATOR_LINKS, API_TRANSLATE.lvl_moderate_links),
        AppLevel(API.LVL_QUEST_MODERATOR, API_TRANSLATE.lvl_admin_quest_moderate),
        AppLevel(API.LVL_ADMIN_USER_REMOVE_STATUS, API_TRANSLATE.lvl_remove_status),
        AppLevel(API.LVL_ADMIN_USER_REMOVE_IMAGE, API_TRANSLATE.lvl_remove_image),
        AppLevel(API.LVL_ADMIN_USER_REMOVE_DESCRIPTION, API_TRANSLATE.lvl_remove_descrpition),
        AppLevel(API.LVL_ADMIN_USER_REMOVE_LINK, API_TRANSLATE.lvl_remove_link),
        AppLevel(API.LVL_MODERATOR_DESCRIPTION, API_TRANSLATE.lvl_moderate_description),
        AppLevel(API.LVL_MODERATOR_TAGS, API_TRANSLATE.lvl_moderate_tags),
        AppLevel(API.LVL_ADMINISTRATOR, API_TRANSLATE.lvl_administrator),
        AppLevel(API.LVL_ADMIN_MODER, API_TRANSLATE.lvl_admin_moder),
        AppLevel(API.LVL_ADMIN_FANDOM_AVATAR, API_TRANSLATE.lvl_admin_fandom_image),
        AppLevel(API.LVL_MODERATOR_FANDOM_IMAGE, API_TRANSLATE.lvl_moderate_image_title),
        AppLevel(API.LVL_ADMIN_BAN, API_TRANSLATE.lvl_ads_ban),
        AppLevel(API.LVL_ADMIN_POST_CHANGE_FANDOM, API_TRANSLATE.lvl_post_fandom_change),
        AppLevel(API.LVL_ADMIN_MOVE_RUBRIC, API_TRANSLATE.lvl_rubric_move),
        AppLevel(API.LVL_ADMIN_USER_REMOVE_NAME, API_TRANSLATE.lvl_remove_name),
        AppLevel(API.LVL_ADMIN_USER_CHANGE_NAME, API_TRANSLATE.lvl_change_user_name),
        AppLevel(API.LVL_ADMIN_TRANSLATE_MODERATOR, API_TRANSLATE.lvl_translate_moderator),
        AppLevel(API.LVL_ADMIN_FANDOM_EFFECTS, API_TRANSLATE.lvl_accounts_effects),
        AppLevel(API.LVL_SUPER_ADMINISTRATOR, API_TRANSLATE.lvl_super_administrator),
        AppLevel(API.LVL_ADMIN_FANDOM_CATEGORY, API_TRANSLATE.lvl_admin_fandom_category),
        AppLevel(API.LVL_ADMIN_FANDOM_PARAMS, API_TRANSLATE.lvl_admin_fandom_genres),
        AppLevel(API.LVL_ADMIN_FANDOMS_ACCEPT, API_TRANSLATE.lvl_fandoms),
        AppLevel(API.LVL_ADMIN_FANDOM_CLOSE, API_TRANSLATE.lvl_fandom_close),
        AppLevel(API.LVL_ADMIN_USER_PUNISHMENTS_REMOVE, API_TRANSLATE.lvl_user_punishment_remove),
        AppLevel(API.LVL_ADMIN_REMOVE_MODERATOR, API_TRANSLATE.lvl_remove_moderator),
        AppLevel(API.LVL_ADMIN_FANDOM_ADMIN, API_TRANSLATE.lvl_fandom_admin),
        AppLevel(API.LVL_ADMIN_FANDOM_VICEROY, API_TRANSLATE.lvl_viceroy),
        AppLevel(API.LVL_ADMIN_FANDOM_SET_COF, API_TRANSLATE.lvl_fandom_set_cof),
        AppLevel(API.LVL_ADMIN_DEBUG_RECOUNT_LEVEL_AND_KARMA, API_TRANSLATE.lvl_admin_recount),
        AppLevel(API.LVL_EXPERT, API_TRANSLATE.lvl_expert),
        AppLevel(API.LVL_ADMIN_FANDOM_NAME, API_TRANSLATE.lvl_admin_fandom_rename),
        AppLevel(API.LVL_ADMIN_MAKE_MODERATOR, API_TRANSLATE.lvl_make_moderators),
        AppLevel(API.LVL_ADMIN_FANDOM_REMOVE, API_TRANSLATE.lvl_remove_fandoms),
        AppLevel(API.LVL_PROTOADMIN, API_TRANSLATE.lvl_protoadmin),
        AppLevel(API.LVL_ADMIN_REMOVE_MEDIA, API_TRANSLATE.lvl_fandom_remove_media),
    )

    val QUESTS_STORY = arrayOf(
        QuestStory(
            API.QUEST_STORY_START,
            API_TRANSLATE.quests_story_start,
            API_TRANSLATE.quests_story_start_button,
            false
        ),
        QuestStory(API.QUEST_STORY_KARMA, API_TRANSLATE.quests_story_karma),
        QuestStory(API.QUEST_STORY_ACHI_SCREEN, API_TRANSLATE.quests_story_achi_screen),
        QuestStory(API.QUEST_STORY_CHAT, API_TRANSLATE.quests_story_chat),
        QuestStory(API.QUEST_STORY_FANDOM, API_TRANSLATE.quests_story_fandom),
        QuestStory(API.QUEST_STORY_PROFILE, API_TRANSLATE.quests_story_profile),
        QuestStory(API.QUEST_STORY_FILTERS, API_TRANSLATE.quests_story_filters),
        QuestStory(API.QUEST_STORY_POST, API_TRANSLATE.quests_story_post),
        QuestStory(API.QUEST_STORY_FINISH, API_TRANSLATE.quests_story_finish, null, false),
        QuestStory(API.QUEST_STORY_COMMENTS, API_TRANSLATE.quests_story_comments),
        QuestStory(API.QUEST_STORY_BOOKMARKS, API_TRANSLATE.quests_story_bookmarks),
        QuestStory(API.QUEST_STORY_BOOKMARKS_SCREEN, API_TRANSLATE.quests_story_bookmarks_screen),
        QuestStory(API.QUEST_STORY_DRAFT, API_TRANSLATE.quests_story_draft),
        QuestStory(API.QUEST_STORY_RATINGS, API_TRANSLATE.quests_story_ratings),
        QuestStory(API.QUEST_STORY_STICKERS, API_TRANSLATE.quests_story_stickers)
    )

    val CATEGORIES = arrayOf(
        FandomParam(API.CATEGORY_GAMES, API_TRANSLATE.category_games),
        FandomParam(API.CATEGORY_ANIME, API_TRANSLATE.category_anime),
        FandomParam(API.CATEGORY_MOVIES, API_TRANSLATE.category_movies),
        FandomParam(API.CATEGORY_BOOKS, API_TRANSLATE.category_books),
        FandomParam(API.CATEGORY_ART, API_TRANSLATE.category_art),
        FandomParam(API.CATEGORY_RP, API_TRANSLATE.category_rp),
        FandomParam(API.CATEGORY_OTHER, API_TRANSLATE.category_other)
    )

    val MOD_COMMENT_TEMPLATES = arrayOf(
        API_TRANSLATE.mod_comment_1_label to API_TRANSLATE.mod_comment_1_full,
        API_TRANSLATE.mod_comment_2_label to API_TRANSLATE.mod_comment_2_full,
        API_TRANSLATE.mod_comment_3_label to API_TRANSLATE.mod_comment_3_full,
        API_TRANSLATE.mod_comment_4_label to API_TRANSLATE.mod_comment_4_full,
        API_TRANSLATE.mod_comment_5_label to API_TRANSLATE.mod_comment_5_full,
        API_TRANSLATE.mod_comment_6_label to API_TRANSLATE.mod_comment_6_full,
        API_TRANSLATE.mod_comment_7_label to API_TRANSLATE.mod_comment_7_full,
        API_TRANSLATE.mod_comment_8_label to API_TRANSLATE.mod_comment_8_full,
        API_TRANSLATE.mod_comment_9_label to API_TRANSLATE.mod_comment_9_full,
        API_TRANSLATE.mod_comment_10_label to API_TRANSLATE.mod_comment_10_full,
        API_TRANSLATE.mod_comment_11_label to API_TRANSLATE.mod_comment_11_full,
        API_TRANSLATE.mod_comment_12_label to API_TRANSLATE.mod_comment_12_full,
    )

    private val GAMES_1_ARRAY = API_TRANSLATE.getAllSame("games_genres_")
    private val GAMES_1 = Array(GAMES_1_ARRAY.size) { FandomParam(it.toLong(), GAMES_1_ARRAY[it]) }
    private val GAMES_2_ARRAY = API_TRANSLATE.getAllSame("games_platform_")
    private val GAMES_2 = Array(GAMES_2_ARRAY.size) { FandomParam(it.toLong(), GAMES_2_ARRAY[it]) }
    private val GAMES_3_ARRAY = API_TRANSLATE.getAllSame("games_age_")
    private val GAMES_3 = Array(GAMES_3_ARRAY.size) { FandomParam(it.toLong(), GAMES_3_ARRAY[it]) }

    private val ANIME_1_ARRAY = API_TRANSLATE.getAllSame("anime_genres_")
    private val ANIME_1 = Array(ANIME_1_ARRAY.size) { FandomParam(it.toLong(), ANIME_1_ARRAY[it]) }
    private val ANIME_2_ARRAY = API_TRANSLATE.getAllSame("anime_type_")
    private val ANIME_2 = Array(ANIME_2_ARRAY.size) { FandomParam(it.toLong(), ANIME_2_ARRAY[it]) }

    private val MOVIES_1_ARRAY = API_TRANSLATE.getAllSame("movies_genres_")
    private val MOVIES_1 = Array(MOVIES_1_ARRAY.size) { FandomParam(it.toLong(), MOVIES_1_ARRAY[it]) }
    private val MOVIES_2_ARRAY = API_TRANSLATE.getAllSame("movies_length_")
    private val MOVIES_2 = Array(MOVIES_2_ARRAY.size) { FandomParam(it.toLong(), MOVIES_2_ARRAY[it]) }

    private val BOOKS_1_ARRAY = API_TRANSLATE.getAllSame("books_type_")
    private val BOOKS_1 = Array(BOOKS_1_ARRAY.size) { FandomParam(it.toLong(), BOOKS_1_ARRAY[it]) }
    private val BOOKS_2_ARRAY = API_TRANSLATE.getAllSame("books_genres_")
    private val BOOKS_2 = Array(BOOKS_2_ARRAY.size) { FandomParam(it.toLong(), BOOKS_2_ARRAY[it]) }

    private val ARTS_1_ARRAY = API_TRANSLATE.getAllSame("arts_")
    private val ARTS_1 = Array(ARTS_1_ARRAY.size) { FandomParam(it.toLong(), ARTS_1_ARRAY[it]) }

    private val RP_1_ARRAY = API_TRANSLATE.getAllSame("rp_genres_")
    private val RP_1 = Array(RP_1_ARRAY.size) { FandomParam(it.toLong(), RP_1_ARRAY[it]) }

    fun getAchievement(info: AchievementInfo): Achievement {
        return getAchievement(info.index)
    }

    fun getAchievement(index: Long): Achievement {
        for (a in ACHIEVEMENTS)
            if (a.info.index == index)
                return a
        return Achievement(
            API.ACHI_UNKNOWN,
            API_TRANSLATE.error_unknown,
            R.color.red_500,
            true,
            ApiResources.IMAGE_ICHI_10
        )
    }

    fun getStoryQuest(index: Long): QuestStory? {
        for (a in QUESTS_STORY)
            if (a.quest.index.toLong() == index)
                return a
        return null
    }

    fun getCategory(index: Long): FandomParam {
        for (a in CATEGORIES)
            if (a.index == index) return a
        return FandomParam(API.CATEGORY_UNKNOWN, API_TRANSLATE.error_unknown)
    }

    fun getParamTitle(categoryId: Long, paramsPosition: Int): String? {
        return when (categoryId) {
            API.CATEGORY_GAMES ->
                when (paramsPosition) {
                    1 -> t(API_TRANSLATE.app_genres)
                    2 -> t(API_TRANSLATE.app_platforms)
                    3 -> t(API_TRANSLATE.app_age_restriction)
                    else -> null
                }

            API.CATEGORY_ANIME ->
                when (paramsPosition) {
                    1 -> t(API_TRANSLATE.app_genres)
                    2 -> t(API_TRANSLATE.app_type)
                    else -> null
                }

            API.CATEGORY_MOVIES ->
                when (paramsPosition) {
                    1 -> t(API_TRANSLATE.app_genres)
                    2 -> t(API_TRANSLATE.app_type)
                    else -> null
                }

            API.CATEGORY_BOOKS ->
                when (paramsPosition) {
                    1 -> t(API_TRANSLATE.app_type)
                    2 -> t(API_TRANSLATE.app_genres)
                    else -> null
                }

            API.CATEGORY_ART ->
                when (paramsPosition) {
                    1 -> t(API_TRANSLATE.app_type)
                    else -> null
                }

            API.CATEGORY_RP ->
                when (paramsPosition) {
                    1 -> t(API_TRANSLATE.app_genres)
                    else -> null
                }

            API.CATEGORY_OTHER -> null
            else -> null
        }
    }

    fun getParams(categoryId: Long, paramsPosition: Int): Array<FandomParam>? {
        return when (categoryId) {
            API.CATEGORY_GAMES ->
                when (paramsPosition) {
                    1 -> GAMES_1
                    2 -> GAMES_2
                    3 -> GAMES_3
                    else -> null
                }

            API.CATEGORY_ANIME ->
                when (paramsPosition) {
                    1 -> ANIME_1
                    2 -> ANIME_2
                    else -> null
                }

            API.CATEGORY_MOVIES ->
                when (paramsPosition) {
                    1 -> MOVIES_1
                    2 -> MOVIES_2
                    else -> null
                }

            API.CATEGORY_BOOKS ->
                when (paramsPosition) {
                    1 -> BOOKS_1
                    2 -> BOOKS_2
                    else -> null
                }

            API.CATEGORY_ART ->
                when (paramsPosition) {
                    1 -> ARTS_1
                    else -> null
                }

            API.CATEGORY_RP ->
                when (paramsPosition) {
                    1 -> RP_1
                    else -> null
                }

            API.CATEGORY_OTHER -> null
            else -> null
        }
    }

    fun getParam(categoryId: Long, paramsPosition: Int, index: Long): FandomParam? {
        val params = getParams(categoryId, paramsPosition) ?: return null
        for (i in params) if (i.index == index) return i
        return FandomParam(0, API_TRANSLATE.error_unknown)
    }

    fun getLvlImage(lvl: Long): ImageRef {
        return when (lvl / 100) {
            1L -> ApiResources.IMAGE_BACKGROUND_LEVEL_1
            2L -> ApiResources.IMAGE_BACKGROUND_LEVEL_2
            3L -> ApiResources.IMAGE_BACKGROUND_LEVEL_3
            4L -> ApiResources.IMAGE_BACKGROUND_LEVEL_4
            5L -> ApiResources.IMAGE_BACKGROUND_LEVEL_5
            6L -> ApiResources.IMAGE_BACKGROUND_LEVEL_6
            7L -> ApiResources.IMAGE_BACKGROUND_LEVEL_7
            8L -> ApiResources.IMAGE_BACKGROUND_LEVEL_8
            9L -> ApiResources.IMAGE_BACKGROUND_LEVEL_9
            10L -> ApiResources.IMAGE_BACKGROUND_LEVEL_10
            11L -> ApiResources.IMAGE_BACKGROUND_LEVEL_11
            12L -> ApiResources.IMAGE_BACKGROUND_LEVEL_12
            13L -> ApiResources.IMAGE_BACKGROUND_LEVEL_13
            14L -> ApiResources.IMAGE_BACKGROUND_LEVEL_14
            15L -> ApiResources.IMAGE_BACKGROUND_LEVEL_15
            16L -> ApiResources.IMAGE_BACKGROUND_LEVEL_16
            17L -> ApiResources.IMAGE_BACKGROUND_LEVEL_17
            18L -> ApiResources.IMAGE_BACKGROUND_LEVEL_18
            19L -> ApiResources.IMAGE_BACKGROUND_LEVEL_19
            else -> ApiResources.IMAGE_BACKGROUND_LEVEL_20
        }
    }

    val HELLO_TEXT = API_TRANSLATE.getAllSame(Regex("^campfire_hello_\\d+$"))

    fun randomAccountText(): String {
        return t(API_TRANSLATE.getAllSame("profile_subtitle_text").random())
    }

    fun randomFeedText(): String {
        return t(API_TRANSLATE.getAllSame("feed_loading_").random())
    }

    private val commentPlaceholders = arrayOf(
        API_TRANSLATE.comment_placeholder_1,
        API_TRANSLATE.comment_placeholder_2,
        API_TRANSLATE.comment_placeholder_3,
        API_TRANSLATE.comment_placeholder_4,
        API_TRANSLATE.comment_placeholder_5,
        API_TRANSLATE.comment_placeholder_6,
        API_TRANSLATE.comment_placeholder_7,
        API_TRANSLATE.comment_placeholder_8,
        API_TRANSLATE.comment_placeholder_9,
    )

    fun randomCommentPlaceholder(): Long {
        return commentPlaceholders.random()
    }
}
