use async_graphql::{InputValueError, InputValueResult, Scalar, ScalarType, Value};

pub struct OkResp;

/// The string `"ok"`. That's it
#[Scalar(name = "Ok")]
impl ScalarType for OkResp {
    fn parse(value: Value) -> InputValueResult<Self> {
        if value == Value::String("ok".to_string()) {
            Ok(Self)
        } else {
            Err(InputValueError::expected_type(value))
        }
    }

    fn to_value(&self) -> Value {
        Value::String("ok".to_string())
    }
}
