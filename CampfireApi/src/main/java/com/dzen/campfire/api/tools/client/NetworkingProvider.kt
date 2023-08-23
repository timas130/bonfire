package com.dzen.campfire.api.tools.client

interface NetworkingProvider {
    fun sendRequest(data: ByteArray, additional: List<ByteArray?>): ByteArray
}
