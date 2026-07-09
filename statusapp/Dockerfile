# Stage 1: builder (jar containing pom dependencies + app code)
FROM maven:3.9.16-eclipse-temurin-21 AS builder

WORKDIR /statusapp

COPY src ./src

COPY pom.xml .

RUN mvn package

# Stage 2: jre software needed to run the image + the app jar
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /statusapp/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]