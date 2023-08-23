package com.dzen.campfire.api.tools.client

import com.sup.dev.java.libs.debug.info
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

class HttpsClientNetworkingProvider(val client: HTTPSClient) : NetworkingProvider {
    override fun sendRequest(data: ByteArray, additional: List<ByteArray?>): ByteArray {
        info("HttpsClientNetworkingProvider sendRequest used")

        val conn = client.connect()
        val dataOutputStream = DataOutputStream(conn.out)
        dataOutputStream.writeInt(data.size)
        dataOutputStream.write(data)
        for (d in additional) {
            d?.let { dataOutputStream.write(it) }
        }
        dataOutputStream.flush()

        val dataInputStream = DataInputStream(conn.input)
        var size = dataInputStream.readInt()
        val byteArrayReader = ByteArrayOutputStream(size.coerceAtMost(1024 * 1024))

        var read: Int
        val buffer = ByteArray(8192)
        while (dataInputStream.read(buffer, 0, size.coerceAtMost(8192)).also { read = it } >= 0) {
            byteArrayReader.write(buffer, 0, read)
            size -= read
        }

        dataInputStream.close()
        return byteArrayReader.toByteArray()
    }
}
