# Build a single Spring Boot module (MODULE_DIR = e.g. demoEurekaServer, MSPharmacie).
# Build from repo root: docker build -f docker/Dockerfile --build-arg MODULE_DIR=MSPharmacie .

FROM eclipse-temurin:17-jdk-jammy AS build
ARG MODULE_DIR
WORKDIR /build
COPY ${MODULE_DIR}/mvnw ${MODULE_DIR}/mvnw.cmd ${MODULE_DIR}/pom.xml ./
COPY ${MODULE_DIR}/.mvn ./.mvn
COPY ${MODULE_DIR}/src ./src
RUN --mount=type=cache,target=/root/.m2/repository \
    sed -i 's/\r$//' mvnw \
    && chmod +x mvnw \
    && ./mvnw -q -DskipTests package \
       -Dmaven.wagon.http.retryHandler.count=5 \
       -Dmaven.wagon.httpconnectionManager.ttlSeconds=120 \
    && cp target/*.jar /build/app.jar

FROM eclipse-temurin:17-jre-jammy
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /build/app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
