# Use Java 17
FROM eclipse-temurin:17-jdk

# Set working directory
WORKDIR /app

# Copy all files
COPY . .

# Give permission to mvnw
RUN chmod +x mvnw

# Build the project
RUN ./mvnw clean install -DskipTests

# Use dynamic port from Render
ENV PORT=8080
EXPOSE 8080

# Run the app
CMD ["sh", "-c", "java -jar target/backend-0.0.1-SNAPSHOT.jar --server.port=$PORT"]