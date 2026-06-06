FROM eclipse-temurin:17-jdk-jammy AS build
ARG MODULE_DIR=MsFacture
WORKDIR /build
COPY ${MODULE_DIR}/mvnw ${MODULE_DIR}/mvnw.cmd .
COPY ${MODULE_DIR}/.mvn .mvn
COPY ${MODULE_DIR}/pom.xml .
RUN ./mvnw -B dependency:go-offline
COPY ${MODULE_DIR}/src src
RUN ./mvnw -B package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
