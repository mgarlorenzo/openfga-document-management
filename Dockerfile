FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/target/document-management-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
CMD ["java","-jar","/app/app.jar"]
