package sh.sit.bonfire.networking

import android.content.Context
import androidx.annotation.WorkerThread
import com.dzen.campfire.api.tools.client.NetworkingProvider
import com.google.net.cronet.okhttptransport.CronetInterceptor
import com.sup.dev.java.libs.debug.info
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.chromium.net.CronetEngine
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

class OkHttpNetworkingProvider(
    context: Context,
    private val apiRoot: String,
) : NetworkingProvider {
    private val client = OkHttpController.getClient(context)

    @WorkerThread
    override fun sendRequest(data: ByteArray, additional: List<ByteArray?>): ByteArray {
        val body = ByteArrayOutputStream(additional.sumOf { it?.size ?: 0 } + data.size + 4)
            .apply {
                val dos = DataOutputStream(this)
                dos.writeInt(data.size)
                dos.write(data)
                additional.forEach {
                    it?.let { dos.write(it) }
                }
            }
            .toByteArray()
            .toRequestBody()

        val call = client.newCall(
            Request.Builder()
                .url(apiRoot)
                .post(body)
                .build()
        )
        val response = call.execute()
        info("OkHttpNetworkingProvider sendRequest used protocol=${response.protocol}")
        return response.use { it.body!!.bytes() }
    }
}
