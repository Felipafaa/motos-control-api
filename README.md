# API Motos Control

API REST para gerenciamento e controle de motocicletas e suas localizações no pátio. Este projeto foi desenvolvido como parte do Challenge de DevOps Tools & Cloud Computing da FIAP.

## Descrição da Solução

Esta API fornece endpoints para realizar operações CRUD (Criar, Ler, Atualizar e Deletar) em entidades de `Moto` e `Localizacao`. A aplicação é construída em Java com Spring Boot e utiliza Flyway para gerenciamento do schema do banco de dados.

## Benefícios para o Negócio 

A solução centraliza e automatiza o controle de ativos (motocicletas), permitindo um gerenciamento mais eficiente do pátio. Isso resulta em:
* **Otimização do Tempo:** Reduz o tempo necessário para localizar e gerenciar o status de cada veículo.
* **Redução de Erros:** Automatiza processos que antes eram manuais, diminuindo a chance de erros humanos.
* **Centralização da Informação:** Fornece um ponto único de verdade sobre a localização e o estado de todas as motos.

## Arquitetura da Solução na Nuvem 

A aplicação foi implantada na Microsoft Azure seguindo o modelo de **Contêineres como Serviço (CaaS)**, utilizando os seguintes recursos:

1.  **Código-Fonte:** Versionado no GitHub.
2.  **Imagem Docker:** A aplicação é empacotada em uma imagem Docker. O build é feito na máquina local do desenvolvedor.
3.  **Azure Container Registry (ACR):** A imagem Docker é enviada para o ACR, um registro de contêiner privado e seguro na Azure. [cite: 8]
4.  **Azure Container Instances (ACI):** O ACI executa a imagem Docker a partir do ACR, expondo a API para a internet. [cite_start]O ACI é uma solução PaaS para rodar contêineres sem gerenciar servidores. [cite: 8]
5.  **Azure Database for PostgreSQL:** Um serviço de banco de dados gerenciado (PaaS) que armazena os dados da aplicação de forma persistente e segura. [cite: 6, 20]

O fluxo de deploy é: `Código Local` -> `Build Docker` -> `Push para ACR` -> `Run no ACI`.

## Autores

* Pedro Henrique de Souza
* Felipe Rosa Peres
* Vinicius de Souza Sant Anna

---

## Passo a Passo para o Deploy na Azure (ACR + ACI) 

Este guia descreve o processo completo para implantar a aplicação do zero na Azure.

### Pré-requisitos
* [Azure CLI](https://learn.microsoft.com/cli/azure/install-azure-cli) instalado.
* [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado e em execução.
* Git instalado.

### Fase 1: Criação da Infraestrutura (Azure Cloud Shell)

Execute estes comandos no **Azure Cloud Shell** para provisionar todos os recursos necessários na nuvem.

```bash
# 1. DEFINA AS VARIÁVEIS
SEU_RM="557636" # Use um identificador único
RG_NAME="rg-motos-challenge-${SEU_RM}"
ACR_NAME="acrmotoschallenge${SEU_RM}"
PG_SERVER_NAME="pgmotoschallenge${SEU_RM}"
DB_NAME="motosdb"
LOCATION="brazilsouth"
PG_PASSWORD="SUA_SENHA_FORTE_AQUI" # Escolha e anote uma senha forte

# 2. REGISTRE OS PROVEDORES DE SERVIÇO
az provider register --namespace Microsoft.ContainerRegistry
az provider register --namespace Microsoft.DBforPostgreSQL
az provider register --namespace Microsoft.ContainerInstance

# 3. CRIE O GRUPO DE RECURSOS
az group create --name $RG_NAME --location $LOCATION

# 4. CRIE O AZURE CONTAINER REGISTRY (ACR)
az acr create --resource-group $RG_NAME --name $ACR_NAME --sku Basic --admin-enabled true

# 5. CRIE O BANCO DE DADOS POSTGRESQL
# Cria o servidor
az postgres flexible-server create \
  --name $PG_SERVER_NAME \
  --resource-group $RG_NAME \
  --location $LOCATION \
  --admin-user "pgadmin" \
  --admin-password $PG_PASSWORD \
  --tier Burstable \
  --sku-name Standard_B1ms \
  --version 14

# Cria a base de dados
az postgres flexible-server db create \
  --resource-group $RG_NAME \
  --server-name $PG_SERVER_NAME \
  --database-name $DB_NAME
  
# Libera o acesso para serviços da Azure (incluindo o ACI)
az postgres flexible-server firewall-rule create \
  --resource-group $RG_NAME \
  --name $PG_SERVER_NAME \
  --rule-name AllowAzureServices \
  --start-ip-address 0.0.0.0 \
  --end-ip-address 0.0.0.0
```

### Fase 2: Build e Push da Imagem (Máquina Local)

Execute estes comandos no terminal da **sua máquina local**, na raiz do projeto.

```bash
# 1. FAÇA LOGIN NO AZURE E NO ACR
az login
az acr login --name <NOME_DO_SEU_ACR> # Ex: acrmotoschallenge557636

# 2. CONSTRUA E ENVIE A IMAGEM DOCKER
# Use uma tag para a versão final, ex: v-final
FINAL_IMAGE=<NOME_DO_SEU_ACR>.azurecr.io/motos-control-api:v-final

# No CMD do Windows:
set FINAL_IMAGE=%FINAL_IMAGE%
docker build -t %FINAL_IMAGE% .
docker push %FINAL_IMAGE%

# No PowerShell ou Linux/Mac:
export FINAL_IMAGE=$FINAL_IMAGE
docker build -t $FINAL_IMAGE .
docker push $FINAL_IMAGE
```

### Fase 3: Deploy do Contêiner (Azure Cloud Shell)

Volte para o **Azure Cloud Shell** para executar o comando final de deploy.

```bash
# 1. REDEFINA AS VARIÁVEIS PARA GARANTIR
SEU_RM="557636" # Use o mesmo identificador
RG_NAME="rg-motos-challenge-${SEU_RM}"
ACR_NAME="acrmotoschallenge${SEU_RM}"
PG_SERVER_NAME="pgmotoschallenge${SEU_RM}"
DB_NAME="motosdb"
PG_PASSWORD="A_MESMA_SENHA_FORTE_QUE_VOCE_ESCOLHEU"

# 2. OBTENHA AS CREDENCIAIS DO ACR
ACR_USERNAME=$(az acr credential show -n $ACR_NAME --query username -o tsv)
ACR_PASSWORD=$(az acr credential show -n $ACR_NAME --query "passwords[0].value" -o tsv)

# 3. CRIE O AZURE CONTAINER INSTANCE (ACI)
az container create \
  --resource-group $RG_NAME \
  --name motos-api-container \
  --image ${ACR_NAME}.azurecr.io/motos-control-api:v-final \
  --os-type Linux \
  --cpu 1.5 --memory 3.0 \
  --ports 8080 \
  --dns-name-label motos-api-challenge-${SEU_RM} \
  --registry-login-server ${ACR_NAME}.azurecr.io \
  --registry-username $ACR_USERNAME \
  --registry-password $ACR_PASSWORD \
  --environment-variables \
    'SPRING_DATASOURCE_URL'="jdbc:postgresql://${PG_SERVER_NAME}[.postgres.database.azure.com:5432/$](https://.postgres.database.azure.com:5432/$){DB_NAME}" \
    'SPRING_DATASOURCE_USERNAME'='pgadmin' \
    'SPRING_DATASOURCE_PASSWORD'=$PG_PASSWORD \
    'SPRING_JPA_HIBERNATE_DDL_AUTO'='update' \
    'SPRING_FLYWAY_BASELINE_ON_MIGRATE'='true' \
    'APP_ADMIN_EMAILS'='admin@example.com'
```

---

## Como Testar a API (Exemplos `curl`) 

Após o deploy, aguarde 2-3 minutos para a aplicação iniciar. A URL pública será exibida no final do comando `az container create`.

Substitua `<URL_DA_SUA_API>` pela URL pública do seu ACI (ex: `http://motos-api-challenge-557636.brazilsouth.azurecontainer.io:8080`).

### Listar Motos (GET)
```bash
curl <URL_DA_SUA_API>/api/motos | jq .
```

### Criar uma Nova Moto (POST) 
```bash
curl -X POST <URL_DA_SUA_API>/api/motos \
-H "Content-Type: application/json" \
-d '{
    "identificador":"MOTO-999", 
    "modelo":"Kawasaki Ninja", 
    "placa":"NINJA01", 
    "ativa":true, 
    "localizacaoId":1
}'
```

### Buscar Moto por ID (GET)
```bash
curl <URL_DA_SUA_API>/api/motos/1 | jq .
```

### Deletar Moto (DELETE)
```bash
curl -X DELETE <URL_DA_SUA_API>/api/motos/1
```