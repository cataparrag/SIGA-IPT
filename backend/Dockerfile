# ---- Stage 1: build the .jar with Maven ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
# Windows saves mvnw with CRLF line endings; convert them so Linux can run it.
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -B -q dependency:go-offline
COPY src src
# Tests run in the CI pipeline, not while building the image.
RUN ./mvnw -B -q package -DskipTests

# ---- Stage 2: small runtime image with only the JRE and the .jar ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
