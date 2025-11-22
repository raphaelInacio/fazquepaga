# Stage 1: Build the application with full source code
FROM eclipse-temurin:17-jdk-jammy AS builder

# Set the working directory
WORKDIR /app

# Copy all project files from the build context to the container
COPY . .

# Grant execute permission to the Maven wrapper
RUN chmod +x backend/mvnw

# Build the application using the backend's pom.xml
# This will also build the frontend as defined in the pom.xml
RUN ./backend/mvnw -f backend/pom.xml clean package -Dmaven.test.skip=true

# Stage 2: Create the final, lean image
FROM eclipse-temurin:17-jdk-jammy

# Set the working directory
WORKDIR /app

# Copy only the built JAR file from the builder stage
COPY --from=builder /app/backend/target/taskandpay-0.0.1-SNAPSHOT.jar .

# Expose the port the application runs on
EXPOSE 8080

# Set the command to run the application
ENTRYPOINT ["java", "-jar", "taskandpay-0.0.1-SNAPSHOT.jar"]