FROM maven:3.8.6-openjdk-8-slim AS build

# Copiar archivos del proyecto
COPY pom.xml .
COPY src ./src

# Construir el proyecto
RUN mvn clean package -DskipTests

# Crear imagen final
FROM openjdk:8-jre-slim

# Copiar dependencias y JAR del build
COPY --from=build target/*.jar app.jar

# Configurar variables de entorno
ENV JAVA_OPTS=""
ENV API_KEY="${API_KEY}"
ENV NSTBROWSER_URL="http://localhost:8888"

# Puerto para NstBrowser
EXPOSE 8888

# Comando para ejecutar
CMD ["java", "-jar", "app.jar"]
