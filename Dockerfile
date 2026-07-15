FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

COPY backend/.mvn ./.mvn
COPY backend/mvnw backend/pom.xml ./
RUN chmod +x mvnw && ./mvnw -q -DskipTests dependency:go-offline

COPY backend/src ./src
RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
