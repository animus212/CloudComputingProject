# Stage 1: Build with Maven and Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy the parent pom and the specific service code
COPY pom.xml /app/service-pom.xml
COPY src /app/src

# Build the application (skipping tests for faster builds)
RUN mvn -f /app/service-pom.xml clean package -DskipTests

# Stage 2: Lightweight Runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy only the built JAR from the first stage
COPY --from=build /app/target/*.jar app.jar

# Expose the standard Spring Boot port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]