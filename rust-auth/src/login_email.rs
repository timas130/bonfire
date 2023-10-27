use crate::util::session::SESSION_EXPIRE_DAYS;
use crate::util::tfa::TfaInfoExt;
use crate::AuthServer;
use c_core::prelude::chrono::Utc;
use c_core::prelude::sqlx::types::ipnetwork::IpNetwork;
use c_core::prelude::tarpc::context;
use c_core::prelude::tokio::time::{sleep_until, Instant};
use c_core::prelude::tracing::{error, info, warn};
use c_core::prelude::{anyhow, chrono};
use c_core::services::auth::jwt::TokenClaims;
use c_core::services::auth::tfa::{TfaInfo, TfaType};
use c_core::services::auth::{
    AuthError, LoginEmailOptions, LoginEmailResponse, OAuthProvider, UserContext,
};
use c_core::services::email::types::EmailTemplate;
use firebase_scrypt::FirebaseScrypt;
use nanoid::nanoid;
use scrypt::password_hash::{PasswordHash, PasswordVerifier};
use scrypt::Scrypt;
use std::net::{IpAddr, Ipv4Addr};
use std::time::Duration;

pub const TFA_MODE_NONE: i32 = 0;
pub const TFA_MODE_TOTP: i32 = 1;
pub const TFA_MODE_EMAIL: i32 = 2;

impl AuthServer {
    pub(crate) async fn send_login_attempt(
        &self,
        email: String,
        username: String,
        ip: IpAddr,
        user_agent: &str,
    ) -> Result<(), AuthError> {
        self.email
            .send(
                context::current(),
                email,
                EmailTemplate::LoginAttempt {
                    username,
                    time: Utc::now(),
                    ip,
                    user_agent: Self::parse_user_agent(user_agent),
                },
            )
            .await
            .map_err(anyhow::Error::from)?
            .map_err(anyhow::Error::from)?;
        Ok(())
    }

    /// Create a new session after a email login,
    /// returning an access token and a refresh token.
    // This method is also called in check_tfa_status after
    // the TFA flow is complete.
    pub(crate) async fn create_session(
        &self,
        user_id: i64,
        context: Option<&UserContext>,
        provider: Option<OAuthProvider>,
        send_login_attempt: bool,
    ) -> Result<(String, String), AuthError> {
        let mut tx = self.base.pool.begin().await?;

        // 1. Create session

        let refresh_token = nanoid!(32);
        let session_id = sqlx::query_scalar!(
            "insert into sessions (user_id, ip, expires, user_agent, refresh_token, provider) \
             values ($1, $2, $5, $3, $4, $6) \
             returning id",
            user_id,
            context.map(|c| IpNetwork::from(c.ip)),
            context.map(|c| &c.user_agent),
            refresh_token,
            Utc::now() + chrono::Duration::days(SESSION_EXPIRE_DAYS),
            provider.map(i32::from),
        )
        .fetch_one(&mut *tx)
        .await?;

        // 2. Send login attempt email

        let user = sqlx::query!("select username, email from users where id = $1", user_id,)
            .fetch_one(&mut *tx)
            .await?;

        if let Some(email) = user.email {
            if send_login_attempt {
                self.send_login_attempt(
                    email,
                    user.username,
                    context
                        .map(|c| c.ip)
                        .unwrap_or_else(|| IpAddr::V4(Ipv4Addr::LOCALHOST)),
                    context
                        .as_ref()
                        .map(|c| c.user_agent.as_str())
                        .unwrap_or("[?]"),
                )
                .await?;
            }
        }

        // 3. Create access token

        let access_token_id = nanoid!(32);
        let access_token = TokenClaims::new_access(access_token_id, session_id, user_id);
        let access_token = jsonwebtoken::encode(
            &self.base.jwt_header,
            &access_token,
            &self.base.jwt_encoding_key,
        )
        .map_err(anyhow::Error::from)?;

        tx.commit().await?;

        Ok((access_token, refresh_token))
    }

    fn get_firebase_scrypt(&self) -> FirebaseScrypt {
        let config = &self.base.config.firebase;
        FirebaseScrypt::new(
            &config.scrypt_salt_separator,
            &config.scrypt_signer_key,
            config.scrypt_rounds,
            config.scrypt_mem_cost,
        )
    }

    pub(crate) fn verify_password(&self, password: &str, hash: &str) -> bool {
        if hash.starts_with("FB:") {
            let mut parts = hash.split(':').skip(1);
            let Some(hash_part) = parts.next() else {
                return false;
            };
            let Some(salt_part) = parts.next() else {
                return false;
            };

            return self
                .get_firebase_scrypt()
                .verify_password_bool(password, salt_part, hash_part);
        }

        Scrypt
            .verify_password(
                password.as_bytes(),
                &match PasswordHash::new(hash) {
                    Ok(hash) => hash,
                    Err(_err) => return false,
                },
            )
            .is_ok()
    }

    pub(crate) async fn _login_email(
        &self,
        opts: LoginEmailOptions,
    ) -> Result<LoginEmailResponse, AuthError> {
        let start = Instant::now();
        let deadline = start + Duration::from_secs(3);

        let LoginEmailOptions {
            email,
            password,
            context,
        } = opts;

        //// Check if data is valid

        // Check the email address
        let email_valid = Self::is_email_valid(&email);
        if !email_valid {
            sleep_until(deadline).await;
            return Err(AuthError::InvalidEmail);
        }

        //// Fetch the user

        let user = sqlx::query!(
            "select id, username, email, email_verified, hard_banned, password, tfa_mode, tfa_data \
             from users where email = $1 limit 1",
            email
        )
        .fetch_optional(&self.base.pool)
        .await;
        let user = match user {
            Ok(Some(user)) => user,
            Ok(None) => {
                sleep_until(deadline).await;
                return Err(AuthError::WrongPasswordOrEmail);
            }
            Err(err) => {
                sleep_until(deadline).await;
                return Err(err.into());
            }
        };

        //// Check password

        let password_hash = match &user.password {
            Some(password) => password,
            None => {
                sleep_until(deadline).await;
                return Err(AuthError::WrongPasswordOrEmail);
            }
        };

        let password_correct = self.verify_password(&password, password_hash);

        if !password_correct {
            sleep_until(deadline).await;
            return Err(AuthError::WrongPasswordOrEmail);
        }

        //// Rehash password if legacy hashing used

        if password_hash.starts_with("FB:") {
            let rehashed_password = Self::hash_password(&password)?;
            sqlx::query!(
                "update users set password = $1 where id = $2",
                rehashed_password,
                user.id,
            )
            .execute(&self.base.pool)
            .await?;
            info!("password rehashed for user {}", user.id);
        }

        //// Other login checks

        if user.hard_banned {
            sleep_until(deadline).await;
            return Err(AuthError::HardBanned);
        }

        // if user.email_verified.is_none() {
        //     sleep_until(deadline).await;
        //     return Err(AuthError::NotVerified);
        // }

        //// Decide on TFA mode

        match user.tfa_mode {
            Some(TFA_MODE_NONE) | None => {
                let (access_token, refresh_token) = self
                    .create_session(user.id, context.as_ref(), None, true)
                    .await?;

                sleep_until(deadline).await;
                Ok(LoginEmailResponse::Success {
                    access_token,
                    refresh_token,
                })
            }
            Some(mode) if matches!(mode, TFA_MODE_EMAIL | TFA_MODE_TOTP) => {
                let tfa_info = TfaInfo::new_login(context);
                let tfa_type = match mode {
                    TFA_MODE_EMAIL => TfaType::EmailLink,
                    TFA_MODE_TOTP => TfaType::Totp,
                    _ => unreachable!(),
                };
                let tfa_wait_token = self
                    .create_tfa(user.id, user.email, user.username, tfa_info, tfa_type)
                    .await?;

                sleep_until(deadline).await;
                Ok(LoginEmailResponse::TfaRequired {
                    tfa_type,
                    tfa_wait_token,
                })
            }
            Some(mode) => {
                sleep_until(deadline).await;
                warn!("unknown tfa mode on {:?}: {mode}", user.email);
                Err(anyhow::Error::msg("unknown tfa mode").into())
            }
        }
    }
}
