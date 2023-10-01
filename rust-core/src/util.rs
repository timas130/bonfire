use std::io::ErrorKind;

pub fn anyhow_unknown() -> anyhow::Error {
    anyhow::Error::msg("Unknown error")
}

pub fn sqlx_unknown() -> sqlx::Error {
    sqlx::Error::Io(std::io::Error::new(ErrorKind::Other, "Unknown error"))
}

pub fn anyhow_clone(_: &anyhow::Error) -> anyhow::Error {
    anyhow_unknown()
}

pub fn sqlx_clone(_: &sqlx::Error) -> sqlx::Error {
    sqlx_unknown()
}
