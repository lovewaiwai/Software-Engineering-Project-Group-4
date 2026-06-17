<#
.SYNOPSIS
Initializes the SwapCampus database on a local SQL Server instance.

.DESCRIPTION
This script is for developers who run SQL Server locally and manage it with
SQL Server Management Studio or Azure Data Studio. It locates sqlcmd, connects
to the selected SQL Server instance, and executes db/migrations/V001__init.sql.

.EXAMPLE
.\scripts\init-local-sqlserver.ps1 -ServerInstance "localhost"

.EXAMPLE
.\scripts\init-local-sqlserver.ps1 -ServerInstance ".\SQLEXPRESS"

.EXAMPLE
.\scripts\init-local-sqlserver.ps1 -ServerInstance "localhost" -SqlAuth -Username "sa" -Password "YourStrong!Passw0rd"
#>

[CmdletBinding()]
param(
    [string]$ServerInstance = "localhost",
    [string]$Database = "SwapCampus",
    [string]$MigrationPath,
    [switch]$SqlAuth,
    [string]$Username = "sa",
    [string]$Password,
    [int]$ConnectTimeout = 30
)

$ErrorActionPreference = "Stop"

$RepoRoot = Split-Path -Parent $PSScriptRoot
if ([string]::IsNullOrWhiteSpace($MigrationPath)) {
    $MigrationPath = Join-Path $RepoRoot "db\migrations\V001__init.sql"
}

function Resolve-SqlCmd {
    $command = Get-Command sqlcmd -ErrorAction SilentlyContinue
    if ($command) {
        return $command.Source
    }

    $candidatePatterns = @(
        "C:\Program Files\Microsoft SQL Server\Client SDK\ODBC\*\Tools\Binn\SQLCMD.EXE",
        "C:\Program Files\Microsoft SQL Server\*\Tools\Binn\SQLCMD.EXE",
        "C:\Program Files (x86)\Microsoft SQL Server\Client SDK\ODBC\*\Tools\Binn\SQLCMD.EXE",
        "C:\Program Files (x86)\Microsoft SQL Server\*\Tools\Binn\SQLCMD.EXE"
    )

    foreach ($pattern in $candidatePatterns) {
        $match = Get-ChildItem -Path $pattern -ErrorAction SilentlyContinue |
            Sort-Object FullName -Descending |
            Select-Object -First 1
        if ($match) {
            return $match.FullName
        }
    }

    throw "sqlcmd was not found. Install Microsoft SQL Server Command Line Utilities, or run the migration manually in SSMS."
}

if (-not (Test-Path -LiteralPath $MigrationPath)) {
    throw "Migration file not found: $MigrationPath"
}

$sqlcmd = Resolve-SqlCmd

$args = @(
    "-S", $ServerInstance,
    "-b",
    "-l", $ConnectTimeout,
    "-i", $MigrationPath
)

# sqlcmd 18+ requires certificate trust flags for common local dev setups.
$sqlcmdHelp = (& $sqlcmd "-?" 2>&1 | Out-String)
if ($sqlcmdHelp -match "(^|\s)-C(\s|$)") {
    $args += @("-C")
}
else {
    Write-Host "sqlcmd does not advertise -C; continuing without certificate trust flag."
}

if ($SqlAuth) {
    if ([string]::IsNullOrWhiteSpace($Password)) {
        $securePassword = Read-Host "SQL Server password for $Username" -AsSecureString
        $bstr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
        try {
            $Password = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($bstr)
        }
        finally {
            [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr)
        }
    }
    $args += @("-U", $Username, "-P", $Password)
}
else {
    $args += @("-E")
}

Write-Host "Using sqlcmd: $sqlcmd"
Write-Host "Server instance: $ServerInstance"
Write-Host "Migration file: $MigrationPath"
Write-Host "Running database initialization..."

& $sqlcmd @args

if ($LASTEXITCODE -ne 0) {
    throw "Database initialization failed with exit code $LASTEXITCODE."
}

Write-Host ""
Write-Host "SwapCampus database initialization completed."
Write-Host "Backend DB_URL example:"
Write-Host "jdbc:sqlserver://$ServerInstance;databaseName=$Database;encrypt=true;trustServerCertificate=true"
