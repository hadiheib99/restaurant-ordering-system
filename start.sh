#!/usr/bin/env bash
set -euo pipefail

printf '\n🍽️  Starting Restaurant Ordering System...\n'

docker compose up --build -d

printf '\n⏳ Waiting for services to become available...\n'

for i in {1..60}; do
  if curl -fsS http://localhost:8080/api/meals >/dev/null 2>&1 && curl -fsS http://localhost:4200 >/dev/null 2>&1; then
    printf '\n✅ Restaurant Ordering System is ready.\n'
    printf '   Frontend: http://localhost:4200\n'
    printf '   Backend:  http://localhost:8080\n'
    printf '   Artemis:  http://localhost:8161\n\n'
    exit 0
  fi
  sleep 2
done

printf '\n⚠️  Containers started, but the application did not become ready within 120 seconds.\n'
printf 'Run: docker compose ps\n'
printf 'Then inspect logs with: docker compose logs --tail=100 backend frontend\n'
exit 1
