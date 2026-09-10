#!/usr/bin/env bash
# Hook do Estágio 1: cruza os shards de regras sempre que um subagente termina.
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"

# Um hook nunca pode derrubar a sessão da equipe: falha aqui vira aviso.
python3 "${repo_root}/.github/scripts/rules_crosscheck.py" || {
  echo "[rules-crosscheck] cruzamento não concluído; execute /crosscheck-rules manualmente." >&2
}

exit 0
