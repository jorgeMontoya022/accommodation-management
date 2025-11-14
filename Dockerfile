# ===============================================================
# Etapa 1: Construcción (Build)
# ===============================================================
# Usamos la imagen de JDK 21 sobre Alpine para compilar
FROM eclipse-temurin:21-jdk-alpine AS builder

# Instalamos utilidades básicas (opcional)
RUN apk add --no-cache bash curl

# Definimos el directorio de trabajo dentro del contenedor
WORKDIR /workspace

# Copiamos primero los archivos de configuración de Gradle
# (esto ayuda a cachear dependencias y acelerar builds futuros)
COPY gradle gradle
COPY gradlew .
COPY build.gradle settings.gradle ./

# Damos permisos de ejecución al wrapper de Gradle
RUN chmod +x ./gradlew

# Descargamos las dependencias (sin compilar aún)
RUN ./gradlew dependencies --no-daemon || true

# Copiamos el código fuente del proyecto
COPY src src

# Compilamos y generamos el JAR ejecutable de Spring Boot
RUN ./gradlew clean bootJar --no-daemon -x test

# ===============================================================
# Etapa 2: Ejecución (Runtime)
# ===============================================================
# Imagen más liviana con solo el JRE de Java 21
FROM eclipse-temurin:21-jre-alpine AS runtime

# Creamos un usuario sin privilegios por seguridad
RUN addgroup -S spring && adduser -S spring -G spring

# Definimos el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiamos el JAR generado desde la etapa de compilación
COPY --from=builder /workspace/build/libs/alojamientos_app-0.0.1-SNAPSHOT.jar app.jar

# Exponemos el puerto en el que corre Spring Boot
EXPOSE 8080

# Variables de entorno por defecto (puedes sobreescribirlas)
ENV SPRING_PROFILES_ACTIVE=prod \
    SERVER_PORT=8080 \
    JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

# Cambiamos al usuario sin privilegios
USER spring

# Comando para ejecutar la aplicación
ENTRYPOINT ["sh","-c","java $JAVA_TOOL_OPTIONS -jar app.jar"]
