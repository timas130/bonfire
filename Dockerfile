FROM gradle:8.0-jdk17 AS builder

COPY . /app
WORKDIR /app/
RUN gradle CampfireServer:build CampfireServerMedia:build

FROM amazoncorretto:17-alpine AS runner

WORKDIR /app/
RUN mkdir -p /app/CampfireServer/res /app/lib

COPY --from=builder /app/docker-entrypoint.sh /app/
COPY --from=builder /app/CampfireServer/build/distributions/CampfireServer.tar /app/
COPY --from=builder /app/CampfireServerMedia/build/distributions/CampfireServerMedia.tar /app/
COPY --from=builder /app/CampfireServer/res /app/CampfireServer/res
RUN apk add tar openssl curl bash
RUN tar -xf CampfireServer.tar && rm CampfireServer.tar
RUN tar -xf CampfireServerMedia.tar && rm CampfireServerMedia.tar
RUN curl -o /app/bcprov.jar https://downloads.bouncycastle.org/java/bcprov-jdk15on-170.jar
RUN chmod +x /app/docker-entrypoint.sh

EXPOSE 4022 4023 4024 4026 4027 4028 4051
VOLUME /app/secrets

CMD "/app/docker-entrypoint.sh"
