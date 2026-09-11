# Issue: Expor contratos de leitura de beneficiário e programa

## Contexto
O plano de `001-benefit-calculation` define comunicação em processo entre `payment`, `beneficiary` e `socialprogram`. Os módulos de leitura devem expor somente contratos públicos de consulta, sem permitir que `payment` acesse tabelas ou classes internas de outro módulo.

Referências: `specs/001-benefit-calculation/spec.md`, `specs/001-benefit-calculation/plan.md` e `02-modern-spec/bounded-contexts.md`.

## Critérios de aceitação
- [ ] `beneficiary` expõe `BeneficiaryQuery` com busca por `Cpf` e retorno `Optional<BeneficiarySnapshot>`.
- [ ] `socialprogram` expõe `SocialProgramCatalog` com busca por programa ativo e `Competence`.
- [ ] Snapshots e parâmetros usam tipos imutáveis e value objects do `sharedkernel`.
- [ ] Nenhum contrato público expõe entidade JPA ou repositório de outro módulo.
- [ ] Testes de contrato cobrem resultado encontrado e ausência de registro.
- [ ] A regra de limite de renda permanece no módulo `payment`; esta Issue entrega apenas leitura.

## Arquivos provavelmente afetados
- Criar: `backend/src/main/java/br/gov/sifap/beneficiary/api/`
- Criar: `backend/src/main/java/br/gov/sifap/socialprogram/api/`
- Criar: testes em `backend/src/test/java/br/gov/sifap/`
- Consultar: `backend/src/main/java/br/gov/sifap/sharedkernel/domain/`

## Abordagem de testes
Executar `cd backend && ./mvnw -B test`. Cobrir contratos com implementações fake em memória nos testes unitários e verificar limites de pacote com ArchUnit quando aplicável.

## Fora do escopo
Não criar endpoints, telas, cadastro/manutenção de beneficiários ou programas, nem implementar o cálculo do benefício.

## Labels
`enhancement`, `copilot-agent`

## Requisitos relacionados
- `REQ-001`
- `REQ-002`
- `REQ-003`
- `REQ-004`
- `REQ-005`
