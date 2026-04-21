# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# 1. Copy the Parent POM from the root context
COPY pom.xml /app/pom.xml

# 2. Copy the specific service code
# (Paths assume you run 'docker build' from the root folder)
COPY RegistrationService /app/RegistrationService

# 3. Build the service
WORKDIR /app/RegistrationService
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/RegistrationService/target/RegistrationService-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]