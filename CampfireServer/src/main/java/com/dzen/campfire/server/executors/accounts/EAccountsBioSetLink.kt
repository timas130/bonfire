package com.dzen.campfire.server.executors.accounts

import com.dzen.campfire.api.API
import com.dzen.campfire.api.models.account.AccountLinks
import com.dzen.campfire.api.requests.accounts.RAccountsBioSetLink
import com.dzen.campfire.server.controllers.ControllerAccounts
import com.dzen.campfire.server.controllers.ControllerCensor
import com.dzen.campfire.server.controllers.ControllerCollisions
import com.dzen.campfire.api.tools.ApiException
import com.sup.dev.java.libs.json.Json


class EAccountsBioSetLink : RAccountsBioSetLink(0, "", "") {

    override fun check() {
        title = ControllerCensor.cens(title)
        if (title.length > API.ACCOUNT_LINK_TITLE_MAX_L) throw ApiException(E_BAD_TITLE)
        if (url.length > API.ACCOUNT_LINK_URL_MAX_L) throw ApiException(E_BAD_URL)
        ControllerAccounts.checkAccountBanned(apiAccount.id)
    }

    override fun execute(): Response {
        val links = AccountLinks(ControllerCollisions.getCollisionValue2(apiAccount.id, API.COLLISION_ACCOUNT_LINKS))

        links.set(index, title, url)
        ControllerCollisions.updateOrCreateValue2(apiAccount.id, API.COLLISION_ACCOUNT_LINKS, links.json(true, Json()).toString())

        return Response()
    }


}