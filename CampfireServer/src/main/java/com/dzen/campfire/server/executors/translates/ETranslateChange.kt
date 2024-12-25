package com.dzen.campfire.server.executors.translates

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.translates.RTranslateChange
import com.dzen.campfire.api.tools.ApiException

class ETranslateChange : RTranslateChange(0, 0, "", "", "") {
    override fun execute(): Response {
        throw ApiException(API.ERROR_GONE)
    }
}
