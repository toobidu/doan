$ErrorActionPreference = 'Stop'

Set-Location (Join-Path $PSScriptRoot '..\..\..')
docker compose --env-file src/main/docker/.env -f src/main/docker/app.yml down
