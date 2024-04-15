package com.dzen.campfire.server.app

import com.dzen.campfire.api.API
import com.dzen.campfire.api.tools.ApiAccount
import com.dzen.campfire.api.tools.server.AccountProvider
import com.dzen.campfire.server.controllers.ControllerResources
import com.dzen.campfire.server.fragment.ShortUser
import com.dzen.campfire.server.rust.RustAuth
import com.dzen.campfire.server.tables.TAccounts
import com.sup.dev.java.libs.debug.err
import com.sup.dev.java.tools.ToolsFiles
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQuerySelect

class AccountProviderImpl : AccountProvider() {
    companion object {
        var PROTOADMIN_AUTORIZATION_ID = 0L
    }

    override fun getByAccessToken(token: String?): ApiAccount? {
        val user = RustAuth.getByToken(token ?: return null) ?: return null

        val account = select(instanceSelect().where(TAccounts.id, "=", user.id))
        if (account != null) return account

        createAccount(user)

        return select(instanceSelect().where(TAccounts.id, "=", user.id))
    }

    private fun instanceSelect() = SqlQuerySelect(TAccounts.NAME,
            TAccounts.id,
            TAccounts.img_id,
            TAccounts.name,
            TAccounts.sex,
            TAccounts.lvl,
            TAccounts.karma_count_30,
            TAccounts.account_settings,
            TAccounts.subscribes,
            TAccounts.date_create)

    private fun select(select: SqlQuerySelect): ApiAccount? {
        val v = Database.select("AccountProviderImpl.select", select)

        if (v.isEmpty) return null

        val account = ApiAccount()
        account.id = v.next()
        account.imageId = v.next()
        account.name = v.next()
        account.sex = v.next()
        account.accessTag = v.next()
        account.accessTagSub = v.next()
        account.settings = v.nextMayNull<String>()?:""
        account.tag_s_1 = v.next()
        account.dateCreate = v.next()

        if (account.id == 1L && PROTOADMIN_AUTORIZATION_ID != 0L && account.id != PROTOADMIN_AUTORIZATION_ID) {
            return select(instanceSelect().where(TAccounts.id, "=", PROTOADMIN_AUTORIZATION_ID))
        }

        return account
    }

    private fun createAccount(user: ShortUser) {
        var image = ToolsFiles.readFileSalient("${App.patchPrefix}res/def_image.png")
        if (image == null) image = ToolsFiles.readFileSalient("CampfireServer/res/def_image.png")
        val imgId = try {
            ControllerResources.put(image!!, API.RESOURCES_PUBLICATION_DATABASE_LINKED)
        } catch (e: Exception) {
            err(e)
            err("failed to set avatar: $e")
            0
        }

        Database.insert(
            "AccountProviderImpl.createAccount", TAccounts.NAME,
            TAccounts.id, user.id.toLong(),
            TAccounts.name, user.username,
            TAccounts.google_id, "",
            TAccounts.date_create, System.currentTimeMillis(),
            TAccounts.img_id, imgId,
            TAccounts.last_online_time, System.currentTimeMillis(),
            TAccounts.subscribes, "",
            TAccounts.account_settings, ""
        )
    }
}
