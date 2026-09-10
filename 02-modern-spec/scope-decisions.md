# Decisões de escopo — Estágio 2

> **Trilha:** [Kit do Time](../README.md) › [Estágio 2](README.md) › **Decisões de escopo**

**Registre as decisões de escopo tomadas durante o Estágio 2: o que foi selecionado, o que foi adiado e quais questões permanecem em aberto.**

| Campo | Valor |
|---|---|
| **Público-alvo** | Dupla 2 durante o Estágio 2; Duplas 3 e 4 durante o handoff H2 |
| **Finalidade** | Apoiar a conversa do estágio; não substitui os artefatos formais do Spec-Kit |
| **Feature relacionada** | [`specs/001-benefit-calculation/`](../specs/001-benefit-calculation/spec.md) |

> [!NOTE]
> Os entregáveis formais permanecem em `specs/<NNN>-<feature>/spec.md`, `plan.md` e `tasks.md`. Não registre requisitos EARS completos aqui. Este arquivo registra somente decisões de escopo e questões em aberto.

---

## Decisões de escopo

| Decisão | Evidência ou justificativa | Impacto nos artefatos formais |
|---|---|---|
| Mapa de contextos delimitados aceito como recomendado pelo `@architect`, com os dois ajustes propostos | [`bounded-contexts.md`](bounded-contexts.md); as 67 arestas de [`dependency-map.md`](../01-archaeology/dependency-map.md) | Cinco contextos e um kernel compartilhado passam a orientar `plan.md` |
| Validadores de CPF e NIS tratados como kernel compartilhado, não como parte do Cadastro | 4 chamadores em 3 contextos: `BATCHPGT.NSP:276`, `CADBENEF.NSP:161`, `CALCCORR.NSP:160`, `CONSBENF.NSP:136` | Nenhum contexto depende do Cadastro apenas para validar documento |
| Conciliação Bancária não é dona de `PAYMENT`; aplica o resultado bancário pela interface de Pagamento | Três vocabulários divergentes para a situação: `PAYMENT.ddm:73-75`, `BATCHCON.NSP:204-231`, `BATCHREL.NSP:177-190` | Escrita única em `PAYMENT`; a máquina de estados vira ADR |
| Consultas e relatórios (`CONSBENF`, `RELPGT`, `BATCHREL`) não formam contexto próprio | Só leem, e leem de contextos diferentes: `RELPGT -->\|READ\| PAYMENT` e `RELPGT -->\|FIND\| BENEFIC` | Viram modelos de leitura dentro dos contextos 1, 3 e 5 |
| Primeira feature do Estágio 2 é `001-benefit-calculation`, no contexto 3 | Maior densidade de regras confirmadas e maior valor de negócio | [`specs/001-benefit-calculation/spec.md`](../specs/001-benefit-calculation/spec.md) com 14 requisitos |
| Somente regras **Confirmada** viraram requisito; **Inferida** e **Mistério** ficaram de fora | 133 regras candidatas, 27 confirmadas ([`business-rules-catalog.md`](../01-archaeology/business-rules-catalog.md)) | Nenhum requisito sem `source_legacy:` conferido |
| Grupo periódico de descontos mapeado como `@OneToMany` com entidade `PaymentDiscount` | Mais gerenciável e auditável isoladamente; iteração por ocorrência já existe em `CALCDSCT.NSP:113-174` | [`ADR-001`](ADRs/adr-001-payment-discount-jpa-mapping.md); afeta REQ-006, REQ-007, REQ-008 e REQ-014 |

### Adiado nesta feature

| Item adiado | Motivo | Para onde vai |
|---|---|---|
| Validação de CPF e NIS (`CCVALCPF.NSC:94-127`, `SUBVALCP.NSN:74-78`) | Pertence ao kernel compartilhado, consumido por três contextos | Feature própria do kernel compartilhado |
| Cadastro de beneficiários e dependentes (`CADBENEF.NSP:145-186`) | Contexto 1, fora do recorte de cálculo | Feature do contexto Cadastro de Beneficiários |
| Agendamento da folha e dos relatórios (`SIFAPJ01.jcl:14-21`, `SIFAPJ02.jcl:14-22`) | É orquestração operacional, não regra de cálculo | Estágio 4 — Engenheiro DevOps |
| Retenção imutável da auditoria por 10 anos (`AUDIT.ddm:14-18`) | Pertence ao contexto 5 | Feature do contexto Trilha de Auditoria |
| Correção retroativa por índice (`CALCCORR.NSP`) | Todas as regras de índice estão classificadas como Mistério | Aguarda validação humana |
| Conciliação bancária CNAB 240 (`BATCHCON.NSP`) | Contexto 4, depende do domínio da situação do pagamento | Aguarda ADR da máquina de estados |

---

## Questões em aberto

| Questão | Fonte consultada | Próxima pessoa responsável |
|---|---|---|
| Qual renda é comparada com o teto do programa em REQ-003: familiar total ou per capita? | `VALELEG.NSN:174-180`, `BENEFIC.ddm:L96` | SENARC/CGPB |
| REQ-007 e REQ-008 valem durante a geração da folha ou apenas em execução avulsa de descontos? | `BATCHPGT.NSP:L20`, `CALCDSCT.NSP:L74` (sem chamador no corpus) | A definir pela dupla |
| Qual é o domínio real da situação do pagamento, necessário antes de projetar a máquina de estados? | `PAYMENT.ddm:73-75`, `CALCBENF.NSN:308-320`, `BATCHREL.NSP:177-190` | A definir pela dupla |
| A folha deve gerar um ou dois pagamentos por CPF e competência? | `BATCHPGT.NSP:L381`, `L488` (TICKET 6622/2011) | Coordenação de Benefícios |

---

### Continue lendo

| Anterior | Próximo |
|---|---|
| [Guia do Estágio 2](GUIDE.md)<br/><sub>Especificação moderna passo a passo.</sub> | [Template de ADR](ADR-TEMPLATE.md)<br/><sub>Registre a decisão de escopo como uma ADR.</sub> |

<sub>[Voltar ao índice do kit](../README.md)</sub>
