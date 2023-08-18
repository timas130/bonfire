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

        val PORT_HTTPS = 4023
        val PORT_HTTP = 4022
        val PORT_CERTIFICATE = 4024
        val IP = "46.254.16.245"
        val VERSION = "1"

        val ERROR_GONE = "ERROR_GONE"
        val ERROR_ACCESS = "ERROR_ACCESS"
    }

    override fun getApiVersion() = VERSION

}