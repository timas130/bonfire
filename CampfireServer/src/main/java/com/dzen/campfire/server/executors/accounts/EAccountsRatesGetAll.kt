package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.models.publications.Rate
import com.dzen.campfire.api.requests.accounts.RAccountsRatesGetAll
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerOptimizer
import com.dzen.campfire.server.tables.TPublicationsKarmaTransactions
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect

class EAccountsRatesGetAll : RAccountsRatesGetAll(0, 0) {

    override fun check() {

    }

    override fun execute(): Response {

        val v = Database.select("EAccountsRatesGetAll", SqlQuerySelect(TPublicationsKarmaTransactions.NAME,
                TPublicationsKarmaTransactions.karma_count,
                TPublicationsKarmaTransactions.karma_cof,
                TPublicationsKarmaTransactions.date_create,
                TPublicationsKarmaTransactions.publication_id,
                TPublicationsKarmaTransactions.PUBLICATION_PARENT_ID,
                TPublicationsKarmaTransactions.PUBLICATION_PARENT_TYPE,
                TPublicationsKarmaTransactions.PUBLICATION_TYPE + " publication_type",
                TPublicationsKarmaTransactions.fandom_id,
                TPublicationsKarmaTransactions.language_id,
                TPublicationsKarmaTransactions.FANDOM_NAME,
                TPublicationsKarmaTransactions.FANDOM_IMAGE_ID,
                TPublicationsKarmaTransactions.FANDOM_CLOSED,
                TPublicationsKarmaTransactions.FANDOM_KARMA_COF,
                TPublicationsKarmaTransactions.from_account_id,
                TPublicationsKarmaTransactions.FROM_LEVEL,
                TPublicationsKarmaTransactions.FROM_LAST_ONLINE_TIME,
                TPublicationsKarmaTransactions.FROM_NAME,
                TPublicationsKarmaTransactions.FROM_IMAGE_ID,
                TPublicationsKarmaTransactions.FROM_SEX,
                TPublicationsKarmaTransactions.FROM_KARMA_30
        )
                .where(TPublicationsKarmaTransactions.from_account_id, "=", accountId)
                .where(TPublicationsKarmaTransactions.PUBLICATION_TYPE, "<>", 0)
                .where(TPublicationsKarmaTransactions.anonymous, "=", 0)
                .offset_count(offset, COUNT)
                .sort(TPublicationsKarmaTransactions.date_create, false))

        val array = Array(v.rowsCount) {
            val r = Rate()
            r.karmaCount = v.next()
            r.karmaCof = v.next()
            r.date = v.next()
            r.publicationId = v.next()
            r.publicationParentId = v.next()
            r.publicationParentType = v.next()
            r.publicationType = v.next()
            r.fandom = Fandom(v.next(), v.next(), v.nextMayNull()?:"", v.nextLongOrZero(), v.nextLongOrZero()==1L, v.nextLongOrZero())
            r.account = ControllerAccounts.instance(v)
            r
        }


        return Response(array)
    }


}
