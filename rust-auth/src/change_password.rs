use crate::register_email::PASSWORD_LENGTH;
use crate::AuthServer;
use c_core::prelude::anyhow;
use c_core::prelude::tarpc::context;
use c_core::services::auth::{AuthError, UserContext};
use c_core::services::email::types::EmailTemplate;

impl AuthServer {
    // This method is used in tfa_approve() and recover_password()
    pub(crate) async fn do_change_password(
        &self,
        user_id: i64,
        hashed_password: String,
    ) -> Result<(), AuthError> {
        let mut tx = self.base.pool.begin().await?;

        let user = sqlx::query!(
            "update users set password = $1 \
             where id = $2 \
             returning email, username",
            hashed_password,
            user_id
        )
        .fetch_one(&mut *tx)
        .await?;

        if let Some(email) = user.email {
            self.email
                .send(
                    context::current(),
                    email,
                    EmailTemplate::PasswordChanged {
                        username: user.username,
                    },
                )
                .await
                .map_err(anyhow::Error::from)?
                .map_err(anyhow::Error::from)?;
        }

        tx.commit().await?;

        Ok(())
    }

    pub(crate) async fn _change_password(
        &self,
        access_token: String,
        old_password: String,
        new_password: String,
        _context: Option<UserContext>,
    ) -> Result<Option<String>, AuthError> {
        let access_token = self.get_access_token_info_secure(access_token).await?;

        let user = sqlx::query!(
            "select password, email, username from users where id = $1",
            access_token.user_id
        )
        .fetch_one(&self.base.pool)
        .await?;

        if user.email.is_none() {
            return Err(AuthError::UserNoEmail);
        }

        if !Self::is_password_valid(&new_password) {
            return Err(AuthError::InvalidPassword(PASSWORD_LENGTH));
        }

        if let Some(current_password) = &user.password {
            let password_matches = self.verify_password(&old_password, current_password);

            if !password_matches {
                return Err(AuthError::WrongPassword);
            }
        }

        let new_hash = Self::hash_password(&new_password)?;

        self.do_change_password(access_token.user_id, new_hash)
            .await?;

        Ok(None)
    }
}
