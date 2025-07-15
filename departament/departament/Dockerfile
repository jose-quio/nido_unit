# Etapa 1: Construcción
FROM gradle:8.13-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle clean build -x test

# Etapa 2: Imagen final
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
COPY src/main/resources/database.db ./database.db

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

