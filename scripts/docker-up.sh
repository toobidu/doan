#!/usr/bin/env sh
set -eu

cd "$(dirname "$0")/.."

if [ ! -f src/main/docker/.env ]; then
  cp src/main/docker/.env.example src/main/docker/.env
  echo "Da tao src/main/docker/.env tu .env.example"
fi

docker compose --env-file src/main/docker/.env -f src/main/docker/app.yml up -d --build
