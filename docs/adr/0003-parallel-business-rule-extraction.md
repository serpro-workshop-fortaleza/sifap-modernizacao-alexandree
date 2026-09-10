# ADR-0003: Extração paralela de regras de negócio com shard por programa

> **Trilha:** [Kit do Time](../../README.md) › [Documentação](../README.md) › [ADRs](README.md) › **ADR-0003**

| Campo | Valor |
|---|---|
| **Status** | accepted |
| **Data** | 2026-09-10 |
| **Autores** | Especialista em Requisitos — dupla do Estágio 1 |
| **Substitui** | N/A |

---

## Contexto

O Estágio 1 extrai regras de negócio dos 15 membros Natural atribuídos em `01-archaeology/legacy-sifap/natural-programs/`. O prompt [`/extract-business-rules`](../../.github/prompts/stage-archaeologist-extract-business-rules.prompt.md) lê **um** programa por vez e acrescenta o resultado a [`business-rules-catalog.md`](../../01-archaeology/business-rules-catalog.md). Com cinco duplas trabalhando na mesma janela de tempo, esse fluxo tem dois limites.

**Limite 1 — serialização.** Ler 15 programas em sequência consome a maior parte do estágio. Os programas são independentes entre si na leitura: `CADBENEF.NSP` e `VALELEG.NSN` não precisam ser lidos na mesma sessão.

**Limite 2 — escrita concorrente.** O catálogo é um arquivo único. Duas sessões que o editem ao mesmo tempo sobrescrevem uma à outra, e o conflito só aparece no `git diff`, depois do trabalho perdido. Esse é o motivo real pelo qual a paralelização ingênua falha: o gargalo não é a leitura, é o destino compartilhado da escrita.

Há ainda um terceiro problema, que só surge **por causa** da paralelização: uma dupla que lê `CALCBENF.NSN` pode declarar uma regra sobre `BENEFIC.IDADE` que contradiz — ou esclarece — uma regra que outra dupla já declarou a partir de `VALELEG.NSN`. Sem cruzamento explícito, essas contradições chegam intactas ao Estágio 2 e viram requisitos EARS conflitantes.

## Decisão

Adotaremos um **agente orquestrador** (`archaeologist-orchestrator`) que particiona a extração por membro Natural e despacha um subagente `@archaeologist` por programa.

Três regras sustentam o desenho:

1. **Disjunção de escrita.** Um programa corresponde a exatamente um shard e a exatamente um subagente. Cada subagente escreve somente em `01-archaeology/rules/<PROGRAMA>.rules.json`. Nenhum subagente toca o catálogo durante o fan-out.
2. **Cruzamento determinístico antes do julgamento semântico.** Um hook `subagentStop` executa um join por campo e literal entre os shards e classifica cada sobreposição como `CONFLITO`, `ESCLARECE`, `DUPLICATA` ou `PENDENTE`. Só os pares `PENDENTE` chegam ao modelo.
3. **Merge serial.** O catálogo é regenerado por um script, em um único passo, a partir dos shards.

O shard usa **JSON**, não YAML, porque os scripts de cruzamento e merge herdam a restrição declarada em [`validate-copilot-primitives.py`](../../.github/scripts/validate-copilot-primitives.py): Python 3.11 com biblioteca padrão apenas, sem `pip install` na integração contínua.

## Alternativas consideradas

| Alternativa | Por que foi rejeitada |
|---|---|
| Manter só o fluxo sequencial de um programa por sessão | Não resolve o custo de tempo do estágio e continua expondo o catálogo à escrita concorrente quando duas duplas trabalham em paralelo, que é o que de fato acontece. |
| Paralelizar mantendo o catálogo como destino de escrita | É a falha que este ADR existe para evitar. Escritas concorrentes em um arquivo Markdown único se sobrescrevem sem aviso; o Git só acusa o problema depois da perda. |
| Despachar subagentes sem índice compartilhado das regras já extraídas | Cada shard ficaria cego para o restante do sistema. Regras que atravessam programas (`BATCHPGT.NSP` chama `CALCBENF.NSN`) nunca seriam percebidas, e duplicatas entrariam no catálogo como regras distintas. |
| Fazer o cruzamento apenas por julgamento do modelo, sem join determinístico | Exigiria carregar todos os shards no contexto a cada verificação, com custo crescente e resultado não reproduzível. O join por campo e literal reduz o conjunto antes do julgamento e é auditável. |
| Shard em YAML | Obrigaria um analisador YAML artesanal nos scripts, já que o repositório proíbe dependências externas na integração contínua. JSON preserva as mesmas propriedades desejadas: um arquivo por programa, legível por máquina e classificado como fonte pelo portal, portanto fora do portão de tradução. |

## Consequências

- **Mais fácil:** ler os 15 programas em uma fração do tempo; detectar contradição entre duplas antes do Estágio 2; auditar a origem de cada regra, já que o shard preserva `arquivo:linha` por regra.
- **Mais difícil:** existe agora um contrato de dados a manter (o esquema do shard) e um passo de merge que não existia. Uma regra escrita à mão diretamente no catálogo é sobrescrita pelo próximo merge.
- **Riscos:** o evento `subagentStop` aparece na lista de eventos aceitos pelo validador, mas não está documentado em [`PRIMITIVE-STANDARD.md`](../../.github/PRIMITIVE-STANDARD.md); pode não disparar em toda versão do editor. Um subagente pode ignorar a instrução de escopo e escrever fora do seu shard.
- **Mitigações:** o hook declara também `postToolUse` como rede de segurança, e o prompt `/crosscheck-rules` executa o mesmo cruzamento sob demanda, de modo que a corretude não depende do hook. O merge só reescreve o trecho entre marcadores `rules:begin` e `rules:end`, preservando a prosa do catálogo. O orquestrador aborta quando dois shards apontam para o mesmo arquivo de saída.

## Relacionados

- REQ-IDs: N/A — o Estágio 1 antecede a especificação formal.
- ADRs: N/A
- Arquivos-fonte do legado: `01-archaeology/legacy-sifap/natural-programs/*.{NSP,NSN}`
- Artefatos afetados: [`business-rules-catalog.md`](../../01-archaeology/business-rules-catalog.md), [`mysteries-found.md`](../../01-archaeology/mysteries-found.md)

## Referências

- Padrão de primitivas do Copilot, incluindo o esquema de hooks: [`PRIMITIVE-STANDARD.md`](../../.github/PRIMITIVE-STANDARD.md)
- Divisão entre membros atribuídos e de apoio: [`natural-programs/README.md`](../../01-archaeology/legacy-sifap/natural-programs/README.md)
- Formato canônico das perguntas em aberto: [`mysteries-checklist.md`](../../01-archaeology/mysteries-checklist.md)

---

### Continue lendo

| Anterior | Próximo |
|---|---|
| [ADR-0002](0002-trilingual-documentation-portal.md)<br/><sub>Portal de documentação trilíngue.</sub> | [ADRs — Índice](README.md)<br/><sub>Índice das decisões registradas.</sub> |

<sub>[Voltar ao índice do kit](../../README.md)</sub>
