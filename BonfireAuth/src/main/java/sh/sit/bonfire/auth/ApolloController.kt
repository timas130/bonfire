package sh.sit.bonfire.auth

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.annotations.ApolloExperimental
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.api.*
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.apollographql.apollo3.cache.normalized.normalizedCache
import com.apollographql.apollo3.network.http.DefaultHttpEngine
import com.dzen.campfire.api.API
import com.sup.dev.android.app.SupAndroid
import com.sup.dev.android.libs.image_loader.ImageLoader
import com.sup.dev.android.libs.image_loader.ImageLoaderRef
import okhttp3.Interceptor
import okhttp3.Response
import sh.sit.bonfire.networking.OkHttpController
import sh.sit.schema.fragment.Ui
import sh.sit.schema.pagination.Pagination

object ApolloController {
    @OptIn(ApolloExperimental::class)
    val apolloClient: ApolloClient = ApolloClient.Builder()
        .serverUrl(API.MELIOR_ROOT)
        .httpEngine(DefaultHttpEngine(
            OkHttpController.getClient(SupAndroid.appContext!!) {
                addInterceptor(AuthInterceptor())
            }
        ))
        .normalizedCache(
            normalizedCacheFactory = MemoryCacheFactory(maxSizeBytes = 10 * 1024 * 1024),
            cacheKeyGenerator = TypePolicyCacheKeyGenerator,
            metadataGenerator = ConnectionMetadataGenerator(Pagination.connectionTypes),
            apolloResolver = FieldPolicyApolloResolver,
            recordMerger = ConnectionRecordMerger,
        )
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

fun ImageLoader.load(ui: Ui) = ImageLoaderRef(ui.u, ui.i.toLong())
