# Use Java 21 runtime
FROM eclipse-temurin:21-jre

# Create app directory
WORKDIR /app

# Copy the Spring Boot fat jar
COPY target/*.jar app.jar

# Expose port (Render uses $PORT internally)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
