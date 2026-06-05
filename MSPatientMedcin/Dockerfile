# Stage 1: build
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /build

COPY .mvn .mvn
COPY mvnw mvnw.cmd pom.xml ./
COPY src ./src
RUN --mount=type=cache,target=/root/.m2/repository \
    sed -i 's/\r$//' mvnw && chmod +x mvnw \
    && ./mvnw -q -DskipTests package -B \
       -Dmaven.wagon.http.retryHandler.count=5 \
       -Dmaven.wagon.httpconnectionManager.ttlSeconds=120

# Stage 2: runtime
FROM eclipse-temurin:17-jre-jammy
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /build/target/*.jar app.jar

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
