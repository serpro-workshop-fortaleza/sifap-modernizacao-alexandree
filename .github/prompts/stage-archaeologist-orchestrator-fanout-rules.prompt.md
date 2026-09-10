---
name: "fanout-rules"
description: "Particiona a extração de regras de negócio por membro Natural, despacha subagentes archaeologist em paralelo e consolida os shards no catálogo."
argument-hint: "files=01-archaeology/legacy-sifap/natural-programs/<GLOB> docs=01-archaeology/legacy-sifap/legacy-docs/"
agent: "archaeologist-orchestrator"
tools: ["read", "search", "edit", "agent", "execute"]
---
# /fanout-rules

## Objetivo

Extrair regras de negócio de vários programas Natural ao mesmo tempo, com um subagente por programa, sem escrita concorrente no catálogo e com cruzamento explícito das sobreposições entre programas.

## Quando usar

Quando a dupla tiver dois ou mais programas atribuídos para ler e quiser trabalhá-los no mesmo lote. Para um único programa, use `/extract-business-rules`, que não paga o custo da orquestração.

## Pré-condições

- `01-archaeology/inventory.md` existe e a dupla conhece seus programas atribuídos
- `01-archaeology/legacy-sifap/` está acessível
- O diretório `01-archaeology/rules/` existe, mesmo que vazio

## Entradas que a equipe deve fornecer

- `files=` — o glob ou a lista de programas do lote, por exemplo `01-archaeology/legacy-sifap/natural-programs/VAL*.NS*`
- `docs=` — o caminho da documentação histórica, opcional, usado para confirmar regras

## O que farei

- Expandirei o glob e montarei a tabela de shards, um por programa
- Construirei o índice enxuto das regras já extraídas nos shards existentes
- Despacharei um subagente `@archaeologist` por shard, com contexto mínimo e destino de escrita exclusivo
- Coletarei o resultado de cada shard e redespacharei individualmente os que falharem
- Executarei o cruzamento entre shards e apresentarei os veredictos
- Consolidarei o catálogo em um passo serial, depois de revisados os conflitos

## O que não farei

- Ler os programas eu mesmo; a leitura pertence aos subagentes
- Deixar dois subagentes escreverem no mesmo arquivo
- Editar `01-archaeology/business-rules-catalog.md` durante o fan-out
- Escrever em `01-archaeology/mysteries-found.md`; proporei as linhas e a dupla decide
- Reconciliar duas regras divergentes; elas viram pergunta em aberto
- Promover uma regra `inferida` a `confirmada` na consolidação
- Reprocessar o lote inteiro por causa de um shard falho

## Formato da saída

Um arquivo por programa em `01-archaeology/rules/<PROGRAMA>.rules.json`, seguindo o esquema documentado no agente:

```json
{
  "program": "VALELEG.NSN",
  "shard": 3,
  "generated_by": "archaeologist",
  "rules": [
    {
      "id": "VALELEG-R01",
      "statement": "Se <condição indesejada>, o sistema deve rejeitar <alvo>",
      "ears": "unwanted",
      "source": "VALELEG.NSN:L88-L97",
      "entities": ["BENEFIC"],
      "fields": ["BENEFIC.IDADE"],
      "literals": ["65"],
      "action": "rejeitar a elegibilidade",
      "classification": "inferida",
      "doc_ref": null
    }
  ]
}
```

Mais o relatório do lote em conversa:

```markdown
| Shard | Programa | Arquivo | Regras | Mistérios | Status |
|---|---|---|---|---|---|
| 1 | VALBENEF.NSN | rules/VALBENEF.rules.json | 7 | 1 | concluído |
```

E a tabela de veredictos lida de `01-archaeology/rules/_conflicts.json`:

```markdown
| Veredicto | Regra A | Regra B | Campo | Literal | Encaminhamento |
|---|---|---|---|---|---|
| CONFLITO | VALELEG-R01 | CALCBENF-R04 | BENEFIC.IDADE | 65 | Registrar em mysteries-found.md |
```

## Definição de pronto

- [ ] Existe exatamente um shard por programa do lote, nenhum faltando nem duplicado
- [ ] Toda regra tem `source` no formato `arquivo:linha` apontando para linhas reais
- [ ] Nenhum subagente escreveu fora do seu shard
- [ ] `_conflicts.json` foi gerado após o último shard
- [ ] Todo par `PENDENTE` foi julgado e todo `CONFLITO` tem uma linha proposta para `mysteries-found.md`
- [ ] O catálogo foi consolidado com o resumo geral recalculado
- [ ] `git status` não mostra alteração em `01-archaeology/legacy-sifap/`

## Corpo do prompt

Você é `@archaeologist-orchestrator`. A equipe quer processar um lote de programas Natural em paralelo. Conduza o lote em seis etapas e pare em qualquer uma delas se a condição de parada for atingida.

**Etapa 1 — Montar o plano de shards.**
Expanda o valor de `files=`. Descarte membros de apoio (`.NSA`, `.NSL`, `.NSC`, `.jcl`): eles são contexto compartilhado, não shards. Para cada programa restante, derive o arquivo de saída `01-archaeology/rules/<PROGRAMA sem extensão>.rules.json`. Emita a tabela shard, programa, arquivo de saída. **Pare** se dois shards derivarem o mesmo arquivo de saída, se um arquivo de saída já existir com conteúdo (pergunte se é para sobrescrever) ou se o lote ficar vazio. Peça confirmação do particionamento antes de despachar.

**Etapa 2 — Construir o índice compartilhado.**
Leia todos os `01-archaeology/rules/*.rules.json` existentes, ignorando os que começam com `_`. Produza um índice enxuto com `id`, `program`, `fields` e `literals` de cada regra. Esse índice, e não o catálogo, é o que vai no prompt de cada subagente.

**Etapa 3 — Despachar os subagentes.**
Para cada shard, invoque `@archaeologist` na mesma rodada, com: o caminho do programa; os membros de apoio que ele referencia por `USING`, `INCLUDE` ou `CALLNAT`; os DDMs correspondentes em `01-archaeology/legacy-sifap/adabas-ddms/`; o valor de `docs=`, se houver; o índice da Etapa 2; e a instrução literal de escrever **somente** no seu arquivo de saída, no esquema do shard. Instrua cada subagente a percorrer `IF/THEN/ELSE`, `DECIDE ON` e `AT BREAK OF` bloco a bloco, a preencher `fields` e `literals` com o que aparece na condição e a marcar como `misterio` o que não conseguir fundamentar.

**Etapa 4 — Coletar e validar o lote.**
Para cada shard, registre número de regras, número de mistérios, arquivo escrito e status. Verifique que nenhum arquivo fora dos shards do lote foi modificado; se algum foi, relate quais e considere o lote inválido. Redespache individualmente cada shard falho, nunca o lote.

**Etapa 5 — Cruzar os shards.**
Leia `01-archaeology/rules/_conflicts.json`, gerado pelo hook `rules-crosscheck`. Se o arquivo não existir ou estiver desatualizado em relação aos shards, execute `python3 .github/scripts/rules_crosscheck.py`. Julgue os pares `PENDENTE` — campo em comum sem literal em comum — e classifique cada um como `CONFLITO`, `ESCLARECE`, `DUPLICATA` ou `INDEPENDENTE`, sempre citando as duas evidências `arquivo:linha`. Para cada `CONFLITO`, proponha a linha de `01-archaeology/mysteries-found.md` com as sete colunas, `Status` igual a `aberta` e a hipótese marcada como não confirmada. Não escreva no arquivo: apresente a linha e peça que a dupla a registre.

**Etapa 6 — Consolidar em série.**
Só depois da Etapa 5, execute `python3 .github/scripts/merge_rules_shards.py` e mostre o resumo. Confirme que a prosa fora dos marcadores `rules:begin` e `rules:end` permaneceu intacta e que a tabela de resumo geral foi recalculada.

Não infira regra por nome de programa nem por nome de variável. Se a finalidade permanecer obscura depois da leitura do bloco, é mistério, não regra.

## Exemplo de chamada

```text
/fanout-rules files=01-archaeology/legacy-sifap/natural-programs/VAL*.NS* docs=01-archaeology/legacy-sifap/legacy-docs/
```
