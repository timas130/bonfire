pub(crate) mod login_email;
mod login_refresh;
mod me;
mod register_email;
mod verify_email;
mod logout;
mod resend_verification;
mod change_password;
mod send_password_recovery;
mod check_recovery;
mod recover_password;
mod oauth_url;
mod login_oauth;
mod bind_oauth;
mod user_by_id;
mod user_by_slug;
mod active_sessions;
mod terminate_session;

use crate::schema::auth::login_email::LoginEmailMutation;
use crate::schema::auth::login_refresh::LoginRefreshMutation;
use crate::schema::auth::me::MeQuery;
use crate::schema::auth::register_email::RegisterEmailMutation;
use crate::schema::auth::verify_email::VerifyEmailMutation;
use async_graphql::MergedObject;
use crate::schema::auth::active_sessions::ActiveSessionsQuery;
use crate::schema::auth::bind_oauth::BindOAuthMutation;
use crate::schema::auth::change_password::ChangePasswordMutation;
use crate::schema::auth::check_recovery::CheckRecoveryQuery;
use crate::schema::auth::login_oauth::LoginOAuthMutation;
use crate::schema::auth::logout::LogoutMutation;
use crate::schema::auth::oauth_url::OAuthUrlQuery;
use crate::schema::auth::recover_password::RecoverPasswordMutation;
use crate::schema::auth::resend_verification::ResendVerificationMutation;
use crate::schema::auth::send_password_recovery::SendPasswordRecoveryMutation;

#[derive(MergedObject, Default)]
pub struct AuthQuery(
    MeQuery,
    CheckRecoveryQuery,
    OAuthUrlQuery,
    ActiveSessionsQuery,
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
);
