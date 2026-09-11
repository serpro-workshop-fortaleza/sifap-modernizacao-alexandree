# Issue: Implementar guarda de reentrada por CPF e competência

## Contexto
O agregado `Payment` e o índice de consulta por CPF/competência já estão implementados em `backend/`. A próxima tarefa do plano é impedir a geração de um segundo pagamento para o mesmo CPF e competência antes da gravação, preservando o comportamento de `BATCHPGT.NSP`.

Referências: `specs/001-benefit-calculation/spec.md`, `specs/001-benefit-calculation/plan.md` e `02-modern-spec/ADRs/adr-002-payment-uniqueness-cpf-competence.md`.

## Critérios de aceitação
- [ ] O serviço de geração consulta `PaymentRepository.findByCpfAndCompetence` antes de persistir um pagamento.
- [ ] Quando já existe pagamento para o par CPF/competência, a operação recusa a nova geração com exceção de domínio mapeável para conflito.
- [ ] Quando não existe pagamento, a operação persiste exatamente um agregado.
- [ ] O comportamento não adiciona índice único nesta tarefa; essa decisão fica isolada na tarefa 2c/P4a.
- [ ] Os testes cobrem pagamento inexistente, duplicidade e ausência de efeitos parciais.

## Arquivos provavelmente afetados
- Modificar: `backend/src/main/java/br/gov/sifap/payment/application/`
- Modificar: `backend/src/main/java/br/gov/sifap/payment/infrastructure/PaymentRepository.java`
- Criar: exceção de conflito e teste unitário do serviço
- Consultar: `backend/src/main/java/br/gov/sifap/payment/domain/Payment.java`

## Abordagem de testes
Executar `cd backend && ./mvnw -B -Dtest=*Payment* test`. Usar mocks somente para o repositório no teste unitário; manter o teste de repositório existente contra PostgreSQL para o contrato de persistência.

## Fora do escopo
Não implementar cálculo de elegibilidade, descontos, endpoints HTTP, alteração do schema ou índice único nesta Issue.

## Labels
`enhancement`, `copilot-agent`

## Requisitos relacionados
- `REQ-010`
- `source_legacy: 01-archaeology/legacy-sifap/natural-programs/BATCHPGT.NSP`
