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
    userAgent: String,
) : NetworkingProvider {
    private val cronetEngine: CronetEngine = CronetEngine.Builder(context)
        .enableHttp2(true)
        .enableQuic(true)
        .setUserAgent(userAgent)
        .build()
    private val client = OkHttpClient.Builder()
        .addInterceptor(CronetInterceptor.newBuilder(cronetEngine).build())
        .build()

    @WorkerThread
    override fun sendRequest(data: ByteArray, additional: List<ByteArray?>): ByteArray {
        val body = ByteArrayOutputStream(additional.sumOf { it?.size ?: 0 } + data.size)
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
