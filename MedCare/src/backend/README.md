# API de consultas — Sprint 2

A API usa armazenamento em memória no `ConsultaService`. Os IDs são UUIDs gerados
pelo servidor e os registros são perdidos ao reiniciar a aplicação. Não há banco,
JPA ou exclusão física. Os métodos do serviço são sincronizados e retornam DTOs,
sem expor os objetos mutáveis armazenados.

| Método | Caminho | Resultado |
| --- | --- | --- |
| POST | `/consultas` | Cadastro, `201` e cabeçalho `Location` |
| GET | `/consultas` | Lista, `200` (inicialmente `[]`) |
| GET | `/consultas/{id}` | Registro, `200`, ou `404` |
| PATCH | `/consultas/{id}/situacao` | Situação atualizada, `200` |

Erros têm o formato `{"mensagem":"Descrição em português."}`: `400` para entrada
inválida, `404` para registro inexistente, `409` para transição incompatível e `503`
para integração necessária indisponível. Modalidades, prioridades e situações
usam os nomes dos enums em maiúsculas. Campos extras são rejeitados. O cadastro
não aceita `id`, `situacao` nem `especialidadeProfissional`.

## Cadastros

Rotina:

```json
{
  "modalidade": "ROTINA",
  "data": "2026-11-05",
  "horario": "10:30:00",
  "pacienteId": "p1",
  "medicoResponsavelId": "m1",
  "motivo": "Retorno",
  "observacoes": "Acompanhamento"
}
```

Especializada (retorna `503` até a integração com profissionais):

```json
{
  "modalidade": "ESPECIALIZADA",
  "data": "2026-11-05",
  "horario": "11:00:00",
  "pacienteId": "p1",
  "medicoResponsavelId": "m1",
  "especialidade": "Cardiologia",
  "observacoes": null
}
```

Emergência:

```json
{
  "modalidade": "EMERGENCIA",
  "data": "2026-11-05",
  "horario": "11:30:00",
  "pacienteId": "p1",
  "medicoResponsavelId": "m1",
  "prioridade": "CRITICA"
}
```

Observações são opcionais. Prioridades: `BAIXA`, `MODERADA`, `ALTA`, `CRITICA`.
As respostas incluem `id`, `modalidade`, os dados comuns e específicos e
`situacao`; campos específicos de outras modalidades são nulos.

## Mudança de situação

Envie para `PATCH /consultas/{id}/situacao`, por exemplo:

```json
{"situacao": "CONFIRMADA"}
```

Rotina e especializada começam em `AGENDADA`. O domínio permite confirmação,
cancelamento ou não comparecimento; após confirmação permite início,
cancelamento ou não comparecimento; após início permite somente conclusão.
Situações finais não podem ser reativadas. Cancelamento usa `CANCELADA`, e novo
agendamento exige outro registro. Repetir a situação atual não gera erro.

Emergência começa em `AGUARDANDO_AVALIACAO`. Para iniciar imediatamente envie
`{"situacao":"EM_ATENDIMENTO"}`; depois, `{"situacao":"CONCLUIDA"}`.
Não há confirmação ou cancelamento no fluxo de emergência definido pelo domínio.
A situação é interpretada conforme a modalidade do registro.

## Integrações pendentes

- Pacientes e profissionais ainda não estão disponíveis. A API verifica apenas
  o preenchimento dos identificadores e a positividade quando numéricos. Isso
  **não comprova a existência** de paciente ou médico; essa consulta deve ser
  integrada aos respectivos módulos quando estiverem disponíveis.
- A consulta especializada precisa verificar compatibilidade com uma informação
  confiável do médico. Por isso, seu cadastro retorna `503` sem armazenar nada.
  O método `obterEspecialidadeProfissional` deve ser integrado ao módulo da Alexia
  usando `medicoResponsavelId`. A especialidade enviada pelo cliente descreve a
  consulta solicitada, nunca comprova a especialidade do médico.
- A mesma consulta ao módulo de profissionais está prevista antes de iniciar uma
  consulta especializada confirmada. Depois da integração, o domínio continuará
  comparando as especialidades. Falhas devem preservar a situação anterior.

## Verificação sem iniciar a aplicação

Na pasta do backend:

```bash
mvn -o -Dtest=AtendimentoTest,SituacoesAtendimentoTest,ConsultaApiTest test
```

Esse comando compila e executa somente os testes de domínio e MockMvc isolado.
Não executa o teste `TrabalhoShApplicationTests`, que carrega o contexto Spring.
