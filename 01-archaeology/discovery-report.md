# Relatório de Descoberta — Estágio 1: Arqueologia Digital

> **Trilha:** [Kit do Time](../README.md) › [Estágio 1](README.md) › **Relatório de Descoberta**

**Artefato preenchido pelo time ao fim do Estágio 1.** Consolida os achados da arqueologia e é a entrada principal do Estágio 2.

| Campo | Valor |
|---|---|
| **Público-alvo** | Todas as duplas — consolidação ao fim do Estágio 1 |
| **Pré-requisitos** | Catálogo de regras, mapa de dependências e glossário preenchidos |
| **Estágio** | Estágio 1 — Arqueologia |
| **Resultado esperado** | Documento de até 3 páginas com resumo, hipóteses de fatiamento e artefatos de origem |

> [!IMPORTANT]
> Este documento consolida todos os achados do Estágio 1. Preencha cada seção com as conclusões do time. Sem ele, a especificação do Estágio 2 não tem base de evidência.

> [!NOTE]
> Guia passo a passo: [`GUIDE.md`](GUIDE.md).

**Time**: <!-- preencher -->
**Data**: 2026-09-10
**Edição**: <!-- preencher -->
**Participantes**: <!-- preencher: 5 duplas cobrindo 10 personas -->

---

## 1. Resumo executivo

O SIFAP tem 24 membros Natural, 4 arquivos Adabas e 3 documentos históricos, todos lidos e cruzados ([inventory.md](inventory.md)). Foram extraídas 133 regras candidatas, das quais **27 estão confirmadas** por documentação ou DDM, 61 apoiam-se somente no código e **45 são perguntas em aberto** ([business-rules-catalog.md](business-rules-catalog.md)). O sistema é fortemente acoplado por reuso: 67 arestas verificadas, com `CCAUDIT` incluído por 7 módulos e `LDASIFAP` compartilhado por 13 ([dependency-map.md](dependency-map.md)). O maior risco para o Estágio 2 é que **quatro perguntas em aberto alteram o valor pago** — pagamento gravado em duplicidade, renda total usada como per capita, código de macrorregião indexando tabela de unidade federativa e ausência de bloqueio por óbito ([mysteries-found.md](mysteries-found.md)). A evidência sustenta confiança **média** para os contextos de cadastro e catálogo de programas e **baixa** para o contexto de pagamento, onde nenhuma fórmula pôde ser confirmada contra a documentação; a ratificação formal cabe ao time na seção 6.

---

## 2. O que sabemos (confirmado)

### 2.1 Regras de negócio

27 regras confirmadas — corroboradas por [BUSINESS-RULES-2012.md](legacy-sifap/legacy-docs/BUSINESS-RULES-2012.md), por DDM ou por JCL. Seleção pelas que sustentam decisão de arquitetura; a lista completa está em [business-rules-catalog.md](business-rules-catalog.md).

| Regra | Candidato EARS | Referência |
|---|---|---|
| Validar CPF por módulo 11, pesos 10→2 e 11→2, resto menor que 2 produz dígito 0 | `O sistema DEVE validar o CPF por módulo 11 conforme o algoritmo padrão.` | [Regras de `CCVALCPF.NSC` nº 3](business-rules-catalog.md) |
| Retornar 1001 quando o dígito verificador não confere | `SE o dígito verificador do CPF não conferir, ENTÃO o sistema DEVE retornar 1001.` | [Regras de `SUBVALCP.NSN` nº 4](business-rules-catalog.md) |
| Exigir CPF, nome, data de nascimento e sexo válidos no cadastro | `O sistema DEVE exigir CPF, nome, data de nascimento e sexo válidos.` | [Regras de `CADBENEF.NSP` nº 2](business-rules-catalog.md) |
| Registrar auditoria em toda inclusão e alteração | `QUANDO um cadastro for incluído ou alterado, o sistema DEVE registrar auditoria.` | [Regras de `CADBENEF.NSP` nº 9](business-rules-catalog.md), [`CCAUDIT.NSC` nº 1](business-rules-catalog.md) |
| Reprovar elegibilidade quando o programa social não estiver ativo | `SE o programa não estiver ativo, ENTÃO o sistema DEVE retornar 2004.` | [Regras de `VALELEG.NSN` nº 3](business-rules-catalog.md) |
| Marcar inelegível enquanto a situação do beneficiário não for ativa | `ENQUANTO a situação do beneficiário não for A, o sistema DEVE marcá-lo inelegível.` | [Regras de `VALELEG.NSN` nº 5](business-rules-catalog.md) |
| Reprovar renda familiar acima do teto do programa | `ONDE o programa definir renda máxima, o sistema DEVE reprovar renda superior.` | [Regras de `VALELEG.NSN` nº 8](business-rules-catalog.md) |
| Não calcular benefício de beneficiário não ativo | `SE o beneficiário não estiver ativo, ENTÃO o sistema DEVE retornar 2002.` | [Regras de `CALCBENF.NSN` nº 2](business-rules-catalog.md) |
| Aplicar o fator da primeira faixa de renda que comporte a renda declarada | `O sistema DEVE aplicar o fator da primeira faixa de renda que comporte a renda declarada.` | [Regras de `CALCBENF.NSN` nº 5](business-rules-catalog.md) |
| Truncar valores monetários em duas casas, sem arredondamento | `O sistema DEVE truncar valores monetários em duas casas decimais.` | [Regras de `CALCBENF.NSN` nº 8](business-rules-catalog.md) |
| Limitar o total de descontos a 30% do valor bruto | `O sistema DEVE limitar o total de descontos a 30% do valor bruto.` | [Regras de `CALCDSCT.NSP` nº 3](business-rules-catalog.md) |
| Isentar descontos judiciais do limite de 30% | `ONDE o desconto for judicial, o sistema DEVE ignorar o limite de 30%.` | [Regras de `CALCDSCT.NSP` nº 4](business-rules-catalog.md) |
| Processar na folha apenas beneficiários com situação ativa | `ENQUANTO o beneficiário não estiver ativo, o sistema DEVE ignorá-lo na folha.` | [Regras de `BATCHPGT.NSP` nº 4](business-rules-catalog.md) |
| Não gerar pagamento quando já existir um para o mesmo CPF e período | `SE já existir pagamento para o CPF e período, ENTÃO o sistema DEVE ignorá-lo.` | [Regras de `BATCHPGT.NSP` nº 6](business-rules-catalog.md) |
| Devolver código de retorno conforme o resultado do processamento | `QUANDO o processamento terminar, o sistema DEVE devolver o código de retorno correspondente ao resultado.` | [Regras de `BATCHPGT.NSP` nº 12 e nº 13](business-rules-catalog.md) |
| Gerar a folha no 1º dia útil às 22:00 e os relatórios no 2º dia útil, condicionados ao sucesso da folha | `QUANDO a folha terminar com RC 0, o sistema DEVE liberar a emissão dos relatórios no 2º dia útil.` | [Regras de `SIFAPJ01`/`SIFAPJ02` nº 1 e nº 2](business-rules-catalog.md) |
| Reter trilha de auditoria e cópia de relatório por 10 anos, sem alteração nem exclusão | `O sistema DEVE manter a trilha de auditoria imutável por 10 anos.` | [Cruzamento com DDMs nº 13](business-rules-catalog.md) |

### 2.2 Dependências

67 arestas verificadas, todas com `arquivo:linha` em [dependency-map.md](dependency-map.md).

| Tipo | Total | Observação |
|---|---:|---|
| `CALLNAT` | 9 | Nenhum destino ausente |
| `INCLUDE` | 9 | Nenhum copycode ausente |
| `USING` (áreas de dados) | 23 | `LDASIFAP` em 13 módulos |
| Programa → arquivo Adabas | 49 | **Nenhum `DELETE` no sistema inteiro** |
| Programa → arquivo sequencial e impressora | 7 | `CMWKF01` designa três conjuntos de dados diferentes |

Nós mais conectados: `CCAUDIT.NSC` (7 `INCLUDE`), `LDASIFAP.NSL` (13 `USING`), `SUBVALCP.NSN` (4 `CALLNAT`). Sem chamador: `CALCDSCT.NSP` e `VALDOCS.NSP`. Sem JCL: `BATCHCON.NSP` e `RELAUDIT.NSP`.

### 2.3 Estruturas de dados

Quatro DDMs e a listagem FDT do arquivo 150 foram cruzados ([business-rules-catalog.md](business-rules-catalog.md), seção de cruzamento).

| Arquivo | FNR | Volume | Chaves e estruturas relevantes |
|---|---:|---:|---|
| `BENEFIC` | 150 | 4,2 milhões | `NUM-CPF` único; grupo periódico de dependentes 1:10; superdescritores por UF+situação e programa+situação |
| `SOCPROG` | 151 | 45 | `COD-PROGRAM` único; grupo periódico de 5 faixas de cálculo; grupo periódico de parâmetro regional 1:6 |
| `PAYMENT` | 152 | 612 milhões | `NUM-PAYMENT` único; grupo periódico de 8 descontos; superdescritor `S1` CPF+competência |
| `AUDIT` | 153 | 418 milhões | Imutável; particionado em 154/155/156 sem DDM publicado |

---

## 3. O que é arriscado

### 3.1 Questões em aberto aguardando validação humana

40 questões registradas, **nenhuma validada**. Todas com status `aberta` e ID canônico pendente de atribuição pela dupla. Reproduzidas abaixo as de impacto financeiro direto; as 36 restantes estão em [mysteries-found.md](mysteries-found.md).

| Questão em aberto | Evidência (`path:line`) | Impacto | Hipótese (não confirmada) | Pessoa/área responsável | Status |
|---|---|---|---|---|---|
| Por que a folha grava dois pagamentos para o mesmo CPF e período, um pelo subprograma chamado e outro pelo cálculo inline da própria folha? | `01-archaeology/legacy-sifap/natural-programs/BATCHPGT.NSP:L381`, `BATCHPGT.NSP:L488`, `CALCBENF.NSN:L319` | Define se a migração reproduz um ou dois registros por competência e como tratar o histórico já gravado | Não confirmada: o comentário `BATCHPGT.NSP:L363-367` diz "REMAINS ACTIVE PENDING A DECISION - TICKET 6622/2011 OPEN" | Coordenação de Benefícios, conforme `BATCHPGT.NSP:L365` | aberta |
| Por que o cálculo usa a renda familiar total onde o cadastro tem um campo de renda per capita calculada, e compara esse valor contra um teto documentado como per capita? | `01-archaeology/legacy-sifap/natural-programs/CALCBENF.NSN:L174`, `adabas-ddms/BENEFIC.ddm:L96`, `natural-programs/VALELEG.NSN:L92` | Muda faixa de cálculo e elegibilidade de toda família com mais de um membro | Não confirmada: nenhum módulo lido escreve ou lê o campo de renda per capita | SENARC/CGPB, conforme `legacy-docs/BUSINESS-RULES-2012.md:L163` | aberta |
| Por que o código de região, definido no cadastro como macrorregião de `01` a `05`, é usado para indexar uma tabela de 27 posições ordenada por unidade federativa? | `01-archaeology/legacy-sifap/adabas-ddms/BENEFIC.ddm:L80`, `natural-programs/CALCBENF.NSN:L200`, `natural-programs/LDASIFAP.NSL:L38` | O fator regional multiplica o valor de todo benefício; define se a base histórica está correta | Não confirmada: o cadastro de programas tem tabela de fator regional que nenhum módulo lido consulta | A definir pela dupla | aberta |
| Por que o indicativo de óbito e os campos de bloqueio judicial e administrativo não são consultados por nenhum programa antes de gerar pagamento? | `01-archaeology/legacy-sifap/adabas-ddms/BENEFIC.ddm:L145`, `BENEFIC.ddm:L147`, `natural-programs/BATCHPGT.NSP:L263` | Define se a aplicação nova deve bloquear pagamentos que hoje são gerados | Não confirmada: o cruzamento de óbito existe no cadastro desde 2001 e nenhum módulo lido o consulta | A definir pela dupla | aberta |
| Qual é a fórmula do 13º benefício, dado que o comentário descreve proporcionalidade por meses ativos e o código executado usa o fator etário? | `01-archaeology/legacy-sifap/natural-programs/CALCBENF.NSN:L271`, `CALCBENF.NSN:L277` | O 13º é pago a toda a base em dezembro; a fórmula errada erra o valor de todos | Não confirmada: o documento de 2012 lista o 13º como não documentado e de prioridade alta | A definir pela dupla | aberta |

> [!IMPORTANT]
> Nenhuma destas perguntas pode virar requisito no Estágio 2 sem validação humana explícita apoiada em evidência. O gate está descrito em [mysteries-checklist.md](mysteries-checklist.md).

### 3.2 Regras com evidência fraca

61 regras estão classificadas como **Inferidas** — extraídas do código sem corroboração em documento ou DDM. Fundamentar requisitos nelas transfere para a aplicação nova comportamentos que ninguém validou. As concentrações de risco:

- **Literais cravados no código sem origem**: renda de 600,00 na elegibilidade por tipo de programa, idades 60 e 16-65, abono de 15%, desconto sindical de 1%, tolerância de 1 centavo na conciliação e fator 0,347215 no cadastro de programas.
- **Parâmetros duplicados entre a área de dados e os programas**: a tabela de fator regional e as faixas de renda existem em `LDASIFAP.NSL` e são reescritas literalmente em `CALCBENF.NSN` e `BATCHPGT.NSP`.
- **20 divergências entre código e o levantamento de 2012** e **11 entre código e DDM**, incluindo limite de dependentes, fórmula do benefício, tratamento do teto de descontos e domínio de tipos de desconto ([business-rules-catalog.md](business-rules-catalog.md)).

---

## 4. Hipóteses de fatiamento recomendadas

> [!NOTE]
> São **hipóteses**, não decisões. Derivam de agrupamentos de dependência em [dependency-map.md](dependency-map.md) e cabe ao `@architect-agent` avaliá-las no Estágio 2.

### Hipótese 1: Cadastro de Beneficiários

- Programas: `CADBENEF`, `CADDEPEN`, `VALBENEF`, `VALDOCS`, `SUBVALCP`, `SUBVALNI`, `CCVALCPF`
- DDMs: `BENEFIC` (150)
- Justificativa: é o único agrupamento que grava em `BENEFIC`, e todo o cluster de validação de documentos existe apenas para servi-lo.

### Hipótese 2: Catálogo de Programas Sociais

- Programas: `CADPROG`
- DDMs: `SOCPROG` (151)
- Justificativa: `CADPROG` é o único escritor de `SOCPROG`, que tem 45 registros e comporta-se como tabela de parâmetros lida por todo o resto.

### Hipótese 3: Cálculo e Geração de Pagamento

- Programas: `BATCHPGT`, `VALELEG`, `CALCBENF`, `CALCDSCT`, `CALCCORR`
- DDMs: `PAYMENT` (152) para escrita; `BENEFIC` e `SOCPROG` somente leitura
- Justificativa: concentra todas as gravações em `PAYMENT` e todas as regras de valor, e é onde estão as quatro perguntas de impacto financeiro.

### Hipótese 4: Conciliação Bancária

- Programas: `BATCHCON`
- DDMs: `PAYMENT` (152) para atualização de situação; arquivos CNAB 240 de remessa e retorno
- Justificativa: acopla-se ao mundo externo por arquivo, não por chamada, e toca `PAYMENT` apenas na máquina de estados — fronteira natural e a única com integração bancária.

### Hipótese 5: Trilha de Auditoria

- Programas: `CCAUDIT`, `RELAUDIT`
- DDMs: `AUDIT` (153) e as partições 154/155/156
- Justificativa: escrita centralizada em um único copycode usado por 7 módulos, leitura por um único relatório, arquivo imutável com exigência legal de retenção — candidato a serviço transversal.

> **Observação sobre consultas e relatórios.** `CONSBENF`, `RELPGT` e `BATCHREL` só leem, e leem de contextos diferentes ao mesmo tempo. Não formam um contexto próprio: são candidatos a modelo de leitura sobre as hipóteses 1, 3 e 5.

---

## 5. Artefatos de origem

| Artefato | Caminho | Status |
|---|---|---|
| Inventário | [inventory.md](inventory.md) | Completo |
| Regras de Negócio | [business-rules-catalog.md](business-rules-catalog.md) | Completo — 133 regras candidatas |
| Dependências | [dependency-map.md](dependency-map.md) | Completo — 67 arestas; diagrama em [dependency-map.mmd](dependency-map.mmd) |
| Questões em aberto | [mysteries-found.md](mysteries-found.md) | Preenchido com 40 questões; **IDs canônicos e parte dos responsáveis pendentes** |
| Glossário | [glossary.md](glossary.md) | **Não iniciado** — a definição de pronto do Estágio 1 exige 15 termos de domínio |

---

## 6. Aprovação do time

- Revisado por: <!-- preencher -->
- Data: <!-- preencher: AAAA-MM-DD -->
- Confiança: <!-- preencher: Alta / Média / Baixa -->

---

## Definição de pronto

- [x] Resumo com no máximo 5 frases.
- [x] De 3 a 5 hipóteses de fatiamento documentadas, rotuladas como hipóteses.
- [x] Todos os artefatos de origem têm status preenchido.
- [x] Questões sem validação humana aparecem com evidência `path:line` e status.
- [x] O documento não passa de 3 páginas.
- [ ] Glossário com 15 termos de domínio.
- [ ] IDs canônicos atribuídos em [mysteries-found.md](mysteries-found.md).
- [ ] Aprovação do time registrada na seção 6.

---

### Continue lendo

| Anterior | Próximo |
|---|---|
| [GUIDE do Estágio 1](GUIDE.md)<br/><sub>Cronograma passo a passo.</sub> | [Estágio 2 — Especificação moderna](../02-modern-spec/README.md)<br/><sub>Handoff H1 e início do EARS.</sub> |

<sub>[Voltar ao índice do kit](../README.md)</sub>
