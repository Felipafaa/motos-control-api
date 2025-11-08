#!/bin/bash
# Script de criação de infraestrutura na Azure
# Executar com Azure CLI logado

# 1. Criar Grupo de Recursos
az group create --name rg-challenge --location brazilsouth

# 2. Criar Azure Container Registry (ACR)
az acr create --resource-group rg-challenge --name devopsfepile --sku Basic --admin-enabled true

# 3. Criar Banco de Dados (ACI com PostgreSQL)
az container create --resource-group rg-challenge --name meu-postgres-db --image postgres:14-alpine --ports 5432 --ip-address Public --os-type Linux --cpu 1 --memory 1.5 --environment-variables "POSTGRES_USER=USUARIOTOP" "POSTGRES_PASSWORD=SENHATOP" "POSTGRES_DB=motosdb"

# 4. Criar Plano de App Service
az appservice plan create --name MeuPlanoAppService --resource-group rg-challenge --is-linux

# 5. Criar Web App for Containers
az webapp create --resource-group rg-challenge --plan MeuPlanoAppService --name nomeunicowebapp --deployment-container-image-name devopsfepile.azurecr.io/motos-control-api:latest

# 6. Configurar Web App para conectar no ACR
az webapp config container set --name nomeunicowebapp --resource-group rg-challenge --docker-custom-image-name devopsfepile.azurecr.io/motos-control-api:latest --docker-registry-server-url https://devopsfepile.azurecr.io --docker-registry-server-user $(az acr credential show -n devopsfepile --query "username" -o tsv) --docker-registry-server-password $(az acr credential show -n devopsfepile --query "passwords[0].value" -o tsv)

echo "Infraestrutura criada. Lembre-se de pegar o IP do banco com:"
echo "az container show --resource-group rg-challenge --name meu-postgres-db --query 'ipAddress.ip' --output tsv"