package sh.sit.bonfire.networking

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.SystemClock
import android.util.Log
import java.io.IOException
import java.net.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class OkHttpProxySelector(context: Context) : ProxySelector() {
    private class ProxyDecision(
        val proxy: Result<mobileproxy.Proxy?>,
        val generation: Int,
        val madeAt: Long,
    ) {
        fun isStale(currentGeneration: Int): Boolean {
            if (proxy.getOrNull() != null) return false
            return generation != currentGeneration ||
                    (proxy.isFailure && SystemClock.elapsedRealtime() - madeAt >= FAILURE_TTL_MS)
        }
    }

    companion object {
        private const val FAILURE_TTL_MS = 30_000L

        private var proxyMutex: Lock = ReentrantLock()
        private var decision: ProxyDecision? = null

        private val networkCallbackRegistered = AtomicBoolean(false)
        private val networkGeneration = AtomicInteger(0)

        private fun registerNetworkCallback(context: Context) {
            if (!networkCallbackRegistered.compareAndSet(false, true)) return

            try {
                val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

                var lastNetwork = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    connectivityManager.activeNetwork
                } else {
                    null
                }
                val callback = object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        if (network != lastNetwork) {
                            lastNetwork = network
                            networkGeneration.incrementAndGet()
                        }
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    connectivityManager.registerDefaultNetworkCallback(callback)
                } else {
                    val request = NetworkRequest.Builder()
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .build()
                    connectivityManager.registerNetworkCallback(request, callback)
                }
            } catch (e: RuntimeException) {
                Log.w("BonfireNetworking", "failed to register network callback", e)
            }
        }
    }

    init {
        registerNetworkCallback(context)
    }

    override fun select(uri: URI?): MutableList<Proxy> {
        val mobileProxy = proxyMutex.withLock {
            val generation = networkGeneration.get()

            var current = decision
            if (current == null || current.isStale(generation)) {
                current = ProxyDecision(MobileProxyFactory.make(), generation, SystemClock.elapsedRealtime())
                decision = current
            }
            current.proxy.getOrNull()
        }

        if (mobileProxy != null) {
            val proxyAddress = InetSocketAddress(mobileProxy.host(), mobileProxy.port().toInt())
            return mutableListOf(Proxy(Proxy.Type.HTTP, proxyAddress))
        } else {
            return mutableListOf()
        }
    }

    override fun connectFailed(uri: URI?, sa: SocketAddress?, ioe: IOException?) {
        /* no-op */
    }
}
