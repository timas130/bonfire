package sh.sit.bonfire.networking

import com.dzen.campfire.api.tools.client.HttpsClientNetworkingProvider
import com.dzen.campfire.api.tools.client.NetworkingProvider

class CombinedNetworkingProvider(
    private val https: HttpsClientNetworkingProvider,
    private val okhttp: OkHttpNetworkingProvider,
) : NetworkingProvider {
    private var failures = 0

    override fun sendRequest(data: ByteArray, additional: List<ByteArray?>): ByteArray {
        return try {
            if (failures > 3) {
                https.sendRequest(data, additional)
            } else {
                okhttp.sendRequest(data, additional)
            }
        } catch (e: Throwable) {
            failures++
            throw e
        }
    }
}
