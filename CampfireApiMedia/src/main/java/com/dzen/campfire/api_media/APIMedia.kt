package com.dzen.campfire.api_media

import com.dzen.campfire.api.tools.client.ApiClient
import com.dzen.campfire.api.tools.client.TokenProvider

class APIMedia(
    projectKey: String,
    tokenProvider: TokenProvider,
) : ApiClient(projectKey, tokenProvider) {

    companion object {
        const val PORT_SERV_JL_V1 = 7080
        const val PORT_SERV_JL = 7081
        const val SERV_ROOT = "https://cf2.bonfire.moe/media"

        const val VERSION = "1"

        const val ERROR_GONE = "ERROR_GONE"
        const val ERROR_ACCESS = "ERROR_ACCESS"
    }

    override fun getApiVersion() = VERSION

}
