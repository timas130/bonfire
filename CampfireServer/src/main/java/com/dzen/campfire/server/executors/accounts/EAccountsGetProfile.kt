package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.account.AccountLinks
import com.dzen.campfire.api.models.publications.post.PublicationPost
import com.dzen.campfire.api.requests.accounts.RAccountsGetProfile
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.*
import com.dzen.campfire.server.optimizers.*
import com.dzen.campfire.server.tables.TAccounts

class EAccountsGetProfile : RAccountsGetProfile(0, "") {

    override fun check() {

    }

    @Throws(ApiException::class)
    override fun execute(): Response {

        if (accountId == 0L) accountId = ControllerAccounts.getByName(accountName)

        val v = ControllerAccounts.get(accountId,
                TAccounts.ban_date,
                TAccounts.lvl,
                TAccounts.date_create,
                TAccounts.isInFollowsList(apiAccount.id),
                TAccounts.isInFollowingList(apiAccount.id),
                TAccounts.FOLLOWS_COUNT,
                TAccounts.FOLLOWERS_COUNT,
                TAccounts.img_title_id,
                TAccounts.img_title_gif_id,
                TAccounts.STATUS,
                TAccounts.AGE,
                TAccounts.DESCRIPTION,
                TAccounts.LINKS,
                TAccounts.NOTE(apiAccount.id),
                TAccounts.PINED_POST_ID,
                TAccounts.BANS_COUNT,
                TAccounts.WARNS_COUNT,
                TAccounts.karma_count_total
        )

        if (v.isEmpty()) throw ApiException(API.ERROR_GONE)

        val banDate: Long = v.next()//0
        val lvl: Long = v.next()
        val dateCreate:Long = v.next()//1543007004777
        val isInFollows = v.next<Any>() as Long == 1L//0
        val followsYou = v.next<Any>() as Long == 1L//0
        val followsCount = v.next<Long>()//124
        val followersCount = v.next<Long>()//146
        val imageTitleId = v.next<Long>()//480063
        val imageTitleGifId = v.next<Long>()//0
        val status: String = v.next()//{Grey Нажмите для изменения статуса}
        val age:Long = v.next()
        val description: String =  v.next()//Описание профиля ещё не заполнено.
        val links = AccountLinks(v.next())
        val note:String = v.next()
        val pinnedPostId:Long = v.next()
        val bansCount:Long = v.next()
        val warnsCount:Long = v.next()
        val karmaTotal:Long = v.next()
        val rates = ControllerAccounts.get(accountId, TAccounts.RATES_COUNT_NO_ANON).nextMayNull<Long>() ?: 0L
        val ratesPositive = EAccountsGetStory.getRatesSum(accountId, true) / 100
        val ratesNegative = EAccountsGetStory.getRatesSum(accountId, false) / 100
        val moderationFandomsCount = if (lvl < API.LVL_MODERATOR_BLOCK.lvl) {
            0L
        } else {
            ControllerFandom.getModerationFandomsCount(accountId)
        }
        val subscribedFandomsCount = ControllerCollisions.getCollisionsCount(accountId, API.COLLISION_FANDOM_SUBSCRIBE)
        val stickersCount = ControllerStickers.getStickersCount(accountId)
        val blackAccountsCount = ControllerAccounts.getBlackListAccountCount(accountId)
        val blackFandomsCount = ControllerAccounts.getBlackListFandomCount(accountId)

        var pinnedPost: PublicationPost? = if (pinnedPostId > 0) ControllerPublications.getPublication(pinnedPostId, apiAccount.id) as PublicationPost? else null
        if (pinnedPost != null && pinnedPost.status != API.STATUS_PUBLIC) pinnedPost = null

        return Response(
            dateCreate,
            pinnedPost,
            banDate,
            status,
            note,
            isInFollows,
            followsYou,
            followsCount,
            followersCount,
            age,
            description,
            links,
            imageTitleId,
            imageTitleGifId,
            bansCount,
            warnsCount,
            karmaTotal,
            rates,
            ratesPositive,
            ratesNegative,
            moderationFandomsCount,
            subscribedFandomsCount,
            stickersCount,
            blackAccountsCount,
            blackFandomsCount,
        )
    }

}
