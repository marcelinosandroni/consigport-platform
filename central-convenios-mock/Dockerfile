# syntax=docker/dockerfile:1

# ── Build ───────────────────────────────────────────────────────────────────
# The reactor parent lives at the repository root, so the build context is the
# repository root and the Dockerfile is selected with -f
# central-convenios-mock/Dockerfile.
FROM maven:3.10-eclipse-temurin-21 AS build
WORKDIR /workspace

# The poms are copied first: the dependency layer survives every source change.
# Both module poms are needed because the reactor parent lists them as modules.
COPY pom.xml ./
COPY backend/pom.xml backend/pom.xml
COPY central-convenios-mock/pom.xml central-convenios-mock/pom.xml
RUN mvn -B -q -pl central-convenios-mock -am -DskipTests dependency:go-offline

COPY central-convenios-mock/src central-convenios-mock/src
RUN mvn -B -q -pl central-convenios-mock -am -DskipTests package

# ── Runtime ─────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

# The mock never touches the filesystem: it answers from memory.
RUN addgroup -S consigport && adduser -S -G consigport consigport
WORKDIR /app
COPY --from=build \
  /workspace/central-convenios-mock/target/central-convenios-mock-*.jar \
  /app/app.jar

USER consigport
EXPOSE 8081

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget -qO- http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
