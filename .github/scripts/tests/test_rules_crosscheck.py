#!/usr/bin/env python3
"""Testes do cruzamento determinístico de shards de regras.

Executar: python3 .github/scripts/tests/test_rules_crosscheck.py

Os shards são criados em diretório temporário, e não versionados, para que o
`01-archaeology/rules/` da equipe nunca seja confundido com dado de teste.
"""

from __future__ import annotations

import importlib.util
import json
import tempfile
import unittest
from pathlib import Path

SCRIPT = Path(__file__).resolve().parents[1] / "rules_crosscheck.py"
spec = importlib.util.spec_from_file_location("rules_crosscheck", SCRIPT)
rules_crosscheck = importlib.util.module_from_spec(spec)
spec.loader.exec_module(rules_crosscheck)


def rule(rule_id, fields, literals, action, classification="inferida"):
    return {
        "id": rule_id,
        "statement": f"Regra {rule_id}",
        "ears": "unwanted",
        "source": f"{rule_id.split('-')[0]}.NSN:L10-L20",
        "entities": ["BENEFIC"],
        "fields": fields,
        "literals": literals,
        "action": action,
        "classification": classification,
        "doc_ref": None,
    }


class CrosscheckTest(unittest.TestCase):
    def setUp(self):
        self.directory = Path(tempfile.mkdtemp())
        self.write("VALELEG.NSN", [
            rule("VALELEG-R01", ["BENEFIC.IDADE"],
                 ["65"], "rejeitar a elegibilidade"),
            rule("VALELEG-R02", ["BENEFIC.RENDA"], ["218"],
                 "rejeitar a elegibilidade", "misterio"),
        ])
        self.write("CALCBENF.NSN", [
            rule("CALCBENF-R01", ["BENEFIC.IDADE"],
                 ["65"], "aplicar acréscimo de idoso"),
            rule("CALCBENF-R02", ["BENEFIC.RENDA"], ["218"], "limitar o valor"),
            rule("CALCBENF-R03", ["BENEFIC.IDADE"],
                 ["16"], "rejeitar a elegibilidade"),
        ])
        self.write("VALDOCS.NSP", [
            rule("VALDOCS-R01", [" benefic.idade "], ["'065'"],
                 "Rejeitar a elegibilidade."),
        ])

    def write(self, program, entries):
        payload = {"program": program, "shard": 1,
                   "generated_by": "test", "rules": entries}
        target = self.directory / f"{program.split('.')[0]}.rules.json"
        target.write_text(json.dumps(payload, ensure_ascii=False),
                          encoding="utf-8")

    def verdicts(self):
        rules, _, problems = rules_crosscheck.load_shards(self.directory)
        self.assertEqual([], problems)
        return {
            (pair["a"]["id"], pair["b"]["id"]): pair["verdict"]
            for pair in rules_crosscheck.crosscheck(rules)
        }

    def test_same_field_and_literal_with_divergent_actions_is_a_conflict(self):
        self.assertEqual("CONFLITO", self.verdicts()[
                         ("CALCBENF-R01", "VALELEG-R01")])

    def test_same_field_and_literal_with_the_same_action_is_a_duplicate(self):
        # Normalização: "benefic.idade" == "BENEFIC.IDADE" e "'065'" == "65".
        self.assertEqual("DUPLICATA", self.verdicts()[
                         ("VALDOCS-R01", "VALELEG-R01")])

    def test_a_mystery_covered_by_another_program_is_a_clarification(self):
        self.assertEqual("ESCLARECE", self.verdicts()[
                         ("CALCBENF-R02", "VALELEG-R02")])

    def test_shared_field_without_shared_literal_needs_judgement(self):
        self.assertEqual("PENDENTE", self.verdicts()[
                         ("CALCBENF-R03", "VALELEG-R01")])

    def test_rules_from_the_same_program_are_never_paired(self):
        pairs = self.verdicts()
        self.assertNotIn(("CALCBENF-R01", "CALCBENF-R03"), pairs)

    def test_malformed_shard_is_reported_and_skipped(self):
        (self.directory / "BROKEN.rules.json").write_text("{",
                                                          encoding="utf-8")
        _, _, problems = rules_crosscheck.load_shards(self.directory)
        self.assertEqual(1, len(problems), problems)
        self.assertIn("BROKEN.rules.json", problems[0])

    def test_output_is_stable_across_runs(self):
        first = rules_crosscheck.crosscheck(
            rules_crosscheck.load_shards(self.directory)[0])
        second = rules_crosscheck.crosscheck(
            rules_crosscheck.load_shards(self.directory)[0])
        self.assertEqual(first, second)


if __name__ == "__main__":
    unittest.main(verbosity=2)
