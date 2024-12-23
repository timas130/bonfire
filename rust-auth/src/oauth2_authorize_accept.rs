use crate::terminate_session::AccessTokenInfo;
use crate::AuthServer;
use c_core::prelude::anyhow::anyhow;
use c_core::prelude::chrono::Utc;
use c_core::prelude::tarpc::context;
use c_core::prelude::{anyhow, chrono};
use c_core::services::auth::user::TfaMode;
use c_core::services::auth::{
    AuthError, OAuthAuthorizeResult, TfaAction, TfaInfo, TfaType, UserContext,
};
use c_core::services::email::types::EmailTemplate;
use nanoid::nanoid;
use std::collections::HashSet;
use std::net::{IpAddr, Ipv4Addr};

impl AuthServer {
    pub(crate) async fn _oauth2_authorize_accept(
        &self,
        flow_id: i64,
        access_token: String,
        user_context: Option<UserContext>,
    ) -> Result<OAuthAuthorizeResult, AuthError> {
        let AccessTokenInfo { session_id, .. } =
            self.get_access_token_info_secure(access_token).await?;

        let flow = sqlx::query!(
            "select flows.*, sess.user_id, cl.display_name from oauth2_flows_as flows \
             inner join sessions sess on flows.session_id = sess.id \
             inner join oauth2_clients cl on flows.client_id = cl.id \
             where flows.id = $1 and session_id = $2 and authorized_at is null",
            flow_id,
            session_id,
        )
        .fetch_optional(&self.base.pool)
        .await?;

        let Some(flow) = flow else {
            return Err(AuthError::FlowNotFound);
        };

        if flow.created_at + chrono::Duration::minutes(30) < Utc::now() {
            return Err(AuthError::FlowExpired);
        }

        // if there are sensitive scopes,
        if !flow
            .scopes
            .into_iter()
            .collect::<HashSet<_>>()
            .is_subset(&self.base.config.auth.insensitive_scopes)
        {
            // create tfa flow and commence tfa

            let user = self
                ._get_by_id(flow.user_id)
                .await?
                .ok_or(anyhow!("out of sync"))?;

            let tfa_type = match user.tfa_mode {
                None | Some(TfaMode::Email) => TfaType::EmailLink,
                Some(TfaMode::Totp) => TfaType::Totp,
            };

            // fixme: this is one of the worst methods in c-auth.
            //        please fix it, ffs
            let tfa_wait_token = self
                .create_tfa(
                    user.id,
                    user.email,
                    user.username,
                    TfaInfo {
                        context: user_context,
                        created: Utc::now(),
                        action: TfaAction::OAuthAuthorize {
                            flow_id: flow.id,
                            service_name: flow.display_name,
                        },
                    },
                    tfa_type,
                )
                .await?;

            return Ok(OAuthAuthorizeResult::TfaRequired {
                tfa_type,
                tfa_wait_token,
            });
        }

        // if there are no sensitive scopes, authorise this bad boy
        let redirect_uri = self
            .do_oauth2_authorize_accept(flow.id, user_context)
            .await?;

        Ok(OAuthAuthorizeResult::Redirect { redirect_uri })
    }

    // returns redirect uri
    // also used in check_tfa_status
    pub(crate) async fn do_oauth2_authorize_accept(
        &self,
        flow_id: i64,
        user_context: Option<UserContext>,
    ) -> Result<String, AuthError> {
        let code = nanoid!(32);

        let mut tx = self.base.pool.begin().await?;

        let flow = sqlx::query!(
            "select flows.*, sess.user_id, u.username, u.email, cl.display_name \
             from oauth2_flows_as flows \
             inner join sessions sess on flows.session_id = sess.id \
             inner join users u on sess.user_id = u.id \
             inner join oauth2_clients cl on flows.client_id = cl.id \
             where flows.id = $1 and authorized_at is null \
             for update",
            flow_id,
        )
        .fetch_one(&mut *tx)
        .await?;

        let grant = sqlx::query!(
            "insert into oauth2_grants (client_id, user_id, scope, last_used_at) \
             values ($1, $2, $3, now()) \
             on conflict (user_id, client_id) do update \
             set scope = merge_arrays(oauth2_grants.scope, excluded.scope), last_used_at = now() \
             returning id",
            flow.client_id,
            flow.user_id,
            &flow.scopes,
        )
        .fetch_one(&mut *tx)
        .await?;

        let authorized_at = sqlx::query_scalar!(
            "update oauth2_flows_as \
             set code = $1, grant_id = $2, authorized_at = now() \
             where id = $3 \
             returning authorized_at",
            code,
            grant.id,
            flow_id
        )
        .fetch_one(&mut *tx)
        .await?
        .ok_or(anyhow!("horrible db error"))?;

        let redirect_uri = self.build_redirect_uri(flow.redirect_uri, code, flow.state)?;

        tx.commit().await?;

        // send a worrying email after finishing everything
        if let Some(email) = flow.email {
            self.email
                .send(
                    context::current(),
                    email,
                    EmailTemplate::OAuthAuthorize {
                        username: flow.username,
                        service_name: flow.display_name,
                        time: authorized_at,
                        ip: user_context
                            .as_ref()
                            .map(|u| u.ip)
                            .unwrap_or(IpAddr::V4(Ipv4Addr::LOCALHOST)),
                        user_agent: user_context
                            .map(|u| u.user_agent)
                            .unwrap_or_else(|| "[?]".into()),
                    },
                )
                .await
                .map_err(anyhow::Error::from)?
                .map_err(anyhow::Error::from)?;
        }

        Ok(redirect_uri)
    }
}
