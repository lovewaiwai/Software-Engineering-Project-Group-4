<#
.SYNOPSIS
Applies SwapCampus SQL migrations and seed scripts to the Docker SQL Server.

.DESCRIPTION
Runs all .sql files from db/migrations and db/seeds against the SQL Server
container. By default, the target database is dropped and recreated first, so
each run starts from a clean local development database.

.EXAMPLE
.\scripts\init-docker-db.ps1

.EXAMPLE
.\scripts\init-docker-db.ps1 -Password "YourStrong!Passw0rd"
#>

[CmdletBinding()]
param(
    [string]$ContainerName = "swapcampus-sqlserver",
    [string]$Database = "SwapCampus",
    [string]$Username = "sa",
    [string]$Password = "YourStrong!Passw0rd",
    [string]$DbRoot,
    [int]$ReadyTimeoutSeconds = 120
)

$ErrorActionPreference = "Stop"

$RepoRoot = Split-Path -Parent $PSScriptRoot
if ([string]::IsNullOrWhiteSpace($DbRoot)) {
    $DbRoot = Join-Path $RepoRoot "db"
}

function Invoke-DockerSqlCmd {
    param(
        [string[]]$Arguments
    )

    $sqlcmdPath = "/opt/mssql-tools18/bin/sqlcmd"
    $test = docker exec $ContainerName bash -lc "test -x $sqlcmdPath"
    if ($LASTEXITCODE -ne 0) {
        $sqlcmdPath = "/opt/mssql-tools/bin/sqlcmd"
    }

    docker exec $ContainerName $sqlcmdPath @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "sqlcmd failed with exit code $LASTEXITCODE."
    }
}

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw "docker command was not found. Start Docker Desktop and reopen PowerShell."
}

$container = docker ps --filter "name=^/$ContainerName$" --format "{{.Names}}"
if ($container -ne $ContainerName) {
    throw "Container '$ContainerName' is not running. Run: docker compose up -d sqlserver"
}

$deadline = (Get-Date).AddSeconds($ReadyTimeoutSeconds)
Write-Host "Waiting for SQL Server in container '$ContainerName'..."
while ($true) {
    docker exec $ContainerName /opt/mssql-tools18/bin/sqlcmd -S localhost -U $Username -P $Password -C -Q "SELECT 1" *> $null
    if ($LASTEXITCODE -eq 0) {
        break
    }
    if ((Get-Date) -gt $deadline) {
        throw "SQL Server did not become ready within $ReadyTimeoutSeconds seconds."
    }
    Start-Sleep -Seconds 2
}

$baseArgs = @("-S", "localhost", "-U", $Username, "-P", $Password, "-C", "-b")
$databaseIdentifier = $Database.Replace("]", "]]")
$databaseLiteral = $Database.Replace("'", "''")

Write-Host "Resetting database '$Database'; existing local data will be removed."
$resetSql = @"
IF DB_ID(N'$databaseLiteral') IS NOT NULL
BEGIN
  ALTER DATABASE [$databaseIdentifier] SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
  DROP DATABASE [$databaseIdentifier];
END;
CREATE DATABASE [$databaseIdentifier];
"@
Invoke-DockerSqlCmd -Arguments ($baseArgs + @("-d", "master", "-Q", $resetSql))

$sqlFiles = @()
$migrationDir = Join-Path $DbRoot "migrations"
$seedDir = Join-Path $DbRoot "seeds"
if (Test-Path -LiteralPath $migrationDir) {
    $sqlFiles += Get-ChildItem -LiteralPath $migrationDir -Filter "*.sql" -File
}
if (Test-Path -LiteralPath $seedDir) {
    $sqlFiles += Get-ChildItem -LiteralPath $seedDir -Filter "*.sql" -File
}
$sqlFiles = $sqlFiles | Sort-Object FullName

if (-not $sqlFiles) {
    Write-Host "No SQL files found under db/migrations or db/seeds."
    exit 0
}

foreach ($file in $sqlFiles) {
    $relativeName = [IO.Path]::GetRelativePath($DbRoot, $file.FullName).Replace("\", "/")
    $containerPath = "/tmp/swapcampus-sql/" + ($relativeName -replace "[/\\]", "_")
    docker exec $ContainerName mkdir -p /tmp/swapcampus-sql
    docker cp $file.FullName "${ContainerName}:$containerPath"

    Write-Host "Applying SQL script: $relativeName"
    Invoke-DockerSqlCmd -Arguments ($baseArgs + @("-i", $containerPath))
}

Write-Host "All SQL scripts have been applied."
