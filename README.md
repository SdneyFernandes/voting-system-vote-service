# 🚀 Microsserviço de Votação - Sistema de Votação

Microsserviço central do **Sistema de Votação**, responsável por gerenciar o ciclo de vida das sessões de votação e registrar os votos dos usuários.

Este serviço opera dentro de um ecossistema de microsserviços, comunicando-se com o **Serviço de Usuários** para validações e registrando-se no **Eureka** para ser descoberto por outros componentes, como o API Gateway.

## 📝 Status do Projeto

| Funcionalidade | Status | Detalhes |
| :--- | :--- | :--- |
| **Gerenciamento de Sessões** | ✅ Concluído | CRUD completo para sessões de votação. |
| **Registro de Votos** | ✅ Concluído | Lógica para registrar votos, com validações. |
| **Cálculo de Resultados** | ✅ Concluído | Apuração de votos e exposição dos resultados. |
| **Comunicação Interna** | ✅ Concluído | Integração síncrona com o Serviço de Usuários. |
| **Segurança** | ✅ Concluído | Modelo de pré-autenticação e autorização por `Role`. |
| **Testes (Unitários/Integração)** | 🟡 **Pendente** | A cobertura de testes ainda não foi implementada. |

-----

## 🏗️ Arquitetura e Comunicação

### Comunicação Síncrona com o Serviço de Usuário

Uma responsabilidade chave deste serviço é garantir a integridade das operações. Para isso, ele realiza chamadas **síncronas (HTTP REST)** para o **Microsserviço de Usuário** em momentos críticos:

1.  **Ao criar uma sessão:** Valida se o `creatorId` corresponde a um usuário existente e se ele possui a `Role` de **ADMIN**.
2.  **Ao registrar um voto:** Valida se o `userId` corresponde a um usuário válido antes de computar o voto.

Essa comunicação é gerenciada pelo **Spring Cloud Load Balancer** e um `RestTemplate`, que utiliza o nome do serviço registrado no Eureka (`voting-system-user-service`) para resolver o endereço.

### Modelo de Segurança: Pré-Autenticação

Assim como outros serviços no ecossistema, este adota um modelo de **pré-autenticação**. Ele confia em um API Gateway para validar o usuário e repassar a identidade através dos cabeçalhos `X-User-Id` e `X-User-Role`, que são propagados nas chamadas internas para manter o contexto de segurança.

-----

## ⚙️ Tecnologias Utilizadas

| Categoria | Tecnologias |
| :--- | :--- |
| **Backend** | Java 21, Spring Boot 3, Spring Security, Spring Data JPA |
| **Banco de Dados** | PostgreSQL (Hospedado na **Render**) |
| **Comunicação** | Spring Cloud Load Balancer, RestTemplate |
| **Monitoramento** | Micrometer, Spring Actuator (endpoint Prometheus) |
| **Infra & Deploy** | Docker, **Render** |
| **Service Discovery**| Spring Cloud Netflix Eureka |
| **Documentação** | Springdoc (Swagger/OpenAPI) |

-----

## 🔌 Endpoints da API

A documentação interativa completa está disponível via Swagger em `/swagger-ui.html`.

### Gerenciamento de Sessões (`/api/votes_session`)

| Método | Endpoint | Descrição | Acesso |
|:---|:---|:---|:---|
| `POST` | `/create` | Cria uma nova sessão de votação. | **ADMIN** |
| `GET` | `/` | Lista todas as sessões de votação existentes. | **Autenticado**|
| `GET` | `/{id}` | Busca uma sessão de votação pelo ID. | **Autenticado**|
| `DELETE`| `/{id}` | Deleta uma sessão e todos os seus votos associados. | **ADMIN** |
| `GET` | `/{id}/results` | Exibe os resultados apurados para uma sessão. | **Autenticado**|
| `GET` | `/created?userId={id}` | Lista sessões criadas por um usuário específico. | **Autenticado**|
| `GET` | `/status?status={status}`| Filtra sessões pelo status (`NOT_STARTED`, `ACTIVE`, `ENDED`).| **Autenticado**|
| `GET` | `/voted?userId={id}` | Lista as sessões em que um usuário já votou. | **Autenticado**|

### Registro de Votos (`/api/votes`)

| Método | Endpoint | Descrição | Acesso |
|:---|:---|:---|:---|
| `POST` | `/{sessionId}/cast` | Registra um voto para um usuário em uma sessão específica. | **Autenticado**|

**Parâmetros para registrar um voto (`/cast`):**

  * **Path Variable**: `sessionId` - O ID da sessão de votação.
  * **Request Params**:
      * `userId`: ID do usuário que está votando.
      * `option`: O texto da opção escolhida (deve corresponder a uma das opções da sessão).
  * **Headers Obrigatórios**: `X-User-Id` e `X-User-Role` (injetados pelo Gateway).

-----

## 📊 Monitoramento e Métricas

As métricas são expostas no formato Prometheus no endpoint `/actuator/prometheus`.

#### Métricas Customizadas Coletadas:

  - `votos.chamadas` (Contador): Total de tentativas de registro de voto.
  - `votos.chamadas.tempo` (Timer): Duração do processo de registro de um voto.
  - `votacao.criar.chamadas` (Contador): Total de chamadas para criar novas sessões.
  - `listar.votacoes.chamadas` (Contador): Total de chamadas para listar todas as sessões.
  - `sessoes.criadas.usuario.chamadas` (Contador): Chamadas para buscar sessões por criador.
  - `sessoes.criadas.usuario.tempo` (Timer): Duração da busca de sessões por criador.

-----

## 🛠️ Configuração e Variáveis de Ambiente

| Variável | Descrição | Exemplo |
| :--- | :--- | :--- |
| `PORT` | Porta em que o serviço irá rodar. | `8082` |
| `SPRING_DATASOURCE_URL`| URL de conexão com o banco de dados PostgreSQL. | `jdbc:postgresql://host:port/dbname`|
| `SPRING_DATASOURCE_USERNAME`| Usuário do banco de dados. | `user_eleicoes` |
| `SPRING_DATASOURCE_PASSWORD`| Senha do banco de dados. | `senha_segura` |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE`| URL do servidor Eureka para registro. | `https://voting-system-discovery.onrender.com/eureka/` |

-----

## 🐳 Como Executar (Docker)

**1. Construindo a Imagem Docker:**
Na raiz do projeto, execute o comando:

```bash
docker build -t voting-system/vote-service .
```

**2. Rodando o Container:**
Substitua as variáveis de ambiente com seus valores e execute o container:

```bash
docker run -p 8082:8082 \
  -e SPRING_DATASOURCE_URL="jdbc:postgresql://..." \
  -e SPRING_DATASOURCE_USERNAME="user" \
  -e SPRING_DATASOURCE_PASSWORD="pass" \
  -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE="..." \
  --name vote-service \
  voting-system/vote-service
```

O serviço estará disponível em `http://localhost:8082`.