use crate::SecurityServer;
use c_core::prelude::chrono::{TimeDelta, Utc};
use c_core::prelude::tracing::{span, warn, Instrument, Level};
use c_core::services::security::SecurityError;
use google_playintegrity1::api::DecodeIntegrityTokenRequest;
use sqlx::{Postgres, Transaction};

impl SecurityServer {
    pub(crate) async fn _save_play_integrity<'a>(
        &self,
        user_id: i64,
        intention_token: String,
        package_name: String,
        token: String,
    ) -> Result<(), SecurityError> {
        let mut tx = self.base.pool.begin().await?;

        let intention = sqlx::query!(
            "select * from security_intentions \
             where token = $1 and user_id = $2",
            intention_token,
            user_id,
        )
        .fetch_optional(&mut *tx)
        .await?;

        // intention exists
        let Some(intention) = intention else {
            return Err(SecurityError::IntentionNotFound);
        };

        // intention not saved/passed yet
        if intention.passed_at.is_some() {
            return Err(SecurityError::IntentionPassed);
        }

        // intention is new enough
        if intention.created_at.signed_duration_since(Utc::now()) > TimeDelta::seconds(120) {
            return Err(SecurityError::IntentionExpired);
        }

        // intention has less than 3 attempts
        if intention.attempts >= 3 {
            return Err(SecurityError::TooManyAttempts);
        }

        // helper function
        let package_name_clone = package_name.clone();
        let save_error = |mut tx: Transaction<'a, Postgres>, err: &'a str| async move {
            let check_id = sqlx::query_scalar!(
                "insert into user_integrity_checks (user_id, status, package_name) \
                 values ($1, $2, $3) \
                 returning id",
                user_id,
                err,
                package_name_clone,
            )
            .fetch_one(&mut *tx)
            .await?;

            sqlx::query!(
                "update security_intentions \
                 set check_id = $1, attempts = attempts + 1 \
                 where id = $2",
                check_id,
                intention.id,
            )
            .execute(&mut *tx)
            .await?;

            tx.commit().await?;

            Ok::<(), sqlx::Error>(())
        };

        // check that package name is allowed
        if !self
            .base
            .config
            .security
            .package_names
            .contains(&package_name)
        {
            save_error(tx, "GPI_INVALID_PACKAGE_NAME").await?;

            return Err(SecurityError::UnknownPackage);
        }

        // empty token indicates some other error the client won't tell us about
        if token.is_empty() {
            save_error(tx, "GPI_NO_TOKEN").await?;

            return Ok(());
        }

        // client-provided error
        if let Some(error) = token.strip_prefix("__error__:") {
            let tracked_errors = [
                "GPI_API_NOT_AVAILABLE",
                "GPI_PLAY_STORE_NOT_FOUND",
                "GPI_PLAY_STORE_ACCOUNT_NOT_FOUND",
                "GPI_PLAY_SERVICES_NOT_FOUND",
                "GPI_CANNOT_BIND_TO_SERVICE",
                "GPI_PLAY_STORE_VERSION_OUTDATED",
                "GPI_PLAY_SERVICES_VERSION_OUTDATED",
                "GPI_CLOUD_PROJECT_NUMBER_IS_INVALID",
                "GPI_INTEGRITY_TOKEN_PROVIDER_INVALID",
            ];
            if let Some(error) = tracked_errors.iter().find(|x| x.ends_with(error)) {
                save_error(tx, error).await?;
                return Ok(());
            } else {
                save_error(tx, "GPI_OTHER").await?;
            };

            return Ok(());
        }

        // decode integrity token if no other error is present
        let result = self
            .play_integrity
            .methods()
            .decode_integrity_token(
                DecodeIntegrityTokenRequest {
                    integrity_token: Some(token),
                },
                &package_name,
            )
            .doit()
            .instrument(span!(Level::INFO, "checking play integrity"))
            .await;
        let (_, resp) = match result {
            Ok(resp) => resp,
            Err(err) => {
                // too lazy to decode server errors
                warn!(error = ?err, package_name, "error checking play integrity");
                save_error(tx, "GPI_UPSTREAM").await?;

                return Err(SecurityError::UpstreamError);
            }
        };

        let resp = resp
            .token_payload_external
            .ok_or(SecurityError::UpstreamError)?;

        // if request hash doesn't match intention token
        if resp
            .request_details
            .and_then(|x| x.request_hash)
            .map(|x| x != intention.token)
            .unwrap_or(true)
        {
            save_error(tx, "GPI_HASH_MISMATCH").await?;
            return Err(SecurityError::HashMismatch);
        }

        // save the report
        let device_recognition = resp
            .device_integrity
            .as_ref()
            .and_then(|x| x.device_recognition_verdict.as_ref())
            .map(|x| x.iter().map(|x| x.as_str()).collect::<Vec<_>>());
        let device_recognition = device_recognition.as_ref();

        let check_id = sqlx::query_scalar!(
            "insert into user_integrity_checks \
             (user_id, status, package_name, app_license, app_recognition, cert_digest, \
              device_integrity, basic_integrity, strong_integrity, device_activity) \
             values ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10) \
             returning id",
            user_id,
            "OK",
            package_name,
            resp.account_details.and_then(|x| x.app_licensing_verdict),
            resp.app_integrity
                .as_ref()
                .and_then(|x| x.app_recognition_verdict.as_ref()),
            resp.app_integrity
                .as_ref()
                .and_then(|x| x.certificate_sha256_digest.as_ref())
                .map(|x| x.join(", ")),
            device_recognition.map(|x| x.contains(&"MEETS_DEVICE_INTEGRITY")),
            device_recognition.map(|x| x.contains(&"MEETS_BASIC_INTEGRITY")),
            device_recognition.map(|x| x.contains(&"MEETS_STRONG_INTEGRITY")),
            resp.device_integrity
                .and_then(|x| x.recent_device_activity)
                .and_then(|x| x.device_activity_level)
        )
        .fetch_one(&mut *tx)
        .await?;
        sqlx::query!(
            "update security_intentions \
             set passed_at = now(), check_id = $1, attempts = attempts + 1 \
             where id = $2",
            check_id,
            intention.id,
        )
        .execute(&mut *tx)
        .await?;

        tx.commit().await?;

        Ok(())
    }
}
