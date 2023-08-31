FROM gradle:8.0-jdk17 AS builder

COPY . /app
WORKDIR /app/
RUN gradle CampfireServer:build CampfireServerMedia:build --no-daemon

FROM rust:1.72-buster AS rust-builder

COPY ./rust-bonfire/Cargo.lock ./rust-bonfire/Cargo.toml /app/rust-bonfire/
WORKDIR /app/rust-bonfire/

RUN mkdir src && \
    echo "fn main() {println!(\"if you see this, the build broke\")}" > src/main.rs && \
    cargo build --release && \
    rm -rf src target/release/deps/rust_bonfire*

COPY ./rust-bonfire/src /app/rust-bonfire/src
COPY ./rust-bonfire/.sqlx /app/rust-bonfire/.sqlx
RUN cargo build --release

FROM amazoncorretto:17-al2023 AS runner

WORKDIR /app/
RUN mkdir -p /app/CampfireServer/res /app/lib

RUN yum install -y tar openssl bash findutils
RUN curl -o /app/bcprov.jar https://downloads.bouncycastle.org/java/bcprov-jdk15on-170.jar

COPY --from=builder /app/docker-entrypoint.sh /app/
RUN chmod +x /app/docker-entrypoint.sh

COPY --from=builder /app/CampfireServer/build/distributions/CampfireServer.tar /app/
COPY --from=builder /app/CampfireServerMedia/build/distributions/CampfireServerMedia.tar /app/
RUN tar -xf CampfireServer.tar && rm CampfireServer.tar
RUN tar -xf CampfireServerMedia.tar && rm CampfireServerMedia.tar

COPY --from=builder /app/CampfireServer/res /app/CampfireServer/res

COPY --from=rust-builder /app/rust-bonfire/target/release/rust-bonfire /app/rust-bonfire

EXPOSE 4022 4023 4024 4026 4027 4028 4051
VOLUME /app/secrets

CMD "/app/docker-entrypoint.sh"
