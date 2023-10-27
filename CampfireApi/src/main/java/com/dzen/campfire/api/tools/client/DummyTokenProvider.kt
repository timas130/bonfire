package com.dzen.campfire.api.tools.client

object DummyTokenProvider : TokenProvider {
    override fun getAccessToken(): String? {
        return ""
    }
}
