FROM gradle:8.7-jdk17 AS builder

COPY . /app
WORKDIR /app/
RUN gradle CampfireServer:build --no-daemon

FROM rust:1.83 AS rust-builder

WORKDIR /app
COPY ./rust-auth /app/rust-auth
COPY ./rust-core /app/rust-core
COPY ./rust-email /app/rust-email
COPY ./rust-level /app/rust-level
COPY ./rust-notification /app/rust-notification
COPY ./rust-melior /app/rust-melior
COPY ./rust-profile /app/rust-profile
COPY ./rust-security /app/rust-security
COPY ./.sqlx /app/.sqlx
COPY ./migrations /app/migrations
COPY ./Cargo.toml ./Cargo.lock /app/
RUN cargo build --release

FROM azul/zulu-openjdk-debian:17-jre AS runner

WORKDIR /app/
RUN mkdir -p /app/CampfireServer/res /app/lib

RUN apt-get update && apt-get install -y tar openssl bash findutils curl
RUN curl -o /app/bcprov.jar https://repo1.maven.org/maven2/org/bouncycastle/bcprov-jdk18on/1.79/bcprov-jdk18on-1.79.jar

RUN curl -o /app/libssl11.deb http://nz2.archive.ubuntu.com/ubuntu/pool/main/o/openssl/libssl1.1_1.1.1f-1ubuntu2_amd64.deb
RUN dpkg -i /app/libssl11.deb
RUN rm /app/libssl11.deb

COPY --from=builder /app/docker-entrypoint.sh /app/
RUN chmod +x /app/docker-entrypoint.sh

COPY --from=builder /app/CampfireServer/build/distributions/CampfireServer.tar /app/
RUN tar -xf CampfireServer.tar && rm CampfireServer.tar

COPY --from=builder /app/CampfireServer/res /app/CampfireServer/res

COPY --from=rust-builder /app/target/release/b-melior /app/rust-bonfire

EXPOSE 4022 4023 4024 4026 4027 4028 4051
VOLUME /app/secrets

CMD "/app/docker-entrypoint.sh"
