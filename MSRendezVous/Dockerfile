FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /build
COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn ./.mvn
COPY src ./src
RUN --mount=type=cache,target=/root/.m2/repository \
    sed -i 's/\r$//' mvnw && \
    chmod +x mvnw && \
    ./mvnw -q -DskipTests package && \
    cp target/*.jar /build/app.jar

FROM eclipse-temurin:17-jre-jammy
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /build/app.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
