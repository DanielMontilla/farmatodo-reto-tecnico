# =================================================================
# == Stage 1: Build the application using Maven and Java 24     ==
# =================================================================
# Use an up-to-date base image with Maven and Eclipse Temurin 24 (a high-quality JDK build)
FROM eclipse-temurin:24-jdk AS build

RUN apt-get update && apt-get install -y maven

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml first to leverage Docker's layer caching.
# This ensures dependencies are only re-downloaded if pom.xml changes.
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the rest of your application's source code
COPY src ./src

# Build the application into a single executable JAR file.
# We skip tests because they should be run in a separate CI/CD step.
RUN mvn package -DskipTests

# =================================================================
# == Stage 2: Create the final, lean production image           ==
# =================================================================
# Use a minimal base image with only the Java 24 Runtime Environment (JRE)
FROM eclipse-temurin:24-jre

# Set the working directory
WORKDIR /app

# Copy the executable JAR from the 'build' stage into this final stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port that the Spring Boot application listens on
EXPOSE 8080

# This is the command that will be run when the container starts.
# It tells Java to run the application JAR file.
# The environment variables (like DB_URL, HMAC_SECRET_KEY, etc.) will be
# provided by the Google Cloud Run service at runtime.
ENTRYPOINT ["java", "-jar", "app.jar"]
