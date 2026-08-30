# ---- Stage 1: build frontend ----
FROM node:24-alpine AS frontend-build
WORKDIR /build/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# ---- Stage 2: build backend jar (embeds built frontend as static resources) ----
FROM maven:3.9-eclipse-temurin-25 AS backend-build
WORKDIR /build/backend
COPY backend/pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline
COPY backend/ ./
COPY --from=frontend-build /build/frontend/dist ./src/main/resources/static
RUN mvn -q -DskipTests package

# ---- Stage 3: runtime image (single container) ----
FROM eclipse-temurin:25-jre
WORKDIR /app

# curl is used by the docker-compose healthcheck
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# JVM options: silent SQLite native-access warning (same as dev run config),
# tuned for a small container (UseSerialGC for low memory footprint).
ENV JAVA_OPTS="--enable-native-access=ALL-UNNAMED -XX:+UseSerialGC"

# Run as a non-root user; the base image's `ubuntu` user is uid 1000, which
# matches the typical first host user so a bind-mounted ./data dir stays writable.
RUN mkdir -p /data && chown 1000:1000 /data

COPY --from=backend-build --chown=1000:1000 /build/backend/target/*.jar ./pension-planner.jar

ENV SERVER_PORT=8080
ENV DATABASE_PATH=/data/pension.db
ENV ALLOW_REGISTRATION=true
ENV COOKIE_SECURE=false

VOLUME ["/data"]
EXPOSE 8080

USER 1000:1000
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/pension-planner.jar"]