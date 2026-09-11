# Issue: Implementar política de cálculo do benefício

## Contexto
Com os value objects, o agregado de pagamento e os contratos de leitura disponíveis, implementar a política de elegibilidade e o cálculo do valor bruto por fator de renda. O cálculo deve preservar truncamento monetário em duas casas e não pode inventar fatores regional, familiar ou etário ainda classificados como mistério.

Referências: `specs/001-benefit-calculation/spec.md`, `specs/001-benefit-calculation/plan.md` e `01-archaeology/business-rules-catalog.md`.

## Critérios de aceitação
- [ ] Beneficiário inativo ou inexistente é recusado conforme os critérios de `spec.md`.
- [ ] Programa inexistente ou inativo é recusado.
- [ ] Teto de renda diferente de zero é aplicado; teto zero desativa essa verificação.
- [ ] O fator da faixa de renda aplicável é usado para calcular o valor bruto.
- [ ] Valores monetários são truncados com `RoundingMode.DOWN` em escala 2.
- [ ] O resultado identifica a decisão e o valor calculado sem expor CPF ou valores sensíveis em logs.
- [ ] Testes cobrem elegibilidade, teto, faixa, truncamento e recusas.

## Arquivos provavelmente afetados
- Criar: `backend/src/main/java/br/gov/sifap/payment/application/`
- Criar: `backend/src/main/java/br/gov/sifap/payment/domain/`
- Criar: testes unitários em `backend/src/test/java/br/gov/sifap/payment/`
- Consultar: contratos de `beneficiary` e `socialprogram`

## Abordagem de testes
Executar `cd backend && ./mvnw -B test`. Usar doubles das interfaces de consulta; não usar banco para a política pura. Cada teste orientado a requisito deve conter o `REQ-ID` inline.

## Fora do escopo
Não implementar descontos, folha, endpoints, fatores sem requisito (regional/familiar/etário), autenticação ou alteração de schema.

## Labels
`enhancement`, `copilot-agent`

## Requisitos relacionados
- `REQ-001`
- `REQ-002`
- `REQ-003`
- `REQ-004`
- `REQ-005`
- `REQ-006`
