# ADR-002: Gerar no máximo um pagamento por CPF e competência no sistema novo

> **Trilha:** [Kit do Time](../../README.md) › [Estágio 2](../README.md) › ADRs › **ADR-002**

| Campo | Valor |
|---|---|
| **Status** | `Proposta` — aguarda ratificação da equipe no handoff H2 |
| **Data** | 2026-09-10 |
| **Feature relacionada** | [`specs/001-benefit-calculation/`](../../specs/001-benefit-calculation/spec.md) |
| **Contexto delimitado** | 3. Pagamento de Benefícios ([bounded-contexts.md](../bounded-contexts.md)) |
| **Substitui** | Questão de projeto `P4` de [`plan.md`](../../specs/001-benefit-calculation/plan.md), agora dividida em P4a e P4b |

---

## Contexto

O plano da primeira feature previa um índice único sobre `(cpf, competência)` na tabela `payment`, descrito como "implementação de REQ-010 no banco". A revisão da dupla 2 apontou que isso fecharia por construção uma questão em aberto com a Coordenação de Benefícios. A leitura do código legado mostrou que a premissa da descrição estava incorreta e que a questão, na verdade, são duas.

### O que REQ-010 realmente exige

[REQ-010](../../specs/001-benefit-calculation/spec.md) tem origem em `BATCHPGT.NSP:L292-L298`:

```natural
* CHECK WHETHER ALREADY GENERATED IN THIS PERIOD
* USES SUPERDESCRIPTOR S1 - CPF + YEAR-MONTH-REF
  COMPRESS BENEFICIARY-V.NUM-CPF #PERIOD-A
      INTO #KEY-CPF-PERIOD LEAVING NO SPACE
  FIND NUMBER PAYMENT-V WITH SUPER-CPF-PERIOD = #KEY-CPF-PERIOD
  IF *NUMBER > 0
    ADD 1 TO #QTY-IGNORED
    ESCAPE TOP
  END-IF
```

É uma **guarda de reentrada**, avaliada antes de processar cada beneficiário. Ela entrega exatamente o critério de aceitação do requisito: reexecutar a folha da competência não altera a quantidade de pagamentos. Uma consulta prévia satisfaz REQ-010 por inteiro; nenhuma restrição de unicidade é necessária para atendê-lo.

### O que a guarda não cobre

A duplicidade registrada como mistério acontece **depois** dessa guarda, dentro da mesma iteração do `READ`: `BATCHPGT` chama `CALCBENF` (`BATCHPGT.NSP:L381`), que grava um pagamento (`CALCBENF.NSN:L319`); em seguida a própria folha recalcula tudo inline e grava de novo (`BATCHPGT.NSP:L390-L488`). A guarda nunca enxerga a segunda gravação. No legado, guarda e duplicidade coexistem sem contradição.

Portanto o índice único **não implementa REQ-010** — ele proíbe algo sobre o qual REQ-010 nunca se pronunciou. É decisão de projeto autônoma e precisa de ADR.

### O que o corpus legado diz sobre a segunda gravação

| Evidência | Fonte |
|---|---|
| `THE INLINE CALCULATION BELOW REMAINS ACTIVE PENDING A DECISION - TICKET 6622/2011 OPEN` | `BATCHPGT.NSP:L363-367` |
| `RETURN AMOUNTS VIA PDACALC - TICKET 6210/2011` | `CALCBENF.NSN:L323` |
| `SCREEN OUTPUT DISABLED DURING SUBPROGRAM CONVERSION` | `CALCBENF.NSN:L331` |

Os três comentários apontam para a mesma coisa: uma **refatoração interrompida**. A intenção registrada era `BATCHPGT` deixar de calcular inline e delegar a `CALCBENF`; o cálculo inline "permanece ativo aguardando decisão" desde 2011. A segunda gravação está classificada como **Mistério** em [`business-rules-catalog.md`](../../01-archaeology/business-rules-catalog.md), não como regra confirmada.

---

## Opções consideradas

### Opção 1: apenas a guarda de reentrada, sem restrição no banco

| Aspecto | Avaliação |
|---|---|
| **Vantagens** | Implementa estritamente o que REQ-010 fundamenta; não antecipa resposta alguma de P4a ou P4b; a carga histórica dos 612 milhões de registros entra sem risco de violação |
| **Desvantagens** | Um defeito de aplicação — a mesma classe de falha que produziu a duplicidade legada — passa a gravar duplicatas sem qualquer barreira; a folha deixa de ser idempotente sob concorrência, porque a guarda é sujeita a corrida entre consulta e gravação |
| **Risco** | Alto em dados financeiros: nada distingue duplicata legítima de defeito |
| **Esforço** | Baixo |

### Opção 2: guarda de reentrada + índice único em migração isolada

| Aspecto | Avaliação |
|---|---|
| **Vantagens** | O banco impede a repetição do defeito de 2011 no sistema novo; a guarda continua respondendo por REQ-010 e o índice responde por P4a, cada um com teste próprio; a reversão custa uma migração de `DROP INDEX`, sem tocar em entidade nem em mapeamento |
| **Desvantagens** | Assume um pagamento por par antes de a Coordenação de Benefícios se pronunciar; a carga histórica falha se a base contiver pares repetidos; se P3 admitir cancelamento com reemissão na mesma competência, o índice simples bloqueia a reemissão |
| **Risco** | Baixo no processamento novo, **médio na carga histórica** — que é justamente o que P4b isola |
| **Esforço** | Baixo |

### Opção 3: reproduzir a dupla gravação do legado

| Aspecto | Avaliação |
|---|---|
| **Vantagens** | Preserva literalmente o comportamento observado em produção e a forma dos dados históricos |
| **Desvantagens** | Exigiria um requisito EARS mandando gerar o segundo pagamento, e esse requisito não pode existir: sua única origem é um mistério sob chamado aberto, e mistério não vira requisito sem validação humana; manteria duas rotas de cálculo divergentes para o mesmo dado |
| **Risco** | Inaceitável — legaliza um defeito financeiro por decisão de migração |
| **Esforço** | Alto |

---

## Decisão

A equipe escolhe a **Opção 2**, com a questão P4 dividida em duas:

**P4a — processamento novo: um pagamento por CPF e competência. Decidida.**
Nenhum requisito de [`spec.md`](../../specs/001-benefit-calculation/spec.md) pede o segundo pagamento, e nenhum pode pedir, porque a única origem seria um mistério sob chamado aberto. Gerar um pagamento não é responder ao mistério — é a ausência de requisito mandando o contrário. A decisão é reversível pela migração isolada.

**P4b — carga histórica: permanece aberta.**
Se os 612 milhões de registros de `PAYMENT` contiverem pares `(CPF, competência)` repetidos, o índice único faz a carga falhar. Essa é a decisão que pertence à Coordenação de Benefícios, conforme `BATCHPGT.NSP:L365`, e alimenta a ADR de coexistência Strangler Fig.

Condições que a implementação deve respeitar:

1. REQ-010 é atendido pela consulta prévia por CPF e competência, e o teste correspondente cita `REQ-010`.
2. O índice único é criado em **migração Flyway própria**, separada do DDL da tabela, e o teste correspondente cita `P4a`, não um REQ-ID.
3. O mistério `BATCHPGT.NSP:L381`/`L488` permanece com status `aberta` em [`mysteries-found.md`](../../01-archaeology/mysteries-found.md). Esta ADR decide o que o sistema novo faz; não decide por que o legado grava duas vezes.

---

## Consequências

### Positivas

- A folha nova é idempotente por garantia do banco, não apenas por consulta sujeita a corrida.
- A distinção entre requisito fundamentado e suposição de projeto fica visível no código, nos testes e no schema, em vez de ficar escondida em um item de lista do plano.
- P4b, que é a parte genuinamente bloqueante, ganha responsável, evidência a coletar e prazo — antes do fim do Estágio 3.
- A reversão está limitada a uma migração; nenhuma entidade, repositório ou regra de cálculo depende do índice.

### Negativas

- Enquanto P4b estiver aberta, o volume da carga histórica é desconhecido e o plano de migração não pode ser dimensionado.
- Há acoplamento com P3: se a máquina de estados admitir cancelamento com reemissão na mesma competência, o índice precisará virar índice parcial sobre as situações não canceladas. Esta ADR não antecipa esse desenho.
- A base migrada deixará de refletir literalmente o histórico caso pares duplicados existam e a equipe opte por consolidá-los; a regra de consolidação seria decisão nova, fora desta ADR.

---

## Evidência a coletar

| Pergunta | Como responder | Responsável |
|---|---|---|
| Quantos pares `(NUM-CPF, YEAR-MONTH-REF)` duplicados existem em `PAYMENT` (FNR 152)? | Contagem agregada na base legada. O visor compartilhado expõe apenas `VIEWBENF`, somente leitura sobre beneficiários, então a consulta depende do facilitador | Coordenação de Benefícios |

Zero torna P4b formalidade. Qualquer valor acima disso dimensiona o trabalho e pode reabrir a Opção 1 para a carga histórica, mantendo a Opção 2 para o processamento novo.

---

## Requisitos relacionados

- `REQ-010` — Não gerar pagamento duplicado para o mesmo CPF e competência

REQ-010 é o **único** requisito relacionado, e mesmo ele o é por delimitação: esta ADR existe para separar o que o requisito exige (a guarda de reentrada) do que ele não exige (a restrição de unicidade). A decisão P4a não é rastreável a nenhum REQ-ID — essa é exatamente a razão de precisar de ADR.

---

## Questões em aberto que esta decisão não resolve

| Questão | Evidência | Status |
|---|---|---|
| P4b — a base histórica migrada admite pares CPF/competência duplicados? | `BATCHPGT.NSP:L381`, `L488`, `CALCBENF.NSN:L319` (TICKET 6622/2011) | aberta |
| Por que o legado grava duas vezes na mesma iteração? | `BATCHPGT.NSP:L363-367` | aberta |
| P3 — qual é o domínio de `PaymentStatus`, e ele admite reemissão na mesma competência? | `PAYMENT.ddm:L73-L75`, `CALCBENF.NSN:L308-L320`, `BATCHREL.NSP:L177-L190` | aberta |

---

### Continue lendo

| Anterior | Próximo |
|---|---|
| [ADR-001](adr-001-payment-discount-jpa-mapping.md)<br/><sub>Mapeamento do grupo periódico de descontos.</sub> | [Plano 001](../../specs/001-benefit-calculation/plan.md)<br/><sub>Ordem de implementação afetada.</sub> |

<sub>[Voltar ao índice do kit](../../README.md)</sub>
