# SpotMyDeal — API Restful

Plataforma de gamificação para incentivar práticas sustentáveis: o usuário cumpre missões
ambientais, acumula pontos e troca pontos por cupons em lojas parceiras próximas a ele.

Entrega da **4ª Sprint** do Challenge FIAP — disciplina Domain Driven Design Using Java.
Equipe **NextStage**, 1TDSPW.

| Integrante | RM |
| --- | --- |
| Tayna Jimenes Silva | 569337 |
| Gabriel Augusto | 573120 |
| Rodrigo Banharelli de Souza | 570539 |
| Nycolas Melo Escobar | 573052 |

## Stack

Spring Boot 4.1.2 · Java 21 · Maven 3.9.11 (wrapper incluso) · Oracle (ojdbc17) · Docker

## Arquitetura

O fluxo de uma requisição vai sempre no mesmo sentido, sem volta:

```
Resource  ->  BO  ->  DAO  ->  ConnectionFactory  ->  Oracle
```

| Camada | Pacote | Responsabilidade |
| --- | --- | --- |
| Model | `model` | As cinco entidades do MER, com os métodos de negócio da própria entidade |
| DAO | `dao` | O SQL de uma tabela cada, com `PreparedStatement` e try-with-resources |
| Conexão | `factory` | `ConnectionFactory` sobre o `DataSource` do Spring |
| BO | `bo` | Regras de negócio e validações; orquestra mais de um DAO quando preciso |
| Resource | `resource` | Os endpoints HTTP; recebe e devolve DTO, não conhece SQL |
| DTO | `dto` | Contrato de entrada (com Bean Validation) e de saída |
| Exceções | `exception` | Exceções de negócio e o `@RestControllerAdvice` que as traduz em status |

## Endpoints

| Verbo | URI | Descrição |
| --- | --- | --- |
| GET POST | `/usuarios` | Lista e cadastra usuários |
| GET PUT DELETE | `/usuarios/{id}` | Busca, atualiza e remove um usuário |
| GET | `/usuarios/{id}/missoes` | Histórico de missões concluídas |
| GET | `/usuarios/{id}/missoes/disponiveis` | Missões que o usuário ainda não concluiu |
| POST | `/usuarios/{id}/missoes` | Registra a conclusão de uma missão |
| GET | `/usuarios/{id}/resgates` | Histórico de cupons resgatados |
| POST | `/usuarios/{id}/resgates` | Resgata um cupom numa loja |
| GET POST | `/lojas` | Lista e cadastra lojas |
| GET | `/lojas/proximas` | Lojas ativas no raio: `?lat=&lon=&raio=` |
| GET PUT DELETE | `/lojas/{id}` | Busca, atualiza e remove uma loja |
| GET POST | `/missoes` | Lista e cadastra missões |
| GET PUT DELETE | `/missoes/{id}` | Busca, atualiza e remove uma missão |
| GET | `/ranking` | Usuários ordenados por pontuação |

Status de resposta: 200, 201, 204 no caminho feliz; 400 para dado inválido ou regra de negócio,
403 para resgate bloqueado, 404 para id inexistente, 409 para conclusão duplicada, 500 para
falha de banco.

## Como rodar

**1. Criar as tabelas.** Execute o [`script.sql`](script.sql) no SQL Developer, conectado ao seu
usuário Oracle da FIAP. Ele cria cinco tabelas, cinco sequences e insere dados de exemplo.

**2. Definir as credenciais.** Elas vêm de variável de ambiente; este repositório não guarda
senha nenhuma.

```
set DB_USER=seu_rm
set DB_PASS=sua_senha_do_oracle
```

**3. Subir a aplicação.**

```
mvnw spring-boot:run
```

A API responde em `http://localhost:8080`. Para conferir, chame `GET /usuarios`: a resposta deve
ser 200 com os três usuários de exemplo.

Sem `DB_PASS` definido, todo endpoint que toca o banco responde 500.

## Deploy

Imagem Docker multi-stage, descrita no [`Dockerfile`](Dockerfile): o primeiro estágio compila com
Maven e é descartado, o segundo leva apenas o `.jar` para uma imagem com JRE 21.

A aplicação lê a porta de `PORT`, que o servidor de aplicação injeta. As variáveis a cadastrar no
painel do provedor são `DB_USER`, `DB_PASS` e, se o banco mudar, `DB_URL`.

URL pública: _a preencher_
