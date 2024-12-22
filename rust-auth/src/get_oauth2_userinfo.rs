use crate::AuthServer;
use c_core::prelude::anyhow::anyhow;
use c_core::services::auth::AuthError;
use openidconnect::core::CoreGenderClaim;
use openidconnect::{
    EndUserEmail, EndUserName, EndUserUsername, StandardClaims, SubjectIdentifier,
};

#[allow(unused)]
pub(crate) struct OAuthAccessTokenInfo {
    pub user_id: i64,
    pub scopes: Vec<String>,
    pub flow_id: i64,
    pub grant_id: i64,
    pub session_id: i64,
    pub client_id: i64,
}

impl AuthServer {
    pub(crate) async fn get_oauth2_access_token_info(
        &self,
        access_token: String,
    ) -> Result<OAuthAccessTokenInfo, AuthError> {
        // access tokens have the format BF/{flow_id}/{random}

        let mut decoded_token = access_token.splitn(3, '/');

        if decoded_token.next() != Some("BF") {
            return Err(AuthError::InvalidToken);
        }

        let flow_id = decoded_token
            .next()
            .ok_or(AuthError::InvalidToken)?
            .parse::<i64>()
            .map_err(|_| AuthError::InvalidToken)?;

        let flow = sqlx::query!(
            "select fl.id, grant_id, session_id, client_id, user_id, scopes \
             from oauth2_flows_as fl \
             inner join sessions on sessions.id = fl.session_id \
             where fl.id = $1 and access_token = $2 and grant_id is not null",
            flow_id,
            access_token,
        )
        .fetch_optional(&self.base.pool)
        .await?
        .ok_or(AuthError::InvalidToken)?;

        Ok(OAuthAccessTokenInfo {
            user_id: flow.user_id,
            scopes: flow.scopes,
            flow_id: flow.id,
            grant_id: flow.grant_id.ok_or(anyhow!("null value grant_id"))?,
            session_id: flow.session_id,
            client_id: flow.client_id,
        })
    }

    pub(crate) async fn _get_oauth2_userinfo(
        &self,
        access_token: String,
    ) -> Result<serde_json::Value, AuthError> {
        let OAuthAccessTokenInfo {
            user_id, scopes, ..
        } = self.get_oauth2_access_token_info(access_token).await?;

        let user = self
            ._get_by_id(user_id)
            .await?
            .ok_or(anyhow!("out of sync"))?;

        let mut claims =
            StandardClaims::<CoreGenderClaim>::new(SubjectIdentifier::new(user.id.to_string()))
                .set_name(Some(EndUserName::new(user.username.clone()).into()))
                .set_preferred_username(Some(EndUserUsername::new(user.username)));

        if scopes.iter().any(|s| s == "email") {
            claims = claims
                .set_email(user.email.map(EndUserEmail::new))
                .set_email_verified(Some(user.email_verified.is_some()));
        }

        Ok(serde_json::to_value(claims).map_err(|_| anyhow!("serialization error"))?)
    }
}
