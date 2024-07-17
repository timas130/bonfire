package sh.sit.bonfire.auth

import android.content.Context
import sh.sit.bonfire.auth.flows.AuthFlow
import sh.sit.bonfire.auth.flows.AuthFlow.FailureReason

fun AuthFlow.AuthException.toUiString(context: Context): String {
    val id = when (reason) {
        FailureReason.NetworkError -> R.string.error_network_error
        FailureReason.Cancelled -> R.string.error_cancelled
        FailureReason.AuthUrlRequest -> R.string.error_auth_url_rejected
        FailureReason.RequestRejected -> R.string.error_request_rejected
        FailureReason.InvalidCredential -> R.string.error_invalid_credential
        FailureReason.FinalizeFailed -> R.string.error_finalize_failed
        FailureReason.SameEmailDifferentAccount -> R.string.error_same_email_different_account
        FailureReason.InvalidEmail -> R.string.error_invalid_email
        FailureReason.InvalidPassword -> R.string.error_invalid_password
        FailureReason.InvalidUsername -> R.string.error_invalid_username
        FailureReason.EmailTaken -> R.string.error_email_taken
        FailureReason.UsernameTaken -> R.string.error_username_taken
        FailureReason.WrongPasswordOrEmail -> R.string.error_wrong_password_or_email
        FailureReason.HardBanned -> R.string.error_hard_banned
        FailureReason.NotVerified -> R.string.error_not_verified
        FailureReason.VerificationEmailFail -> R.string.error_verification_email_fail
        FailureReason.TfaExpired -> R.string.error_tfa_expired
        FailureReason.TooManyAttempts -> R.string.error_too_many_attempts
        FailureReason.TryAgainLater -> R.string.error_try_again_later
        FailureReason.UserNotFound -> R.string.error_user_not_found
        FailureReason.SessionTooNew -> R.string.error_session_too_new
        FailureReason.AnotherAccountExists -> R.string.error_another_account_exists
        FailureReason.BirthdayAlreadySet -> R.string.error_birthday_already_set
        FailureReason.TooYoung -> R.string.error_too_young
        FailureReason.Unknown -> R.string.error_unknown
    }

    val translated = context.getString(id)
    if (reason != FailureReason.Unknown) return translated

    return "$translated $details"
}
