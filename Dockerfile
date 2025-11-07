# Estágio 1: Build da aplicação com Maven (Rodando os testes)
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
# Corrigido: Removido -DskipTests para rodar os testes
RUN mvn -B package

# Estágio 2: Imagem final, leve e segura
FROM eclipse-temurin:17-jre-jammy

# Cria um grupo e usuário não-root para rodar a aplicação
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
WORKDIR /app

# Copia o .jar compilado do estágio de build
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080

# Define o usuário não-root para executar a aplicação
USER appuser
ENTRYPOINT ["java", "-jar", "/app/app.jar"]