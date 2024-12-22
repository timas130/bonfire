package sh.sit.bonfire.auth.flows

import android.content.Context
import com.apollographql.apollo3.api.Error

abstract class AuthFlow(val context: Context) {
    @Throws(AuthException::class)
    abstract suspend fun start()

    enum class FailureReason {
        NetworkError,

        Cancelled,
        AuthUrlRequest,
        RequestRejected,
        InvalidCredential,
        FinalizeFailed,
        SameEmailDifferentAccount,

        InvalidEmail,
        InvalidPassword,
        InvalidUsername,
        EmailTaken,
        UsernameTaken,
        WrongPassword,
        WrongEmail,
        HardBanned,
        NotVerified,
        VerificationEmailFail,
        TfaExpired,
        TooManyAttempts,
        TryAgainLater,
        UserNotFound,
        SessionTooNew,
        AnotherAccountExists,
        BirthdayAlreadySet,
        TooYoung,
        Unknown,
    }

    class AuthException(val reason: FailureReason, val details: String? = null) : Exception(reason.toString()) {
        companion object {
            fun fromError(error: Error): AuthException {
                val code = error.message.substringBefore(':')
                val reason = when (code) {
                    "InvalidEmail" -> FailureReason.InvalidEmail
                    "InvalidPassword" -> FailureReason.InvalidPassword
                    "InvalidUsername" -> FailureReason.InvalidUsername
                    "EmailTaken" -> FailureReason.EmailTaken
                    "UsernameTaken" -> FailureReason.UsernameTaken
                    "WrongPassword" -> FailureReason.WrongPassword
                    "WrongEmail" -> FailureReason.WrongEmail
                    "HardBanned" -> FailureReason.HardBanned
                    "NotVerified" -> FailureReason.NotVerified
                    "VerificationEmailFail" -> FailureReason.VerificationEmailFail
                    "TfaExpired" -> FailureReason.TfaExpired
                    "TooManyAttempts" -> FailureReason.TooManyAttempts
                    "TryAgainLater" -> FailureReason.TryAgainLater
                    "UserNotFound" -> FailureReason.UserNotFound
                    "SessionTooNew" -> FailureReason.SessionTooNew
                    "AnotherAccountExists" -> FailureReason.AnotherAccountExists
                    "BirthdayAlreadySet" -> FailureReason.BirthdayAlreadySet
                    "TooYoung" -> FailureReason.TooYoung
                    else -> FailureReason.Unknown
                }

                return AuthException(reason, error.message)
            }
        }
    }
}
