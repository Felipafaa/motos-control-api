# Stage 1: build (usa Maven + JDK para compilar o jar)
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn -B -DskipTests package

# Stage 2: runtime (imagem leve)
FROM eclipse-temurin:17-jre-jammy
# criar grupo e user sem privilégios
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
# executar como usuário não-root
USER appuser
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
