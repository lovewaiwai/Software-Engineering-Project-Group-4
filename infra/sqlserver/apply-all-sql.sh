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

DB_NAME_IDENTIFIER="${DB_NAME//]/]]}"
DB_NAME_LITERAL="${DB_NAME//\'/\'\'}"

echo "Resetting database ${DB_NAME}; existing local data will be removed."
"${SQLCMD}" "${SQLCMD_BASE[@]}" -d master -Q "
IF DB_ID(N'${DB_NAME_LITERAL}') IS NOT NULL
BEGIN
  ALTER DATABASE [${DB_NAME_IDENTIFIER}] SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
  DROP DATABASE [${DB_NAME_IDENTIFIER}];
END;
CREATE DATABASE [${DB_NAME_IDENTIFIER}];
"

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
  echo "Applying SQL script: ${relative_name}"
  "${SQLCMD}" "${SQLCMD_BASE[@]}" -i "${file}"
done

echo "All SQL scripts have been applied."
