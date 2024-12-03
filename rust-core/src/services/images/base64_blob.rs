use base64::engine::general_purpose;
use base64::Engine;
use serde::de::Visitor;
use serde::{de, Deserialize, Deserializer, Serialize, Serializer};
use std::fmt;
use std::fmt::Debug;

pub struct Base64Blob(pub Vec<u8>);

impl Debug for Base64Blob {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        f.write_str("Base64Blob(...)")
    }
}

impl Serialize for Base64Blob {
    fn serialize<S>(&self, serializer: S) -> Result<S::Ok, S::Error>
    where
        S: Serializer,
    {
        let result = general_purpose::STANDARD_NO_PAD.encode(&self.0);
        serializer.serialize_str(&result)
    }
}

impl<'de> Deserialize<'de> for Base64Blob {
    fn deserialize<D>(deserializer: D) -> Result<Self, D::Error>
    where
        D: Deserializer<'de>,
    {
        deserializer.deserialize_str(Base64Visitor).map(Self)
    }
}

struct Base64Visitor;
impl Visitor<'_> for Base64Visitor {
    type Value = Vec<u8>;

    fn expecting(&self, formatter: &mut fmt::Formatter) -> fmt::Result {
        formatter.write_str("a base64 string")
    }

    fn visit_str<E>(self, v: &str) -> Result<Self::Value, E>
    where
        E: de::Error,
    {
        general_purpose::STANDARD_NO_PAD
            .decode(v)
            .map_err(|_| de::Error::custom("failed to decode base64 string"))
    }
}
