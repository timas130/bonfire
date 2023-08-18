package com.dzen.campfire.server.executors.post

import com.dzen.campfire.api.models.account.Account
import com.dzen.campfire.api.models.fandoms.Fandom
import com.dzen.campfire.api.models.publications.Rate
import com.dzen.campfire.api.requests.post.RPostRatesGetAll
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerOptimizer
import com.dzen.campfire.server.tables.TAccounts
import com.dzen.campfire.server.tables.TPublicationsKarmaTransactions
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect

class EPostRatesGetAll : RPostRatesGetAll(0, 0) {

    override fun check() {

    }

    override fun execute(): Response {

        val v = Database.select("EPostRatesGetAll", SqlQuerySelect(TPublicationsKarmaTransactions.NAME,
                TPublicationsKarmaTransactions.karma_count,
                TPublicationsKarmaTransactions.karma_cof,
                TPublicationsKarmaTransactions.date_create,
                TPublicationsKarmaTransactions.anonymous,
                TPublicationsKarmaTransactions.PUBLICATION_PARENT_ID,
                TPublicationsKarmaTransactions.PUBLICATION_PARENT_TYPE,
                TPublicationsKarmaTransactions.PUBLICATION_TYPE + " publication_type",
                TPublicationsKarmaTransactions.fandom_id,
                TPublicationsKarmaTransactions.language_id,
                TPublicationsKarmaTransactions.FANDOM_NAME,
                TPublicationsKarmaTransactions.FANDOM_IMAGE_ID,
                TPublicationsKarmaTransactions.FANDOM_CLOSED,
                TPublicationsKarmaTransactions.FANDOM_KARMA_COF)
                .where(TPublicationsKarmaTransactions.publication_id, "=", publicationId)
                .where(TAccounts.NAME + "." + TAccounts.id, "=", TPublicationsKarmaTransactions.from_account_id)
                .join(SqlQuerySelect(TAccounts.NAME,
                        TAccounts.id,
                        TAccounts.lvl,
                        TAccounts.last_online_time,
                        TAccounts.name,
                        TAccounts.img_id,
                        TAccounts.sex,
                        TAccounts.karma_count_30))
                .offset_count(offset, COUNT)
                .sort(TPublicationsKarmaTransactions.NAME + "." + TPublicationsKarmaTransactions.karma_count, false))

        val array = Array(v.rowsCount) {
            val r = Rate()
            r.karmaCount = v.next()
            r.karmaCof = v.next()
            r.date = v.next()
            r.anonymous = v.next()
            r.publicationId = publicationId
            r.publicationParentId = v.next()
            r.publicationParentType = v.next()
            r.publicationType = v.next()

            r.fandom = Fandom(v.nextLongOrZero(), v.nextLongOrZero(), v.nextMayNull<String>()?:"", v.nextLongOrZero(), v.nextLongOrZero()==1L, v.nextLongOrZero())

            r.account = ControllerAccounts.instance(v)

            if (r.anonymous != 0L) {
                r.account = Account()
            }

            r
        }

        return Response(array)
    }


}
