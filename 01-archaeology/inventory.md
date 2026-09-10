# Inventário do Legado — Time `<preencher>`

> **Trilha:** [Kit do Time](../README.md) › [Estágio 1](README.md) › **Inventário**

**Primeiro artefato do Estágio 1.** Varra a estrutura e conte os arquivos sem abrir nenhum programa — use apenas os nomes de arquivo e a estrutura de pastas.

| Campo | Valor |
|---|---|
| **Público-alvo** | Dupla responsável pela varredura inicial |
| **Pré-requisitos** | Acesso ao diretório `legacy-sifap/` |
| **Estágio** | Estágio 1 — Arqueologia, Passo 1 |
| **Resultado esperado** | Contagens corretas, padrões de nomenclatura identificados e 3 itens estranhos sinalizados |

> [!NOTE]
> Monte este inventário sem abrir nenhum programa. Trabalhe apenas com nomes de arquivo e estrutura de pastas. Ele será revisado à medida que o time extrai regras, mapeia dependências e registra mistérios.

**Data:** 2026-09-10
**Dupla responsável:** <!-- preencher -->
**Caminho varrido:** `01-archaeology/legacy-sifap/`

> [!IMPORTANT]
> Esta é a **primeira varredura**, feita apenas sobre nomes de arquivo e estrutura de pastas. Nenhum programa foi aberto. Toda coluna "hipótese" é não confirmada e deve ser revisada durante `/extract-business-rules` e `/map-dependencies`.

---

## Estrutura de pastas

```text
01-archaeology/legacy-sifap/
├── README.md
├── HOW-TO-READ-NATURAL.md
├── adabas-ddms/
│   ├── README.md
│   ├── AUDIT.ddm
│   ├── BENEFIC.ddm
│   ├── PAYMENT.ddm
│   ├── SOCPROG.ddm
│   └── FDT-150-BENEFICIARY.txt
├── legacy-docs/
│   ├── README.md
│   ├── ORIGINAL-ARCHITECTURE-1997.md
│   ├── ORIGINAL-ARCHITECTURE-1997.docx
│   ├── TECHNICAL-MANUAL-SIFAP-2008.md
│   ├── TECHNICAL-MANUAL-SIFAP-2008.docx
│   ├── BUSINESS-RULES-2012.md
│   └── BUSINESS-RULES-2012.docx
└── natural-programs/
    ├── README.md
    ├── BATCHCON.NSP   BATCHPGT.NSP   BATCHREL.NSP
    ├── CADBENEF.NSP   CADDEPEN.NSP   CADPROG.NSP
    ├── CALCCORR.NSP   CALCDSCT.NSP   CONSBENF.NSP
    ├── RELAUDIT.NSP   RELPGT.NSP     VALDOCS.NSP
    ├── CALCBENF.NSN   VALBENEF.NSN   VALELEG.NSN
    ├── SUBVALCP.NSN   SUBVALNI.NSN
    ├── CCAUDIT.NSC    CCVALCPF.NSC
    ├── PDACALC.NSA    PDAVALID.NSA
    ├── LDASIFAP.NSL
    └── SIFAPJ01.jcl   SIFAPJ02.jcl
```

**Diretórios:** 1 raiz + 3 subdiretórios (`adabas-ddms/`, `legacy-docs/`, `natural-programs/`). Nenhum aninhamento além do segundo nível.
**Total de arquivos:** 40.

---

## Contagem de arquivos por tipo

| Extensão | Contagem | Finalidade provável |
|---|---|---|
| `.NSP` | 12 | Programa Natural (unidade executável, ponto de entrada) |
| `.NSN` | 5 | Subprograma Natural (invocado por `CALLNAT`) |
| `.NSC` | 2 | Copycode (fragmento compartilhado via `INCLUDE`) |
| `.NSA` | 2 | Parameter Data Area (PDA) — contrato de parâmetros de `CALLNAT` |
| `.NSL` | 1 | Local Data Area (LDA) — variáveis locais compartilhadas |
| `.jcl` | 2 | Job Control Language — agendamento/execução batch |
| `.ddm` | 4 | Data Definition Module (visão Natural de um arquivo Adabas) |
| `.txt` | 1 | Listagem de FDT (Field Definition Table) do Adabas |
| `.docx` | 3 | Documentação histórica (binário, não pesquisável por `grep`) |
| `.md` | 8 | READMEs do kit + transcrições Markdown da documentação histórica |

**Membros Natural (código + áreas de dados + JCL):** 24.

Verificação independente por outra pessoa da dupla:

```bash
find 01-archaeology/legacy-sifap -type f | wc -l
find 01-archaeology/legacy-sifap -type f | sed 's/.*\.//' | sort | uniq -c | sort -rn
```

---

## Padrões da convenção de nomes

Os nomes têm 8 caracteres (limite clássico de membro Natural) e usam **prefixo funcional**, não delimitadores. Dois eixos coexistem: o prefixo sugere a função e a extensão sugere o tipo de módulo.

| Prefixo | Contagem | Hipótese de domínio (não confirmada) |
|---|---|---|
| `BATCH*` | 3 | Pontos de entrada batch — `BATCHPGT`, `BATCHCON`, `BATCHREL` |
| `CAD*` | 3 | Cadastro/manutenção — `CADBENEF`, `CADDEPEN`, `CADPROG` |
| `CALC*` | 3 | Cálculo — `CALCBENF`, `CALCCORR`, `CALCDSCT` |
| `VAL*` | 3 | Validação — `VALBENEF`, `VALDOCS`, `VALELEG` |
| `SUBVAL*` | 2 | Subvalidações reutilizáveis (`.NSN`) — `SUBVALCP`, `SUBVALNI` |
| `REL*` | 2 | Relatórios — `RELAUDIT`, `RELPGT` |
| `CC*` | 2 | Copycode (`.NSC`) — prefixo marca o **tipo**, não o domínio |
| `PDA*` | 2 | Parameter Data Area (`.NSA`) — prefixo marca o **tipo** |
| `LDA*` | 1 | Local Data Area (`.NSL`) — prefixo marca o **tipo** |
| `CONS*` | 1 | Consulta — `CONSBENF` |
| `SIFAPJ*` | 2 | Jobs JCL numerados do sistema — `SIFAPJ01`, `SIFAPJ02` |

Sufixos recorrentes sugerem entidades compartilhadas entre módulos: `*BENF`/`*BENEF` (4 arquivos), `*PGT` (2), `*AUDIT` (2), `*CP`/`*CPF` (2). Confirmar contra os DDMs antes de tratar como entidade.

---

## Itens estranhos (top 3)

| # | Caminho do arquivo | O que o torna estranho | Investigação sugerida |
|---|---|---|---|
| 1 | [legacy-sifap/adabas-ddms/FDT-150-BENEFICIARY.txt](legacy-sifap/adabas-ddms/FDT-150-BENEFICIARY.txt) | Única extensão `.txt` da árvore e único nome que carrega um número (`150`) e usa inglês. O número provavelmente é o FNR do arquivo Adabas — mas só 1 dos 4 DDMs tem FDT correspondente. | Ler junto com `BENEFIC.ddm` e checar por que os outros três não têm FDT; se o FNR não bater, vira mistério. |
| 2 | Nenhum arquivo `.NSM` em [legacy-sifap/natural-programs](legacy-sifap/natural-programs) | **Ausência**: existem programas com cara de online (`CAD*`, `CONS*`), mas nenhuma tela MAP. Ou as telas foram perdidas, ou os `INPUT` são inline. | Ao abrir `CADBENEF.NSP`, verificar se há `INPUT USING MAP` (tela ausente) ou `INPUT` inline. |
| 3 | [legacy-sifap/legacy-docs](legacy-sifap/legacy-docs) — 3 pares `.docx` + `.md` | Cada documento existe em binário e em Markdown, com datas distintas (1997, 2008, 2012). Duas fontes podem divergir e a versão binária não é pesquisável. | Tratar o `.md` como fonte de trabalho e o `.docx` como referência; qualquer divergência de regra entre 1997/2008/2012 vira candidato a mistério. |

> **Observação adicional:** `LDASIFAP.NSL` é a única LDA e leva o nome do sistema — indício de área de dados compartilhada por vários programas. Se confirmado, é o melhor ponto único para entender o vocabulário de dados do sistema.

---

## Ordem de leitura proposta

> Hipótese baseada só em nomes e extensões. Reordene assim que `/map-dependencies` revelar as arestas reais de `CALLNAT`/`INCLUDE`.

1. **Dados antes de código** — `adabas-ddms/*.ddm` (4) + `FDT-150-BENEFICIARY.txt`. Definem entidades e descritores usados por todo o resto.
2. **Contratos compartilhados** — `LDASIFAP.NSL`, `PDACALC.NSA`, `PDAVALID.NSA`, `CCAUDIT.NSC`, `CCVALCPF.NSC`. Áreas de dados e copycodes são lidos por muitos módulos; entendê-los antes evita releitura.
3. **Pontos de entrada batch** — `SIFAPJ01.jcl`, `SIFAPJ02.jcl` e depois `BATCHPGT.NSP`, `BATCHCON.NSP`, `BATCHREL.NSP`. O JCL nomeia quem inicia o fluxo.
4. **Núcleo de regra (prováveis mais conectados)** — `CALC*.NSN`/`.NSP` e `VAL*.NSN`, depois `SUBVALCP.NSN` e `SUBVALNI.NSN`. Subprogramas `.NSN` são, por definição, alvos de `CALLNAT` e tendem a ser os nós de maior grau.
5. **Fluxos de usuário e saída** — `CAD*.NSP`, `CONSBENF.NSP`, `RELPGT.NSP`, `RELAUDIT.NSP`, `VALDOCS.NSP`.
6. **Documentação histórica** — `legacy-docs/*.md`, por último e só para confrontar hipóteses; o código é a fonte de verdade.

O [README de natural-programs](legacy-sifap/natural-programs/README.md) divide os membros entre atribuídos e de apoio — cruze esta ordem com a atribuição da sua dupla antes de começar.

---

## Definição de pronto

- [x] O inventário existe com contagens corretas.
- [x] 3 padrões de nomenclatura ou mais identificados (11 padrões).
- [x] 3 itens estranhos sinalizados.

---

### Continue lendo

| Anterior | Próximo |
|---|---|
| [GUIDE do Estágio 1](GUIDE.md)<br/><sub>Cronograma passo a passo.</sub> | [Catálogo de Regras](business-rules-catalog.md)<br/><sub>Passo 2 — extração de regras.</sub> |

<sub>[Voltar ao índice do kit](../README.md)</sub>
