package sh.sit.bonfire.auth

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.apollographql.apollo3.cache.normalized.normalizedCache
import com.apollographql.apollo3.network.http.DefaultHttpEngine
import com.sup.dev.android.app.SupAndroid
import okhttp3.Interceptor
import okhttp3.Response
import sh.sit.bonfire.networking.OkHttpController

object ApolloController {
    val apolloClient: ApolloClient = ApolloClient.Builder()
        .serverUrl("https://api.bonfire.moe/")
        .httpEngine(DefaultHttpEngine(
            OkHttpController.getClient(SupAndroid.appContext!!) {
                addInterceptor(AuthInterceptor())
            }
        ))
        .normalizedCache(MemoryCacheFactory(maxSizeBytes = 10 * 1024 * 1024))
        .fetchPolicy(FetchPolicy.NetworkFirst)
        .build()
}

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .apply {
                if (chain.request().header("Authorization") == null) {
                    AuthController.getAccessToken()?.let { token ->
                        addHeader("Authorization", "Bearer $token")
                    }
                }
            }
            .build()
        return chain.proceed(request)
    }
}

val apollo: ApolloClient
    inline get() = ApolloController.apolloClient
