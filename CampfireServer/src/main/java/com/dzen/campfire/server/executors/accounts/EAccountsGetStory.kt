package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.accounts.RAccountsGetStory
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.tables.TPublications
import com.dzen.campfire.server.tables.TPublicationsKarmaTransactions
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.Sql
import com.sup.dev.java_pc.sql.SqlQuerySelect

class EAccountsGetStory : RAccountsGetStory(0) {

    override fun check() {

    }

    @Throws(ApiException::class)
    override fun execute(): Response {


        val karmaPlus: Long = Database.select("EAccountsGetStory select_1", SqlQuerySelect(TPublicationsKarmaTransactions.NAME, Sql.SUM(TPublicationsKarmaTransactions.karma_count))
                .where(TPublicationsKarmaTransactions.target_account_id, "=", accountId)
                .where(TPublicationsKarmaTransactions.change_account_karma, "=", true)
                .where(TPublicationsKarmaTransactions.karma_count, ">", 0)
        ).sumOrZero()

        val karmaMinus: Long = Database.select("EAccountsGetStory select_2", SqlQuerySelect(TPublicationsKarmaTransactions.NAME, Sql.SUM(TPublicationsKarmaTransactions.karma_count))
                .where(TPublicationsKarmaTransactions.target_account_id, "=", accountId)
                .where(TPublicationsKarmaTransactions.change_account_karma, "=", true)
                .where(TPublicationsKarmaTransactions.karma_count, "<", 0)
        ).sumOrZero()

        val ratesPlus: Long = Database.select("EAccountsGetStory select_3", SqlQuerySelect(TPublicationsKarmaTransactions.NAME, Sql.SUM(TPublicationsKarmaTransactions.karma_count))
                .where(TPublicationsKarmaTransactions.from_account_id, "=", accountId)
                .where(TPublicationsKarmaTransactions.change_account_karma, "=", true)
                .where(TPublicationsKarmaTransactions.karma_count, ">", 0)
        ).sumOrZero()

        val ratesMinus: Long = Database.select("EAccountsGetStory select_4", SqlQuerySelect(TPublicationsKarmaTransactions.NAME, Sql.SUM(TPublicationsKarmaTransactions.karma_count))
                .where(TPublicationsKarmaTransactions.from_account_id, "=", accountId)
                .where(TPublicationsKarmaTransactions.change_account_karma, "=", true)
                .where(TPublicationsKarmaTransactions.karma_count, "<", 0)
        ).sumOrZero()

        val posts: Long = Database.select("EAccountsGetStory select_5", SqlQuerySelect(TPublications.NAME, Sql.COUNT)
                .where(TPublications.creator_id, "=", accountId)
                .where(TPublications.status, "=", API.STATUS_PUBLIC)
                .where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_POST)
        ).nextMayNullOrNull()?:0

        val comments: Long = Database.select("EAccountsGetStory select_6", SqlQuerySelect(TPublications.NAME, Sql.COUNT)
                .where(TPublications.creator_id, "=", accountId)
                .where(TPublications.status, "=", API.STATUS_PUBLIC)
                .where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_COMMENT)
        ).nextMayNullOrNull()?:0

        val messages: Long = Database.select("EAccountsGetStory select_7", SqlQuerySelect(TPublications.NAME, Sql.COUNT)
                .where(TPublications.creator_id, "=", accountId)
                .where(TPublications.status, "=", API.STATUS_PUBLIC)
                .where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_CHAT_MESSAGE)
                .where(TPublications.tag_1, "=", API.CHAT_TYPE_FANDOM_ROOT)
        ).nextMayNullOrNull()?:0

        val bestPost: Long = Database.select("EAccountsGetStory select_9", SqlQuerySelect(TPublications.NAME, TPublications.id)
                .where(TPublications.creator_id, "=", accountId)
                .where(TPublications.status, "=", API.STATUS_PUBLIC)
                .where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_POST)
                .sort(TPublications.karma_count, false)
                .count(1)
        ).nextMayNullOrNull()?:0

        val bestCommentV = Database.select("EAccountsGetStory select_10", SqlQuerySelect(TPublications.NAME, TPublications.id, TPublications.parent_publication_id, TPublications.PARENT_PUBLICATION_TYPE)
                .where(TPublications.creator_id, "=", accountId)
                .where(TPublications.status, "=", API.STATUS_PUBLIC)
                .where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_COMMENT)
                .sort(TPublications.karma_count, false)
                .count(1)
        )

        var bestComment = 0L
        var bestCommentUnitType = 0L
        var bestCommentUnitId = 0L

        if(!bestCommentV.isEmpty){
            bestComment = bestCommentV.next()
            bestCommentUnitId = bestCommentV.next()
            bestCommentUnitType = bestCommentV.next()
        }

        return Response(karmaPlus, karmaMinus, ratesPlus, ratesMinus, comments, posts, messages, bestPost, bestComment, bestCommentUnitType, bestCommentUnitId)
    }

}