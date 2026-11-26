
# Etapa 1: Compilación
FROM eclipse-temurin:21-jdk-noble AS build

WORKDIR /workspace

COPY gradlew build.gradle.kts settings.gradle.kts ./
COPY gradle/ ./gradle/
COPY src/ ./src/

RUN chmod +x gradlew \
    && ./gradlew --no-daemon test bootJar

# Etapa 2: Ejecución
FROM eclipse-temurin:21-jre-noble

WORKDIR /app

COPY --from=build /workspace/build/libs/*.jar /app/app.jar

ENV JAVA_TOOL_OPTIONS="-Djava.io.tmpdir=/tmp -Duser.home=/tmp -XX:MaxRAMPercentage=60.0"

USER 10001:10001

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
