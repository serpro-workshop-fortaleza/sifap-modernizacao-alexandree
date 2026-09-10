#!/usr/bin/env python3
"""Cruza os shards de regras de negócio do Estágio 1 e classifica as sobreposições.

Cada subagente `@archaeologist` escreve um shard exclusivo em
`01-archaeology/rules/<PROGRAMA>.rules.json`. Nenhum shard enxerga os demais, de
modo que uma regra sobre `BENEFIC.IDADE` extraída de um programa pode contradizer
— ou esclarecer — uma regra extraída de outro. Este script faz o join
determinístico entre shards para que o julgamento do modelo recaia apenas sobre o
conjunto reduzido de pares que um critério objetivo não resolve.

Critério
--------
Dois pares só são comparados quando vêm de programas diferentes e compartilham ao
menos um campo de condição. A partir daí:

- `ESCLARECE`  uma das regras é um mistério e a outra cobre o mesmo campo e literal
- `DUPLICATA`  mesmo campo, mesmo literal e mesma ação
- `CONFLITO`   mesmo campo, mesmo literal e ações divergentes
- `PENDENTE`   campo em comum sem literal em comum; exige julgamento semântico

A verificação de mistério vem antes da comparação de ações: a ação de uma regra
não confirmada não é base para declarar divergência.

Restrições de projeto
---------------------
- Python 3.11+, somente biblioteca padrão, como o validador de primitivas. A CI
  não deve precisar de `pip install`; por isso o shard é JSON, e não YAML.
- Encerra sempre com código 0. Este script roda como hook `subagentStop` e uma
  falha aqui não pode derrubar a sessão da equipe.
- A saída não carrega horário de parede. Ela registra o digest de cada shard, de
  modo que rodar duas vezes sobre os mesmos shards produz um arquivo idêntico.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import re
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
DEFAULT_RULES_DIR = REPO_ROOT / "01-archaeology" / "rules"
CONFLICTS_FILENAME = "_conflicts.json"
VERDICTS = ("CONFLITO", "ESCLARECE", "DUPLICATA", "PENDENTE")

WHITESPACE_RE = re.compile(r"\s+")
LEADING_ZEROS_RE = re.compile(r"^0+(?=\d)")


def normalize_field(value: str) -> str:
    return WHITESPACE_RE.sub("", str(value)).upper()


def normalize_literal(value: str) -> str:
    text = str(value).strip().strip("'\"").strip().upper()
    return LEADING_ZEROS_RE.sub("", text) if text.isdigit() else text


def normalize_action(value: str) -> str:
    return WHITESPACE_RE.sub(" ", str(value or "").strip().lower()).rstrip(".;")


def load_shards(rules_dir: Path) -> tuple[list[dict], list[dict], list[str]]:
    """Retorna (regras achatadas, digests dos shards, problemas encontrados)."""
    rules: list[dict] = []
    digests: list[dict] = []
    problems: list[str] = []
    for path in sorted(rules_dir.glob("*.rules.json")):
        if path.name.startswith("_"):
            continue
        raw = path.read_bytes()
        try:
            data = json.loads(raw.decode("utf-8"))
        except (json.JSONDecodeError, UnicodeDecodeError) as exc:
            problems.append(f"{path.name}: JSON inválido ({exc})")
            continue
        program = data.get("program")
        entries = data.get("rules")
        if not isinstance(program, str) or not isinstance(entries, list):
            problems.append(
                f"{path.name}: esquema inválido; exige 'program' (texto) e 'rules' (lista)")
            continue
        digests.append({
            "file": path.name,
            "digest": hashlib.sha256(raw).hexdigest()[:16],
        })
        for entry in entries:
            if not isinstance(entry, dict) or not entry.get("id"):
                problems.append(f"{path.name}: regra sem 'id'; ignorada")
                continue
            fields = {normalize_field(f)
                      for f in entry.get("fields") or [] if str(f).strip()}
            literals = {normalize_literal(
                v) for v in entry.get("literals") or [] if str(v).strip()}
            rules.append({
                "id": entry["id"],
                "program": program,
                "source": entry.get("source", ""),
                "statement": entry.get("statement", ""),
                "classification": str(entry.get("classification", "")).strip().lower(),
                "action_key": normalize_action(entry.get("action")),
                "fields": fields,
                "literals": literals,
            })
    return rules, digests, problems


def classify(first: dict, second: dict, shared_literals: set[str]) -> str:
    if not shared_literals:
        return "PENDENTE"
    if "misterio" in (first["classification"], second["classification"]):
        return "ESCLARECE"
    if first["action_key"] and first["action_key"] == second["action_key"]:
        return "DUPLICATA"
    return "CONFLITO"


def crosscheck(rules: list[dict]) -> list[dict]:
    pairs: list[dict] = []
    ordered = sorted(rules, key=lambda rule: (rule["program"], rule["id"]))
    for index, first in enumerate(ordered):
        for second in ordered[index + 1:]:
            if first["program"] == second["program"]:
                continue
            shared_fields = first["fields"] & second["fields"]
            if not shared_fields:
                continue
            shared_literals = first["literals"] & second["literals"]
            pairs.append({
                "verdict": classify(first, second, shared_literals),
                "a": {"id": first["id"], "program": first["program"], "source": first["source"]},
                "b": {"id": second["id"], "program": second["program"], "source": second["source"]},
                "fields": sorted(shared_fields),
                "literals": sorted(shared_literals),
            })
    return sorted(pairs, key=lambda pair: (VERDICTS.index(pair["verdict"]), pair["a"]["id"], pair["b"]["id"]))


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--rules-dir", default=str(DEFAULT_RULES_DIR),
                        help="diretório dos shards (padrão: 01-archaeology/rules)")
    args = parser.parse_args()

    rules_dir = Path(args.rules_dir)
    if not rules_dir.is_dir():
        print(
            f"[rules-crosscheck] {rules_dir} não existe; nada a cruzar.", file=sys.stderr)
        return 0

    rules, digests, problems = load_shards(rules_dir)
    for problem in problems:
        print(f"[rules-crosscheck] {problem}", file=sys.stderr)

    pairs = crosscheck(rules)
    summary = {verdict: sum(
        1 for pair in pairs if pair["verdict"] == verdict) for verdict in VERDICTS}
    payload = {
        "shards": digests,
        "rules_indexed": len(rules),
        "summary": summary,
        "problems": problems,
        "pairs": pairs,
    }
    output = rules_dir / CONFLICTS_FILENAME
    rendered = json.dumps(payload, ensure_ascii=False, indent=2) + "\n"
    # Roda também como postToolUse, portanto pode disparar muitas vezes por sessão:
    # só toca o disco quando o resultado muda, para não poluir o diff da equipe.
    if not output.is_file() or output.read_text(encoding="utf-8") != rendered:
        output.write_text(rendered, encoding="utf-8")

    print(
        f"[rules-crosscheck] {len(digests)} shard(s), {len(rules)} regra(s): "
        + ", ".join(f"{verdict}={summary[verdict]}" for verdict in VERDICTS)
        + f" -> {output.relative_to(REPO_ROOT) if output.is_relative_to(REPO_ROOT) else output}"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
