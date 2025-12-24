package com.dzen.campfire.api

import com.dzen.campfire.api.models.AchievementInfo
import com.dzen.campfire.api.models.Language
import com.dzen.campfire.api.models.Link
import com.dzen.campfire.api.models.lvl.LvlInfoAdmin
import com.dzen.campfire.api.models.lvl.LvlInfoModeration
import com.dzen.campfire.api.models.lvl.LvlInfoUser
import com.dzen.campfire.api.models.project.StoryQuest
import com.dzen.campfire.api.tools.client.ApiClient
import com.dzen.campfire.api.tools.client.TokenProvider
import com.sup.dev.java.tools.ToolsText
import java.util.*

class API(
    projectKey: String,
    tokenProvider: TokenProvider,
    onError: (Throwable) -> Unit = {},
) : ApiClient(projectKey, tokenProvider, onError) {

    companion object {
        const val PORT_SERV_JL_V1 = 7070
        const val PORT_SERV_JL = 7071

        lateinit var SERV_ROOT: String
        lateinit var MELIOR_ROOT: String
        lateinit var S3_ROOT: String
        const val TL_ROOT = "https://tlp.bonfire.moe"

        lateinit var DOMEN: String
        const val DOMEN_DL = "bf://link/"
        const val VERSION = "3.1"
        const val SUPPORTED_VERSION = "2.0"

        const val PROJECT_KEY_CAMPFIRE = "Campfire"
        const val PROJECT_KEY_CAMPFIRE_FORTNITE = "Campfire-Fortnite"
        const val PROJECT_KEY_CAMPFIRE_TERRARIA = "Campfire-Terraria"
        const val PROJECT_KEY_CAMPFIRE_PUBG = "Campfire-Pubg"
        const val PROJECT_KEY_CAMPFIRE_ANIME = "Campfire-Anime"

        val PROTOADMINS = arrayOf(1L)

        val LINK_BOX_WITH_FIREWORKS = Link("box-with-fireworks", true)
        val LINK_BOX_WITH_SUMMER = Link("box-with-summer", true)
        val LINK_BOX_WITH_AUTUMN = Link("box-with-autumn", true)
        val LINK_BOX_WITH_WINTER = Link("box-with-winter", true)
        val LINK_BOX_WITH_CRASH = Link("box-with-crash", true)
        val LINK_BOX_WITH_BOMB = Link("box-with-bomb", true)
        val LINK_BOX_WITH_SNOW = Link("box-with-snow", true)
        val LINK_BOX_WITH_MINIGAME = Link("box-with-minigame", true)
        val LINK_BOX_WITH_BOX = Link("box-with-box", true)
        val LINK_BOX_WITH_MAGIC = Link("box-with-magic", true)
        val LINK_BOX_WITH_MAGIC_SCREEN = Link("screen-with-magic", true)
        val LINK_BOX_WITH_MAGIC_X2 = Link("box-with-magic-x2", true)
        val LINK_BOX_WITH_MAGIC_SCREEN_X2 = Link("screen-with-magic-x2", true)
        val LINK_BOX_WITH_GOOSE = Link("box-with-goose", true)
        val LINK_BOX_WITH_CONFETTI = Link("box-with-confetti", true)
        val LINK_RULES_USER = Link("app-rules", true)
        val LINK_RULES_MODER = Link("app-rules-moderator", true)
        val LINK_RULES_GALLERY = Link("app-gallery", true)
        val LINK_CREATORS = Link("app-creators", true)
        val LINK_ABOUT = Link("app-about", true)
        val LINK_DONATE = Link("app-donate", true)
        val LINK_DONATE_MAKE = Link("app-donateMake", true)
        val LINK_POST = Link("post")
        val LINK_CHAT = Link("chat")
        val LINK_CONF = Link("conf")
        val LINK_FANDOM = Link("fandom")
        val LINK_PROFILE_ID = Link("profileid")
        val LINK_PROFILE_NAME = Link("profile")
        val LINK_MODERATION = Link("moderation")
        val LINK_STICKER = Link("sticker")
        val LINK_STICKERS_PACK = Link("stickers")
        val LINK_EVENT = Link("event")
        val LINK_TAG = Link("tag")
        val LINK_WIKI_FANDOM = Link("wikifandom")
        val LINK_WIKI_SECTION = Link("wikisection")
        val LINK_WIKI_ARTICLE = Link("wikiarticle")
        val LINK_RUBRIC = Link("rubric")
        val LINK_FANDOM_CHAT = Link("fandomchat")
        val LINK_ACTIVITY = Link("activity")
        val LINK_QUEST = Link("quest")

        val LINKS_ARRAY = arrayOf(
                LINK_POST,
                LINK_CHAT,
                LINK_CONF,
                LINK_FANDOM,
                LINK_PROFILE_ID,
                LINK_MODERATION,
                LINK_STICKER,
                LINK_STICKERS_PACK,
                LINK_EVENT,
                LINK_TAG,
                LINK_WIKI_FANDOM,
                LINK_WIKI_SECTION,
                LINK_WIKI_ARTICLE,
                LINK_RUBRIC,
                LINK_FANDOM_CHAT,
                LINK_ACTIVITY,
                LINK_BOX_WITH_FIREWORKS,
                LINK_BOX_WITH_SUMMER,
                LINK_BOX_WITH_AUTUMN,
                LINK_BOX_WITH_WINTER,
                LINK_BOX_WITH_CRASH,
                LINK_BOX_WITH_BOMB,
                LINK_BOX_WITH_SNOW,
                LINK_BOX_WITH_MINIGAME,
                LINK_BOX_WITH_BOX,
                LINK_BOX_WITH_MAGIC,
                LINK_BOX_WITH_MAGIC_SCREEN,
                LINK_BOX_WITH_MAGIC_X2,
                LINK_BOX_WITH_MAGIC_SCREEN_X2,
                LINK_BOX_WITH_GOOSE,
                LINK_BOX_WITH_CONFETTI,
                LINK_RULES_USER,
                LINK_RULES_MODER,
                LINK_RULES_GALLERY,
                LINK_CREATORS,
                LINK_ABOUT,
                LINK_DONATE,
                LINK_DONATE_MAKE,
                LINK_QUEST,
        )

        const val LINK_TAG_PROFILE_NAME = "profile"
        const val LINK_SHORT_PROFILE = "@"
        const val LINK_SHORT_PROFILE_SECOND = "#"

        const val ERROR_GONE = "ERROR_GONE"
        const val ERROR_BAD_COMMENT = "ERROR_BAD_COMMENT"
        const val ERROR_ACCOUNT_IS_BANED = "ERROR_ACCOUNT_IS_BANED"
        const val ERROR_ACCESS = "ERROR_ACCESS"
        const val ERROR_ALREADY = "ERROR_ALREADY"

        const val ERROR_RELAY_NEXT_BANED = "ERROR_RELAY_NEXT_BANED"
        const val ERROR_RELAY_NEXT_ALREADY = "ERROR_RELAY_NEXT_ALREADY"
        const val ERROR_RELAY_NEXT_REJECTED = "ERROR_RELAY_NEXT_REJECTED"
        const val ERROR_RELAY_NEXT_NOT_ALLOWED = "ERROR_RELAY_NEXT_NOT_ALLOWED"

        val ENGLISH =
            ToolsText.LATIS_S + ToolsText.LATIS_S.uppercase(Locale.ENGLISH) + ToolsText.NUMBERS_S + ToolsText.TEXT_CHARS_s + ToolsText.SPEC

        const val ACCOUNT_IMG_SIDE = 384
        const val ACCOUNT_IMG_SIDE_GIF = 92
        const val ACCOUNT_IMG_WEIGHT = 1024 * 32
        const val ACCOUNT_IMG_WEIGHT_GIF = 1024 * 256
        const val ACCOUNT_TITLE_IMG_W = 1200
        const val ACCOUNT_TITLE_IMG_H = 600
        const val ACCOUNT_TITLE_IMG_GIF_W = 400
        const val ACCOUNT_TITLE_IMG_GIF_H = 200
        const val ACCOUNT_TITLE_IMG_WEIGHT = 1024 * 256
        const val ACCOUNT_TITLE_IMG_GIF_WEIGHT = 1024 * 1024 * 2
        const val ACCOUNT_STATUS_MAX_L = 100
        @Deprecated("use ToolsText.isValidUsername")
        val ACCOUNT_LOGIN_CHARS = ToolsText.LATIS_S + ToolsText.NUMBERS_S + "_"
        const val ACCOUNT_AGE_MAX = 200
        const val ACCOUNT_DESCRIPTION_MAX_L = 1000
        const val ACCOUNT_LINK_TITLE_MAX_L = 30
        const val ACCOUNT_LINK_URL_MAX_L = 500
        const val ACCOUNT_LINK_MAX = 7
        const val ACCOUNT_NOTE_MAX = 200

        const val FANDOM_TITLE_IMG_W = 1200
        const val FANDOM_TITLE_IMG_H = 600
        const val FANDOM_TITLE_IMG_WEIGHT = 1024 * 64
        const val FANDOM_TITLE_IMG_GIF_W = 400
        const val FANDOM_TITLE_IMG_GIF_H = 200
        const val FANDOM_TITLE_IMG_GIF_WEIGHT = 1024 * 1024 * 2
        const val FANDOM_IMG_SIDE = 384
        val FANDOM_NAME_CHARS = ENGLISH
        const val FANDOM_IMG_WEIGHT = 1024 * 32
        const val CHAT_IMG_BACKGROUND_W = 720
        const val CHAT_IMG_BACKGROUND_H = 1280
        const val CHAT_IMG_BACKGROUND_WEIGHT = 1024 * 128
        const val FANDOM_NAME_MAX = 100
        const val FANDOM_GALLERY_MAX_SIDE = 1280
        const val FANDOM_GALLERY_MAX_WEIGHT = 1024 * 256
        const val FANDOM_DESCRIPTION_MAX_L = 500
        const val FANDOM_GALLERY_MAX = 10
        const val FANDOM_LINKS_URL_MAX_L = 500
        const val FANDOM_LINKS_TITLE_MAX_L = 30
        const val FANDOM_LINKS_MAX = 10
        const val FANDOM_NAMES_MAX_L = 20
        const val FANDOM_NAMES_MAX = 10
        const val FANDOM_KARMA_COF_MIN = 1
        const val FANDOM_KARMA_COF_MAX = 300

        const val RESOURCES_PUBLICATION_NONE = 0L
        const val RESOURCES_PUBLICATION_DATABASE_LINKED = -1L
        const val RESOURCES_PUBLICATION_CHAT_MESSAGE = -2L
        const val RESOURCES_PUBLICATION_COMMENT = -3L
        const val RESOURCES_PUBLICATION_FANDOM_GALLERY = -4L
        const val RESOURCES_PUBLICATION_STICKER = -5L
        const val RESOURCES_PUBLICATION_STICKER_PACK = -6L
        const val RESOURCES_PUBLICATION_TAG = -7L
        const val RESOURCES_PUBLICATION_WIKI = -8L
        const val RESOURCES_PUBLICATION_ERROR = -100L

        const val EFFECT_TAG_SOURCE_SYSTEM = 1L
        const val EFFECT_COMMENT_TAG_GODS = 1L
        const val EFFECT_COMMENT_TAG_REJECTED = 2L
        const val EFFECT_COMMENT_TAG_TOO_MANY = 3L
        const val EFFECT_COMMENT_SWEARING = 4L
        const val EFFECT_COMMENT_HATE = 5L
        const val EFFECT_COMMENT_UNCULTURED = 6L
        const val EFFECT_INDEX_HATE = 1L
        const val EFFECT_INDEX_PIG = 2L
        const val EFFECT_INDEX_VAHTER = 3L
        const val EFFECT_INDEX_GOOSE = 4L
        const val EFFECT_INDEX_SNOW = 5L
        const val EFFECT_INDEX_ADMIN_BAN = 6L
        const val EFFECT_INDEX_TRANSLATOR = 7L
        const val EFFECT_INDEX_MENTION_LOCK = 8L

        const val CHAT_IMG_SIDE = 384
        const val CHAT_IMG_SIDE_GIF = 92
        const val CHAT_IMG_WEIGHT = 1024 * 32
        const val CHAT_IMG_WEIGHT_GIF = 1024 * 256
        const val CHAT_NAME_MIN = 1
        const val CHAT_NAME_MAX = 100

        const val WIKI_NAME_MAX = 200
        const val WIKI_IMG_SIDE = 384
        const val WIKI_IMG_SIDE_GIF = 92
        const val WIKI_IMG_WEIGHT = 1024 * 32
        const val WIKI_IMG_WEIGHT_GIF = 1024 * 256
        const val WIKI_TITLE_IMG_W = 1200
        const val WIKI_TITLE_IMG_H = 600
        const val WIKI_TITLE_IMG_GIF_W = 400
        const val WIKI_TITLE_IMG_GIF_H = 200
        const val WIKI_TITLE_IMG_WEIGHT = 1024 * 64
        const val WIKI_TITLE_IMG_GIF_WEIGHT = 1024 * 1024
        const val WIKI_TYPE_SECION = 1L
        const val WIKI_TYPE_ARTICLE = 2L
        const val WIKI_PAGES_EVENT_TYPE_PUT = 1L
        const val WIKI_PAGES_EVENT_TYPE_REMOVE = 2L
        const val WIKI_PAGES_EVENT_TYPE_CHANGE = 3L
        const val WIKI_PAGES_EVENT_TYPE_MOVE = 4L
        const val WIKI_PAGES_EVENT_TYPE_CHANGE_LANGUAGE = 5L

        const val MODERATION_COMMENT_MIN_L = 1
        const val MODERATION_COMMENT_MAX_L = 4000

        val LVL_APP_ACCESS = LvlInfoUser(100L, 0L)

        // NOTE: Every "key level" (e.g. trusted, experienced) MUST be a LvlInfoUser.
        val LVL_TRUSTED = LvlInfoUser(300L, 0L)
        val LVL_CAN_CHANGE_PROFILE_IMAGE = LvlInfoUser(300L, 0L)
        val LVL_CAN_CHANGE_STATUS = LvlInfoUser(300L, 0L)
        val LVL_CAN_MENTION = LvlInfoUser(300L, 0L)
        val LVL_CAN_CHANGE_AVATAR_GIF = LvlInfoUser(300L, 0L)
        val LVL_CAN_PIN_POST = LvlInfoUser(300L, 0L)
        val LVL_CREATE_STICKERS = LvlInfoUser(300L, 0L)
        val LVL_CREATE_QUESTS = LvlInfoUser(300L, 0L)

        val LVL_EXPERIENCED = LvlInfoUser(450L, 150)
        val LVL_CAN_CHANGE_PROFILE_IMAGE_GIF = LvlInfoUser(450L, 150)
        val LVL_MODERATOR_POST_TAGS = LvlInfoModeration(450L, 150)
        val LVL_MODERATOR_WIKI_EDIT = LvlInfoModeration(450L, 150)
        val LVL_ANONYMOUS = LvlInfoUser(450L, 150)
        val LVL_MODERATOR_SET_NSFW = LvlInfoModeration(450L, 150)

        val LVL_CURATOR = LvlInfoUser(600L, 400)
        val LVL_MODERATOR_TRANSLATE = LvlInfoAdmin(600L, 400)
        val LVL_MODERATOR_TO_DRAFTS = LvlInfoModeration(600L, 400)
        val LVL_MODERATOR_BLOCK = LvlInfoModeration(600L, 400)
        val LVL_MODERATOR_CLOSE_POST = LvlInfoModeration(600L, 400)
        val LVL_MODERATOR_CHATS = LvlInfoModeration(600L, 400)
        val LVL_MODERATOR_PIN_POST = LvlInfoModeration(600L, 400)
        val LVL_MODERATOR_IMPORTANT = LvlInfoModeration(600L, 400)
        val LVL_MODERATOR_RELAY_RACE = LvlInfoModeration(600L, 400)
        val LVL_MODERATOR_RUBRIC = LvlInfoModeration(600L, 400)

        val LVL_MODERATOR = LvlInfoUser(700L, 500)
        val LVL_MODERATOR_NAMES = LvlInfoModeration(700L, 500)
        val LVL_MODERATOR_GALLERY = LvlInfoModeration(700L, 500)
        val LVL_MODERATOR_LINKS = LvlInfoModeration(700L, 500)
        val LVL_QUEST_MODERATOR = LvlInfoAdmin(700L, 500)
        val LVL_ADMIN_USER_REMOVE_STATUS = LvlInfoAdmin(700L, 500)
        val LVL_ADMIN_USER_REMOVE_IMAGE = LvlInfoAdmin(700L, 500)
        val LVL_ADMIN_USER_REMOVE_DESCRIPTION = LvlInfoAdmin(700L, 500)
        val LVL_ADMIN_USER_REMOVE_LINK = LvlInfoAdmin(700L, 500)
        val LVL_MODERATOR_DESCRIPTION = LvlInfoModeration(700L, 500)
        val LVL_MODERATOR_TAGS = LvlInfoModeration(700L, 500)

        val LVL_ADMINISTRATOR = LvlInfoUser(850L, 700)
        val LVL_ADMIN_MODER = LvlInfoAdmin(850L, 700)
        val LVL_MODERATOR_FANDOM_IMAGE = LvlInfoModeration(850L, 700)
        val LVL_MODERATOR_BACKGROUND_IMAGE = LvlInfoModeration(850L, 700)
        val LVL_ADMIN_FANDOM_AVATAR = LvlInfoAdmin(850L, 700)
        val LVL_ADMIN_BAN = LvlInfoAdmin(850L, 700)
        val LVL_ADMIN_POST_CHANGE_FANDOM = LvlInfoAdmin(850L, 700)
        val LVL_ADMIN_MOVE_RUBRIC = LvlInfoAdmin(850L, 700)
        val LVL_ADMIN_USER_REMOVE_NAME = LvlInfoAdmin(850L, 700)
        val LVL_ADMIN_USER_CHANGE_NAME = LvlInfoAdmin(850L, 700)
        val LVL_ADMIN_TRANSLATE_MODERATOR = LvlInfoAdmin(850L, 700)
        val LVL_ADMIN_FANDOM_EFFECTS = LvlInfoAdmin(850L, 700)

        val LVL_SUPER_ADMINISTRATOR = LvlInfoUser(1000L, 1000)
        val LVL_ADMIN_FANDOM_CATEGORY = LvlInfoAdmin(1000L, 1000)
        val LVL_ADMIN_FANDOM_PARAMS = LvlInfoAdmin(1000L, 1000)
        val LVL_ADMIN_FANDOMS_ACCEPT = LvlInfoAdmin(1000L, 1000)
        val LVL_ADMIN_FANDOM_CLOSE = LvlInfoAdmin(1000L, 1000)
        val LVL_ADMIN_USER_PUNISHMENTS_REMOVE = LvlInfoAdmin(1000L, 1000)
        val LVL_ADMIN_REMOVE_MODERATOR = LvlInfoAdmin(1000L, 1000)
        val LVL_ADMIN_FANDOM_ADMIN = LvlInfoAdmin(1000L, 1000)
        val LVL_ADMIN_FANDOM_VICEROY = LvlInfoAdmin(1000L, 1000)
        val LVL_ADMIN_FANDOM_SET_COF = LvlInfoAdmin(1000L, 1000)
        val LVL_ADMIN_DEBUG_RECOUNT_LEVEL_AND_KARMA = LvlInfoAdmin(1000L, 1000)

        val LVL_EXPERT = LvlInfoUser(1200L, 1300)
        val LVL_ADMIN_FANDOM_NAME = LvlInfoAdmin(1200L, 1300)
        val LVL_ADMIN_MAKE_MODERATOR = LvlInfoAdmin(1200L, 1300)
        val LVL_ADMIN_FANDOM_REMOVE = LvlInfoAdmin(1200L, 1300)

        val LVL_PROTOADMIN = LvlInfoUser(50000L, 1000000)
        val LVL_ADMIN_REMOVE_MEDIA = LvlInfoAdmin(50000L, 1000000)

        const val KARMA_CHANGE_CALLDOWN = (1000 * 60 * 60 * 24).toLong()
        const val KARMA_CATEGORY_ABYSS = 0L
        const val KARMA_CATEGORY_GOOD = 1L
        const val KARMA_CATEGORY_BEST = 2L

        const val FANDOM_CAMPFIRE_ID = 10L
        const val FANDOM_CAMPFIRE_ANYTHING_ID = 1627L
        const val FANDOM_CAMPFIRE_GAMES_ID = 992L
        const val FANDOM_CAMPFIRE_ANIME_ID = 993L
        const val FANDOM_CAMPFIRE_MUSIC_ID = 1209L
        const val FANDOM_CAMPFIRE_PROGRAMS_ID = 0L
        const val FANDOM_CAMPFIRE_MOVIEWS_ID = 0L
        const val FANDOM_CAMPFIRE_OTHER_ID = 0L
        const val FANDOM_CAMPFIRE_SITES_ID = 0L
        const val FANDOM_CAMPFIRE_ORGANIZATIONS_ID = 0L
        const val FANDOM_CAMPFIRE_EVENTS_ID = 0L
        const val FANDOM_CAMPFIRE_HELLO_ID = 2776L
        const val FANDOM_CAMPFIRE_FORTNITE_ID = 7L
        const val FANDOM_CAMPFIRE_TERRARIA_ID = 6L
        const val FANDOM_CAMPFIRE_PUBG_ID = 70L
        const val FANDOM_CAMPFIRE_ANIME = 993L
        const val FANDOM_CHAT_TEXT_MIN_L = 1
        const val FANDOM_CHAT_TEXT_MAX_L = 5000

        const val ACCOUNT_CONTENT_GUY_ID = 128L

        //  #     #    #    ######  #     # ### #     #  #####
        //  #  #  #   # #   #     # ##    #  #  ##    # #     #
        //  #  #  #  #   #  #     # # #   #  #  # #   # #
        //  #  #  # #     # ######  #  #  #  #  #  #  # #  ####
        //  #  #  # ####### #   #   #   # #  #  #   # # #     #
        //  #  #  # #     # #    #  #    ##  #  #    ## #     #
        //   ## ##  #     # #     # #     # ### #     #  #####
        //
        // If you are going to add an achievement, don't forget to add
        // the counting code into rust-bonfire!
        //
        // Contact @sit for assistance.
        val ACHI_APP_SHARE = AchievementInfo(2, 5, 1)
        val ACHI_CONTENT_SHARE = AchievementInfo(3, 5, 1, 10, 30)
        val ACHI_ADD_RECRUITER = AchievementInfo(4, 2 * 10, 1)
        val ACHI_ENTERS = AchievementInfo(5, 3 * 5, *IntArray(500) { (it + 1) * 5 })
        val ACHI_KARMA_COUNT = AchievementInfo(6, 4 * 5, *IntArray(20) { (it + 1) * 20000 })
        val ACHI_REFERRALS_COUNT = AchievementInfo(7, 2 * 10, 3, 12, 36)
        val ACHI_RATES_COUNT = AchievementInfo(8, 3 * 5, 100)
        val ACHI_COMMENTS_KARMA = AchievementInfo(12, 35, 5000, 15000, 20000, 30000)
        val ACHI_POSTS_COUNT = AchievementInfo(15, 4 * 5, 10, 20, 50, 100, 150)
        val ACHI_COMMENTS_COUNT = AchievementInfo(16, 4 * 5, 100)
        val ACHI_LOGIN = AchievementInfo(28, 2 * 5, 1)
        val ACHI_CHAT = AchievementInfo(29, 3 * 5, 1)
        val ACHI_COMMENT = AchievementInfo(30, 3 * 5, 1)
        val ACHI_ANSWER = AchievementInfo(31, 3 * 5, 1)
        val ACHI_RATE = AchievementInfo(32, 3 * 5, 1)
        val ACHI_CHANGE_PUBLICATION = AchievementInfo(33, 3 * 5, 1)
        val ACHI_CHANGE_COMMENT = AchievementInfo(34, 3 * 5, 1)
        val ACHI_POST_KARMA = AchievementInfo(36, 4 * 10, 10000, 40000, 70000)
        val ACHI_FIRST_POST = AchievementInfo(37, 4 * 5, 1)
        val ACHI_SUBSCRIBE = AchievementInfo(38, 3 * 5, 1)
        val ACHI_TAGS_SEARCH = AchievementInfo(39, 3 * 5, 1)
        val ACHI_LANGUAGE = AchievementInfo(40, 3 * 5, 1)
        val ACHI_TITLE_IMAGE = AchievementInfo(41, 3 * 5, 1)
        val ACHI_CREATE_TAG = AchievementInfo(42, 3 * 5, 1)
        val ACHI_QUESTS = AchievementInfo(43, 1, *IntArray(2000) { it + 1 })
        val ACHI_FANDOMS = AchievementInfo(44, 5, 1, 5, 10, 20)
        val ACHI_RULES_USER = AchievementInfo(45, 4 * 5, 1)
        val ACHI_RULES_MODERATOR = AchievementInfo(46, 2 * 5, 1)
        val ACHI_FOLLOWERS = AchievementInfo(47, 5, 10, 100, 200, 400, 600, 800, 1000, 3000)
        val ACHI_MODER_CHANGE_POST_TAGS = AchievementInfo(48, 3 * 5, 1)
        val ACHI_FIREWORKS = AchievementInfo(50, 3 * 1, 1)
        val ACHI_MAKE_MODER = AchievementInfo(51, 5, 1)
        val ACHI_CREATE_CHAT = AchievementInfo(52, 2 * 5, 1)
        val ACHI_REVIEW_MODER_ACTION = AchievementInfo(53, 2 * 5, 1)
        val ACHI_ACCEPT_FANDOM = AchievementInfo(54, 2 * 5, 1)
        val ACHI_MODERATOR_COUNT = AchievementInfo(55, 2 * 5, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        val ACHI_MODERATOR_ACTION_KARMA = AchievementInfo(56, 2 * 5, 5000, 8000, 10000, 25000, 40000)
        val ACHI_KARMA_30 = AchievementInfo(57, 3 * 5, 50000, 75000, 100000, 130000, 160000, 200000, 250000)
        val ACHI_UP_RATES = AchievementInfo(58, 4 * 2, 10, 50, 150, 300, 500, 750, 1000)
        val ACHI_UP_RATES_OVER_DOWN = AchievementInfo(59, 3 * 2, 5, 20, 50, 150, 300, 500, 750)
        val ACHI_CHAT_SUBSCRIBE = AchievementInfo(60, 3 * 2, 1)
        val ACHI_STICKERS_KARMA = AchievementInfo(61, 3 * 10, 5000, 25000, 50000)
        val ACHI_UNKNOWN = AchievementInfo(62, 3 * 0, 0)
        val ACHI_RELAY_RACE_FIRST_POST = AchievementInfo(64, 3 * 1, 1)
        val ACHI_RELAY_RACE_FIRST_NEXT_MEMBER = AchievementInfo(65, 3 * 1, 1)
        val ACHI_RELAY_RACE_FIRST_CREATE = AchievementInfo(66, 3 * 1, 1)
        val ACHI_RELAY_RACE_POSTS_COUNT = AchievementInfo(67, 3 * 5, 5, 10, 20, 50)
        val ACHI_RELAY_RACE_MY_RACE_POSTS_COUNT = AchievementInfo(68, 3 * 5, 5, 10, 20, 50, 100)
        val ACHI_VICEROY_ASSIGN = AchievementInfo(69, 3 * 1, 1)
        val ACHI_VICEROY_POSTS_COUNT = AchievementInfo(70, 3 * 2, 5, 10, 20, 50)
        val ACHI_VICEROY_WIKI_COUNT = AchievementInfo(71, 3 * 2, 1, 5, 10, 50, 100)
        val ACHI_VICEROY_KARMA_COUNT = AchievementInfo(72, 3 * 2, 50000, 200000, 500000, 1000000)
        val ACHI_VICEROY_SUBSCRIBERS_COUNT = AchievementInfo(73, 3 * 2, 10, 20, 50, 100, 500)
        val ACHI_VICEROY_LINK = AchievementInfo(74, 3 * 1, 1)
        val ACHI_VICEROY_IMAGES = AchievementInfo(75, 3 * 1, 1)
        val ACHI_VICEROY_DESCRIPTION = AchievementInfo(76, 3 * 1, 1)
        val ACHI_QUEST_KARMA = AchievementInfo(77, 3 * 15, 7000, 14000, 25000, 37000)
        val ACHI_BONUS = AchievementInfo(78, 10, *IntArray(500) { it + 1 })

        val ACHI_PACK_1 = arrayOf(ACHI_RULES_USER, ACHI_LOGIN, ACHI_CHAT, ACHI_CHAT_SUBSCRIBE, ACHI_COMMENT, ACHI_ANSWER, ACHI_RATE, ACHI_CHANGE_PUBLICATION, ACHI_CHANGE_COMMENT, ACHI_FIRST_POST, ACHI_SUBSCRIBE, ACHI_TAGS_SEARCH, ACHI_LANGUAGE, ACHI_TITLE_IMAGE, ACHI_RELAY_RACE_FIRST_POST, ACHI_RELAY_RACE_FIRST_NEXT_MEMBER)
        val ACHI_PACK_2 = arrayOf(ACHI_APP_SHARE, ACHI_CONTENT_SHARE, ACHI_ADD_RECRUITER, ACHI_REFERRALS_COUNT, ACHI_FOLLOWERS)
        val ACHI_PACK_3 = arrayOf(ACHI_POSTS_COUNT, ACHI_COMMENTS_COUNT, ACHI_POST_KARMA, ACHI_COMMENTS_KARMA, ACHI_STICKERS_KARMA, ACHI_KARMA_COUNT, ACHI_KARMA_30, ACHI_UP_RATES, ACHI_UP_RATES_OVER_DOWN, ACHI_RELAY_RACE_POSTS_COUNT, ACHI_RELAY_RACE_MY_RACE_POSTS_COUNT, ACHI_QUEST_KARMA)
        val ACHI_PACK_4 = arrayOf(ACHI_CREATE_TAG, ACHI_RULES_MODERATOR, ACHI_MODER_CHANGE_POST_TAGS, ACHI_MAKE_MODER, ACHI_CREATE_CHAT, ACHI_REVIEW_MODER_ACTION, ACHI_ACCEPT_FANDOM, ACHI_MODERATOR_COUNT, ACHI_MODERATOR_ACTION_KARMA, ACHI_RELAY_RACE_FIRST_CREATE)
        val ACHI_PACK_5 = arrayOf(ACHI_RATES_COUNT, ACHI_ENTERS, ACHI_QUESTS, ACHI_FANDOMS, ACHI_FIREWORKS, ACHI_BONUS)
        val ACHI_PACK_6 = arrayOf(ACHI_VICEROY_ASSIGN, ACHI_VICEROY_POSTS_COUNT, ACHI_VICEROY_WIKI_COUNT, ACHI_VICEROY_KARMA_COUNT, ACHI_VICEROY_SUBSCRIBERS_COUNT, ACHI_VICEROY_LINK, ACHI_VICEROY_IMAGES, ACHI_VICEROY_DESCRIPTION)

        const val DONATE_COMMENT_MAX_L = 300

        val QUEST_STORY_START = StoryQuest(0)
        val QUEST_STORY_KARMA = StoryQuest(3)
        val QUEST_STORY_ACHI_SCREEN = StoryQuest(1)
        val QUEST_STORY_CHAT = StoryQuest(5)
        val QUEST_STORY_FANDOM = StoryQuest(1)
        val QUEST_STORY_PROFILE = StoryQuest(1)
        val QUEST_STORY_COMMENTS = StoryQuest(5)
        val QUEST_STORY_FILTERS = StoryQuest(1)
        val QUEST_STORY_BOOKMARKS = StoryQuest(1)
        val QUEST_STORY_BOOKMARKS_SCREEN = StoryQuest(1)
        val QUEST_STORY_DRAFT = StoryQuest(3L)
        val QUEST_STORY_RATINGS = StoryQuest(1)
        val QUEST_STORY_STICKERS = StoryQuest(1)
        val QUEST_STORY_POST = StoryQuest(1)
        val QUEST_STORY_FINISH = StoryQuest(0)
        val QUEST_STORY_FUTURE = StoryQuest(1)

        val QUEST_STORY_ORDER_ARRAY = arrayOf(
                QUEST_STORY_START,
                QUEST_STORY_KARMA,
                QUEST_STORY_ACHI_SCREEN,
                QUEST_STORY_CHAT,
                QUEST_STORY_FANDOM,
                QUEST_STORY_PROFILE,
                QUEST_STORY_COMMENTS,
                QUEST_STORY_FILTERS,
                QUEST_STORY_BOOKMARKS,
                QUEST_STORY_BOOKMARKS_SCREEN,
                QUEST_STORY_DRAFT,
                QUEST_STORY_RATINGS,
                QUEST_STORY_STICKERS,
                QUEST_STORY_POST,
                QUEST_STORY_FINISH,
                QUEST_STORY_FUTURE
        )

        init {
            for (i in QUEST_STORY_ORDER_ARRAY.indices) QUEST_STORY_ORDER_ARRAY[i].index = i
        }

        const val CATEGORY_GAMES = 1L
        const val CATEGORY_ANIME = 2L
        const val CATEGORY_MOVIES = 5L
        const val CATEGORY_BOOKS = 8L
        const val CATEGORY_ART = 15L
        const val CATEGORY_RP = 16L
        const val CATEGORY_OTHER = 100L
        const val CATEGORY_UNKNOWN = 101L

        const val STATUS_DRAFT = 1L
        const val STATUS_PUBLIC = 2L
        const val STATUS_BLOCKED = 3L
        const val STATUS_DEEP_BLOCKED = 4L
        const val STATUS_PENDING = 5L
        const val STATUS_ARCHIVE = 6L
        const val STATUS_REMOVED = 7L

        const val RUBRIC_NAME_MIN = 1
        const val RUBRIC_NAME_MAX = 100
        const val RUBRIC_COF_MAX = 300L
        const val RUBRIC_COF_MIN = 100L
        const val RUBRIC_COF_STEP_DOWN = 10L
        const val RUBRIC_COF_STEP_UP = 5L
        const val RUBRIC_KARMA_BOUND = 10000

        const val ACTIVITIES_NAME_MIN = 1
        const val ACTIVITIES_NAME_MAX = 100
        const val ACTIVITIES_DESC_MIN = 1
        const val ACTIVITIES_DESC_MAX = 1000
        const val ACTIVITIES_TYPE_RELAY_RACE = 1L
        const val ACTIVITIES_RELAY_RACE_TIME = 1000L * 60 * 60 * 24
        const val ACTIVITIES_COLLISION_TYPE_RELAY_RACE_POST = 1L
        const val ACTIVITIES_COLLISION_TYPE_RELAY_RACE_MEMBER = 2L
        const val ACTIVITIES_COLLISION_TYPE_SUBSCRIBE = 3L
        const val ACTIVITIES_COLLISION_TYPE_RELAY_RACE_REJECTED = 4L
        const val ACTIVITIES_COLLISION_TYPE_RELAY_RACE_LOST= 5L

        const val PUBLICATION_TYPE_COMMENT = 1L
        const val PUBLICATION_TYPE_CHAT_MESSAGE = 8L
        const val PUBLICATION_TYPE_POST = 9L
        const val PUBLICATION_TYPE_TAG = 10L
        const val PUBLICATION_TYPE_MODERATION = 11L
        const val PUBLICATION_TYPE_EVENT_USER = 12L
        const val PUBLICATION_TYPE_STICKERS_PACK = 15L
        const val PUBLICATION_TYPE_STICKER = 16L
        const val PUBLICATION_TYPE_EVENT_MODER = 17L
        const val PUBLICATION_TYPE_EVENT_ADMIN = 18L
        const val PUBLICATION_TYPE_EVENT_FANDOM = 19L
        const val PUBLICATION_TYPE_QUEST = 21L
        const val PUBLICATION_TYPE_UNKNOWN = 20L

        const val PUBLICATION_IMPORTANT_IMPORTANT = -1L
        const val PUBLICATION_IMPORTANT_DEFAULT = 0L
        const val PUBLICATION_IMPORTANT_NONE = 1L

        const val PAGE_TYPE_TEXT = 1L
        const val PAGE_TYPE_IMAGE = 2L
        const val PAGE_TYPE_IMAGES = 3L
        const val PAGE_TYPE_LINK = 4L
        const val PAGE_TYPE_QUOTE = 5L
        const val PAGE_TYPE_SPOILER = 6L
        const val PAGE_TYPE_POLLING = 7L
        const val PAGE_TYPE_VIDEO = 9L
        const val PAGE_TYPE_TABLE = 10L
        const val PAGE_TYPE_DOWNLOAD = 11L
        const val PAGE_TYPE_CAMPFIRE_OBJECT = 12L
        const val PAGE_TYPE_USER_ACTIVITY = 13L
        const val PAGE_TYPE_LINK_IMAGE = 14L
        const val PAGE_TYPE_UNKNOWN = 15L
        const val PAGE_TYPE_CODE = 16L

        const val MODERATION_TYPE_BLOCK = 1L
        const val MODERATION_TYPE_TAG_CREATE = 2L
        const val MODERATION_TYPE_TAG_CHANGE = 3L
        const val MODERATION_TYPE_TAG_REMOVE = 4L
        const val MODERATION_TYPE_DESCRIPTION = 5L
        const val MODERATION_TYPE_GALLERY_ADD = 6L
        const val MODERATION_TYPE_GELLERY_REMOVE = 7L
        const val MODERATION_TYPE_TITLE_IMAGE = 8L
        const val MODERATION_TYPE_IMPORTANT = 9L
        const val MODERATION_TYPE_LINK_ADD = 10L
        const val MODERATION_TYPE_LINK_REMOVE = 11L
        const val MODERATION_TYPE_TO_DRAFTS = 12L
        const val MODERATION_TYPE_POST_TAGS = 13L
        const val MODERATION_TYPE_NAMES = 14L
        const val MODERATION_TYPE_FORGIVE = 15L
        const val MODERATION_TYPE_BACKGROUND_IMAGE = 16L
        const val MODERATION_TYPE_LINK_CHANGE = 17L
        const val MODERATION_TYPE_TAG_MOVE = 170L
        const val MODERATION_TYPE_TAG_MOVE_BETWEEN_CATEGORY = 171L
        const val MODERATION_TYPE_TAG_PIN_POST_IN_FANDOM = 172L
        const val MODERATION_TYPE_UNKNOWN = 173L
        const val MODERATION_TYPE_MULTILINGUAL_NOT = 174L
        const val MODERATION_TYPE_POST_CLSOE = 175L
        const val MODERATION_TYPE_POST_CLSOE_NO = 176L
        const val MODERATION_TYPE_RUBRIC_CHANGE_NAME = 177L
        const val MODERATION_TYPE_RUBRIC_CHANGE_OWNER = 178L
        const val MODERATION_TYPE_RUBRIC_CREATE = 179L
        const val MODERATION_TYPE_RUBRIC_REMOVE = 180L
        const val MODERATION_TYPE_CHAT_CREATE = 181L
        const val MODERATION_TYPE_CHAT_CHANGE = 182L
        const val MODERATION_TYPE_CHAT_REMOVE = 183L
        const val MODERATION_TYPE_BACKGROUND_IMAGE_SUB = 184L
        const val MODERATION_TYPE_ACTIVITIES_CREATE = 185L
        const val MODERATION_TYPE_ACTIVITIES_CHANGE = 186L
        const val MODERATION_TYPE_ACTIVITIES_REMOVE = 187L
        const val MODERATION_TYPE_RUBRIC_MOVE_FANDOM = 188L
        const val MODERATION_TYPE_SET_NSFW = 189L


        const val PUBLICATION_EVENT_USER_ACHIEVEMENT = 1L
        const val PUBLICATION_EVENT_USER_FANDOM_SUGGEST = 3L
        const val PUBLICATION_EVENT_USER_UNKNOWN = 33L
        const val PUBLICATION_EVENT_USER_ADMIN_BANED = 34L
        const val PUBLICATION_EVENT_USER_ADMIN_WARNED = 35L
        const val PUBLICATION_EVENT_USER_ADMIN_NAME_CHANGED = 36L
        const val PUBLICATION_EVENT_USER_ADMIN_REMOVE_PUNISHMENT = 37L
        const val PUBLICATION_EVENT_USER_ADMIN_REMOVE_DESCRIPTION = 38L
        const val PUBLICATION_EVENT_USER_ADMIN_REMOVE_LINK = 39L
        const val PUBLICATION_EVENT_USER_ADMIN_REMOVE_STATUS = 40L
        const val PUBLICATION_EVENT_USER_ADMIN_REMOVE_IMAGE = 41L
        const val PUBLICATION_EVENT_USER_ADMIN_REMOVE_NAME = 42L
        const val PUBLICATION_EVENT_USER_ADMIN_REMOVE_TITLE_IMAGE = 43L
        const val PUBLICATION_EVENT_USER_ADMIN_MAKE_MODER = 44L
        const val PUBLICATION_EVENT_USER_ADMIN_REMOVE_MODER = 45L
        const val PUBLICATION_EVENT_USER_ADMIN_MODERATION_REJECTED = 46L
        const val PUBLICATION_EVENT_USER_ADMIN_PUBLICATION_RESTORED = 47L
        const val PUBLICATION_EVENT_USER_ADMIN_POST_CHANGE_FANDOM = 50L
        const val PUBLICATION_EVENT_USER_ADMIN_PUBLICATION_BLOCKED = 51L
        const val PUBLICATION_EVENT_USER_QUEST_FINISH = 52L
        const val PUBLICATION_EVENT_USER_ADMIN_VICEROY_ASSIGN = 53L
        const val PUBLICATION_EVENT_USER_ADMIN_VICEROY_REMOVE = 54L
        const val PUBLICATION_EVENT_USER_EFFECT_ADD = 55L
        const val PUBLICATION_EVENT_USER_EFFECT_REMOVE = 56L
        const val PUBLICATION_EVENT_USER_TRANSLATE_REJECTED = 57L
        const val PUBLICATION_EVENT_USER_ADMIN_POST_REMOVE_MEDIA = 58L
        const val PUBLICATION_EVENT_USER_ADMIN_VOTE_CANCELED_FOR_ADMIN = 59L
        const val PUBLICATION_EVENT_USER_ADMIN_VOTE_CANCELED_FOR_USER = 60L

        const val PUBLICATION_EVENT_ADMIN_USER_CHANGE_NAME = 4L
        const val PUBLICATION_EVENT_ADMIN_USER_REMOVE_NAME = 5L
        const val PUBLICATION_EVENT_ADMIN_USER_REMOVE_IMAGE = 6L
        const val PUBLICATION_EVENT_ADMIN_USER_REMOVE_TITILE_IMAGE = 7L
        const val PUBLICATION_EVENT_ADMIN_FANDOM_REMOVE = 8L
        const val PUBLICATION_EVENT_ADMIN_FANDOM_CHANGE_AVATAR = 9L
        const val PUBLICATION_EVENT_ADMIN_FANDOM_RENAME = 11L
        const val PUBLICATION_EVENT_ADMIN_USER_REMOVE_STATUS = 14L
        const val PUBLICATION_EVENT_ADMIN_BAN = 15L
        const val PUBLICATION_EVENT_ADMIN_USER_REMOVE_DESCRIPTION = 16L
        const val PUBLICATION_EVENT_ADMIN_USER_REMOVE_LINK = 17L
        const val PUBLICATION_EVENT_ADMIN_FANDOM_CHANGE_PARAMS = 19L
        const val PUBLICATION_EVENT_ADMIN_WARN = 21L
        const val PUBLICATION_EVENT_ADMIN_FANDOM_MAKE_MODERATOR = 22L
        const val PUBLICATION_EVENT_ADMIN_FANDOM_REMOVE_MODERATOR = 23L
        const val PUBLICATION_EVENT_ADMIN_PUNISHMENT_REMOVE = 24L
        const val PUBLICATION_EVENT_ADMIN_MODERATION_REJECTED = 25L
        const val PUBLICATION_EVENT_ADMIN_PUBLICATION_RESTORE = 26L
        const val PUBLICATION_EVENT_ADMIN_POST_CHANGE_FANDOM = 27L
        const val PUBLICATION_EVENT_ADMIN_FANDOM_CHANGE_CATEGORY = 28L
        const val PUBLICATION_EVENT_ADMIN_FANDOM_CLOSE = 29L
        const val PUBLICATION_EVENT_ADMIN_FANDOM_COF_CHANGED = 31L
        const val PUBLICATION_EVENT_ADMIN_UNKNOWN = 32L
        const val PUBLICATION_EVENT_ADMIN_FANDOM_SUGGEST = 200L
        const val PUBLICATION_EVENT_ADMIN_BLOCK_PUBLICATION = 201L
        const val PUBLICATION_EVENT_ADMIN_FANDOM_VICEROY_ASSIGN = 202L
        const val PUBLICATION_EVENT_ADMIN_FANDOM_VICEROY_REMOVE = 203L
        const val PUBLICATION_EVENT_ADMIN_TRANSLATE = 204L
        const val PUBLICATION_EVENT_ADMIN_EFFECT_ADD = 205L
        const val PUBLICATION_EVENT_ADMIN_EFFECT_REMOVE = 206L
        const val PUBLICATION_EVENT_ADMIN_TRANSLATE_REJECTED = 207L
        const val PUBLICATION_EVENT_ADMIN_POST_REMOVE_MEDIA = 208L
        const val PUBLICATION_EVENT_ADMIN_ADMIN_VOTE_CANCELED = 209L
        const val PUBLICATION_EVENT_ADMIN_QUEST_TO_DRAFTS = 210L

        const val ADMIN_VOTE_UNKNOWN = 1L
        const val ADMIN_VOTE_ACCOUNT_RECOUNT_ACHI = 2L
        const val ADMIN_VOTE_ACCOUNT_CHANGE_NAME = 3L
        const val ADMIN_VOTE_ACCOUNT_EFFECT = 4L
        const val ADMIN_VOTE_ACCOUNT_PUNISH = 5L
        const val ADMIN_VOTE_ACCOUNT_RECOUNT_KARMA = 6L
        const val ADMIN_VOTE_ACCOUNT_REMOVE_AVATAR = 7L
        const val ADMIN_VOTE_ACCOUNT_REMOVE_BACKGROUND = 8L
        const val ADMIN_VOTE_ACCOUNT_REMOVE_NAME = 9L
        const val ADMIN_VOTE_ACCOUNT_REMOVE_REPORTS = 10L
        const val ADMIN_VOTE_ACCOUNT_REMOVE_STATUS = 11L
        const val ADMIN_VOTE_FANDOM_REMOVE = 12L

        const val PUBLICATION_EVENT_MODER_UNKNOWN = 30L

        const val PUBLICATION_EVENT_FANDOM_UNKNOWN = 100L
        const val PUBLICATION_EVENT_FANDOM_ACCEPTED = 101L
        const val PUBLICATION_EVENT_FANDOM_CHANGE_CATEGORY = 102L
        const val PUBLICATION_EVENT_FANDOM_RENAME = 103L
        const val PUBLICATION_EVENT_FANDOM_CHANGE_AVATAR = 104L
        const val PUBLICATION_EVENT_FANDOM_CHANGE_PARAMS = 105L
        const val PUBLICATION_EVENT_FANDOM_CLOSE = 106L
        const val PUBLICATION_EVENT_FANDOM_MAKE_MODERATOR = 107L
        const val PUBLICATION_EVENT_FANDOM_REMOVE = 108L
        const val PUBLICATION_EVENT_FANDOM_REMOVE_MODERATOR = 109L
        const val PUBLICATION_EVENT_FANDOM_KARMA_COF_CHANGED = 110L
        const val PUBLICATION_EVENT_FANDOM_VICEROY_ASSIGN = 111L
        const val PUBLICATION_EVENT_FANDOM_VICEROY_REMOVE = 112L

        const val BOOKMARKS_FOLDERS_NAME_MAX = 50L
        const val BOOKMARKS_FOLDERS_MAX = 5L

        const val COLLISION_BOOKMARK = 6L
        const val COLLISION_SHARE = 9L
        const val COLLISION_TAG = 11L
        const val COLLISION_KARMA_30 = 14L
        const val COLLISION_PAGE_POLLING_VOTE = 21L
        const val COLLISION_COMMENTS_WATCH = 24L
        const val COLLISION_PUNISHMENTS_WARN = 25L
        const val COLLISION_PUNISHMENTS_BAN = 26L
        const val COLLISION_ACHIEVEMENT_RULES_USER = 10000L
        const val COLLISION_ACHIEVEMENT_RULES_MODER = 10001L
        const val COLLISION_ACHIEVEMENT_SHARE_APP = 10002L
        const val COLLISION_ACHIEVEMENT_TAG_SEARCH = 10003L
        const val COLLISION_ACHIEVEMENT_CHANGE_PUBLICATION = 10004L
        const val COLLISION_ACHIEVEMENT_FANDOM_LANGUAGE = 10005L
        const val COLLISION_ACHIEVEMENT_TAG_CREATE = 10006L
        const val COLLISION_ACHIEVEMENT_CHANGE_COMMENT = 10007L
        const val COLLISION_ACHIEVEMENT_COMMENT = 10008L
        const val COLLISION_ACHIEVEMENT_ANSWER = 10009L
        const val COLLISION_ACHIEVEMENT_MODERATIONS_POST_TAGS = 10010L
        const val COLLISION_ACHIEVEMENT_FAERWORKS = 10012L
        const val COLLISION_ACHIEVEMENT_MAKE_MODER = 10013L
        const val COLLISION_ACHIEVEMENT_CREATE_FANDOM_CHAT = 10014L
        const val COLLISION_ACHIEVEMENT_REVIEW_MODER_ACTION = 10015L
        const val COLLISION_ACHIEVEMENT_ACCEPT_FANDOM = 10016L
        const val COLLISION_ACHIEVEMENT_CHAT_SUBSCRIBE = 10017L
        const val COLLISION_ACHIEVEMENT_RATE = 10017L
        const val COLLISION_ACHIEVEMENT_CHAT = 10018L
        const val COLLISION_ACHIEVEMENT_RELAY_RACE_FIRST_POST = 10019L
        const val COLLISION_ACHIEVEMENT_RELAY_RACE_FIRST_NEXT_MEMBER = 10020L
        const val COLLISION_ACHIEVEMENT_RELAY_RACE_FIRST_CREATE = 10021L
        const val COLLISION_ACHIEVEMENT_RELAY_RACE_POSTS_COUNT = 10022L
        const val COLLISION_ACHIEVEMENT_VICEROY_ASSIGN = 10024L
        const val COLLISION_ACHIEVEMENT_VICEROY_LINK = 10025L
        const val COLLISION_ACHIEVEMENT_VICEROY_IMAGES = 10026L
        const val COLLISION_ACHIEVEMENT_VICEROY_DESCRIPTIONS = 10027L
        const val COLLISION_FANDOM_SUBSCRIBE = 20L
        const val COLLISION_FANDOM_DESCRIPTION = 20000L
        const val COLLISION_FANDOM_GALLERY = 20001L
        const val COLLISION_FANDOM_LINK = 20002L
        const val COLLISION_FANDOM_NOTIFY_IMPORTANT = 20003L
        const val COLLISION_FANDOM_TITLE_IMAGE = 20004L
        const val COLLISION_FANDOM_NAMES = 20005L
        const val COLLISION_CHAT_BACKGROUND_IMAGE = 20012L
        const val COLLISION_FANDOM_PINNED_POST = 20013L
        const val COLLISION_FANDOM_VICEROY = 20014L
        const val COLLISION_FANDOM_PARAMS_4 = 20015L
        const val COLLISION_FANDOM_PARAMS_1 = 20016L
        const val COLLISION_FANDOM_PARAMS_2 = 20017L
        const val COLLISION_FANDOM_PARAMS_3 = 20018L
        const val COLLISION_FANDOM_SUGGESTION_NOTES = 20019L
        const val COLLISION_ACCOUNT_FOLLOW = 8L
        const val COLLISION_ACCOUNT_QUEST = 17L
        const val COLLISION_ACCOUNT_NOTIFICATION_TOKEN = 23L
        const val COLLISION_ACCOUNT_STATUS = 30002L
        const val COLLISION_ACCOUNT_AGE = 30003L
        const val COLLISION_ACCOUNT_DESCRIPTION = 30004L
        const val COLLISION_ACCOUNT_LINKS = 30005L
        const val COLLISION_ACCOUNT_BLACK_LIST_ACCOUNT = 30006L
        const val COLLISION_ACCOUNT_FOLLOW_NOTIFY = 30007L
        const val COLLISION_ACCOUNT_REPORT = 30008L
        const val COLLISION_ACCOUNT_NOTE = 30009L
        const val COLLISION_ACCOUNT_COMMENTS_COUNT = 30010L
        const val COLLISION_ACCOUNT_POSTS_COUNT = 30011L
        const val COLLISION_ACCOUNT_COMMENTS_KARMA = 30012L
        const val COLLISION_ACCOUNT_POSTS_KARMA = 30013L
        const val COLLISION_ACCOUNT_UP_RATES = 30014L
        const val COLLISION_ACCOUNT_UP_OVER_DOWN_RATES = 30015L
        const val COLLISION_ACCOUNT_KARMA_COUNT = 30016L
        const val COLLISION_ACCOUNT_LAST_DAILY_ENTER_DATE = 30017L
        const val COLLISION_ACCOUNT_DAILY_ENTERS_COUNT = 30018L
        const val COLLISION_ACCOUNT_MODERATION_KARMA = 30019L
        const val COLLISION_ACCOUNT_PINNED_POST = 30020L
        const val COLLISION_ACCOUNT_STICKERS_KARMA = 30021L
        const val COLLISION_ACCOUNT_BLACK_LIST_FANDOM = 30022L
        const val COLLISION_ACCOUNT_STICKERPACKS = 30023L
        const val COLLISION_ACCOUNT_STICKERS = 30024L
        const val COLLISION_ACCOUNT_PROJECT_INIT = 30025L
        const val COLLISION_ACCOUNT_QUESTS_KARMA = 30026L
        const val COLLISION_ACCOUNT_ACHIEVEMENTS = 30027L
        const val COLLISION_PROJECT_AB_PARAMS = 40000L
        const val COLLISION_PROJECT_KEY = 40001L
        const val COLLISION_PROJECT_MINIGAME_HUMANS = 40002L
        const val COLLISION_PROJECT_MINIGAME_ROBOTS = 40003L
        const val COLLISION_PROJECT_API_INFO = 40004L
        const val COLLISION_PUBLICATION_REPORT = 7L
        const val COLLISION_PUBLICATION_REACTION = 50000L
        const val COLLISION_RUBRICS_NOTIFICATIONS = 60000L

        const val REPORT_COMMENT_L = 500

        const val COMMENT_MIN_L = 1
        const val COMMENT_MAX_L = 2000

        const val CHAT_TYPE_FANDOM_ROOT = 1L
        const val CHAT_TYPE_PRIVATE = 2L
        const val CHAT_TYPE_CONFERENCE = 3L
        const val CHAT_TYPE_FANDOM_SUB = 4L
        const val CHAT_MESSAGE_TEXT_MAX_L = 2000
        const val CHAT_MESSAGE_TEXT_MIN_L = 1
        const val CHAT_MESSAGE_IMAGE_WEIGHT = 1024 * 256
        const val CHAT_MESSAGE_IMAGE_SIDE = 1080
        const val CHAT_MESSAGE_IMAGE_SIDE_GIF = 400
        const val CHAT_MESSAGE_GIF_MAX_WEIGHT = 1024 * 1024
        const val CHAT_MESSAGE_MAX_IMAGES_COUNT = 5
        const val CHAT_MESSAGE_VOICE_MAX_MS = 20000L
        const val CHAT_MESSAGE_QUOTE_MAX_SIZE = 300
        const val CHAT_MEMBER_STATUS_DELETE = 0L
        const val CHAT_MEMBER_STATUS_ACTIVE = 1L
        const val CHAT_MEMBER_STATUS_LEAVE = 2L
        const val CHAT_MEMBER_STATUS_DELETE_AND_LEAVE = 3L
        const val CHAT_MEMBER_LVL_USER = 1L
        const val CHAT_MEMBER_LVL_MODERATOR = 2L
        const val CHAT_MEMBER_LVL_ADMIN = 3L

        const val TAG_NAME_MIN_L = 1
        const val TAG_NAME_MAX_L = 20
        const val TAG_IMAGE_SIDE = 64
        const val TAG_IMAGE_WEIGHT = 1024 * 8

        const val STICKERS_PACK_IMAGE_SIDE = 384
        const val STICKERS_PACK_IMAGE_WEIGHT = 1024 * 32
        const val STICKERS_PACK_NAME_L_MIN = 2
        const val STICKERS_PACK_NAME_L_MAX = 100
        const val STICKERS_IMAGE_SIDE = 300
        const val STICKERS_IMAGE_SIDE_GIF = 300
        const val STICKERS_IMAGE_WEIGHT = 1024 * 32
        const val STICKERS_IMAGE_WEIGHT_GIF = 1024 * 256
        const val STICKERS_MAX_COUNT_IN_PACK = 100
        const val STICKERS_PACK_MAX_COUNT_ON_ACCOUNT = 50
        const val STICKERS_MAX_COUNT_ON_ACCOUNT = 100

        const val HISTORY_PUBLICATION_TYPE_UNKNOWN = 1L
        const val HISTORY_PUBLICATION_TYPE_CREATED = 2L
        const val HISTORY_PUBLICATION_TYPE_ADMIN_BACK_DRAFT = 3L
        const val HISTORY_PUBLICATION_TYPE_ADMIN_BLOCK = 4L
        const val HISTORY_PUBLICATION_TYPE_ADMIN_CHANGE_FANDOM = 5L
        const val HISTORY_PUBLICATION_TYPE_ADMIN_CLEAR_REPORTS = 6L
        const val HISTORY_PUBLICATION_TYPE_ADMIN_DEEP_BLOCK = 7L
        const val HISTORY_PUBLICATION_TYPE_ADMIN_NOT_BLOCK = 8L
        const val HISTORY_PUBLICATION_TYPE_ADMIN_NOT_MULTILINGUAL = 9L
        const val HISTORY_PUBLICATION_TYPE_BACK_DRAFT = 10L
        const val HISTORY_PUBLICATION_TYPE_CHANGE_FANDOM = 11L
        const val HISTORY_PUBLICATION_TYPE_EDIT_PUBLIC = 12L
        const val HISTORY_PUBLICATION_TYPE_MULTILINGUAL = 13L
        const val HISTORY_PUBLICATION_TYPE_NOT_MULTILINGUAL = 14L
        const val HISTORY_PUBLICATION_TYPE_PUBLISH = 15L
        const val HISTORY_PUBLICATION_TYPE_ADMIN_CHANGE_TAGS = 16L
        const val HISTORY_PUBLICATION_TYPE_ADMIN_IMPORTANT = 17L
        const val HISTORY_PUBLICATION_TYPE_ADMIN_NOT_IMPORTANT = 18L
        const val HISTORY_PUBLICATION_TYPE_ADMIN_PIN_FANDOM = 19L
        const val HISTORY_PUBLICATION_TYPE_ADMIN_UNPIN_FANDOM = 20L
        const val HISTORY_PUBLICATION_TYPE_CHANGE_TAGS = 21L
        const val HISTORY_PUBLICATION_TYPE_PIN_PROFILE = 22L
        const val HISTORY_PUBLICATION_TYPE_UNPIN_PROFILE = 23L
        const val HISTORY_PUBLICATION_TYPE_ADMIN_NOT_DEEP_BLOCK = 24L
        const val HISTORY_PUBLICATION_TYPE_CLOSE = 25L
        const val HISTORY_PUBLICATION_TYPE_ADMIN_CLOSE = 26L
        const val HISTORY_PUBLICATION_TYPE_CLOSE_NO = 27L
        const val HISTORY_PUBLICATION_TYPE_ADMIN_CLOSE_NO = 28L
        const val HISTORY_PUBLICATION_TYPE_ADMIN_REMOVE_MEDIA = 29L
        const val HISTORY_PUBLICATION_TYPE_SET_NSFW = 30L
        const val HISTORY_PUBLICATION_TYPE_ADMIN_SET_NSFW = 31L
        const val HISTORY_PUBLICATION_TYPE_PENDING = 32L

        //
        //  Post
        //


        const val PAGE_TEXT_MAX_L = 50000
        const val PAGE_TEXT_TITLE_MAX_L = 100
        const val PAGE_IMAGE_SIDE = 1500
        const val PAGE_IMAGE_SIDE_GIF = 400
        const val PAGE_IMAGE_WEIGHT = 1024 * 768
        const val PAGE_IMAGE_GIF_WEIGHT = 1024 * 1024 * 6
        const val PAGE_VIDEO_IMAGE_SIDE = 1000
        const val PAGE_VIDEO_IMAGE_WEIGHT = 1024 * 128
        const val PAGE_QUOTE_AUTHOR_MAX_L = 50
        const val PAGE_QUOTE_TEXT_MAX_L = 1000
        const val PAGE_LINK_NAME_MAX_L = 100
        const val PAGE_LINK_WEB_MAX_L = 500
        const val PAGE_LINK_SPOILER_MAX = 10
        const val PAGE_LINK_SPOILER_NAME_MAX_L = 100
        const val PAGE_POLLING_OPTION_MAX_TEXT = 200
        const val PAGE_POLLING_OPTION_MAX_COUNT = 10
        const val PAGE_POLLING_MAX_DURATION = 1000L * 60 * 60 * 24 * 30
        const val PAGE_POLLING_TITLE_MAX = 100
        const val PAGE_POLLING_BLACKLIST_MAX = 10
        const val PAGE_IMAGES_MINI_SIDE = 500
        const val PAGE_IMAGES_MINI_WEIGHT = 1024 * 128
        const val PAGE_IMAGES_MINI_SIDE_GIF = 128
        const val PAGE_IMAGES_MINI_WEIGHT_GIF = 1024 * 384
        const val PAGE_IMAGES_SIDE = 1980
        const val PAGE_IMAGES_WEIGHT = 1024 * 1024
        const val PAGE_IMAGES_SIDE_GIF = 400
        const val PAGE_IMAGES_WEIGHT_GIF = 1024 * 1024 * 6
        const val PAGE_IMAGES_MAX_COUNT = 10
        const val PAGE_IMAGES_TITLE_MAX = 100
        const val PAGE_TABLE_MAX_COLUMNS = 5
        const val PAGE_TABLE_TITLE_MAX = 100
        const val PAGE_TABLE_MAX_ROWS = 30
        const val PAGE_TABLE_MAX_TEXT_SIZE = 1000
        const val PAGE_TABLE_MAX_IMAGE_SIDE = 512
        const val PAGE_TABLE_MAX_IMAGE_SIDE_GIF = 128
        const val PAGE_TABLE_MAX_IMAGE_WEIGHT = 1024 * 512
        const val PAGE_TABLE_MAX_IMAGE_WEIGHT_GIF = 1024 * 1024
        const val PAGE_DOWNLOAD_TITLE_MAX = 100
        const val PAGE_DOWNLOAD_SIZE_MAX = 1024 * 1024 * 50
        const val PAGE_CAMPFIRE_OBJECT_LINK_MAX = 2000
        const val PAGE_LINK_IMAGE_W = 1200
        const val PAGE_LINK_IMAGE_H = 600
        const val PAGE_LINK_IMAGE_WEIGHT = 1024 * 256

        const val PAGES_SOURCE_TYPE_POST = 1L
        const val PAGES_SOURCE_TYPE_WIKI = 2L

        const val POST_MAX_PAGES_COUNT = 100

        //
        //  Notifications
        //

        const val NOTIF_KARMA_ADD = 1L
        const val NOTIF_COMMENT = 2L
        const val NOTIF_COMMENT_ANSWER = 3L
        const val NOTIF_ACCOUNT_FOLLOWS_ADD = 4L
        const val NOTIF_ACHI = 7L
        const val NOTIF_CHAT_MESSAGE = 8L
        const val NOTIF_CHAT_MESSAGE_ANSWER = 9L
        const val NOTIF_FOLLOWS_PUBLICATION = 10L
        const val NOTIF_CHAT_MESSAGE_CHANGE = 11L
        const val NOTIF_PUBLICATION_BLOCK = 12L
        const val NOTIF_CHAT_MESSAGE_REMOVE = 13L
        const val NOTIF_CHAT_TYPING = 14L
        const val NOTIF_FANDOM_ACCPTED = 15L
        const val NOTIF_QUEST_POGRESS = 18L
        const val NOTIF_PUBLICATION_IMPORTANT = 19L
        const val NOTIF_MODERATION_TO_DRAFTS = 20L
        const val NOTIF_PROJECT_AB_PARAMS_CHAGED = 21L
        const val NOTIF_MODERATION_POST_TAGS = 22L
        const val NOTIF_MODERATION_FORGIVE = 23L
        const val NOTIF_CHAT_READ = 24L
        const val NOTIF_BLOCK = 25L
        const val NOTIF_FANDOM_MAKE_MODERATOR = 26L
        const val NOTIF_FANDOM_REMOVE_MODERATOR = 27L
        const val NOTIF_PUNISHMENT_REMOVE = 28L
        const val NOTIF_PUBLICATION_RESTORE = 29L
        const val NOTIF_MODERATION_REJECTED = 30L
        const val NOTIF_ADMIN_STATUS_REMOVE = 31L
        const val NOTIF_ADMIN_DESCRIPTION_REMOVE = 32L
        const val NOTIF_ADMIN_NAME_REMOVE = 33L
        const val NOTIF_ADMIN_LINK_REMOVE = 34L
        const val NOTIF_ADMIN_POST_FANDOM_CHANGE = 35L
        const val NOTIF_PUBLICATION_BLOCK_AFTER_REPORT = 36L
        const val NOTIF_MENTION = 37L
        const val NOTIF_UNKNOWN = 38L
        const val NOTIF_MODERATION_MULTILINGUAL_NOT = 39L
        const val NOTIF_QUEST_FINISH = 40L
        const val NOTIF_MODERATION_POST_CLOSE = 41L
        const val NOTIF_MODERATION_POST_CLOSE_NO = 42L
        const val NOTIF_RUBRICS_CHANGE_NAME = 43L
        const val NOTIF_RUBRICS_CHANGE_OWNER = 44L
        const val NOTIF_RUBRICS_MAKE_OWNER = 45L
        const val NOTIF_RUBRICS_REMOVE = 46L
        const val NOTIF_RUBRICS_KARMA_COF_CHANGED = 47L
        const val NOTIF_RUBRICS_MOVE_FANDOM = 65L
        const val NOTIF_PUBLICATION_REACTION = 48L
        const val NOTIF_ACTIVITIES_RELAY_RACE_TURN = 49L
        const val NOTIF_ACTIVITIES_RELAY_RACE_LOST = 50L
        const val NOTIF_ACTIVITIES_NEW_POST = 51L
        const val NOTIF_ACTIVITIES_RELAY_RACE_REJECTED = 52L
        const val NOTIF_FANDOM_VICEROY_ASSIGN = 53L
        const val NOTIF_FANDOM_VICEROY_REMOVE = 54L
        const val NOTIF_ALIVE = 55L
        const val NOTIF_DONATE = 56L
        const val NOTIF_EFFECT_ADD = 57L
        const val NOTIF_EFFECT_REMOVE = 58L
        const val NOTIF_TRANSLATE_ACCEPTED = 59L
        const val NOTIF_TRANSLATE_REJECTED = 60L
        const val NOTIF_ADMIN_POST_MEDIA_REMOVE = 61L
        const val NOTIF_FANDOM_REMOVE_CANCEL = 62L
        const val NOTIF_ACCOUNT_ADMIN_VOTE_CANCELED_FOR_ADMIN = 63L
        const val NOTIF_ACCOUNT_ADMIN_VOTE_CANCELED_FOR_USER = 64L
        const val NOTIF_ACCOUNT_FOLLOWS_REMOVE = 66L
        const val NOTIF_MODERATION_POST_SET_NSFW = 67L

        val notificationTypes = setOf(
            NOTIF_KARMA_ADD,
            NOTIF_COMMENT,
            NOTIF_COMMENT_ANSWER,
            NOTIF_ACCOUNT_FOLLOWS_ADD,
            NOTIF_ACHI,
            NOTIF_CHAT_MESSAGE,
            NOTIF_CHAT_MESSAGE_ANSWER,
            NOTIF_FOLLOWS_PUBLICATION,
            NOTIF_CHAT_MESSAGE_CHANGE,
            NOTIF_PUBLICATION_BLOCK,
            NOTIF_CHAT_MESSAGE_REMOVE,
            NOTIF_CHAT_TYPING,
            NOTIF_FANDOM_ACCPTED,
            NOTIF_QUEST_POGRESS,
            NOTIF_PUBLICATION_IMPORTANT,
            NOTIF_MODERATION_TO_DRAFTS,
            NOTIF_PROJECT_AB_PARAMS_CHAGED,
            NOTIF_MODERATION_POST_TAGS,
            NOTIF_MODERATION_FORGIVE,
            NOTIF_CHAT_READ,
            NOTIF_BLOCK,
            NOTIF_FANDOM_MAKE_MODERATOR,
            NOTIF_FANDOM_REMOVE_MODERATOR,
            NOTIF_PUNISHMENT_REMOVE,
            NOTIF_PUBLICATION_RESTORE,
            NOTIF_MODERATION_REJECTED,
            NOTIF_ADMIN_STATUS_REMOVE,
            NOTIF_ADMIN_DESCRIPTION_REMOVE,
            NOTIF_ADMIN_NAME_REMOVE,
            NOTIF_ADMIN_LINK_REMOVE,
            NOTIF_ADMIN_POST_FANDOM_CHANGE,
            NOTIF_PUBLICATION_BLOCK_AFTER_REPORT,
            NOTIF_MENTION,
            NOTIF_UNKNOWN,
            NOTIF_MODERATION_MULTILINGUAL_NOT,
            NOTIF_QUEST_FINISH,
            NOTIF_MODERATION_POST_CLOSE,
            NOTIF_MODERATION_POST_CLOSE_NO,
            NOTIF_RUBRICS_CHANGE_NAME,
            NOTIF_RUBRICS_CHANGE_OWNER,
            NOTIF_RUBRICS_MAKE_OWNER,
            NOTIF_RUBRICS_REMOVE,
            NOTIF_RUBRICS_KARMA_COF_CHANGED,
            NOTIF_RUBRICS_MOVE_FANDOM,
            NOTIF_PUBLICATION_REACTION,
            NOTIF_ACTIVITIES_RELAY_RACE_TURN,
            NOTIF_ACTIVITIES_RELAY_RACE_LOST,
            NOTIF_ACTIVITIES_NEW_POST,
            NOTIF_ACTIVITIES_RELAY_RACE_REJECTED,
            NOTIF_FANDOM_VICEROY_ASSIGN,
            NOTIF_FANDOM_VICEROY_REMOVE,
            NOTIF_ALIVE,
            NOTIF_DONATE,
            NOTIF_EFFECT_ADD,
            NOTIF_EFFECT_REMOVE,
            NOTIF_TRANSLATE_ACCEPTED,
            NOTIF_TRANSLATE_REJECTED,
            NOTIF_ADMIN_POST_MEDIA_REMOVE,
            NOTIF_FANDOM_REMOVE_CANCEL,
            NOTIF_ACCOUNT_ADMIN_VOTE_CANCELED_FOR_ADMIN,
            NOTIF_ACCOUNT_ADMIN_VOTE_CANCELED_FOR_USER,
            NOTIF_ACCOUNT_FOLLOWS_REMOVE,
            NOTIF_MODERATION_POST_SET_NSFW,
        )

        //
        //  Quests
        //

        const val QUEST_TITLE_MIN_L = 2
        const val QUEST_TITLE_MAX_L = 100
        const val QUEST_DESCRIPTION_MAX_L = 2000
        const val QUEST_VARIABLES_MAX = 100
        const val QUEST_VARIABLE_MAX_NAME_L = 200
        const val QUEST_VARIABLE_MAX_VALUE_L = 200
        const val QUEST_PARTS_MAX = 750
        const val QUEST_DEV_LABEL_MAX_L = 100
        const val QUEST_MAX_DEPTH = 200

        const val QUEST_TEXT_TITLE_MAX_L = 200
        const val QUEST_TEXT_TEXT_MAX_L = 20000
        const val QUEST_TEXT_INPUTS_MAX = 5
        const val QUEST_TEXT_BUTTONS_MAX = 10

        const val QUEST_IMAGE_W = 800
        const val QUEST_IMAGE_H = 400
        const val QUEST_IMAGE_WEIGHT = 1024 * 256

        const val QUEST_INPUT_HINT_MAX_L = 100

        const val QUEST_BUTTON_LABEL_MIN_L = 1
        const val QUEST_BUTTON_LABEL_MAX_L = 50

        const val QUEST_TYPE_TEXT = 1L
        const val QUEST_TYPE_NUMBER = 2L
        const val QUEST_TYPE_BOOL = 3L

        const val QUEST_BUTTON_COLOR_DEFAULT = 1L
        const val QUEST_BUTTON_COLOR_RED = 2L
        const val QUEST_BUTTON_COLOR_ORANGE = 3L
        const val QUEST_BUTTON_COLOR_YELLOW = 4L
        const val QUEST_BUTTON_COLOR_GREEN = 5L
        const val QUEST_BUTTON_COLOR_AQUA = 6L
        const val QUEST_BUTTON_COLOR_BLUE = 7L
        const val QUEST_BUTTON_COLOR_PURPLE = 8L
        const val QUEST_BUTTON_COLOR_PINK = 9L
        const val QUEST_BUTTON_COLOR_WHITE = 10L
        // put new colors into the array **in the same order**
        // and do not skip numbers for const values
        val QUEST_BUTTON_COLORS = arrayOf(
            QUEST_BUTTON_COLOR_DEFAULT, QUEST_BUTTON_COLOR_RED, QUEST_BUTTON_COLOR_ORANGE,
            QUEST_BUTTON_COLOR_YELLOW, QUEST_BUTTON_COLOR_GREEN, QUEST_BUTTON_COLOR_AQUA,
            QUEST_BUTTON_COLOR_BLUE, QUEST_BUTTON_COLOR_PURPLE, QUEST_BUTTON_COLOR_PINK,
            QUEST_BUTTON_COLOR_WHITE
        )

        const val QUEST_EFFECT_MAX_L = 5L

        const val QUEST_EFFECT_TYPE_BOX = 1L
        const val QUEST_EFFECT_TYPE_RESET_BOX = 2L
        const val QUEST_EFFECT_TYPE_VIBRATE = 3L
        const val QUEST_EFFECT_TYPE_UNKNOWN = 100L

        const val QUEST_EFFECT_VIBRATE_COUNT_MAX = 5L
        const val QUEST_EFFECT_VIBRATE_LENGTH_MAX = 1000L
        const val QUEST_EFFECT_VIBRATE_DELAY_START_MAX = 5000L
        const val QUEST_EFFECT_VIBRATE_DELAY_BETWEEN_MIN = 50L
        const val QUEST_EFFECT_VIBRATE_DELAY_BETWEEN_INF_MIN = 200L
        const val QUEST_EFFECT_VIBRATE_DELAY_BETWEEN_MAX = 5000L

        const val QUEST_CONDITION_VALUE_LITERAL_LONG = 1L
        const val QUEST_CONDITION_VALUE_LITERAL_TEXT = 2L
        const val QUEST_CONDITION_VALUE_LITERAL_BOOL = 3L
        const val QUEST_CONDITION_VALUE_VAR = 4L

        const val QUEST_CONDITION_LESS = 1L     // <
        const val QUEST_CONDITION_LEQ = 2L      // <=
        const val QUEST_CONDITION_EQ = 3L       // ==
        const val QUEST_CONDITION_NEQ = 4L      // !=
        const val QUEST_CONDITION_GEQ = 5L      // >=
        const val QUEST_CONDITION_GREATER = 6L  // >

        // explanations are in QuestPartAction.kt
        const val QUEST_ACTION_SET_LITERAL = 1L
        const val QUEST_ACTION_SET_RANDOM  = 2L
        const val QUEST_ACTION_SET_ANOTHER = 3L
        const val QUEST_ACTION_ADD_LITERAL = 4L
        const val QUEST_ACTION_ADD_ANOTHER = 5L
        const val QUEST_ACTION_SUB_ANOTHER = 11L
        const val QUEST_ACTION_SET_ARANDOM = 6L
        const val QUEST_ACTION_MULTIPLY    = 7L
        const val QUEST_ACTION_DIVIDE      = 8L
        const val QUEST_ACTION_BIT_AND     = 9L
        const val QUEST_ACTION_BIT_OR      = 10L

        const val QUEST_PART_TYPE_TEXT = 1L
        const val QUEST_PART_TYPE_CONDITION = 2L
        const val QUEST_PART_TYPE_ACTION = 3L
        const val QUEST_PART_TYPE_UNKNOWN = 100L

        //
        //  Language
        //

        val LANGUAGES: ArrayList<Language> = ArrayList()

        const val LANGUAGE_EN = 1L
        const val LANGUAGE_RU = 2L
        const val LANGUAGE_PT = 3L
        const val LANGUAGE_UK = 4L
        const val LANGUAGE_DE = 5L
        const val LANGUAGE_IT = 6L
        const val LANGUAGE_PL = 7L
        const val LANGUAGE_FR = 8L

        init {
            LANGUAGES.add(Language(LANGUAGE_EN, "English", "en"))
            LANGUAGES.add(Language(LANGUAGE_RU, "Русский", "ru"))
            LANGUAGES.add(Language(LANGUAGE_PT, "Portugues", "pt"))
            LANGUAGES.add(Language(LANGUAGE_UK, "Українська", "uk"))
            LANGUAGES.add(Language(LANGUAGE_DE, "Deutsch", "de"))
            LANGUAGES.add(Language(LANGUAGE_IT, "Italiano", "it"))
            LANGUAGES.add(Language(LANGUAGE_PL, "Polski", "pl"))
            LANGUAGES.add(Language(LANGUAGE_FR, "Français", "fr"))

            val list = LANGUAGES.sortedWith(compareBy { it.name })
            LANGUAGES.clear()
            for (i in list) LANGUAGES.add(i)
        }

        fun selector(p: Language): String = p.name

        fun getLanguage(code: String): Language {
            for (i in LANGUAGES) if (i.code == code.lowercase(Locale.getDefault())) return i
            return LANGUAGES[0]
        }

        fun getLanguage(languageId: Long): Language {
            for (l in LANGUAGES) if (l.id == languageId) return l
            return LANGUAGES[0]
        }

        fun isLanguageExsit(languageId: Long): Boolean {
            for (l in LANGUAGES) if (l.id == languageId) return true
            return false
        }

    }


    override fun getApiVersion() = VERSION

}
