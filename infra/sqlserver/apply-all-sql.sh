#!/usr/bin/env bash
set -euo pipefail

DB_HOST="${DB_HOST:-sqlserver}"
DB_PORT="${DB_PORT:-1433}"
DB_NAME="${DB_NAME:-SwapCampus}"
DB_USERNAME="${DB_USERNAME:-sa}"
DB_PASSWORD="${DB_PASSWORD:-YourStrong!Passw0rd}"
DB_ROOT="${DB_ROOT:-/workspace/db}"

SUPPORTS_TRUST_CERT=0
if [[ -x /opt/mssql-tools18/bin/sqlcmd ]]; then
  SQLCMD=/opt/mssql-tools18/bin/sqlcmd
  SUPPORTS_TRUST_CERT=1
elif [[ -x /opt/mssql-tools/bin/sqlcmd ]]; then
  SQLCMD=/opt/mssql-tools/bin/sqlcmd
else
  echo "sqlcmd was not found in this image." >&2
  exit 1
fi

SQLCMD_BASE=(
  -S "${DB_HOST},${DB_PORT}"
  -U "${DB_USERNAME}"
  -P "${DB_PASSWORD}"
  -b
)

if [[ "${SUPPORTS_TRUST_CERT}" == "1" ]]; then
  SQLCMD_BASE+=(-C)
fi

echo "Waiting for SQL Server at ${DB_HOST},${DB_PORT}..."
for attempt in {1..60}; do
  if "${SQLCMD}" "${SQLCMD_BASE[@]}" -Q "SELECT 1" >/dev/null 2>&1; then
    break
  fi
  if [[ "${attempt}" == "60" ]]; then
    echo "SQL Server did not become ready in time." >&2
    exit 1
  fi
  sleep 2
done

echo "Ensuring database and migration history table exist..."
"${SQLCMD}" "${SQLCMD_BASE[@]}" -Q "IF DB_ID(N'${DB_NAME}') IS NULL CREATE DATABASE [${DB_NAME}];"
"${SQLCMD}" "${SQLCMD_BASE[@]}" -d "${DB_NAME}" -Q "IF OBJECT_ID(N'dbo.__schema_migrations', N'U') IS NULL CREATE TABLE dbo.__schema_migrations (script_name NVARCHAR(260) NOT NULL PRIMARY KEY, applied_at DATETIME2(0) NOT NULL DEFAULT SYSDATETIME());"

mapfile -t SQL_FILES < <(
  {
    find "${DB_ROOT}/migrations" -maxdepth 1 -type f -name '*.sql' 2>/dev/null
    find "${DB_ROOT}/seeds" -maxdepth 1 -type f -name '*.sql' 2>/dev/null
  } | sort
)

if [[ "${#SQL_FILES[@]}" -eq 0 ]]; then
  echo "No SQL files found under ${DB_ROOT}/migrations or ${DB_ROOT}/seeds."
  exit 0
fi

for file in "${SQL_FILES[@]}"; do
  relative_name="${file#${DB_ROOT}/}"
  escaped_name="${relative_name//\'/\'\'}"
  already_applied="$("${SQLCMD}" "${SQLCMD_BASE[@]}" -d "${DB_NAME}" -h -1 -W -Q "SET NOCOUNT ON; SELECT COUNT(1) FROM dbo.__schema_migrations WHERE script_name = N'${escaped_name}';" | tr -d '[:space:]')"

  if [[ "${already_applied}" == "1" ]]; then
    echo "Skipping already applied script: ${relative_name}"
    continue
  fi

  if [[ "${relative_name}" == "migrations/V001__init.sql" ]]; then
    existing_core_tables="$("${SQLCMD}" "${SQLCMD_BASE[@]}" -d "${DB_NAME}" -h -1 -W -Q "SET NOCOUNT ON; SELECT COUNT(1) FROM sys.tables WHERE name IN (N'users', N'products', N'orders', N'audit_logs');" | tr -d '[:space:]')"
    if [[ "${existing_core_tables}" != "0" ]]; then
      echo "Core tables already exist; recording ${relative_name} as applied."
      "${SQLCMD}" "${SQLCMD_BASE[@]}" -d "${DB_NAME}" -Q "INSERT INTO dbo.__schema_migrations (script_name) VALUES (N'${escaped_name}');"
      continue
    fi
  fi

  echo "Applying SQL script: ${relative_name}"
  "${SQLCMD}" "${SQLCMD_BASE[@]}" -i "${file}"
  "${SQLCMD}" "${SQLCMD_BASE[@]}" -d "${DB_NAME}" -Q "INSERT INTO dbo.__schema_migrations (script_name) VALUES (N'${escaped_name}');"
done

echo "All pending SQL scripts have been applied."
