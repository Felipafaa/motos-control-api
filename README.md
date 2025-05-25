# API Motos Control

API REST para gerenciamento e controle de motocicletas e suas localizações no pátio da Mottu.

## Descrição do Projeto

Esta API fornece endpoints para realizar operações CRUD (Criar, Ler, Atualizar e Deletar) em entidades de `Moto` e `Localizacao`. Ela permite:

* Cadastrar novas motos.
* Visualizar todas as motos com paginação e ordenação.
* Buscar motos por modelo.
* Atualizar informações de motos existentes.
* Remover motos do sistema.
* Associar uma moto a uma localização.
* Desassociar uma moto de uma localização.
* Cadastrar novas localizações.
* Visualizar todas as localizações (com cache).
* Buscar localizações por zona.
* Atualizar informações de localizações existentes.
* Remover localizações do sistema.
* Associar uma localização a uma moto.

## Autores

* Pedro Henrique de Souza
* Felipe Rosa Peres
* Vinicius de Souza Sant Anna

## Pré-requisitos

* JDK 17 ou superior instalado.
* Maven instalado.

## Como Executar o Projeto

1.  **Clone o repositório:**
    ```bash
    git clone https://github.com/PedroSouza1111/motos-control-api.git
    cd motos-control-api
    ```

2.  **Compile e Empacote o Projeto (usando Maven Wrapper incluído):**
    No Linux ou macOS:
    ```bash
    ./mvnw clean package
    ```
    No Windows:
    ```bash
    .\mvnw.cmd clean package
    ```
    Isso irá gerar um arquivo `.jar` no diretório `target/`.

3.  **Execute a Aplicação:**
    ```bash
    java -jar target/motos-control-api-0.0.1-SNAPSHOT.jar
    ```
    (Substitua `motos-control-api-0.0.1-SNAPSHOT.jar` pelo nome do arquivo JAR gerado, se for diferente).

4.  **Acesse a API:**
    A API estará disponível em `http://localhost:8080`.

    * **Documentação Swagger UI:** Você pode acessar a documentação interativa da API (Swagger UI) em:
        `http://localhost:8080/swagger-ui.html`
    * **Endpoints Principais:**
        * Motos: `/api/motos`
        * Localizações: `/api/localizacoes`

## Endpoints da API

A API expõe os seguintes endpoints principais:

### Motos

* `GET /api/motos`: Lista todas as motos com paginação e filtro por modelo.
    * Parâmetros de Query: `page`, `size`, `sort`, `modelo`
* `GET /api/motos/{id}`: Busca uma moto pelo ID.
* `POST /api/motos`: Cria uma nova moto.
    * Corpo da Requisição: `MotoDTO`
* `PUT /api/motos/{id}`: Atualiza uma moto existente.
    * Corpo da Requisição: `MotoDTO`
* `DELETE /api/motos/{id}`: Remove uma moto.
* `PUT /api/motos/{idMoto}/localizacao/{idLocalizacao}`: Associa uma localização existente a uma moto.

### Localizações

* `GET /api/localizacoes`: Lista todas as localizações (resultado cacheado).
* `GET /api/localizacoes/zona/{zona}`: Busca localizações por zona.
* `POST /api/localizacoes`: Cria uma nova localização.
    * Corpo da Requisição: `LocalizacaoDTO`
* `PUT /api/localizacoes/{id}`: Atualiza uma localização existente.
    * Corpo da Requisição: `LocalizacaoDTO`
* `DELETE /api/localizacoes/{id}`: Remove uma localização.
* `PUT /api/localizacoes/{idLocalizacao}/moto/{idMoto}`: Associa uma moto existente a uma localização.
