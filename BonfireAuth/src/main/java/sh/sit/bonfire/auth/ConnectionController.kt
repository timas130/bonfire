package sh.sit.bonfire.auth

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.apollographql.apollo3.ApolloCall
import com.apollographql.apollo3.api.Query
import com.apollographql.apollo3.cache.normalized.watch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.withContext

private fun Context.isInternetAvailable(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    } else {
        @Suppress("DEPRECATION")
        val network = connectivityManager.activeNetworkInfo
        @Suppress("DEPRECATION")
        return network?.isConnected == true
    }
}

private suspend fun Context.waitForConnection() {
    withContext(Dispatchers.IO) {
        do {
            delay(1000)
        } while (!isInternetAvailable())
    }
}

fun <D : Query.Data> ApolloCall<D>.watchExt(context: Context) = this
    .watch(fetchThrows = true, refetchThrows = true)
    .retry {
        it.printStackTrace()
        context.waitForConnection()
        true
    }
