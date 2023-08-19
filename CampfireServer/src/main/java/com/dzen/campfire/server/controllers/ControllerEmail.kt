package com.dzen.campfire.server.controllers

import com.dzen.campfire.server.tables.TAccountsEmails
import com.sup.dev.java.tools.ToolsCryptography
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect
import com.sup.dev.java_pc.sql.SqlQueryUpdate

object ControllerEmail {

    fun checkExist(email:String):Boolean{
        return Database.select("ControllerEmail.checkExist", SqlQuerySelect(TAccountsEmails.NAME, TAccountsEmails.id)
                .whereValue(TAccountsEmails.account_email, "=", email)
        ).hasNext()
    }

    fun getAccountId(email:String, password: String):Long{

        val rows = Database.select("ControllerEmail.getAccountId 1",
                SqlQuerySelect(TAccountsEmails.NAME, TAccountsEmails.account_id, TAccountsEmails.account_password)
                .whereValue(TAccountsEmails.account_email, "=", email)
        )

        while (rows.hasNext()){
            val accountId = rows.nextLongOrZero()
            val realPasswordBCrypt = rows.next<String>()

            if (ToolsCryptography.bcryptCheck(password, realPasswordBCrypt)) {
                return accountId
            }
        }

        return 0
    }

    fun insert(accountId:Long, email:String, password: String){
        Database.insert("ControllerEmail.insert", TAccountsEmails.NAME,
                TAccountsEmails.account_id, accountId,
                TAccountsEmails.account_email, email,
                TAccountsEmails.account_password, ToolsCryptography.bcrypt(password),
                TAccountsEmails.date_create, System.currentTimeMillis())

    }

    fun setPassword(accountId:Long, email:String, password:String){
        Database.update("ControllerEmail.setPassword", SqlQueryUpdate(TAccountsEmails.NAME)
            .where(TAccountsEmails.account_id, "=", accountId)
            .updateValue(TAccountsEmails.account_password, ToolsCryptography.bcrypt(password))
            .whereValue(TAccountsEmails.account_email, "=", email))
    }

}
