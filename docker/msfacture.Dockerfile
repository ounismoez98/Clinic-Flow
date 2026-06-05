# Dockerfile for MSFacture service
# Builds the MSFacture Spring Boot module and creates a lightweight runtime image.

FROM eclipse-temurin:17-jdk-jammy AS build
ARG MODULE_DIR=MSFacture
WORKDIR /build
COPY ${MODULE_DIR}/mvnw ${MODULE_DIR}/mvnw.cmd ${MODULE_DIR}/pom.xml ./
COPY ${MODULE_DIR}/.mvn ./.mvn
COPY ${MODULE_DIR}/src ./src
RUN --mount=type=cache,target=/root/.m2/repository \
    sed -i '\r$//' mvnw && \
    chmod +x mvnw && \
    ./mvnw -q -DskipTests package && \
    cp target/*.jar /build/app.jar

FROM eclipse-temurin:17-jre-jammy
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /build/app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
