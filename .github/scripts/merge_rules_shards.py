#!/usr/bin/env python3
"""Consolida os shards de regras do Estágio 1 no catálogo de regras de negócio.

O fan-out grava um shard por programa em `01-archaeology/rules/`. Este script é o
passo serial que os transforma em subseções de
`01-archaeology/business-rules-catalog.md`, evitando que vários subagentes editem
o mesmo arquivo Markdown ao mesmo tempo.

A escrita é confinada a um único bloco delimitado por marcadores HTML:

    <!-- rules:begin -->  ... subseções por programa ...  <!-- rules:end -->

Todo o restante do catálogo é preservado byte a byte, inclusive as seções que a
equipe escreveu à mão. Uma regra editada manualmente dentro do bloco é perdida no
próximo merge; ali o shard é a fonte da verdade.

Restrições de projeto
---------------------
- Python 3.11+, somente biblioteca padrão, como o validador de primitivas.
- Determinístico: mesma entrada produz o mesmo arquivo. `--check` encerra com
  código 1 quando o catálogo está defasado em relação aos shards.
"""

from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
DEFAULT_RULES_DIR = REPO_ROOT / "01-archaeology" / "rules"
DEFAULT_CATALOG = REPO_ROOT / "01-archaeology" / "business-rules-catalog.md"

RULES_BLOCK = ("<!-- rules:begin -->", "<!-- rules:end -->")

EARS_LABELS = {
    "ubiqua": "Ubíqua",
    "event-driven": "Orientada a evento",
    "state-driven": "Orientada a estado",
    "optional": "Opcional",
    "unwanted": "Indesejada",
}
CLASSIFICATION_LABELS = {
    "confirmada": "Confirmada",
    "inferida": "Inferida",
    "misterio": "Mistério",
}


def escape_cell(value: object) -> str:
    """Neutraliza o que quebraria uma célula de tabela GFM."""
    text = str(value or "").replace("|", "\\|").replace("\n", " ").strip()
    return text or "—"


def load_shards(rules_dir: Path) -> list[dict]:
    shards: list[dict] = []
    for path in sorted(rules_dir.glob("*.rules.json")):
        if path.name.startswith("_"):
            continue
        data = json.loads(path.read_text(encoding="utf-8"))
        if not isinstance(data.get("program"), str) or not isinstance(data.get("rules"), list):
            raise SystemExit(
                f"Shard com esquema inválido: {path.name}. Exige 'program' (texto) e 'rules' (lista).")
        shards.append(data)
    return sorted(shards, key=lambda shard: shard["program"])


def render_rules(shards: list[dict]) -> str:
    if not shards:
        return ("> [!NOTE]\n> Nenhum shard em [`rules/`](rules/) ainda. "
                "Execute `/fanout-rules` para gerar o primeiro lote.")
    blocks: list[str] = []
    for shard in shards:
        lines = [
            f"### Regras de `{shard['program']}` (shard)",
            "",
            "| # | Enunciado da regra | Candidato EARS | Origem | Classificação | Notas |",
            "|---|---|---|---|---|---|",
        ]
        for position, rule in enumerate(shard["rules"], start=1):
            ears = EARS_LABELS.get(
                str(rule.get("ears", "")).strip().lower(), rule.get("ears"))
            classification = CLASSIFICATION_LABELS.get(
                str(rule.get("classification", "")).strip().lower(), rule.get("classification"))
            note = rule.get("doc_ref") or (
                "Registrada em `mysteries-found.md`"
                if str(rule.get("classification", "")).strip().lower() == "misterio"
                else ""
            )
            lines.append(
                f"| {position} | {escape_cell(rule.get('statement'))} | {escape_cell(ears)} | "
                f"`{escape_cell(rule.get('source'))}` | {escape_cell(classification)} | {escape_cell(note)} |"
            )
        blocks.append("\n".join(lines))
    blocks.append(render_totals(shards))
    return "\n\n".join(blocks)


def render_totals(shards: list[dict]) -> str:
    rules = [rule for shard in shards for rule in shard["rules"]]

    def count(classification: str) -> int:
        return sum(1 for rule in rules if str(rule.get("classification", "")).strip().lower() == classification)

    entities = {
        str(entity).strip()
        for rule in rules
        for entity in rule.get("entities") or []
        if str(entity).strip()
    }
    return "\n".join([
        "#### Totais dos shards",
        "",
        "| Métrica | Valor |",
        "|---|---:|",
        f"| Programas consolidados por shard | {len(shards)} |",
        f"| DDMs citados | {len(entities)} |",
        f"| Regras confirmadas | {count('confirmada')} |",
        f"| Regras inferidas | {count('inferida')} |",
        f"| Mistérios | {count('misterio')} |",
    ])


def replace_block(text: str, markers: tuple[str, str], body: str, catalog: Path) -> str:
    begin, end = markers
    pattern = re.compile(
        f"{re.escape(begin)}.*?{re.escape(end)}", re.DOTALL)
    if not pattern.search(text):
        raise SystemExit(
            f"Marcadores {begin} ... {end} ausentes em {catalog}. "
            "Insira-os no catálogo antes de consolidar."
        )
    return pattern.sub(lambda _: f"{begin}\n\n{body}\n\n{end}", text, count=1)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--rules-dir", default=str(DEFAULT_RULES_DIR))
    parser.add_argument("--catalog", default=str(DEFAULT_CATALOG))
    parser.add_argument("--check", action="store_true",
                        help="não escreve; encerra com 1 se o catálogo estiver defasado")
    args = parser.parse_args()

    rules_dir = Path(args.rules_dir)
    catalog = Path(args.catalog)
    if not rules_dir.is_dir():
        print(f"{rules_dir} não existe; nada a consolidar.", file=sys.stderr)
        return 0

    shards = load_shards(rules_dir)
    original = catalog.read_text(encoding="utf-8")
    updated = replace_block(original, RULES_BLOCK,
                            render_rules(shards), catalog)

    if args.check:
        if updated != original:
            print(
                f"{catalog.name} está defasado em relação aos shards. Execute o merge sem --check.")
            return 1
        print(f"{catalog.name} está em dia com {len(shards)} shard(s).")
        return 0

    if updated == original:
        print(f"{catalog.name} já estava em dia com {len(shards)} shard(s).")
        return 0

    catalog.write_text(updated, encoding="utf-8")
    total = sum(len(shard["rules"]) for shard in shards)
    print(
        f"{catalog.name} consolidado: {len(shards)} shard(s), {total} regra(s).")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
