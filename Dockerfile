
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .

RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-jammy

RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080

USER appuser
ENTRYPOINT ["java", "-jar", "/app/app.jar"]