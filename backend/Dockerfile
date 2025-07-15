FROM gradle:jdk21 AS builder
WORKDIR /app
COPY . .
RUN ./gradlew clean build -x test

FROM openjdk:21-slim
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]