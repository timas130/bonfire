package com.dzen.campfire.server.executors.project

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.project.RProjectStatistic
import com.dzen.campfire.server.tables.TAccounts
import com.dzen.campfire.server.tables.TAccountsEnters
import com.dzen.campfire.server.tables.TPublications
import com.dzen.campfire.server.tables.TPublicationsKarmaTransactions
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.Sql
import com.sup.dev.java_pc.sql.SqlQuerySelect

class EProjectStatistic : RProjectStatistic() {


    companion object{

        var lastUpdate = 0L
        var last_accounts = ArrayList<Long>()
        var last_posts = ArrayList<Long>()
        var last_comments = ArrayList<Long>()
        var last_messages = ArrayList<Long>()
        var last_enters = ArrayList<Long>()
        var last_rates = ArrayList<Long>()

    }

    override fun check() {

    }

    override fun execute(): Response {

        if(lastUpdate > System.currentTimeMillis() - 1000L * 60 * 60 * 6){
            return Response(last_accounts.toTypedArray(), last_posts.toTypedArray(), last_comments.toTypedArray(), last_messages.toTypedArray(), last_enters.toTypedArray(), last_rates.toTypedArray())
        }

        lastUpdate = System.currentTimeMillis()

        val dates = arrayOf(7L, 30L, 90L, 180L)

        val accounts = ArrayList<Long>()
        val posts = ArrayList<Long>()
        val comments = ArrayList<Long>()
        val messages = ArrayList<Long>()
        val enters = ArrayList<Long>()
        val rates = ArrayList<Long>()

        accounts.add(Database.select("EProjectStatistic select_1",SqlQuerySelect(TAccounts.NAME, Sql.COUNT)).next())
        for(d in dates) load(accounts, d, TAccounts.NAME, TAccounts.date_create)

        posts.add(Database.select("EProjectStatistic select_2",SqlQuerySelect(TPublications.NAME, Sql.COUNT).where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_POST)).next())
        for(d in dates) loadUnit(posts, d, API.PUBLICATION_TYPE_POST)

        comments.add(Database.select("EProjectStatistic select_3",SqlQuerySelect(TPublications.NAME, Sql.COUNT).where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_COMMENT)).next())
        for(d in dates) loadUnit(comments, d, API.PUBLICATION_TYPE_COMMENT)

        messages.add(Database.select("EProjectStatistic select_4",SqlQuerySelect(TPublications.NAME, Sql.COUNT).where(TPublications.publication_type, "=", API.PUBLICATION_TYPE_CHAT_MESSAGE)).next())
        for(d in dates) loadUnit(messages, d, API.PUBLICATION_TYPE_CHAT_MESSAGE)

        enters.add(Database.select("EProjectStatistic select_5",SqlQuerySelect(TAccountsEnters.NAME, Sql.COUNT)).next())
        for(d in dates) load(enters, d, TAccountsEnters.NAME, TAccountsEnters.date_create)

        rates.add(Database.select("EProjectStatistic select_6",SqlQuerySelect(TPublicationsKarmaTransactions.NAME, Sql.COUNT)).next())
        for(d in dates) load(rates, d, TPublicationsKarmaTransactions.NAME, TPublicationsKarmaTransactions.date_create)

        last_accounts = accounts
        last_posts = posts
        last_comments = comments
        last_messages = messages
        last_enters = enters
        last_rates = rates

        return Response(accounts.toTypedArray(), posts.toTypedArray(), comments.toTypedArray(), messages.toTypedArray(), enters.toTypedArray(), rates.toTypedArray())
    }


    private fun load(list:ArrayList<Long>, d:Long, table:String, date:String){
        val d1 = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * d
        val d2 = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * (d*2)
        list.add(Database.select("EProjectStatistic load_1",SqlQuerySelect(table, Sql.COUNT).where(date, ">=", d1)).next())
        list.add(Database.select("EProjectStatistic load_2",SqlQuerySelect(table, Sql.COUNT).where(date, "<", d1).where(date, ">=", d2)).next())

    }

    private fun loadUnit(list:ArrayList<Long>, d:Long, type:Long){
        val d1 = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * d
        val d2 = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * (d*2)
        list.add(Database.select("EProjectStatistic loadUnit_1",SqlQuerySelect(TPublications.NAME, Sql.COUNT).where(TPublications.publication_type, "=", type).where(TPublications.date_create, ">=", d1)).next())
        list.add(Database.select("EProjectStatistic loadUnit_2",SqlQuerySelect(TPublications.NAME, Sql.COUNT).where(TPublications.publication_type, "=", type).where(TPublications.date_create, "<", d1).where(TPublications.date_create, ">=", d2)).next())


    }
}
