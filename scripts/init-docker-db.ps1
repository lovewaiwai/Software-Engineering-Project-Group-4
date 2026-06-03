<#
.SYNOPSIS
Applies SwapCampus SQL migrations and seed scripts to the Docker SQL Server.

.DESCRIPTION
Runs all .sql files from db/migrations and db/seeds against the SQL Server
container. Applied files are tracked in dbo.__schema_migrations, so each file is
run only once unless the database volume is reset.

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

Write-Host "Ensuring database and migration history table exist..."
Invoke-DockerSqlCmd -Arguments ($baseArgs + @("-Q", "IF DB_ID(N'$Database') IS NULL CREATE DATABASE [$Database];"))
Invoke-DockerSqlCmd -Arguments ($baseArgs + @("-d", $Database, "-Q", "IF OBJECT_ID(N'dbo.__schema_migrations', N'U') IS NULL CREATE TABLE dbo.__schema_migrations (script_name NVARCHAR(260) NOT NULL PRIMARY KEY, applied_at DATETIME2(0) NOT NULL DEFAULT SYSDATETIME());"))

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
    $escapedName = $relativeName.Replace("'", "''")
    $countOutput = docker exec $ContainerName /opt/mssql-tools18/bin/sqlcmd -S localhost -U $Username -P $Password -C -d $Database -h -1 -W -Q "SET NOCOUNT ON; SELECT COUNT(1) FROM dbo.__schema_migrations WHERE script_name = N'$escapedName';"
    $alreadyApplied = ($countOutput -join "").Trim()

    if ($alreadyApplied -eq "1") {
        Write-Host "Skipping already applied script: $relativeName"
        continue
    }

    if ($relativeName -eq "migrations/V001__init.sql") {
        $coreTableOutput = docker exec $ContainerName /opt/mssql-tools18/bin/sqlcmd -S localhost -U $Username -P $Password -C -d $Database -h -1 -W -Q "SET NOCOUNT ON; SELECT COUNT(1) FROM sys.tables WHERE name IN (N'users', N'products', N'orders', N'audit_logs');"
        $existingCoreTables = ($coreTableOutput -join "").Trim()
        if ($existingCoreTables -ne "0") {
            Write-Host "Core tables already exist; recording $relativeName as applied."
            Invoke-DockerSqlCmd -Arguments ($baseArgs + @("-d", $Database, "-Q", "INSERT INTO dbo.__schema_migrations (script_name) VALUES (N'$escapedName');"))
            continue
        }
    }

    $containerPath = "/tmp/swapcampus-sql/" + ($relativeName -replace "[/\\]", "_")
    docker exec $ContainerName mkdir -p /tmp/swapcampus-sql
    docker cp $file.FullName "${ContainerName}:$containerPath"

    Write-Host "Applying SQL script: $relativeName"
    Invoke-DockerSqlCmd -Arguments ($baseArgs + @("-i", $containerPath))
    Invoke-DockerSqlCmd -Arguments ($baseArgs + @("-d", $Database, "-Q", "INSERT INTO dbo.__schema_migrations (script_name) VALUES (N'$escapedName');"))
}

Write-Host "All pending SQL scripts have been applied."
