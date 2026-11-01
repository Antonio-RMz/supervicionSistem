# Etapa 1: Construcción (compila el código usando Maven)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copia el pom.xml y el código fuente
COPY pom.xml .
COPY src ./src

# Empaqueta la aplicación saltando las pruebas
RUN mvn clean package -DskipTests

# Etapa 2: Servidor (ejecuta el .jar compilado)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copia el archivo .jar desde la etapa de construcción anterior
COPY --from=build /app/target/*.jar app.jar

# Expone el puerto donde corre tu app (cámbialo si tu app no usa el 8080)
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]