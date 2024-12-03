use crate::check_tfa_status::RawTfaFlow;
use crate::AuthServer;
use async_trait::async_trait;
use c_core::prelude::anyhow::anyhow;
use c_core::prelude::tarpc::context;
use c_core::prelude::{anyhow, chrono};
use c_core::services::auth::jwt::{TokenClaims, JWT_TFA_EXPIRY_SECONDS};
use c_core::services::auth::tfa::{TfaAction, TfaInfo};
use c_core::services::auth::{AuthError, TfaType, UserContext};
use c_core::services::email::types::EmailTemplate;
use nanoid::nanoid;
use serde::{Deserialize, Serialize};
use sqlx::types::chrono::Utc;
use sqlx::types::ipnetwork::IpNetwork;

pub trait TfaInfoExt {
    fn new_login(context: Option<UserContext>) -> Self
    where
        Self: Sized;

    fn new_change_password(context: Option<UserContext>, password: String) -> Self
    where
        Self: Sized;
}

impl TfaInfoExt for TfaInfo {
    fn new_login(context: Option<UserContext>) -> Self {
        Self {
            context,
            created: Utc::now(),
            action: TfaAction::Login,
        }
    }

    fn new_change_password(context: Option<UserContext>, password: String) -> Self {
        Self {
            context,
            created: Utc::now(),
            action: TfaAction::PasswordChange(password),
        }
    }
}

impl AuthServer {
    pub(crate) async fn create_tfa(
        &self,
        user_id: i64,
        email: Option<String>,
        username: String,
        tfa_info: TfaInfo,
        tfa_type: TfaType,
    ) -> Result<String, AuthError> {
        let mut tx = self.base.pool.begin().await?;

        let token_id = nanoid!(32);
        let action_stripped = tfa_info.action.strip_data();

        let (action_id, action_data) = tfa_info.action.to_db();
        sqlx::query!(
            "insert into tfa_flows (user_id, id, expires, ip, user_agent, action_type, action_data) \
             values ($1, $2, $3, $4, $5, $6, $7)",
            user_id,
            token_id,
            Utc::now() + chrono::Duration::seconds(JWT_TFA_EXPIRY_SECONDS as i64),
            tfa_info.context.as_ref().map(|context| IpNetwork::from(context.ip)),
            tfa_info.context.map(|context| context.user_agent),
            action_id,
            action_data,
        )
        .execute(&mut *tx)
        .await?;

        let tfa_wait_token = TokenClaims::new_tfa_wait(token_id.clone(), user_id);
        let tfa_wait_token = jsonwebtoken::encode(
            &self.base.jwt_header,
            &tfa_wait_token,
            &self.base.jwt_encoding_key,
        )
        .map_err(anyhow::Error::from)?;

        if tfa_type == TfaType::EmailLink {
            let email = email.ok_or(AuthError::UserNoEmail)?;

            let tfa_token = TokenClaims::new_tfa(token_id, user_id);
            let tfa_token = jsonwebtoken::encode(
                &self.base.jwt_header,
                &tfa_token,
                &self.base.jwt_encoding_key,
            )
            .map_err(anyhow::Error::from)?;

            let tfa_secret_link = format!("{}{tfa_token}", self.base.config.urls.email_tfa_link);

            self.email
                .send(
                    context::current(),
                    email,
                    EmailTemplate::TfaEmail {
                        username,
                        link: tfa_secret_link,
                        action: action_stripped,
                    },
                )
                .await
                .map_err(anyhow::Error::from)?
                .map_err(anyhow::Error::from)?;
        }

        tx.commit().await?;

        Ok(tfa_wait_token)
    }
}

const TFA_ACTION_LOGIN: i32 = 1;
const TFA_ACTION_PASSWORD_CHANGE: i32 = 2;
const TFA_ACTION_OAUTH_AUTHORIZE: i32 = 3;

#[async_trait]
pub trait TfaActionExt {
    fn from_db(num: i32, data: String) -> Result<Self, ()>
    where
        Self: Sized;

    fn to_db(self) -> (i32, String)
    where
        Self: Sized;
}

#[derive(Serialize, Deserialize)]
struct OAuthAuthorizeData {
    flow_id: i64,
    service_name: String,
}

impl TfaActionExt for TfaAction {
    fn from_db(num: i32, data: String) -> Result<Self, ()> {
        Ok(match num {
            TFA_ACTION_LOGIN => Self::Login,
            TFA_ACTION_PASSWORD_CHANGE => Self::PasswordChange(data),
            TFA_ACTION_OAUTH_AUTHORIZE => {
                let data = serde_json::from_str::<OAuthAuthorizeData>(&data).map_err(|_| ())?;
                Self::OAuthAuthorize {
                    flow_id: data.flow_id,
                    service_name: data.service_name,
                }
            }
            _ => return Err(()),
        })
    }

    fn to_db(self) -> (i32, String) {
        match self {
            TfaAction::Login => (TFA_ACTION_LOGIN, String::new()),
            TfaAction::PasswordChange(data) => (TFA_ACTION_PASSWORD_CHANGE, data),
            TfaAction::OAuthAuthorize {
                flow_id,
                service_name,
            } => {
                let data = serde_json::to_string(&OAuthAuthorizeData {
                    flow_id,
                    service_name,
                })
                .expect("Failed to serialize OAuthAuthorizeData");
                (TFA_ACTION_OAUTH_AUTHORIZE, data)
            }
        }
    }
}

impl RawTfaFlow {
    pub(crate) async fn do_exactly_that(self, server: &AuthServer) -> Result<(), AuthError> {
        let action = TfaAction::from_db(self.action_type, self.action_data)
            .map_err(|_| anyhow!("invalid tfa action"))?;

        if let TfaAction::PasswordChange(pwd) = action {
            server.do_change_password(self.user_id, pwd).await?;
        }

        Ok(())
    }
}
