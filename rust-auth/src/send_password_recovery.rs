use crate::AuthServer;
use c_core::prelude::chrono::Utc;
use c_core::prelude::tarpc::context;
use c_core::prelude::{anyhow, chrono, tokio};
use c_core::services::auth::{AuthError, UserContext};
use c_core::services::email::types::EmailTemplate;
use nanoid::nanoid;
use sqlx::types::ipnetwork::IpNetwork;
use std::net::{IpAddr, Ipv4Addr};

const RECOVERY_EXPIRY: i64 = 1200; // 20 min

impl AuthServer {
    pub(crate) async fn _send_password_recovery(
        &self,
        email: String,
        context: Option<UserContext>,
    ) -> Result<(), AuthError> {
        let email_valid = Self::is_email_valid(&email);
        if !email_valid {
            return Err(AuthError::InvalidEmail);
        }

        // Get the user ID and username

        let user = sqlx::query!(
            "select id, username, email_verified from users \
             where email = $1 \
             limit 1",
            email,
        )
        .fetch_optional(&self.base.pool)
        .await?;
        let user = match user {
            Some(user) if user.email_verified.is_some() => user,
            _ => return Ok(()),
        };

        // Check whether a recovery flow already exists

        let existing_count = sqlx::query_scalar!(
            "select count(*) from recovery_flows \
             where user_id = $1 and expires > now() \
             limit 1",
            user.id,
        )
        .fetch_one(&self.base.pool)
        .await?
        .unwrap_or(0);

        if existing_count > 0 {
            return Ok(());
        }

        // Create the flow and send the email

        let mut tx = self.base.pool.begin().await?;

        let flow_id = nanoid!(32);

        let db_fut = sqlx::query!(
            "insert into recovery_flows (user_id, id, expires, ip, user_agent) \
             values ($1, $2, $3, $4, $5)",
            user.id,
            &flow_id,
            Utc::now() + chrono::Duration::seconds(RECOVERY_EXPIRY),
            context.as_ref().map(|ctx| IpNetwork::from(ctx.ip)),
            context.as_ref().map(|ctx| ctx.user_agent.as_str()),
        )
        .execute(&mut *tx);

        let email_fut = self.email.send(
            context::current(),
            email,
            EmailTemplate::PasswordRecovery {
                username: user.username,
                link: format!("{}{}", self.base.config.urls.recovery_link, flow_id),
                ip: context
                    .as_ref()
                    .map(|ctx| ctx.ip)
                    .unwrap_or_else(|| IpAddr::V4(Ipv4Addr::LOCALHOST)),
                user_agent: context
                    .map(|ctx| Self::parse_user_agent(&ctx.user_agent))
                    .unwrap_or_else(|| String::from("[?]")),
            },
        );

        let (f1, f2) = tokio::join!(db_fut, email_fut);
        f1?;
        f2.map_err(anyhow::Error::from)?
            .map_err(anyhow::Error::from)?;

        tx.commit().await?;

        Ok(())
    }
}
