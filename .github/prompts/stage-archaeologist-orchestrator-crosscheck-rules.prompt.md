---
name: "crosscheck-rules"
description: "Cruza os shards de regras já extraídos, classifica as sobreposições entre programas e propõe as perguntas em aberto decorrentes."
argument-hint: "scope=01-archaeology/rules/ (opcional)"
agent: "archaeologist-orchestrator"
tools: ["read", "edit", "execute"]
---
# /crosscheck-rules

## Objetivo

Detectar contradições, duplicatas e esclarecimentos entre regras extraídas de programas diferentes, sem depender de o hook `rules-crosscheck` ter disparado.

## Quando usar

Depois de um lote de `/fanout-rules`, quando o hook não tiver rodado; depois de acrescentar um shard isolado com `/extract-business-rules`; ou antes de fechar o Estágio 1, como verificação final do catálogo.

## Pré-condições

- Existem ao menos dois arquivos em `01-archaeology/rules/*.rules.json`
- Os shards seguem o esquema documentado em [`archaeologist-orchestrator`](../agents/archaeologist-orchestrator.agent.md)

## Entradas que a equipe deve fornecer

- `scope=` — opcional; o diretório de shards, quando diferente de `01-archaeology/rules/`

## O que farei

- Executarei o cruzamento determinístico entre todos os shards
- Apresentarei a tabela de veredictos com as duas evidências de cada par
- Julgarei apenas os pares marcados como `PENDENTE`
- Proporei a linha de `mysteries-found.md` para cada `CONFLITO`

## O que não farei

- Reler os programas Natural; trabalho apenas sobre os shards
- Escrever em `01-archaeology/mysteries-found.md` ou alterar o status de um mistério existente
- Decidir qual das duas regras divergentes está correta
- Editar um shard para acomodar o outro
- Reclassificar uma regra `inferida` como `confirmada`

## Formato da saída

`01-archaeology/rules/_conflicts.json`, gerado pelo script:

```json
{
  "generated_at": "2026-09-10T00:00:00Z",
  "summary": {"CONFLITO": 1, "ESCLARECE": 0, "DUPLICATA": 2, "PENDENTE": 4},
  "pairs": [
    {
      "verdict": "CONFLITO",
      "a": {"id": "VALELEG-R01", "program": "VALELEG.NSN", "source": "VALELEG.NSN:L88-L97"},
      "b": {"id": "CALCBENF-R04", "program": "CALCBENF.NSN", "source": "CALCBENF.NSN:L61-L70"},
      "fields": ["BENEFIC.IDADE"],
      "literals": ["65"]
    }
  ]
}
```

Mais, em conversa, a tabela de veredictos e as linhas propostas:

```markdown
| ID | Questão em aberto | Evidência (`path:line`) | Impacto | Hipótese (não confirmada) | Pessoa/área responsável | Status |
|---|---|---|---|---|---|---|
| `BONUS` | Por que a idade limite difere entre validação e cálculo? | `VALELEG.NSN:88` e `CALCBENF.NSN:61` | Regra de elegibilidade ambígua no Java | Uma das faixas pode ser resíduo de versão anterior | <!-- preencher --> | aberta |
```

## Definição de pronto

- [ ] `_conflicts.json` reflete o estado atual de todos os shards
- [ ] Nenhum par permanece com veredicto `PENDENTE`
- [ ] Cada `CONFLITO` tem uma linha proposta com as sete colunas e status `aberta`
- [ ] Cada `DUPLICATA` indica qual regra fica no catálogo e qual vira `source` adicional
- [ ] Nenhum shard foi modificado por este comando

## Corpo do prompt

Você é `@archaeologist-orchestrator`. A equipe quer cruzar os shards já existentes.

**Etapa 1 — Executar o cruzamento determinístico.**
Rode `python3 .github/scripts/rules_crosscheck.py`. Ele carrega todos os shards, ignora os arquivos iniciados por `_`, normaliza `fields` e `literals` e gera os pares de regras de programas diferentes com campo em comum. Se o script relatar shard com esquema inválido, aponte o arquivo e pare: um shard malformado é invisível para o cruzamento.

**Etapa 2 — Ler os veredictos determinísticos.**
Abra `01-archaeology/rules/_conflicts.json`. Os veredictos `CONFLITO`, `ESCLARECE` e `DUPLICATA` já vêm decididos por critério objetivo; não os revise. Apresente-os na tabela de veredictos com as duas evidências.

**Etapa 3 — Julgar somente os pares `PENDENTE`.**
Um par `PENDENTE` compartilha campo, mas não literal. Compare os dois `statement` e as duas `action` e classifique: `CONFLITO`, quando as ações forem incompatíveis para condições que podem coexistir; `ESCLARECE`, quando uma regra explique um literal que a outra deixou como mistério; `DUPLICATA`, quando forem a mesma regra vista de dois programas; `INDEPENDENTE`, quando o campo comum for coincidência de nome. Cite `arquivo:linha` das duas regras em toda classificação.

**Etapa 4 — Propor as perguntas em aberto.**
Para cada `CONFLITO`, monte a linha de `01-archaeology/mysteries-found.md` com as sete colunas na ordem do arquivo. Use um ID canônico `SIFAP-M-NN` da faixa da dupla, conforme `01-archaeology/mysteries-checklist.md`, ou o literal `BONUS` quando o achado estiver fora da lista canônica. A questão precisa ser uma pergunta real terminada em ponto de interrogação, a evidência precisa ser `path:line`, a hipótese precisa estar marcada como não confirmada e o status precisa ser `aberta`. Apresente as linhas e peça que a dupla as registre; não edite o arquivo.

**Etapa 5 — Encaminhar as duplicatas.**
Para cada `DUPLICATA`, indique qual regra permanece no catálogo e registre o programa da outra como `source` adicional no shard vencedor, sem apagar o shard perdedor.

Não conclua um mistério. Um cruzamento produz perguntas melhores, não respostas.

## Exemplo de chamada

```text
/crosscheck-rules
```
