FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q -DskipTests dependency:go-offline
COPY src src
RUN ./mvnw -q clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S restaurant && adduser -S restaurant -G restaurant
COPY --from=build /app/target/*.jar app.jar
USER restaurant
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
