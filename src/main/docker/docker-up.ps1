$ErrorActionPreference = 'Stop'

Set-Location (Join-Path $PSScriptRoot '..\..\..')

$envFile = 'src/main/docker/.env'
if (-not (Test-Path $envFile)) {
    Copy-Item 'src/main/docker/.env.example' $envFile
    Write-Host "Da tao $envFile tu .env.example"
}

docker compose --env-file src/main/docker/.env -f src/main/docker/app.yml up -d --build
