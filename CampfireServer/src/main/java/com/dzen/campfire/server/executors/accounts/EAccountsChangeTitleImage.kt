package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.accounts.RAccountsChangeTitleImage
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerAchievements
import com.dzen.campfire.server.controllers.ControllerFandom
import com.dzen.campfire.server.controllers.ControllerResources
import com.dzen.campfire.server.tables.TAccounts
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java_pc.sql.Database
import com.sup.dev.java_pc.sql.SqlQueryUpdate
import com.sup.dev.java_pc.tools.ToolsImage


class EAccountsChangeTitleImage : RAccountsChangeTitleImage(null, null) {

    @Throws(ApiException::class)
    override fun check() {
        ControllerFandom.checkCan(apiAccount, API.LVL_CAN_CHANGE_PROFILE_IMAGE)
        if (image!!.size > API.ACCOUNT_TITLE_IMG_WEIGHT) throw ApiException(E_BAD_IMG_WEIGHT)

        if (imageGif != null) {
            ControllerFandom.checkCan(apiAccount, API.LVL_CAN_CHANGE_PROFILE_IMAGE_GIF)

            if (imageGif!!.size > API.ACCOUNT_TITLE_IMG_GIF_WEIGHT) throw ApiException(E_BAD_IMG_WEIGHT)
            if (!ToolsImage.checkImageMaxScaleUnknownType(imageGif!!, API.ACCOUNT_TITLE_IMG_GIF_W, API.ACCOUNT_TITLE_IMG_GIF_H, true, true, true)) throw ApiException(E_BAD_IMG_SIDES)
            if (!ToolsImage.checkImageMaxScaleUnknownType(image!!, API.ACCOUNT_TITLE_IMG_GIF_W, API.ACCOUNT_TITLE_IMG_GIF_H, true, false, true)) throw ApiException(E_BAD_IMG_SIDES)
        }else{
            if (!ToolsImage.checkImageMaxScaleUnknownType(image!!, API.ACCOUNT_TITLE_IMG_W, API.ACCOUNT_TITLE_IMG_H, true, false, true)) throw ApiException(E_BAD_IMG_SIDES)
        }
    }

    override fun execute(): Response {
        val v = ControllerAccounts.get(apiAccount.id, TAccounts.img_title_id, TAccounts.img_title_gif_id)
        var imageId = v.next<Long>()
        var imageGifId = v.next<Long>()

        imageId = ControllerResources.removeAndPut(imageId, image!!, API.RESOURCES_PUBLICATION_DATABASE_LINKED)

        if (imageGif != null) imageGifId = ControllerResources.removeAndPut(imageGifId, imageGif!!, API.RESOURCES_PUBLICATION_DATABASE_LINKED)
        else if (imageGifId != 0L) {
            ControllerResources.remove(imageGifId)
            imageGifId = 0L
        }

        Database.update("EAccountsChangeTitleImage", SqlQueryUpdate(TAccounts.NAME)
                .where(TAccounts.id, "=", apiAccount.id)
                .update(TAccounts.img_title_id, imageId)
                .update(TAccounts.img_title_gif_id, imageGifId))

        ControllerAchievements.addAchievementWithCheck(apiAccount.id, API.ACHI_TITLE_IMAGE)
        return Response(imageId, imageGifId)
    }


}