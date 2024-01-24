use crate::AuthServer;
use c_core::prelude::chrono::Utc;
use c_core::prelude::tokio::time::{sleep_until, Instant};
use c_core::prelude::{anyhow, tokio};
use c_core::services::auth::jwt::TokenClaims;
use c_core::services::auth::{AuthError, RegisterEmailOptions, RegisterEmailResponse};
use jsonwebtoken::{Algorithm, Validation};
use lazy_static::lazy_static;
use nanoid::nanoid;
use scrypt::password_hash::rand_core::OsRng;
use scrypt::password_hash::{PasswordHasher, SaltString};
use scrypt::Scrypt;
use std::ops::RangeInclusive;
use std::time::Duration;

lazy_static! {
    static ref USERNAME_REGEX: regex::Regex = regex::Regex::new(r"^[a-zA-Z_0-9]{3,25}$").unwrap();
    static ref NUMBERS_ONLY_REGEX: regex::Regex = regex::Regex::new(r"^[0-9]+$").unwrap();
}

pub const PASSWORD_LENGTH: RangeInclusive<usize> = 8..=128;

impl AuthServer {
    pub(crate) fn is_email_valid(email: &str) -> bool {
        fast_chemail::is_valid_email(email)
    }
    pub(crate) fn is_username_valid(username: &str) -> bool {
        USERNAME_REGEX.is_match(username) && !NUMBERS_ONLY_REGEX.is_match(username)
    }
    pub(crate) fn is_password_valid(password: &str) -> bool {
        PASSWORD_LENGTH.contains(&password.len())
    }

    pub(crate) fn create_verify_token(&self, address: String) -> Result<String, AuthError> {
        jsonwebtoken::encode(
            &self.base.jwt_header,
            &TokenClaims::new_verify(address),
            &self.base.jwt_encoding_key,
        )
        .map_err(anyhow::Error::from)
        .map_err(AuthError::from)
    }

    pub(crate) fn get_verify_token_email(&self, token: String) -> Result<String, AuthError> {
        jsonwebtoken::decode::<TokenClaims>(
            &token,
            &self.base.jwt_decoding_key,
            &TokenClaims::get_verify_validation(Validation::new(Algorithm::HS256)),
        )
        .map_err(anyhow::Error::from)
        .map_err(AuthError::from)
        .map(|token| token.claims.sub)
    }

    pub(crate) fn hash_password(password: &str) -> anyhow::Result<String> {
        let salt = SaltString::generate(&mut OsRng);
        let params = scrypt::Params::new(18, 8, 1, 32).unwrap();

        Ok(Scrypt
            .hash_password_customized(password.as_bytes(), None, None, params, &salt)?
            .to_string())
    }

    pub(crate) async fn _register_email(
        &self,
        opts: RegisterEmailOptions,
    ) -> Result<RegisterEmailResponse, AuthError> {
        let deadline = Instant::now() + Duration::from_secs(3);

        let RegisterEmailOptions {
            email,
            password,
            username,
            context,
        } = opts;

        //// Check if data is valid

        // Check email address
        let email_address_valid = Self::is_email_valid(&email);
        if !email_address_valid {
            sleep_until(deadline).await;
            return Err(AuthError::InvalidEmail);
        }

        // Check username
        if let Some(username) = &username {
            let username_valid = Self::is_username_valid(username);
            if !username_valid {
                sleep_until(deadline).await;
                return Err(AuthError::InvalidUsername);
            }
        }

        // Check password
        let password_valid = Self::is_password_valid(&password);
        if !password_valid {
            sleep_until(deadline).await;
            return Err(AuthError::InvalidPassword(PASSWORD_LENGTH));
        }

        //// Check if stuff was taken

        let (email_taken, username_taken) = tokio::join!(
            sqlx::query_scalar!(
                "select count(*) from users where lower(email) = lower($1) limit 1",
                email
            )
            .fetch_one(&self.base.pool),
            sqlx::query_scalar!(
                "select count(*) from users where username = $1 limit 1",
                username
            )
            .fetch_one(&self.base.pool)
        );

        let email_taken = match email_taken {
            Ok(taken) => taken,
            Err(err) => {
                sleep_until(deadline).await;
                return Err(err.into());
            }
        };
        let username_taken = match username_taken {
            Ok(taken) => taken,
            Err(err) => {
                sleep_until(deadline).await;
                return Err(err.into());
            }
        };

        // FIXME: email enumeration
        if email_taken.unwrap_or(0) > 0 {
            sleep_until(deadline).await;
            return Err(AuthError::EmailTaken);
        }
        if username_taken.unwrap_or(0) > 0 {
            sleep_until(deadline).await;
            return Err(AuthError::UsernameTaken);
        }

        //// Register

        let hash = Self::hash_password(&password)?;

        let mut tx = self.base.pool.begin().await?;

        let temp_username = nanoid!(32);
        let user_id = sqlx::query_scalar!(
            "insert into users (username, email, password, email_verification_sent) \
             values ($1, $2, $3, $4) \
             returning id",
            username.as_ref().unwrap_or(&temp_username),
            &email,
            hash,
            Utc::now(),
        )
        .fetch_one(&mut *tx)
        .await?;

        let username = if let Some(username) = username {
            username
        } else {
            let new_username = format!("User#{user_id}");
            sqlx::query!(
                "update users set username = $1 where id = $2",
                new_username,
                user_id,
            )
            .execute(&mut *tx)
            .await?;
            new_username
        };

        //// Send verification email

        let email_result = self.send_verification_email(email.clone(), username).await;

        match email_result {
            Ok(_) => {}
            Err(err) => {
                sleep_until(deadline).await;
                return Err(err);
            }
        };

        tx.commit().await?;

        let (access_token, refresh_token) = self
            .create_session(user_id, context.as_ref(), None, false)
            .await?;

        Ok(RegisterEmailResponse {
            user_id,
            access_token,
            refresh_token,
        })
    }
}

#[cfg(test)]
mod tests {
    use crate::register_email::PASSWORD_LENGTH;
    use crate::AuthServer;
    use c_core::prelude::tarpc::context;
    use c_core::prelude::tokio;
    use c_core::services::auth::{AuthError, AuthService, RegisterEmailOptions};

    #[tokio::test]
    async fn register() {
        let server1 = AuthServer::load().await.unwrap();

        async fn should_fail(
            server: &AuthServer,
            email: &str,
            password: &str,
            username: &str,
            error: AuthError,
        ) {
            let server = server.to_owned();
            let result = server
                .register_email(
                    context::current(),
                    RegisterEmailOptions {
                        email: email.to_string(),
                        password: password.to_string(),
                        username: Some(username.to_string()),
                        context: None,
                    },
                )
                .await;
            assert_eq!(result.unwrap_err(), error);
        }

        // This one will succeed
        let server = server1.clone();
        let user_id1 = server
            .register_email(
                context::current(),
                RegisterEmailOptions {
                    email: "test_register1@bonfire.moe".to_string(),
                    password: "abcABC123!@#".to_string(),
                    username: Some("test_register1".to_string()),
                    context: None,
                },
            )
            .await
            .unwrap();

        let server = server1.clone();
        let user_id2 = server
            .register_email(
                context::current(),
                RegisterEmailOptions {
                    email: "test_register2@bonfire.moe".to_string(),
                    password: "abcABC123!@#".to_string(),
                    username: Some("test_register2".to_string()),
                    context: None,
                },
            )
            .await
            .unwrap();

        assert_ne!(user_id1.user_id, user_id2.user_id);

        tokio::join!(
            should_fail(
                &server1,
                "abc@",
                "abcABC123!@#",
                "test_account",
                AuthError::InvalidEmail
            ),
            should_fail(
                &server1,
                "fail@bonfire.moe",
                "abc",
                "test_account",
                AuthError::InvalidPassword(PASSWORD_LENGTH),
            ),
            should_fail(
                &server1,
                "fail@bonfire.moe",
                "abcABC123!@#",
                "test account",
                AuthError::InvalidUsername,
            ),
            should_fail(
                &server1,
                "test_register1@bonfire.moe",
                "abcABC123!@#",
                "test_fail1",
                AuthError::EmailTaken,
            ),
            should_fail(
                &server1,
                "fail@bonfire.moe",
                "abcABC123!@#",
                "test_register1",
                AuthError::UsernameTaken,
            ),
        );

        // Cleanup

        let server = server1.clone();
        server
            .unsafe_delete_user(context::current(), user_id1.user_id)
            .await
            .expect("failed to cleanup");
        let server = server1.clone();
        server
            .unsafe_delete_user(context::current(), user_id2.user_id)
            .await
            .expect("failed to cleanup");
    }
}
