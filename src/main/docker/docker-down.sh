#!/usr/bin/env sh
set -eu

cd "$(dirname "$0")/../../.."
docker compose --env-file src/main/docker/.env -f src/main/docker/app.yml down
