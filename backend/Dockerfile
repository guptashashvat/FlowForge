FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app

RUN addgroup --system flowforge && adduser --system --ingroup flowforge flowforge
COPY --from=build /app/target/flowforge-backend-0.1.0.jar app.jar
USER flowforge

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
