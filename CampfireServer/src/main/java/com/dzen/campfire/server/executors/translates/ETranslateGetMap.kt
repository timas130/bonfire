package com.dzen.campfire.server.executors.translates

import com.dzen.campfire.api.API
import com.dzen.campfire.api.requests.translates.RTranslateGetMap
import com.dzen.campfire.api.tools.ApiException

class ETranslateGetMap : RTranslateGetMap(0) {
    override fun execute(): Response {
        throw ApiException(API.ERROR_GONE)
    }
}
