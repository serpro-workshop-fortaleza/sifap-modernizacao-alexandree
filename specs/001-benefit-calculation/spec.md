# Especificação — 001 Cálculo de Benefício

> **Feature:** `001-benefit-calculation`
> **Contexto delimitado:** 3. Pagamento de Benefícios ([bounded-contexts.md](../../02-modern-spec/bounded-contexts.md))
> **Branch:** `spec/001-benefit-calculation`
> **Data:** 2026-09-10
> **Status:** Rascunho para revisão da Dupla 2

## Escopo

Calcular o valor de um benefício para um beneficiário em uma competência mensal, gerar o registro de pagamento correspondente e produzir a folha mensal da competência.

**Fora de escopo nesta feature** (adiado, ver [scope-decisions.md](../../02-modern-spec/scope-decisions.md)): cadastro de beneficiários e dependentes, cadastro de programas sociais, conciliação bancária CNAB 240, correção retroativa por índice, relatórios consolidados e consulta online.

> [!IMPORTANT]
> Todos os requisitos abaixo derivam **exclusivamente** de regras classificadas como **Confirmada** em [`business-rules-catalog.md`](../../01-archaeology/business-rules-catalog.md). Cada `source_legacy:` foi aberto e conferido no arquivo indicado. Regras **Inferidas** e **Mistérios** não viraram requisito.

---

## Requisitos

### REQ-001 — Exigir programa social ativo para conceder elegibilidade

SE o programa social vinculado ao beneficiário não estiver ativo, ENTÃO o sistema DEVE reprovar a elegibilidade com o código de retorno 2004.

- source_legacy: `01-archaeology/legacy-sifap/natural-programs/VALELEG.NSN:L114-L118`
- Aceitação (Dado/Quando/Então):
  - Dado um beneficiário vinculado a um programa social cuja situação não seja `A`
  - Quando a elegibilidade for avaliada para uma competência
  - Então o sistema retorna o código 2004 e encerra a avaliação sem verificar as demais condições

### REQ-002 — Marcar como inelegível o beneficiário sem situação ativa

ENQUANTO a situação cadastral do beneficiário não for ativa, o sistema DEVE marcá-lo inelegível e registrar o motivo correspondente à situação (`S` suspenso, `C` ou `D` cancelado, `I` inativo).

- source_legacy: `01-archaeology/legacy-sifap/natural-programs/VALELEG.NSN:L133-L151`
- Aceitação (Dado/Quando/Então):
  - Dado um beneficiário com situação `S`, `C`, `D` ou `I`
  - Quando a elegibilidade for avaliada
  - Então o resultado é inelegível e o motivo registrado corresponde à situação encontrada

### REQ-003 — Reprovar renda acima do teto definido pelo programa

ONDE o programa social definir um teto de renda maior que zero, o sistema DEVE reprovar a elegibilidade do beneficiário cuja renda considerada seja superior ao teto.

- source_legacy: `01-archaeology/legacy-sifap/natural-programs/VALELEG.NSN:L174-L180`
- Aceitação (Dado/Quando/Então):
  - Dado um programa social com teto de renda maior que zero
  - Quando a renda considerada do beneficiário for superior ao teto
  - Então o resultado é inelegível com o motivo de renda acima do teto do programa
  - E, quando o teto for zero, a verificação não é aplicada

> [!WARNING]
> **Qual renda é comparada com o teto permanece questão em aberto.** O campo do programa chama-se `MAX-PERCAP-INCOME` (per capita) e o valor comparado é a renda familiar total. Este requisito fixa a existência da regra, não a natureza da renda. Ver questão em aberto sobre renda per capita.

### REQ-004 — Não calcular benefício de beneficiário sem situação ativa

SE a situação do beneficiário não for ativa no momento do cálculo, ENTÃO o sistema DEVE retornar o código 2002 e não calcular valor algum.

- source_legacy: `01-archaeology/legacy-sifap/natural-programs/CALCBENF.NSN:L180-L186`
- Aceitação (Dado/Quando/Então):
  - Dado um beneficiário cuja situação não seja `A`
  - Quando o cálculo do benefício for solicitado para uma competência
  - Então o sistema retorna 2002, não produz valor bruto, desconto ou líquido, e não grava pagamento

### REQ-005 — Aplicar o fator da primeira faixa de renda que comporte a renda

O sistema DEVE determinar o fator de renda pela primeira faixa, em ordem crescente de teto, cujo limite superior seja maior ou igual à renda considerada.

- source_legacy: `01-archaeology/legacy-sifap/natural-programs/CALCBENF.NSN:L344-L352`
- Aceitação (Dado/Quando/Então):
  - Dado um conjunto de faixas de renda ordenadas de forma crescente por teto
  - Quando a renda considerada for avaliada contra as faixas
  - Então o fator aplicado é o da primeira faixa cujo teto seja maior ou igual à renda
  - E faixas posteriores que também comportem a renda são ignoradas

### REQ-006 — Truncar valores monetários em duas casas decimais

O sistema DEVE truncar todo valor monetário em duas casas decimais, sem arredondamento.

- source_legacy: `01-archaeology/legacy-sifap/natural-programs/CALCBENF.NSN:L265-L266`, `CALCBENF.NSN:L279-L280`, `CALCBENF.NSN:L287-L288`, `CALCBENF.NSN:L305-L306`
- Aceitação (Dado/Quando/Então):
  - Dado um valor calculado com mais de duas casas decimais
  - Quando o valor for persistido ou devolvido
  - Então as casas decimais além da segunda são descartadas
  - E um valor terminado em `,XX5` não é elevado para `,XX6` nem para a casa seguinte

### REQ-007 — Limitar o total de descontos a 30% do valor bruto

O sistema DEVE limitar o total de descontos aplicados a um pagamento a 30% do seu valor bruto.

- source_legacy: `01-archaeology/legacy-sifap/natural-programs/CALCDSCT.NSP:L107-L111`, `CALCDSCT.NSP:L170-L174`
- Aceitação (Dado/Quando/Então):
  - Dado um pagamento com valor bruto conhecido e descontos sujeitos ao limite
  - Quando a soma dos descontos exceder 30% do valor bruto
  - Então o total de descontos aplicado é reduzido a exatamente 30% do valor bruto
  - E o teto de 30% é truncado em duas casas decimais conforme REQ-006

### REQ-008 — Isentar desconto judicial do limite de 30%

ONDE o desconto for de natureza judicial, o sistema DEVE aplicá-lo integralmente, sem sujeitá-lo ao limite de 30% do valor bruto.

- source_legacy: `01-archaeology/legacy-sifap/natural-programs/CALCDSCT.NSP:L128-L137`, `CALCDSCT.NSP:L170-L174`
- Aceitação (Dado/Quando/Então):
  - Dado um pagamento com um desconto judicial que, somado aos demais, ultrapassa 30% do valor bruto
  - Quando os descontos forem calculados
  - Então o desconto judicial é aplicado por inteiro
  - E a verificação do teto não é executada na iteração do desconto judicial

### REQ-009 — Ignorar na folha o beneficiário sem situação ativa

ENQUANTO o beneficiário não estiver com situação ativa, o sistema DEVE ignorá-lo na geração da folha e contabilizá-lo como ignorado.

- source_legacy: `01-archaeology/legacy-sifap/natural-programs/BATCHPGT.NSP:L263-L267`
- Aceitação (Dado/Quando/Então):
  - Dado um beneficiário com situação diferente de `A` na base
  - Quando a folha da competência for gerada
  - Então nenhum pagamento é criado para ele
  - E o contador de ignorados é incrementado em um

### REQ-010 — Não gerar pagamento duplicado para o mesmo CPF e competência

SE já existir pagamento registrado para o mesmo CPF e a mesma competência, ENTÃO o sistema DEVE ignorar o beneficiário e não gerar novo pagamento.

- source_legacy: `01-archaeology/legacy-sifap/natural-programs/BATCHPGT.NSP:L292-L298`
- Aceitação (Dado/Quando/Então):
  - Dado um pagamento já existente para o CPF e a competência processada
  - Quando a folha for executada novamente para a mesma competência
  - Então nenhum pagamento adicional é criado para esse CPF
  - E a reexecução da folha completa não altera a quantidade de pagamentos da competência

### REQ-011 — Derivar a competência da data corrente quando não informada

SE a competência não for informada na execução da folha, ENTÃO o sistema DEVE derivá-la do ano e do mês da data corrente.

- source_legacy: `01-archaeology/legacy-sifap/natural-programs/BATCHPGT.NSP:L171-L173`
- Aceitação (Dado/Quando/Então):
  - Dado que nenhuma competência foi informada na chamada da folha
  - Quando a execução iniciar
  - Então a competência processada é o ano e o mês da data corrente
  - E, quando a competência for informada, ela prevalece sobre a data corrente

### REQ-012 — Devolver código de retorno conforme o resultado da folha

QUANDO a geração da folha terminar, o sistema DEVE devolver o código de retorno correspondente ao resultado: 8 quando nenhum pagamento foi gerado, 4 quando houve registros rejeitados e 0 quando não houve rejeição.

- source_legacy: `01-archaeology/legacy-sifap/natural-programs/BATCHPGT.NSP:L558-L566`
- Aceitação (Dado/Quando/Então):
  - Dado uma execução da folha concluída sem gerar pagamento algum
  - Quando o processamento terminar
  - Então o código de retorno é 8
  - E, havendo pagamentos gerados e ao menos um rejeitado, o código é 4
  - E, havendo pagamentos gerados e nenhum rejeitado, o código é 0

### REQ-013 — Desfazer a transação e encerrar com 12 em caso de erro de execução

SE ocorrer erro de execução durante a geração da folha, ENTÃO o sistema DEVE desfazer a transação em curso, encerrar os recursos abertos e terminar com o código de retorno 12.

- source_legacy: `01-archaeology/legacy-sifap/natural-programs/BATCHPGT.NSP:L572-L582`
- Aceitação (Dado/Quando/Então):
  - Dado uma folha em processamento que sofre um erro de execução
  - Quando o erro for capturado
  - Então as alterações não confirmadas são desfeitas, os arquivos de trabalho são fechados e o código de retorno é 12
  - E o diagnóstico registra o identificador do erro, o último CPF lido e a competência

### REQ-014 — Registrar trilha de auditoria em toda alteração de dados

QUANDO um pagamento for gerado ou alterado, o sistema DEVE registrar a trilha de auditoria com ação, módulo, entidade, identificador e data/hora do evento.

- source_legacy: `01-archaeology/legacy-sifap/natural-programs/CCAUDIT.NSC:L7-L10`
- Aceitação (Dado/Quando/Então):
  - Dado um pagamento gerado pela folha
  - Quando a operação for confirmada
  - Então existe um registro de auditoria correspondente com ação, módulo, tipo de entidade, identificador e data/hora
  - E a ausência do registro de auditoria invalida a operação

---

## Matriz de rastreabilidade

| REQ-ID | Padrão EARS | source_legacy | Regra de origem | Arquivo de origem |
|---|---|---|---|---|
| REQ-001 | Indesejado | `VALELEG.NSN:L114-L118` | Regra 3 de `VALELEG` | `business-rules-catalog.md` |
| REQ-002 | Estado | `VALELEG.NSN:L133-L151` | Regra 5 de `VALELEG` | `business-rules-catalog.md` |
| REQ-003 | Opcional | `VALELEG.NSN:L174-L180` | Regra 8 de `VALELEG` | `business-rules-catalog.md` |
| REQ-004 | Indesejado | `CALCBENF.NSN:L180-L186` | Regra 2 de `CALCBENF` | `business-rules-catalog.md` |
| REQ-005 | Ubíquo | `CALCBENF.NSN:L344-L352` | Regra 5 de `CALCBENF` | `business-rules-catalog.md` |
| REQ-006 | Ubíquo | `CALCBENF.NSN:L265-L266`, `L279-L280`, `L287-L288`, `L305-L306` | Regra 8 de `CALCBENF` | `business-rules-catalog.md` |
| REQ-007 | Ubíquo | `CALCDSCT.NSP:L107-L111`, `L170-L174` | Regra 3 de `CALCDSCT` | `business-rules-catalog.md` |
| REQ-008 | Opcional | `CALCDSCT.NSP:L128-L137`, `L170-L174` | Regra 4 de `CALCDSCT` | `business-rules-catalog.md` |
| REQ-009 | Estado | `BATCHPGT.NSP:L263-L267` | Regra 4 de `BATCHPGT` | `business-rules-catalog.md` |
| REQ-010 | Indesejado | `BATCHPGT.NSP:L292-L298` | Regra 6 de `BATCHPGT` | `business-rules-catalog.md` |
| REQ-011 | Indesejado | `BATCHPGT.NSP:L171-L173` | Regra 1 de `BATCHPGT` | `business-rules-catalog.md` |
| REQ-012 | Evento | `BATCHPGT.NSP:L558-L566` | Regra 12 de `BATCHPGT` | `business-rules-catalog.md` |
| REQ-013 | Indesejado | `BATCHPGT.NSP:L572-L582` | Regra 13 de `BATCHPGT` | `business-rules-catalog.md` |
| REQ-014 | Evento | `CCAUDIT.NSC:L7-L10` | Regra 1 de `CCAUDIT` | `business-rules-catalog.md` |

Todas as origens foram abertas e conferidas nas linhas indicadas. Nenhum requisito usa `[GREENFIELD]`.

---

## Questões em aberto

Reproduzidas de [`mysteries-found.md`](../../01-archaeology/mysteries-found.md) **sem alteração de status**. Nenhuma foi respondida nesta especificação e nenhuma virou requisito. Os IDs canônicos permanecem pendentes de atribuição pela dupla.

> [!NOTE]
> A primeira questão da tabela continua aberta. O que [`ADR-002`](../../02-modern-spec/ADRs/adr-002-payment-uniqueness-cpf-competence.md) decide é apenas quantos pagamentos a folha **nova** gera por CPF e competência (um), o que não explica nem legitima a dupla gravação legada. O tratamento do histórico já gravado permanece com a Coordenação de Benefícios.

| ID | Questão em aberto | Evidência (`path:line`) | Impacto | Hipótese (não confirmada) | Pessoa/área responsável | Status |
|---|---|---|---|---|---|---|
| `SIFAP-M-__` | Por que a folha grava dois pagamentos para o mesmo CPF e período, um pelo subprograma chamado e outro pelo cálculo inline da própria folha? | `01-archaeology/legacy-sifap/natural-programs/BATCHPGT.NSP:L381`, `BATCHPGT.NSP:L488`, `CALCBENF.NSN:L319` | Define se a migração reproduz um ou dois registros por competência e como tratar o histórico já gravado | Não confirmada: o comentário `BATCHPGT.NSP:L363-367` diz "REMAINS ACTIVE PENDING A DECISION - TICKET 6622/2011 OPEN" | Coordenação de Benefícios, conforme `BATCHPGT.NSP:L365` | aberta |
| `SIFAP-M-__` | Por que o cálculo usa a renda familiar total onde o cadastro tem um campo de renda per capita calculada, e compara esse valor contra um teto documentado como per capita? | `01-archaeology/legacy-sifap/natural-programs/CALCBENF.NSN:L174`, `adabas-ddms/BENEFIC.ddm:L96`, `natural-programs/VALELEG.NSN:L92` | Muda faixa de cálculo e elegibilidade de toda família com mais de um membro | Não confirmada: nenhum módulo lido escreve ou lê o campo de renda per capita | SENARC/CGPB, conforme `legacy-docs/BUSINESS-RULES-2012.md:L163` | aberta |
| `SIFAP-M-__` | Por que o código de região, definido no cadastro como macrorregião de `01` a `05`, é usado para indexar uma tabela de 27 posições ordenada por unidade federativa? | `01-archaeology/legacy-sifap/adabas-ddms/BENEFIC.ddm:L80`, `natural-programs/CALCBENF.NSN:L200`, `natural-programs/LDASIFAP.NSL:L38` | O fator regional multiplica o valor de todo benefício; define se a base histórica está correta | Não confirmada: o cadastro de programas tem tabela de fator regional que nenhum módulo lido consulta | A definir pela dupla | aberta |
| `SIFAP-M-__` | Qual é a fórmula do 13º benefício, dado que o comentário descreve proporcionalidade por meses ativos e o código executado usa o fator etário? | `01-archaeology/legacy-sifap/natural-programs/CALCBENF.NSN:L271`, `CALCBENF.NSN:L277`, `legacy-docs/BUSINESS-RULES-2012.md:L253` | O 13º é pago a toda a base em dezembro; a fórmula errada erra o valor de todos | Não confirmada: o documento de 2012 lista o 13º como não documentado e de prioridade alta | A definir pela dupla | aberta |
| `SIFAP-M-__` | Por que existem duas regras de contribuição social, uma com alíquota única de 3% no cálculo e outra com quatro faixas progressivas no programa de descontos? | `01-archaeology/legacy-sifap/natural-programs/CALCBENF.NSN:L358`, `CALCDSCT.NSP:L198` | O mesmo pagamento recebe descontos diferentes conforme quem calcula | Não confirmada: o comentário do código admite "SIMPLIFIED (SEE CALCDSCT FOR FULL VERSION)" | A definir pela dupla | aberta |
| `SIFAP-M-__` | Por que a elegibilidade acumula até dez motivos de reprovação e devolve apenas o primeiro, descartando os demais sem registro? | `01-archaeology/legacy-sifap/natural-programs/VALELEG.NSN:L231`, `VALELEG.NSN:L45` | O beneficiário e o atendimento recebem uma explicação parcial; a migração precisa decidir se expõe todos | Não confirmada: o vetor de motivos existe desde a criação da rotina | A definir pela dupla | aberta |
| `SIFAP-M-__` | Quem autorizou o bypass total de elegibilidade do beneficiário de região 99 antes de qualquer verificação, e quantos registros de produção usam esse código? | `01-archaeology/legacy-sifap/natural-programs/VALELEG.NSN:L123`, `adabas-ddms/BENEFIC.ddm:L80` | Beneficiários de região 99 recebem pagamento sem nenhuma verificação de elegibilidade | Não confirmada: doc §4.2 registra "sem explicação conhecida - prioridade Alta" | A definir pela dupla | aberta |
| `BONUS` | Por que o indicativo de óbito e os campos de bloqueio judicial e administrativo não são consultados por nenhum programa antes de gerar pagamento? | `01-archaeology/legacy-sifap/adabas-ddms/BENEFIC.ddm:L145`, `BENEFIC.ddm:L147`, `natural-programs/BATCHPGT.NSP:L263` | Define se a aplicação nova deve bloquear pagamentos que hoje são gerados | Não confirmada: o cruzamento de óbito existe no cadastro desde 2001 e nenhum módulo lido o consulta | A definir pela dupla | aberta |
| `SIFAP-M-__` | Como o programa de descontos é acionado, se não existe chamada a ele em nenhum módulo do corpus? | `01-archaeology/legacy-sifap/natural-programs/BATCHPGT.NSP:L20`, `CALCDSCT.NSP:L74` | Define se REQ-007 e REQ-008 se aplicam durante a geração da folha ou apenas em execução avulsa | Não confirmada: os comentários afirmam a chamada, o código não a contém | A definir pela dupla | aberta |
| `SIFAP-M-__` | Qual é o domínio real da situação do pagamento, dado que DDM, cálculo e relatório usam três vocabulários distintos para o mesmo campo? | `01-archaeology/legacy-sifap/adabas-ddms/PAYMENT.ddm:L73-L75`, `natural-programs/CALCBENF.NSN:L308-L320`, `natural-programs/BATCHREL.NSP:L177-L190` | Define a máquina de estados do pagamento e a leitura da base histórica | Não confirmada: doc §5.1 diz `P`, o código grava `G`, o DDM define os dois como estados distintos | A definir pela dupla | aberta |

> [!IMPORTANT]
> Nenhuma destas questões pode virar requisito sem validação humana explícita apoiada em evidência, conforme [`mysteries-checklist.md`](../../01-archaeology/mysteries-checklist.md).

---

## Definição de pronto

- [x] Contém somente requisitos desta feature
- [x] Cada requisito tem formulação EARS, critério verificável e `source_legacy:` conferido
- [x] Questões em aberto preservadas fora dos requisitos, sem mudança de status
- [x] Matriz relaciona cada REQ-ID à evidência revisada
- [ ] Revisão da Dupla 2 registrada
