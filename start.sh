#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

ENV_FILE="$SCRIPT_DIR/.env"

generate_secret() {
  openssl rand -hex "$1"
}

if [[ ! -f "$ENV_FILE" ]]; then
  if ! command -v openssl >/dev/null 2>&1; then
    printf '\n❌ openssl is required to generate local development secrets.\n'
    exit 1
  fi

  umask 077
  cat > "$ENV_FILE" <<EOF
POSTGRES_PASSWORD=$(generate_secret 18)
ARTEMIS_PASSWORD=$(generate_secret 18)
JWT_SECRET=$(generate_secret 32)
APP_SEED_DATA=true
SEED_ADMIN_PASSWORD=$(generate_secret 12)
SEED_WAITER_PASSWORD=$(generate_secret 12)
SEED_CUSTOMER_PASSWORD=$(generate_secret 12)
SEED_CHEF_PASSWORD=$(generate_secret 12)
EOF
  chmod 600 "$ENV_FILE"
  printf '\n🔐 Created local .env with generated development secrets.\n'
  printf '   The file is ignored by Git and must not be committed.\n'
fi

set -a
# shellcheck disable=SC1090
source "$ENV_FILE"
set +a

for variable in POSTGRES_PASSWORD ARTEMIS_PASSWORD JWT_SECRET; do
  if [[ -z "${!variable:-}" ]]; then
    printf '\n❌ %s is missing from .env.\n' "$variable"
    exit 1
  fi
done

if [[ "${APP_SEED_DATA:-false}" == "true" ]]; then
  for variable in SEED_ADMIN_PASSWORD SEED_WAITER_PASSWORD SEED_CUSTOMER_PASSWORD SEED_CHEF_PASSWORD; do
    if [[ -z "${!variable:-}" ]]; then
      printf '\n❌ %s is required when APP_SEED_DATA=true.\n' "$variable"
      exit 1
    fi
  done
fi

printf '\n🍽️  Starting Restaurant Ordering System...\n'
printf '   Starting PostgreSQL and ActiveMQ Artemis first...\n'

docker compose up -d postgres artemis

printf '\n⏳ Waiting for PostgreSQL...\n'
for i in {1..30}; do
  if docker exec restaurant-db pg_isready -U postgres -d restaurant_db >/dev/null 2>&1; then
    break
  fi
  if [[ "$i" -eq 30 ]]; then
    printf '\n❌ PostgreSQL did not become ready.\n'
    docker compose logs --tail=100 postgres
    exit 1
  fi
  sleep 2
done

# Keep an existing local PostgreSQL volume compatible with a newly generated .env.
# The official image allows local socket administration from inside the container,
# so this safely rotates the postgres role to the current ignored .env value.
docker exec -i restaurant-db \
  psql -U postgres -d postgres --set=postgres_password="$POSTGRES_PASSWORD" >/dev/null <<'SQL'
ALTER ROLE postgres WITH PASSWORD :'postgres_password';
SQL

printf '   Local PostgreSQL credential synchronized with .env.\n'

docker compose up --build -d

printf '\n⏳ Waiting for services to become available...\n'

for i in {1..60}; do
  if curl -fsS http://localhost:8080/api/meals >/dev/null 2>&1 && curl -fsS http://localhost:4200 >/dev/null 2>&1; then
    printf '\n✅ Restaurant Ordering System is ready.\n'
    printf '   Frontend: http://localhost:4200\n'
    printf '   Backend:  http://localhost:8080\n'
    printf '   Artemis:  http://localhost:8161\n'

    if [[ "${APP_SEED_DATA:-false}" == "true" ]]; then
      printf '\n🔑 Local demo accounts (passwords come from .env):\n'
      printf '   Admin:    admin@restaurant.com / %s\n' "$SEED_ADMIN_PASSWORD"
      printf '   Waiter:   waiter1@restaurant.com / %s\n' "$SEED_WAITER_PASSWORD"
      printf '   Chef:     chef1@restaurant.com / %s\n' "$SEED_CHEF_PASSWORD"
      printf '   Customer: customer1@restaurant.com / %s\n' "$SEED_CUSTOMER_PASSWORD"
    fi

    printf '\n'
    exit 0
  fi
  sleep 2
done

printf '\n⚠️  Containers started, but the application did not become ready within 120 seconds.\n'
printf 'Run: docker compose ps\n'
printf 'Then inspect logs with: docker compose logs --tail=100 backend frontend\n'
exit 1
