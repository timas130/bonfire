package com.dzen.campfire.api.tools.client

interface TokenProvider {
    fun getAccessToken(): String?
}
