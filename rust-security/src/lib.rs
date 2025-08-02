mod methods;

use c_core::prelude::anyhow;
use c_core::prelude::tarpc::context::Context;
use c_core::services::security::{IntentionType, SecurityError, SecurityService};
use c_core::{host_tcp, ServiceBase};
use google_playintegrity1::hyper::client::HttpConnector;
use google_playintegrity1::hyper_rustls::{HttpsConnector, HttpsConnectorBuilder};
use google_playintegrity1::oauth2::ServiceAccountAuthenticator;
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
        let hyper_client = google_playintegrity1::hyper::Client::builder().build(
            HttpsConnectorBuilder::new()
                .with_native_roots()?
                .https_only()
                .enable_http1()
                .enable_http2()
                .build(),
        );
        let auth =
            ServiceAccountAuthenticator::builder(base.config.firebase.service_account.clone())
                .hyper_client(hyper_client.clone())
                .build()
                .await?;
        let play_integrity = PlayIntegrity::new(hyper_client, auth);

        Ok(SecurityServer {
            base,
            play_integrity: Arc::new(play_integrity),
        })
    }

    host_tcp!(security);
}

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
