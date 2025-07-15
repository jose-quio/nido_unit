# Imagen base oficial de Java
FROM openjdk:21-jdk-slim

# Directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiamos el .jar construido
COPY build/libs/*.jar app.jar

# Copiamos la base de datos SQLite
COPY src/main/resources/database.db /app/database.db

# Puerto expuesto (por defecto Spring Boot usa 8080)
EXPOSE 8080

# Comando para ejecutar la app
ENTRYPOINT ["java", "-jar", "app.jar"]
