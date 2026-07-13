# Stage 1: builder (jar containing pom dependencies + app code)
FROM maven:3.9.16-eclipse-temurin-21 AS builder

WORKDIR /rotating-secret-operator

COPY src ./src

COPY pom.xml .

RUN mvn package -DskipTests

# Stage 2: jre software needed to run the image + the app jar
FROM eclipse-temurin:21-jre

WORKDIR /operator

COPY --from=builder /rotating-secret-operator/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]