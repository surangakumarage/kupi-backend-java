# Runtime image for kupi-auth-service.
# Build the jar first:  mvn clean package -DskipTests
# Then from kupi-auth-service/:        docker build -f docker/Dockerfile -t kupi/kupi-auth-service:latest .
FROM eclipse-temurin:21-jre

WORKDIR /app
# Logback writes to ${LOG_PATH}; point it at a folder the non-root user can write
ENV LOG_PATH=/tmp/logs
RUN useradd --system --uid 1001 kupi

COPY target/*.jar app.jar

USER kupi
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
