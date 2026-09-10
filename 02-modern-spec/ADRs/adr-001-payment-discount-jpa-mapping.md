# ADR-001: Mapear o grupo periódico de descontos do Adabas para `@OneToMany` com entidade própria

> **Trilha:** [Kit do Time](../../README.md) › [Estágio 2](../README.md) › ADRs › **ADR-001**

| Campo | Valor |
|---|---|
| **Status** | `Proposta` — aceita pela equipe em 2026-09-10, aguarda validação formal no handoff H2 |
| **Data** | 2026-09-10 |
| **Feature relacionada** | [`specs/001-benefit-calculation/`](../../specs/001-benefit-calculation/spec.md) |
| **Contexto delimitado** | 3. Pagamento de Benefícios ([bounded-contexts.md](../bounded-contexts.md)) |

---

## Contexto

O arquivo Adabas `PAYMENT` (FNR 152, 612 milhões de registros) armazena os descontos aplicados a um pagamento em um **grupo periódico** de até 8 ocorrências:

```text
 P 1 CA GRP-DISC                         (1:8) APPLIED DISCOUNTS
   2 CB TYPE-DISC                A    3  N   IR/JD/CS/PA/EM/TX/OU/EX
   2 CC AMT-DISC                 P  7,2  N   DISCOUNT AMOUNT
   2 CD PCT-DISC                 P  3,2  N   APPLIED PERCENTAGE
   2 CE NUM-CASE                 A   20  N   COURT CASE NO. (IF JD)
   2 CF DT-START-DISC            N    8  N   YYYYMMDD
   2 CG DT-END-DISC              N    8  N   YYYYMMDD (0=UNDEFINED)
```

Fonte: `01-archaeology/legacy-sifap/adabas-ddms/PAYMENT.ddm:L47-L55`.

O PostgreSQL 16 não possui equivalente direto para grupo periódico. A decisão precisa ser tomada agora porque dois requisitos da primeira feature dependem da forma de acesso a cada ocorrência:

- **REQ-007** exige limitar o total de descontos a 30% do valor bruto — soma item a item.
- **REQ-008** exige isentar o desconto judicial desse limite — depende de identificar o tipo de cada ocorrência isoladamente.

O legado percorre o grupo com `FOR #INDEX = 1 TO C*GRP-DISC` (`CALCDSCT.NSP:L113-L174`), avalia vigência por ocorrência (`CALCDSCT.NSP:L117-L124`) e aplica o teto por iteração (`CALCDSCT.NSP:L170-L174`). Ou seja, o comportamento confirmado já opera sobre ocorrências individuais, não sobre o grupo como bloco.

Há ainda um agravante conhecido: `PAYMENT.ddm:L47` define o tipo de desconto com 2 caracteres em campo `A3` (`IR/JD/CS/PA/EM/TX/OU/EX`), enquanto `CALCDSCT.NSP:L127-L167` compara com 1 caractere (`C/I/J/S/P/A`). A divergência está registrada como questão em aberto e a decisão de mapeamento não a resolve — mas influencia a facilidade de investigá-la nos dados migrados.

---

## Opções consideradas

### Opção 1: `@ElementCollection` de um tipo `@Embeddable`

Os descontos viram uma coleção de objetos de valor sem identidade própria, gravados em tabela filha gerenciada integralmente pela entidade `Payment`.

| Aspecto | Avaliação |
|---|---|
| **Vantagens** | Semântica idêntica à do grupo periódico: a ocorrência não existe fora do pagamento; ciclo de vida e remoção em cascata sem código adicional; não inventa identidade que o legado não possui |
| **Desvantagens** | Não há repositório próprio, então investigar a divergência de domínio de `TYPE-DISC` em 612 milhões de registros exige consulta nativa; Hibernate apaga e regrava a coleção inteira em alterações, o que é caro na correção retroativa; auditoria por desconto individual fica sem chave estável |
| **Risco** | Médio — o custo aparece na conciliação e na correção, não no cálculo |
| **Esforço** | Baixo |

### Opção 2: `@OneToMany` com entidade `PaymentDiscount`

Cada desconto vira uma entidade com chave primária própria, repositório próprio e relacionamento gerenciado a partir de `Payment`.

| Aspecto | Avaliação |
|---|---|
| **Vantagens** | Cada desconto é consultável e auditável isoladamente, o que sustenta REQ-014 no nível do item e permite investigar a divergência `TYPE-DISC` por consulta JPQL; alteração de um desconto não regrava os oito; suporta índice por tipo e por vigência sem SQL nativo |
| **Desvantagens** | Cria identidade que não existe no legado, exigindo regra explícita de geração de chave na migração dos 612 milhões de registros; a entidade precisa ser protegida para não ser criada fora do agregado `Payment`; um join a mais na leitura da folha |
| **Risco** | Médio — concentrado na migração de dados, não no comportamento de cálculo |
| **Esforço** | Médio |

### Opção 3: Coluna `JSONB` no PostgreSQL 16

A lista de até 8 descontos é serializada em uma única coluna `jsonb` da tabela de pagamento.

| Aspecto | Avaliação |
|---|---|
| **Vantagens** | Uma única leitura por pagamento, sem join, o que favorece a varredura da folha mensal; absorve sem migração de schema a divergência entre o tipo de 2 e o de 1 caractere; espelha o limite de 8 ocorrências sem tabela adicional |
| **Desvantagens** | Integridade do tipo de desconto sai do banco e vira responsabilidade exclusiva da aplicação, justo onde já existe divergência confirmada entre DDM e código; auditoria por item perde chave; consulta analítica por desconto depende de índice GIN e de expressões específicas do PostgreSQL |
| **Risco** | Alto — abre mão de validação relacional em dados financeiros com domínio já sabidamente inconsistente |
| **Esforço** | Baixo |

---

## Decisão

A equipe escolhe a **Opção 2 — `@OneToMany` com a entidade `PaymentDiscount`**, por ser mais gerenciável e permitir auditar cada desconto isoladamente.

`PaymentDiscount` pertence ao agregado `Payment` e **não possui repositório público exposto a outros contextos**: o acesso de escrita ocorre sempre pela raiz do agregado, preservando a regra de que apenas o contexto de Pagamento de Benefícios grava em `PAYMENT`.

---

## Consequências

### Positivas

- REQ-007 e REQ-008 podem ser implementados e testados por desconto, refletindo a iteração `FOR #INDEX = 1 TO C*GRP-DISC` do legado.
- A divergência de domínio de `TYPE-DISC` entre `PAYMENT.ddm:L47` e `CALCDSCT.NSP:L127-L167` pode ser quantificada nos dados migrados com uma consulta agregada, sem SQL nativo.
- A trilha de auditoria de REQ-014 pode referenciar um identificador estável de desconto quando a equipe decidir descer a esse nível.
- Alterações pontuais de desconto na correção retroativa não regravam as demais ocorrências.

### Negativas

- A migração precisa gerar chave primária para cada ocorrência de desconto de 612 milhões de pagamentos; a regra de geração deve ser determinística para permitir reprocessamento idempotente.
- A entidade precisa de proteção explícita (construtor de pacote e ausência de repositório público) para que nenhum código crie um desconto fora do agregado `Payment`.
- A leitura da folha ganha um join; a consulta de geração deve usar `join fetch` para não incorrer em N+1 ao varrer a competência.
- O modelo passa a permitir mais de 8 descontos por pagamento, limite que o Adabas garantia por estrutura; a restrição precisa ser reafirmada na aplicação ou por constraint.

---

## Requisitos relacionados

- `REQ-007` — Limitar o total de descontos a 30% do valor bruto
- `REQ-008` — Isentar desconto judicial do limite de 30%
- `REQ-006` — Truncar valores monetários em duas casas decimais
- `REQ-014` — Registrar trilha de auditoria em toda alteração de dados

---

## Questões em aberto que esta decisão não resolve

| Questão | Evidência | Status |
|---|---|---|
| Qual é o domínio real de `TYPE-DISC`: 2 caracteres do DDM ou 1 caractere do código? | `PAYMENT.ddm:L47`, `CALCDSCT.NSP:L127-L167` | aberta |
| REQ-007 e REQ-008 aplicam-se durante a geração da folha ou apenas em execução avulsa? | `BATCHPGT.NSP:L20`, `CALCDSCT.NSP:L74` (sem chamador no corpus) | aberta |

---

### Continue lendo

| Anterior | Próximo |
|---|---|
| [Contextos delimitados](../bounded-contexts.md)<br/><sub>Mapa aceito pela equipe.</sub> | [Especificação 001](../../specs/001-benefit-calculation/spec.md)<br/><sub>Requisitos afetados.</sub> |

<sub>[Voltar ao índice do kit](../../README.md)</sub>
