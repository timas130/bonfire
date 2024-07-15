package sh.sit.bonfire.auth.integrity

import android.content.Context
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.StandardIntegrityException
import com.google.android.play.core.integrity.StandardIntegrityManager
import com.google.android.play.core.integrity.StandardIntegrityManager.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import sh.sit.bonfire.auth.BuildConfig
import sh.sit.bonfire.auth.CreateSecurityIntentionMutation
import sh.sit.bonfire.auth.SavePlayIntegrityMutation
import sh.sit.bonfire.auth.apollo
import sh.sit.schema.type.IntentionType
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object IntegrityController {
    private lateinit var integrityManager: StandardIntegrityManager
    private lateinit var packageName: String

    private var tokenProvider = MutableStateFlow<Result<StandardIntegrityTokenProvider>?>(null)

    fun init(context: Context) {
        packageName = context.packageName
        integrityManager = IntegrityManagerFactory.createStandard(context.applicationContext)

        integrityManager.prepareIntegrityToken(
            PrepareIntegrityTokenRequest.builder()
                .setCloudProjectNumber(BuildConfig.CLOUD_PROJECT_ID)
                .build()
        )
            .addOnSuccessListener {
                tokenProvider.value = Result.success(it)
            }
            .addOnFailureListener { ex ->
                tokenProvider.getAndUpdate {
                    if (it?.isSuccess == true) it
                    else Result.failure(ex)
                }
            }
    }

    private var sendingTokenIntentionType: IntentionType? = null
    private var sendingTokenJob: Job? = null

    suspend fun sendIntegrityToken(scope: CoroutineScope, intentionType: IntentionType) {
        val currentJob = sendingTokenJob

        if (currentJob?.isActive == true && sendingTokenIntentionType == intentionType) {
            currentJob.join()
            return
        }

        sendingTokenJob = scope.launch {
            val intentionToken = apollo
                .mutation(CreateSecurityIntentionMutation(IntentionType.GENERIC))
                .execute()
                .dataAssertNoErrors
                .createSecurityIntention

            suspend fun sendToken(token: String) {
                apollo
                    .mutation(SavePlayIntegrityMutation(intentionToken, packageName, token))
                    .execute()
                    .dataAssertNoErrors
                    .savePlayIntegrity
            }

            try {
                val tokenProvider = tokenProvider.first { it != null }!!
                    .getOrThrow()

                val result = suspendCoroutine { continuation ->
                    tokenProvider.request(
                        StandardIntegrityTokenRequest.builder()
                            .setRequestHash(intentionToken)
                            .build()
                    ).addOnSuccessListener {
                        continuation.resume(it)
                    }.addOnFailureListener {
                        continuation.resumeWithException(it)
                    }
                }

                sendToken(result.token())
            } catch (e: StandardIntegrityException) {
                try {
                    sendToken("__error__:${errorCodeToName(e.errorCode)}")
                } catch (_: Exception) {}
                throw e
            } finally {
                sendingTokenJob = null
                sendingTokenIntentionType = null
            }
        }
    }

    private fun errorCodeToName(code: Int): String {
        return when (code) {
            -1 -> "API_NOT_AVAILABLE"
            -2 -> "PLAY_STORE_NOT_FOUND"
            -3 -> "NETWORK_ERROR"
            -5 -> "APP_NOT_INSTALLED"
            // -4
            -6 -> "PLAY_SERVICES_NOT_FOUND"
            -7 -> "APP_UID_MISMATCH"
            -8 -> "TOO_MANY_REQUESTS"
            -9 -> "CANNOT_BIND_TO_SERVICE"
            // -10, -11
            -12 -> "GOOGLE_SERVER_UNAVAILABLE"
            // -13
            -14 -> "PLAY_STORE_VERSION_OUTDATED"
            -15 -> "PLAY_SERVICES_VERSION_OUTDATED"
            -16 -> "CLOUD_PROJECT_NUMBER_IS_INVALID"
            -17 -> "REQUEST_HASH_TOO_LONG"
            -18 -> "CLIENT_TRANSIENT_ERROR"
            -19 -> "INTEGRITY_TOKEN_PROVIDER_INVALID"
            else -> "CODE_$code"
        }
    }
}
