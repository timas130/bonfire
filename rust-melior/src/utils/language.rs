use axum::http;
use axum::http::HeaderName;
use axum_extra::headers::{Error, Header, HeaderValue};
use itertools::Itertools;
use std::iter;

pub struct AcceptLanguage(pub Vec<(String, f32)>);

impl Header for AcceptLanguage {
    fn name() -> &'static HeaderName {
        &http::header::ACCEPT_LANGUAGE
    }

    fn decode<'i, I>(values: &mut I) -> Result<Self, Error>
    where
        Self: Sized,
        I: Iterator<Item = &'i HeaderValue>,
    {
        let value = values.next().ok_or(Error::invalid())?;
        if values.next().is_some() {
            return Err(Error::invalid());
        }

        Ok(Self(accept_language::parse_with_quality(
            value.to_str().map_err(|_| Error::invalid())?,
        )))
    }

    fn encode<E: Extend<HeaderValue>>(&self, values: &mut E) {
        let value = HeaderValue::from_str(
            &self
                .0
                .iter()
                .map(|(lang, q)| format!("{lang};q={q}"))
                .join(","),
        )
        .unwrap();
        values.extend(iter::once(value));
    }
}
