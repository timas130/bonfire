#!/bin/bash

echo "[docker] Bonfire for Docker by sit"

if [ -e "/app/secrets/Secrets.json" ]; then
    echo "[docker] found Secrets.json, skipping"
else
    echo "[docker] fatal: Secrets.json not found"
    exit 1
fi

if [ -z "$JKS_PASSWORD" ]; then
    JKS_PASSWORD="docker"
fi

if [ -e "/app/secrets/Certificate.jks" ] && [ -e "/app/secrets/Certificate.bks" ]; then
    echo "[docker] found Certificate.jks and Certificate.bks, skipping"
else
    rm -f /app/secrets/Certificate.jks /app/secrets/Certificate.bks

    echo "[docker] generating self-signed Certificate.jks and Certificate.bks"
    openssl req -new -x509 -newkey rsa:4096 -keyout /app/secrets/cert.key \
            -out /app/secrets/cert.crt -subj "/C=XX/CN=docker" -days 365 \
            -passout pass:docker
    openssl pkcs12 -export -out /app/secrets/cert.pfx -inkey /app/secrets/cert.key \
            -in /app/secrets/cert.crt -passin pass:docker -passout pass:docker
    keytool -importkeystore -srckeystore /app/secrets/cert.pfx -srcstoretype pkcs12 \
            -srcalias 1 -srcstorepass docker -destkeystore /app/secrets/Certificate.jks \
            -deststoretype jks -deststorepass $JKS_PASSWORD -destalias Campfire -noprompt
    keytool -import -alias Campfire -file /app/secrets/cert.crt -storetype BKS \
            -storepass $JKS_PASSWORD -keystore /app/secrets/Certificate.bks \
            -provider org.bouncycastle.jce.provider.BouncyCastleProvider \
            -providerpath /app/bcprov.jar -noprompt
fi

echo "[docker] starting java"
cd /app/ || exit

if [ "$START_RUST" != "false" ]; then
    if [ "$START_MEDIA" != "false" ] || [ "$START_SERVER" != "false" ]; then
        /app/rust-bonfire &
    else
        /app/rust-bonfire
    fi
else
    echo "[docker] rust-bonfire start prevented, START_RUST is false"
fi

if [ "$START_SERVER" != "false" ]; then
    /app/CampfireServer/bin/CampfireServer
else
    echo "[docker] CampfireServer start prevented, START_CAMPFIRE is false"
fi
