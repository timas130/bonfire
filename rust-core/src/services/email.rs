pub mod types;

use crate::client_tcp;
use crate::services::email::types::EmailTemplate;
use crate::util::{anyhow_clone, anyhow_unknown};
use educe::Educe;
use serde::{Deserialize, Serialize};
use thiserror::Error;

#[derive(Error, Debug, Deserialize, Serialize, Educe)]
#[educe(Eq, PartialEq, Clone)]
pub enum EmailError {
    #[error("AddressParseError: Failed to parse email address")]
    AddressParseError,

    #[error("Anyhow: Unknown error: {source}")]
    Anyhow {
        #[source]
        #[from]
        #[serde(skip)]
        #[serde(default = "anyhow_unknown")]
        #[educe(Eq(ignore), Clone(method = "anyhow_clone"))]
        source: anyhow::Error,
    },
}

#[tarpc::service]
pub trait EmailService {
    async fn send(address: String, email: EmailTemplate) -> Result<(), EmailError>;
}

pub struct Email;
impl Email {
    client_tcp!(EmailServiceClient);
}
