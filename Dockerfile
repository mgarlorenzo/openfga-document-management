FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

# Install OpenFGA
RUN apt-get update && apt-get install -y curl unzip && \
    curl -L https://github.com/openfga/openfga/releases/latest/download/openfga_linux_amd64.zip -o openfga.zip && \
    unzip openfga.zip && \
    mv openfga /usr/local/bin/ && \
    rm openfga.zip && \
    apt-get remove -y unzip && \
    apt-get autoremove -y && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Copy the Java application
COPY --from=build /workspace/target/document-management-0.0.1-SNAPSHOT.jar app.jar

# Create startup script
RUN echo '#!/bin/sh\n\
# Start OpenFGA in the background (only listening on localhost)\n\
openfga run --http-addr 127.0.0.1:9080 &\n\
# Start the Java application\n\
java -jar /app/app.jar\n\
' > /app/start.sh && chmod +x /app/start.sh

# Only expose the Java application port
EXPOSE 8080

# Use the startup script
CMD ["/app/start.sh"]
