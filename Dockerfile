# syntax=docker/dockerfile:1

# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Resolve dependencies first so this layer is cached unless pom.xml changes
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Build the executable jar (tests skipped; they need Redis/SMTP at runtime)
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre-jammy AS runtime
WORKDIR /app

# curl is used by the container healthcheck; clean apt cache to keep the layer small
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Run as an unprivileged user
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring:spring

COPY --from=build /build/target/*.jar app.jar

EXPOSE 8080

# Tune the JVM at runtime without rebuilding the image
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -fsS http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
