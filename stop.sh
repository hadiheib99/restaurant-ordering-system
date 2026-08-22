#!/usr/bin/env bash
set -euo pipefail

printf '\n🛑 Stopping Restaurant Ordering System...\n'
docker compose down
printf '✅ All application containers are stopped.\n\n'
