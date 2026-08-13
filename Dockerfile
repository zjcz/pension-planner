FROM node:24-alpine AS frontend-build
WORKDIR /build/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

FROM maven:3.9-eclipse-temurin-25 AS backend-build
WORKDIR /build/backend
COPY backend/pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline
COPY backend/ ./
COPY --from=frontend-build /build/frontend/dist ./src/main/resources/static
RUN mvn -q -DskipTests package

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=backend-build /build/backend/target/*.jar ./pension-planner.jar
ENV SERVER_PORT=8080
ENV DATABASE_PATH=/data/pension.db
ENV ALLOW_REGISTRATION=true
ENV COOKIE_SECURE=false
VOLUME ["/data"]
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/pension-planner.jar"]
