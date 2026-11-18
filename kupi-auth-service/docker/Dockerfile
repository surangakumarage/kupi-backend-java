
# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Copy Maven project files
COPY pom.xml .
COPY src ./src

# Build the project
RUN mvn clean package -DskipTests

# Stage 2: Create the runtime image
FROM eclipse-temurin:21-jre-slim

WORKDIR /app

# Copy only the built jar from builder stage
COPY --from=builder /build/target/kupi-auth-service-0.0.1-SNAPSHOT.jar kupi-auth-service.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "kupi-auth-service.jar"]
