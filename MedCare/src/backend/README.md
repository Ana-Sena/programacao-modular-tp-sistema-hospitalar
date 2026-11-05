# MedCare - Backend

## Sprint 2 - Ana: pacientes, internações e quartos

Implementação em Java 21 e Spring Boot, organizada como a API da branch
`sprint2-Evelyn`: domínio em `dominio`, controllers, serviços e DTOs em `api`,
testes nas mesmas estruturas de pacotes. Cada classe pública fica em seu próprio
arquivo, com nomes e mensagens em português e indentação de quatro espaços.

Os registros são mantidos **em memória** e são perdidos ao reiniciar a aplicação,
seguindo o armazenamento temporário utilizado pela API de consultas da sprint 2.
Não é necessário configurar banco de dados.

## Executar e testar

Na pasta `MedCare/src/backend`, com Java 21 e Maven instalados:

```powershell
mvn spring-boot:run
```

A API estará disponível em `http://localhost:8080`. Para executar os testes:

```powershell
mvn test
```

O wrapper existente também pode ser utilizado: `./mvnw.cmd test` no Windows.

## Endpoints

| Método | Caminho | Operação |
| --- | --- | --- |
| POST | `/pacientes` | Cadastrar paciente |
| GET | `/pacientes` | Listar pacientes |
| GET | `/pacientes/{id}` | Consultar paciente |
| PUT | `/pacientes/{id}` | Substituir nome e dados de contato/endereço |
| POST | `/quartos` | Cadastrar quarto |
| GET | `/quartos` | Listar quartos com ocupação e situação |
| GET | `/quartos/{id}` | Consultar quarto |
| POST | `/internacoes` | Registrar entrada e ocupar uma vaga |
| GET | `/internacoes` | Listar internações, inclusive as encerradas |
| GET | `/internacoes/{id}` | Consultar internação |
| PATCH | `/internacoes/{id}/situacao` | Registrar alta ou consultar/reafirmar o estado ativo |

Cadastros retornam `201 Created`, o registro e o cabeçalho `Location` com sua URL.
Consultas, atualização e alteração de situação retornam `200 OK`.
IDs são UUIDs gerados pelo servidor. Os corpos de requisição usam
`Content-Type: application/json` e datas no formato `AAAA-MM-DD`.

### 1. Cadastrar paciente

`POST /pacientes`

```json
{
  "nome": "Ana Sena",
  "cpf": "529.982.247-25",
  "dataNascimento": "2000-01-01",
  "telefone": "31999999999",
  "email": "ana@exemplo.com",
  "endereco": {
    "logradouro": "Rua A",
    "numero": "10",
    "complemento": "Apartamento 201",
    "bairro": "Centro",
    "cidade": "Belo Horizonte",
    "uf": "MG",
    "cep": "30100-000"
  }
}
```

O CPF aceita os formatos com ou sem pontuação e valida os dígitos verificadores.
CPFs repetidos e sequências como `11111111111` são rejeitados. CPF e CEP são
armazenados sem pontuação; UF é normalizada para maiúsculas e deve ser uma UF
brasileira válida. Nome, telefone, e-mail, nascimento e endereço são obrigatórios;
complemento é opcional. Nascimento futuro é rejeitado.

Para `PUT /pacientes/{id}`, envie `nome`, `telefone`, `email` e `endereco` completos.
CPF, nascimento e ID não são campos de atualização. Uma falha de validação
preserva todos os dados anteriores.

### 2. Cadastrar quarto

`POST /quartos`

```json
{
  "numero": "101",
  "andar": 1,
  "tipo": "ENFERMARIA",
  "capacidadeMaxima": 2
}
```

O número deve ser único em todo o hospital, desconsiderando espaços nas
extremidades e diferenças entre maiúsculas e minúsculas. Andar é um inteiro
obrigatório; zero e negativos representam térreo e subsolos. A capacidade deve ser
positiva. Ocupação inicial é zero e a situação é calculada pelo servidor:

- `DISPONIVEL`: ainda existe vaga, mesmo que algumas vagas estejam ocupadas.
- `OCUPADO`: a capacidade máxima foi atingida.

**Decisão de implementação:** o UML e os cartões CRC especificam capacidade e
situação, mas não enumeram os tipos de quarto. Para atender ao escopo da sprint,
foram adotados `ENFERMARIA` (capacidade configurável), `APARTAMENTO` e `UTI`
(uma vaga por quarto). Essas categorias e a regra de capacidade individual
devem ser confirmadas pela equipe se houver requisitos adicionais do professor.
Não foram presumidas restrições clínicas, de gênero ou de idade para os tipos.

O cliente não pode definir `id`, `ocupacaoAtual` ou `situacao` no cadastro.

### 3. Registrar internação

`POST /internacoes`

```json
{
  "pacienteId": "UUID retornado pelo cadastro do paciente",
  "medicoResponsavelId": "m1",
  "quartoId": "UUID retornado pelo cadastro do quarto",
  "dataEntrada": "2026-11-05",
  "dataPrevistaAlta": "2026-11-10",
  "observacoes": "Acompanhamento clínico"
}
```

Use uma data de entrada entre o nascimento do paciente e o dia atual. Previsão de
alta é opcional e não pode anteceder a entrada; observações são opcionais.

Paciente e quarto devem existir. O paciente só pode ter uma internação `ATIVA`
por vez e o quarto precisa possuir vaga. A entrada gera a situação `ATIVA` e
ocupa uma vaga. As validações ocorrem antes da ocupação. Entrada e alta usam o
mesmo monitor de sincronização das leituras dos quartos: duas requisições
concorrentes não conseguem ocupar a última vaga nem internar o mesmo paciente
simultaneamente em quartos diferentes.

`medicoResponsavelId` é obrigatório e deve ser não vazio. Ele é uma referência
textual para integração com o módulo de profissionais. **A existência, a
especialidade e a disponibilidade do profissional ainda não são verificadas:**
a branch `sprint2-Aléxia` consultada não oferece serviço de busca por ID.
Não há duplicação das classes de profissionais neste módulo.

### 4. Registrar alta

`PATCH /internacoes/{id}/situacao`

```json
{
  "situacao": "ALTA",
  "dataEfetivaAlta": "2026-11-05"
}
```

Fluxo permitido: `ATIVA` → `ALTA`. A data efetiva de alta é obrigatória nessa
operação e deve estar entre a entrada e o dia atual. A alta libera exatamente
uma vaga e preserva a internação e suas associações para consulta.

Repetir a mesma alta com a mesma data é idempotente e não libera outra vaga.
Depois da alta, não é possível alterar sua data nem reabrir a internação.
Uma nova internação pode ser cadastrada para o paciente, respeitando as vagas.
Enviar `{"situacao":"ATIVA"}` para uma internação ativa mantém seu estado;
uma internação ativa não pode receber `dataEfetivaAlta`.

## Erros e limites da integração

Os três módulos retornam erros no formato `{"mensagem":"Descrição do erro."}`:

- `400 Bad Request`: dados ausentes ou inválidos, JSON malformado, enum
  desconhecido, campo não permitido ou datas incoerentes.
- `404 Not Found`: paciente, quarto ou internação não encontrado.
- `409 Conflict`: CPF ou número duplicado, internação ativa duplicada, falta de
  vagas ou tentativa de reabrir/alterar alta encerrada.

Os DTOs rejeitam campos extras no nível principal, incluindo ID, ocupação e
situação inicial. Respostas são snapshots e não expõem referências mutáveis ou
ciclos entre as entidades. Os cadastros e internações não possuem exclusão nesta
sprint, preservando as associações e o histórico de entradas e altas.
Não foi implementado o módulo separado `HistoricoMedico` ou o agendamento de
consultas, que dependem da integração com as demais entregas.

## Validação automatizada

A suíte contém 20 testes: validações de paciente/endereço, capacidade e alta no
domínio, contratos HTTP, associações, preservação do estado nas falhas, repetição
de alta, dois cenários concorrentes e inicialização do contexto Spring.
Os testes usam datas relativas ao dia de execução para os fluxos de internação.
