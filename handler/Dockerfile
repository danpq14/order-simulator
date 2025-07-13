# Dockerfile for Handler Service
FROM gradle:8.5-jdk17-alpine AS builder

WORKDIR /app

# Copy gradle files
COPY build.gradle settings.gradle ./
COPY gradlew ./
COPY gradle/ gradle/

# Make gradlew executable
RUN chmod +x ./gradlew

# Copy common module (dependency)
COPY common/ common/

# Copy handler module
COPY handler/ handler/

# Build handler
RUN ./gradlew :handler:bootJar

# Runtime stage
FROM openjdk:17-jdk-slim

WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy the built jar
COPY --from=builder /app/handler/build/libs/*.jar app.jar

# Create non-root user
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
