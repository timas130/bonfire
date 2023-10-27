mod active_sessions;
mod bind_oauth;
mod change_password;
mod check_recovery;
pub(crate) mod login_email;
mod login_internal;
mod login_oauth;
mod login_refresh;
mod logout;
mod me;
mod oauth_url;
mod recover_password;
mod register_email;
mod resend_verification;
pub(crate) mod security_settings;
mod send_password_recovery;
mod terminate_session;
mod user_by_id;
mod user_by_slug;
mod verify_email;
mod change_email;

use crate::schema::auth::active_sessions::ActiveSessionsQuery;
use crate::schema::auth::bind_oauth::BindOAuthMutation;
use crate::schema::auth::change_password::ChangePasswordMutation;
use crate::schema::auth::check_recovery::CheckRecoveryQuery;
use crate::schema::auth::login_email::LoginEmailMutation;
use crate::schema::auth::login_internal::LoginInternalMutation;
use crate::schema::auth::login_oauth::LoginOAuthMutation;
use crate::schema::auth::login_refresh::LoginRefreshMutation;
use crate::schema::auth::logout::LogoutMutation;
use crate::schema::auth::me::MeQuery;
use crate::schema::auth::oauth_url::OAuthUrlQuery;
use crate::schema::auth::recover_password::RecoverPasswordMutation;
use crate::schema::auth::register_email::RegisterEmailMutation;
use crate::schema::auth::resend_verification::ResendVerificationMutation;
use crate::schema::auth::send_password_recovery::SendPasswordRecoveryMutation;
use crate::schema::auth::user_by_id::UserByIDQuery;
use crate::schema::auth::user_by_slug::UserBySlugQuery;
use crate::schema::auth::verify_email::VerifyEmailMutation;
use async_graphql::MergedObject;
use crate::schema::auth::change_email::ChangeEmailMutation;
use crate::schema::auth::terminate_session::TerminateSessionMutation;

#[derive(MergedObject, Default)]
pub struct AuthQuery(
    MeQuery,
    CheckRecoveryQuery,
    OAuthUrlQuery,
    ActiveSessionsQuery,
    UserByIDQuery,
    UserBySlugQuery,
);

#[derive(MergedObject, Default)]
pub struct AuthMutation(
    RegisterEmailMutation,
    VerifyEmailMutation,
    LoginEmailMutation,
    LoginRefreshMutation,
    LogoutMutation,
    ResendVerificationMutation,
    ChangePasswordMutation,
    SendPasswordRecoveryMutation,
    RecoverPasswordMutation,
    LoginOAuthMutation,
    BindOAuthMutation,
    LoginInternalMutation,
    ChangeEmailMutation,
    TerminateSessionMutation,
);
