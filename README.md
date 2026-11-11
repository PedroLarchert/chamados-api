# API de Chamados de Suporte

API REST desenvolvida com Java 21, Maven e Spring Boot para cadastrar e consultar chamados de suporte técnico. Os dados são mantidos somente em memória: a aplicação inicia com dez chamados fictícios e não utiliza banco de dados, JPA, camada de serviço, repositório ou autenticação.

## Como executar

Pré-requisitos:

- Java 21;
- acesso à internet na primeira execução, para o Maven baixar as dependências.

Na raiz do projeto, execute:

```bash
./mvnw spring-boot:run
```

No Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

A API ficará disponível em `http://localhost:8080`. Para executar os testes automatizados:

```bash
./mvnw test
```

## Modelo de chamado

Cada chamado possui os campos abaixo:

```json
{
  "id": 1,
  "titulo": "Impressora sem conexão",
  "solicitante": "Ana Souza",
  "setor": "Financeiro",
  "prioridade": "ALTA",
  "status": "ABERTO"
}
```

Os valores aceitos em `prioridade` são `BAIXA`, `MEDIA` e `ALTA`. Os valores aceitos em `status` são `ABERTO`, `EM_ANDAMENTO` e `RESOLVIDO`.

## Endpoints

| Método | Rota | Finalidade |
| --- | --- | --- |
| `GET` | `/chamados` | Lista todos os chamados |
| `GET` | `/chamados/{id}` | Consulta um chamado pelo ID |
| `GET` | `/chamados/setor?nome=financeiro` | Filtra por setor, sem diferenciar maiúsculas de minúsculas |
| `GET` | `/chamados/prioridade?nivel=alta` | Filtra por prioridade, sem diferenciar maiúsculas de minúsculas |
| `GET` | `/chamados/busca?texto=impressora` | Busca um trecho no título, sem diferenciar maiúsculas de minúsculas |
| `GET` | `/chamados/criticos` | Lista chamados de prioridade alta que ainda não estão resolvidos |
| `GET` | `/chamados/resumo` | Conta os chamados por status e informa o total |
| `POST` | `/chamados` | Cadastra um chamado e gera seu ID |

## Evidências dos testes realizados

Os resultados abaixo foram obtidos em uma execução local com a aplicação recém-iniciada. Os comandos usam `curl -i` para permitir a reprodução do status HTTP e do JSON. A suíte automatizada também foi executada com `./mvnw test`: **11 testes executados, 0 falhas e 0 erros**.

### 1. Listar todos os chamados

```bash
curl -i http://localhost:8080/chamados
```

Status obtido: `200 OK`.

JSON obtido:

```json
[
  {"id":1,"titulo":"Impressora não imprime","solicitante":"Ana","setor":"Financeiro","prioridade":"ALTA","status":"ABERTO"},
  {"id":2,"titulo":"Computador lento","solicitante":"Bruno","setor":"Recursos Humanos","prioridade":"MEDIA","status":"EM_ANDAMENTO"},
  {"id":3,"titulo":"Redefinição de senha","solicitante":"Carla","setor":"Tecnologia","prioridade":"BAIXA","status":"RESOLVIDO"},
  {"id":4,"titulo":"Sem acesso à internet","solicitante":"Diego","setor":"Comercial","prioridade":"ALTA","status":"EM_ANDAMENTO"},
  {"id":5,"titulo":"Instalação de software","solicitante":"Elisa","setor":"Financeiro","prioridade":"MEDIA","status":"ABERTO"},
  {"id":6,"titulo":"Conta de e-mail bloqueada","solicitante":"Fábio","setor":"Marketing","prioridade":"ALTA","status":"RESOLVIDO"},
  {"id":7,"titulo":"Monitor sem imagem","solicitante":"Gabriela","setor":"Recursos Humanos","prioridade":"MEDIA","status":"RESOLVIDO"},
  {"id":8,"titulo":"Erro no sistema de vendas","solicitante":"Henrique","setor":"Comercial","prioridade":"ALTA","status":"ABERTO"},
  {"id":9,"titulo":"Teclado com defeito","solicitante":"Isabela","setor":"Administrativo","prioridade":"BAIXA","status":"EM_ANDAMENTO"},
  {"id":10,"titulo":"Configuração de impressora","solicitante":"João","setor":"Administrativo","prioridade":"BAIXA","status":"ABERTO"}
]
```

Explicação: a API retornou os dez registros mockados criados na inicialização, com IDs de `1` a `10`.

### 2. Consultar um chamado pelo ID

```bash
curl -i http://localhost:8080/chamados/1
```

Status obtido: `200 OK`.

JSON obtido:

```json
{"id":1,"titulo":"Impressora não imprime","solicitante":"Ana","setor":"Financeiro","prioridade":"ALTA","status":"ABERTO"}
```

Explicação: a consulta localizou e retornou somente o chamado cujo ID é `1`.

### 3. Filtrar por setor sem considerar maiúsculas e minúsculas

```bash
curl -i "http://localhost:8080/chamados/setor?nome=financeiro"
```

Status obtido: `200 OK`.

JSON obtido:

```json
[
  {"id":1,"titulo":"Impressora não imprime","solicitante":"Ana","setor":"Financeiro","prioridade":"ALTA","status":"ABERTO"},
  {"id":5,"titulo":"Instalação de software","solicitante":"Elisa","setor":"Financeiro","prioridade":"MEDIA","status":"ABERTO"}
]
```

Explicação: mesmo com `financeiro` em letras minúsculas, o filtro retornou os dois registros do setor Financeiro, IDs `1` e `5`.

### 4. Filtrar por prioridade

```bash
curl -i "http://localhost:8080/chamados/prioridade?nivel=alta"
```

Status obtido: `200 OK`.

JSON obtido:

```json
[
  {"id":1,"titulo":"Impressora não imprime","solicitante":"Ana","setor":"Financeiro","prioridade":"ALTA","status":"ABERTO"},
  {"id":4,"titulo":"Sem acesso à internet","solicitante":"Diego","setor":"Comercial","prioridade":"ALTA","status":"EM_ANDAMENTO"},
  {"id":6,"titulo":"Conta de e-mail bloqueada","solicitante":"Fábio","setor":"Marketing","prioridade":"ALTA","status":"RESOLVIDO"},
  {"id":8,"titulo":"Erro no sistema de vendas","solicitante":"Henrique","setor":"Comercial","prioridade":"ALTA","status":"ABERTO"}
]
```

Explicação: o valor `alta`, enviado em letras minúsculas, retornou somente os quatro chamados de prioridade `ALTA`.

### 5. Buscar um trecho no título

```bash
curl -i "http://localhost:8080/chamados/busca?texto=IMPRESSORA"
```

Status obtido: `200 OK`.

JSON obtido:

```json
[
  {"id":1,"titulo":"Impressora não imprime","solicitante":"Ana","setor":"Financeiro","prioridade":"ALTA","status":"ABERTO"},
  {"id":10,"titulo":"Configuração de impressora","solicitante":"João","setor":"Administrativo","prioridade":"BAIXA","status":"ABERTO"}
]
```

Explicação: a busca encontrou o texto nos títulos dos IDs `1` e `10`, embora o parâmetro tenha sido enviado em letras maiúsculas.

### 6. Consultar chamados críticos

```bash
curl -i http://localhost:8080/chamados/criticos
```

Status obtido: `200 OK`.

JSON obtido:

```json
[
  {"id":1,"titulo":"Impressora não imprime","solicitante":"Ana","setor":"Financeiro","prioridade":"ALTA","status":"ABERTO"},
  {"id":4,"titulo":"Sem acesso à internet","solicitante":"Diego","setor":"Comercial","prioridade":"ALTA","status":"EM_ANDAMENTO"},
  {"id":8,"titulo":"Erro no sistema de vendas","solicitante":"Henrique","setor":"Comercial","prioridade":"ALTA","status":"ABERTO"}
]
```

Explicação: foram retornados apenas os chamados de prioridade alta que ainda não estavam resolvidos. O ID `6`, apesar de ter prioridade alta, não apareceu porque seu status era `RESOLVIDO`.

### 7. Consultar o resumo

```bash
curl -i http://localhost:8080/chamados/resumo
```

Status obtido: `200 OK`.

JSON obtido:

```json
{
  "abertos": 4,
  "emAndamento": 3,
  "resolvidos": 3,
  "total": 10
}
```

Explicação: o resumo contabilizou quatro chamados abertos, três em andamento e três resolvidos. A soma dos status corresponde ao total de dez registros.

### 8. Testar uma busca sem correspondência

```bash
curl -i "http://localhost:8080/chamados/busca?texto=texto-que-nao-existe-987654"
```

Status obtido: `200 OK`.

JSON obtido:

```json
[]
```

Explicação: uma busca válida sem nenhum título correspondente retornou um array vazio, sem ser tratada como erro.

### 9. Cadastrar um chamado

```bash
curl -i -X POST http://localhost:8080/chamados \
  -H "Content-Type: application/json" \
  -d '{
    "titulo": "Configurar acesso ao sistema de chamados",
    "solicitante": "João Lima",
    "setor": "Recursos Humanos",
    "prioridade": "MEDIA",
    "status": "ABERTO"
  }'
```

Status obtido: `201 Created`.

JSON obtido:

```json
{"id":11,"titulo":"Configurar acesso ao sistema de chamados","solicitante":"João Lima","setor":"Recursos Humanos","prioridade":"MEDIA","status":"ABERTO"}
```

Explicação: a API cadastrou o chamado, gerou automaticamente o ID único `11` e devolveu o novo recurso com status `201`.

### 10. Verificar o cadastro

Use no endereço o ID retornado pelo teste anterior. Para uma aplicação recém-iniciada:

```bash
curl -i http://localhost:8080/chamados/11
```

Status obtido: `200 OK`.

JSON obtido:

```json
{"id":11,"titulo":"Configurar acesso ao sistema de chamados","solicitante":"João Lima","setor":"Recursos Humanos","prioridade":"MEDIA","status":"ABERTO"}
```

Explicação: a consulta pelo ID gerado confirmou que o registro permaneceu disponível na coleção em memória durante a mesma execução.

### 11. Conferir o resumo após o cadastro

```bash
curl -i http://localhost:8080/chamados/resumo
```

Status obtido: `200 OK`.

JSON obtido:

```json
{
  "abertos": 5,
  "emAndamento": 3,
  "resolvidos": 3,
  "total": 11
}
```

Explicação: o novo chamado tinha status `ABERTO`; por isso, a quantidade de abertos passou de quatro para cinco e o total passou de dez para onze.

## Análise técnica

### Como o Spring Boot recebe e processa requisições HTTP?

Ao iniciar, o Spring Boot configura um servidor web embutido e registra os componentes da aplicação. Quando uma requisição chega, o Spring MVC identifica o método do Controller cuja anotação corresponde ao método HTTP e à rota solicitada, extrai parâmetros da URL ou o corpo da requisição e chama esse método. O valor retornado pelo método é usado para montar a resposta HTTP.

### Qual é a função do Controller?

O Controller define os endpoints da API e coordena o processamento de cada requisição. Neste projeto simples, ele também mantém a coleção de chamados em memória, aplica os filtros e buscas, localiza registros, gera novos IDs e escolhe o status HTTP da resposta. Em um sistema maior, essas regras normalmente seriam separadas em outras camadas.

### Como ocorre a conversão entre objetos Java e JSON?

O Spring Web MVC usa a biblioteca Jackson por meio de conversores HTTP. Em um `POST`, o JSON enviado no corpo é desserializado para um objeto `Chamado`. Nas respostas, objetos Java, listas e mapas são serializados para JSON automaticamente. As anotações `@RequestBody` e `@RestController` indicam onde essas conversões devem acontecer.

### Por que os dados não permanecem após reiniciar a aplicação?

Os chamados ficam em uma coleção Java mantida na memória do processo. Quando a aplicação é encerrada, essa memória é liberada; ao iniciar novamente, a coleção é recriada somente com os dez dados fictícios definidos no código. Para persistir os cadastros entre reinicializações, seria necessário gravá-los em um banco de dados, arquivo ou outro armazenamento externo.

