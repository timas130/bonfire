package com.dzen.campfire.api_media

import com.dzen.campfire.api.tools.client.ApiClient
import com.dzen.campfire.api.tools.client.TokenProvider

class APIMedia(
        projectKey:String,
        tokenProvider: TokenProvider,
        host: String,
        portHttps: Int,
        portCertificate: Int,
        saver: (String, String?) -> Unit,
        loader: (String) -> String?
) : ApiClient(projectKey, tokenProvider, host, portHttps, portCertificate, saver, loader) {

    companion object {
        const val PORT_SERV_JL_V1 = 7080
        const val PORT_SERV_JL = 7081
        const val SERV_ROOT = "https://cf2.bonfire.moe/media"

        const val PORT_HTTPS = 4023
        const val PORT_HTTP = 4022
        const val PORT_CERTIFICATE = 4024
        const val IP = "cf.bonfire.moe"
        const val VERSION = "1"

        const val ERROR_GONE = "ERROR_GONE"
        const val ERROR_ACCESS = "ERROR_ACCESS"
    }

    override fun getApiVersion() = VERSION

}
