mod methods;

use c_core::prelude::tarpc::context::Context;
use c_core::prelude::{anyhow, tarpc};
use c_core::services::security::{IntentionType, SecurityError, SecurityService};
use c_core::{google_api, host_tcp, ServiceBase};
use google_playintegrity1::hyper::client::HttpConnector;
use google_playintegrity1::hyper_rustls::HttpsConnector;
use google_playintegrity1::PlayIntegrity;
use std::sync::Arc;

#[derive(Clone)]
pub struct SecurityServer {
    base: ServiceBase,

    play_integrity: Arc<PlayIntegrity<HttpsConnector<HttpConnector>>>,
}
impl SecurityServer {
    pub async fn load() -> anyhow::Result<Self> {
        Self::with_base(ServiceBase::load().await?).await
    }

    pub async fn with_base(base: ServiceBase) -> anyhow::Result<Self> {
        let play_integrity = google_api!(base, google_playintegrity1, PlayIntegrity);

        Ok(SecurityServer {
            base,
            play_integrity: Arc::new(play_integrity),
        })
    }

    host_tcp!(security);
}

#[tarpc::server]
impl SecurityService for SecurityServer {
    async fn create_intention(
        self,
        _: Context,
        user_id: i64,
        intention_type: IntentionType,
    ) -> Result<String, SecurityError> {
        self._create_intention(user_id, intention_type).await
    }

    async fn save_play_integrity(
        self,
        _: Context,
        user_id: i64,
        intention_token: String,
        package_name: String,
        token: String,
    ) -> Result<(), SecurityError> {
        self._save_play_integrity(user_id, intention_token, package_name, token)
            .await
    }
}
