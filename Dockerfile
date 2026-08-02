# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy pom first — dependency layer is cached until pom.xml changes
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy source and build the fat JAR, skipping tests
COPY src ./src
RUN mvn package -DskipTests -q

# ─── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

# Non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy the fat JAR from the builder stage
COPY --from=builder /app/target/wisewealth-api-1.0.0.jar app.jar

# Directory for wealth-check PDF reports (matches app.wealth-check.reports-dir)
RUN mkdir -p /app/wealth-check-reports && chown appuser:appgroup /app/wealth-check-reports

USER appuser

# Expose the Spring Boot port
EXPOSE 8080

# Override with environment variables at runtime:
#   DB_URL, DB_USERNAME, DB_PASSWORD, JWT_SECRET,
#   BREVO_USERNAME, BREVO_PASSWORD, CORS_ORIGIN,
#   FRONTEND_URL, TRUSTED_PROXY, MAIL_FROM, WEALTH_REPORTS_DIR
ENV WEALTH_REPORTS_DIR=/app/wealth-check-reports

# Use prod profile in container (no local dev overrides)
ENTRYPOINT ["java", \
  "-Dspring.profiles.active=prod", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
