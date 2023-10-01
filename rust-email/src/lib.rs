use c_core::prelude::futures::future::try_join_all;
use c_core::prelude::tarpc::context;
use c_core::prelude::tokio::time::sleep;
use c_core::prelude::tracing::{info, instrument, span, warn, Instrument, Level, Span};
use c_core::prelude::{anyhow, tarpc, tokio};
use c_core::services::email::types::EmailTemplate;
use c_core::services::email::{EmailError, EmailService};
use c_core::{host_tcp, ServiceBase};
use crossbeam_queue::SegQueue;
use itertools::Itertools;
use lettre::message::{Mailbox, MultiPart, SinglePart};
use lettre::transport::smtp::authentication::Credentials;
use lettre::{AsyncSmtpTransport, AsyncTransport, Message, Tokio1Executor};
use nanoid::nanoid;
use std::convert::Infallible;
use std::ops::Deref;
use std::sync::Arc;
use std::time::Duration;
use tracing::error;

include!(concat!(env!("OUT_DIR"), "/templates.rs"));

#[derive(Clone)]
pub struct EmailServer {
    base: ServiceBase,
    mailer: Arc<Option<AsyncSmtpTransport<Tokio1Executor>>>,
    from: Mailbox,
    queue: Arc<SegQueue<(Span, Message)>>,
}
impl EmailServer {
    pub async fn load() -> anyhow::Result<Self> {
        Self::with_base(ServiceBase::load().await?).await
    }

    pub async fn with_base(base: ServiceBase) -> anyhow::Result<Self> {
        let from: Mailbox = base
            .config
            .email
            .from
            .parse()
            .expect("invalid \"from\" value");

        let mailer = if from.email.user() == "dummy" {
            None
        } else {
            let creds = Credentials::new(
                base.config.email.username.to_owned(),
                base.config.email.password.to_owned(),
            );
            Some(if base.config.email.tls {
                AsyncSmtpTransport::<Tokio1Executor>::relay(&base.config.email.host)?
                    .credentials(creds)
                    .port(base.config.email.port)
                    .build()
            } else {
                AsyncSmtpTransport::<Tokio1Executor>::builder_dangerous(&base.config.email.host)
                    .credentials(creds)
                    .port(base.config.email.port)
                    .build()
            })
        };

        let ret = Self {
            base,
            mailer: Arc::new(mailer),
            from,
            queue: Arc::new(SegQueue::new()),
        };

        let cloned = ret.clone();
        tokio::spawn(async move {
            cloned.start_email_sender().await;
        });

        Ok(ret)
    }

    host_tcp!(email);
}

#[tarpc::server]
//noinspection RsSortImplTraitMembers
impl EmailService for EmailServer {
    async fn send(
        self,
        _: context::Context,
        address_raw: String,
        email: EmailTemplate,
    ) -> Result<(), EmailError> {
        self._send(address_raw, email).await
    }

    async fn send_telegram(self, _: context::Context, content: String) -> Result<(), EmailError> {
        self._send_telegram(content).await
    }
}

impl EmailServer {
    async fn _send(self, address_raw: String, email: EmailTemplate) -> Result<(), EmailError> {
        let address = address_raw
            .parse()
            .map_err(|_| EmailError::AddressParseError)?;

        // Create the email body.
        let mut buf_plain = Vec::new();
        let mut buf_html = Vec::new();
        match &email {
            EmailTemplate::VerifyEmail {
                username,
                verify_link,
            } => {
                templates::verify_email_plain_html(&mut buf_plain, username, verify_link)
                    .map_err(anyhow::Error::from)?;
                templates::verify_email_html(&mut buf_html, username, verify_link)
                    .map_err(anyhow::Error::from)?;
            }
            EmailTemplate::TfaEmail {
                username,
                link,
                action,
            } => {
                templates::tfa_email_plain_html(&mut buf_plain, username, link, action)
                    .map_err(anyhow::Error::from)?;
                templates::tfa_email_html(&mut buf_html, username, link, action)
                    .map_err(anyhow::Error::from)?;
            }
            EmailTemplate::LoginAttempt {
                username,
                time,
                ip,
                user_agent,
            } => {
                templates::login_attempt_plain_html(&mut buf_plain, username, time, ip, user_agent)
                    .map_err(anyhow::Error::from)?;
                templates::login_attempt_html(&mut buf_html, username, time, ip, user_agent)
                    .map_err(anyhow::Error::from)?;
            }
            EmailTemplate::PasswordChanged { username } => {
                templates::password_changed_plain_html(&mut buf_plain, username)
                    .map_err(anyhow::Error::from)?;
                templates::password_changed_html(&mut buf_html, username)
                    .map_err(anyhow::Error::from)?;
            }
            EmailTemplate::PasswordRecovery {
                username,
                link,
                ip,
                user_agent,
            } => {
                templates::recover_password_plain_html(
                    &mut buf_plain,
                    username,
                    link,
                    ip,
                    user_agent,
                )
                .map_err(anyhow::Error::from)?;
                templates::recover_password_html(&mut buf_html, username, link, ip, user_agent)
                    .map_err(anyhow::Error::from)?;
            }
            EmailTemplate::CancelEmailChange {
                username,
                link,
                email,
            } => {
                templates::cancel_email_change_plain_html(&mut buf_plain, username, link, email)
                    .map_err(anyhow::Error::from)?;
                templates::cancel_email_change_html(&mut buf_html, username, link, email)
                    .map_err(anyhow::Error::from)?;
            }
        }
        let buf_plain = String::from_utf8(buf_plain).map_err(anyhow::Error::from)?;
        let buf_html = String::from_utf8(buf_html).map_err(anyhow::Error::from)?;

        // Extract the subject from the email and remove it.
        let mut subject = None;
        let buf_plain = buf_plain
            .lines()
            .filter_map(|line| {
                if let Some(stripped) = line.trim().strip_prefix("# ") {
                    subject = Some(stripped);
                    None
                } else {
                    Some(line.trim())
                }
            })
            .join("\n");

        if subject.is_none() {
            return Err(anyhow::Error::msg("subject is not in the template").into());
        }

        let mid = format!("<{}@email.bonfire.moe>", nanoid!());

        if self.mailer.is_none() {
            info!("sending email to {address}: {buf_plain}");
        }

        if self.mailer.is_some() {
            let span = span!(
                Level::INFO,
                "sending email",
                address = address_raw,
                template = <EmailTemplate as Into<&'static str>>::into(email),
                id = mid,
            );
            let message = Message::builder()
                .message_id(Some(mid))
                .from(self.from)
                .to(address)
                .subject(subject.unwrap())
                .multipart(
                    MultiPart::alternative()
                        .singlepart(SinglePart::plain(buf_plain))
                        .singlepart(SinglePart::html(buf_html)),
                )
                .map_err(anyhow::Error::from)?;

            self.queue.push((span, message));
        }

        Ok(())
    }

    #[instrument(skip_all)]
    async fn _send_telegram(&self, content: String) -> Result<(), EmailError> {
        let Some(key) = self.base.config.telegram.bot_key.clone() else {
            info!("telegram not configured, skipping");
            return Ok(());
        };

        let chat_ids = if content.starts_with("[verbose]") {
            self.base
                .config
                .telegram
                .verbose_chat_ids
                .clone()
                .unwrap_or_default()
        } else {
            self.base
                .config
                .telegram
                .chat_ids
                .clone()
                .unwrap_or_default()
        };

        let futures = chat_ids.into_iter().map(move |chat_id| {
            reqwest::Client::new()
                .get(format!("https://api.telegram.org/bot{key}/sendMessage"))
                .query(&[("chat_id", chat_id.as_str()), ("text", &content)])
                .send()
        });

        tokio::spawn(async move {
            let _ = try_join_all(futures)
                .await
                .map_err(|err| error!("failed to send tg notification: {err:?}"));
        });

        Ok(())
    }

    async fn start_email_sender(&self) -> Infallible {
        loop {
            let message = self.queue.pop();
            let Some((span, message)) = message else {
                sleep(Duration::from_millis(100)).await;
                continue;
            };

            if let Some(mailer) = self.mailer.deref() {
                let _ = mailer.send(message).instrument(span).await.map_err(|err| {
                    warn!("error sending email: {err:?}");
                });
            }
        }
    }
}
