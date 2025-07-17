# ------------ STEP 1: Build Stage ----------------
FROM maven:3.8.6-openjdk-17 AS build
WORKDIR /app

# Copy everything and build the project
COPY . .
RUN mvn clean package -DskipTests

# ------------ STEP 2: Runtime Stage ----------------
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the default Spring Boot port
EXPOSE 8080

# Command to run the jar
ENTRYPOINT ["java", "-jar", "app.jar"]
