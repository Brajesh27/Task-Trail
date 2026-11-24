# Stage 1: build the jar with Maven (tests already run in CI / on your machine)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q package -DskipTests

# Stage 2: a small image that only has Java and the jar
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/tasktrail-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
