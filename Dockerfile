# Multi-stage build for optimization

# Stage 1: Build
FROM maven:3.8-openjdk-11-slim AS builder

WORKDIR /app

# Copy POM
COPY pom.xml .

# Copy source
COPY src ./src

# Build application
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM openjdk:11-jre-slim

# Install Chromium for headless browser
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    chromium-browser \
    xvfb \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /app/target/taxila-notification-bot-1.0.0.jar .

# Copy logging configuration
COPY src/main/resources/logback.xml .

# Create data directory
RUN mkdir -p /app/data /app/logs

# Health check
HEALTHCHECK --interval=5m --timeout=30s --start-period=30s --retries=3 \
    CMD pgrep -f "taxila-notification-bot" || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "taxila-notification-bot-1.0.0.jar"]
