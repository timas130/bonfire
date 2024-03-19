package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.accounts.RAccountsChangeAvatar
import com.dzen.campfire.api.tools.ApiException
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.controllers.ControllerResources
import com.dzen.campfire.server.tables.TAccounts
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate
import com.sup.dev.java_pc.tools.ToolsImage


class EAccountsChangeAvatar : RAccountsChangeAvatar(null) {
    @Throws(ApiException::class)
    override fun check() {
        ControllerAccounts.checkAccountBanned(apiAccount.id, 0, 0)

        if (ToolsImage.isGIF(image!!)) {
            ControllerFandom.checkCan(apiAccount, API.LVL_CAN_CHANGE_AVATAR_GIF)
            if (image!!.size > API.ACCOUNT_IMG_WEIGHT_GIF) throw ApiException(
                E_BAD_IMG_WEIGHT,
                " " + image!!.size + " > " + API.ACCOUNT_IMG_WEIGHT_GIF
            )
            if (!ToolsImage.checkImageMaxScaleUnknownType(
                    image!!,
                    API.ACCOUNT_IMG_SIDE_GIF,
                    API.ACCOUNT_IMG_SIDE_GIF,
                    png = true,
                    gif = true,
                    jpg = true
                )
            ) throw ApiException(E_BAD_IMG_SIDES)
        } else {
            if (image!!.size > API.ACCOUNT_IMG_WEIGHT) throw ApiException(
                E_BAD_IMG_WEIGHT,
                " " + image!!.size + " > " + API.ACCOUNT_IMG_WEIGHT
            )
            if (!ToolsImage.checkImageMaxScaleUnknownType(
                    image!!,
                    API.ACCOUNT_IMG_SIDE,
                    API.ACCOUNT_IMG_SIDE,
                    png = true,
                    gif = true,
                    jpg = true
                )
            ) throw ApiException(E_BAD_IMG_SIDES)
        }
    }

    override fun execute(): Response {
        val v = ControllerAccounts.get(apiAccount.id, TAccounts.img_id)

        val imgId = v.next<Long>()

        Database.update("EAccountsChangeAvatar update", SqlQueryUpdate(TAccounts.NAME)
            .update(TAccounts.img_id, ControllerResources.removeAndPut(imgId, image!!, API.RESOURCES_PUBLICATION_DATABASE_LINKED))
            .where(TAccounts.id, "=", apiAccount.id))

        return Response()
    }
}
