package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.accounts.RAccountsRegistration
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.app.App
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerResources
import com.dzen.campfire.server.tables.TAccounts
import com.sup.dev.java.tools.ToolsFiles
import com.sup.dev.java_pc.google.GoogleAuth
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryInsert
import com.sup.dev.java_pc.sql.SqlQueryUpdate
import com.sup.dev.java_pc.tools.ToolsImage

class EAccountsRegistration : RAccountsRegistration(0, null) {
    private var googleId: String? = null

    @Throws(ApiException::class)
    override fun check() {
        if (loginToken == null) throw RuntimeException("No google token")

        googleId = GoogleAuth.getGoogleId(loginToken!!)

        if (googleId != null && ControllerAccounts.checkGoogleIdExist(googleId!!))
            throw ApiException(E_GOOGLE_ID_EXIST)
        if (image != null) {
            if (image!!.size > API.ACCOUNT_IMG_WEIGHT)
                throw ApiException(E_IMAGE_WEIGHT)
            if (!ToolsImage.checkImageScaleUnknownType(image!!, API.ACCOUNT_IMG_SIDE, API.ACCOUNT_IMG_SIDE, true, false, true))
                throw ApiException(E_IMAGE_SCALE)
        }
    }

    override fun execute(): Response {
        if (image == null) image = ToolsFiles.readFileSalient("${App.patchPrefix}res/def_image.png")
        val imgId = ControllerResources.put(image!!, API.RESOURCES_PUBLICATION_DATABASE_LINKED)

        val accountId = Database.insert("EAccountsRegistration insert",TAccounts.NAME,
            TAccounts.google_id, googleId!!,
            TAccounts.date_create, System.currentTimeMillis(),
            TAccounts.name, System.currentTimeMillis(),
            TAccounts.img_id, imgId,
            TAccounts.last_online_time, System.currentTimeMillis(),
            TAccounts.subscribes, "",
            TAccounts.refresh_token, "",
            TAccounts.refresh_token_date_create, 0L,
            TAccounts.account_settings, ""
        )

        Database.update("EAccountsRegistration update", SqlQueryUpdate(TAccounts.NAME)
            .updateValue(TAccounts.name, "user#$accountId")
            .where(TAccounts.id, "=", accountId))

        try {
            Database.insert(
                "EAccountsRegistration i2", SqlQueryInsert("users")
                    .put("id", accountId)
                    .putValue("username", "user#$accountId")
                    .put("email_verified", "now()")
            )
            Database.insert(
                "EAccountsRegistration i3", SqlQueryInsert("auth_sources")
                    .put("user_id", accountId)
                    .put("provider", 2)
                    .putValue("provider_account_id", googleId!!)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return Response(accountId, imgId)
    }

}
