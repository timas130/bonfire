use crate::AuthServer;
use woothee::parser::Parser;

impl AuthServer {
    pub(crate) fn parse_user_agent(user_agent: &str) -> String {
        let parser = Parser::new();
        let result = parser.parse(user_agent);
        match result {
            Some(parsed) => format!(
                "{} {} @ {} {}",
                parsed.name,
                parsed.version,
                parsed.os,
                if parsed.os_version == "UNKNOWN" {
                    ""
                } else {
                    &parsed.os_version
                },
            ),
            None => user_agent.to_string(),
        }
    }
}
