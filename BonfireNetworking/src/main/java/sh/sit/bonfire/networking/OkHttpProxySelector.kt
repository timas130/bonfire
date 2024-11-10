package sh.sit.bonfire.networking

import java.io.IOException
import java.net.*
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class OkHttpProxySelector : ProxySelector() {
    companion object {
        private var proxyMutex: Lock = ReentrantLock()
        private var mobileProxy: Result<mobileproxy.Proxy?>? = null
    }

    override fun select(uri: URI?): MutableList<Proxy> {
        val mobileProxy = proxyMutex.withLock {
            if (mobileProxy != null) {
                return@withLock mobileProxy!!.getOrNull()
            }
            mobileProxy = MobileProxyFactory.make()
            mobileProxy!!.getOrNull()
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
