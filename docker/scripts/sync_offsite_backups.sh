#!/usr/bin/env bash
set -eo pipefail
BACKUP_DIR="/opt/nexusedge-erp/backups"
LOCAL_VAULT="/opt/nexusedge-erp/vault-snapshots"
LOG_FILE="/var/log/ga_erp_backup.log"
RETENTION_DAYS=14

mkdir -p "${BACKUP_DIR}" "${LOCAL_VAULT}"

cd "${BACKUP_DIR}"
for archive in *.sql.gz; do
    [ -e "${archive}" ] || continue
    if [ ! -f "${archive}.sha256" ]; then
        sha256sum "${archive}" > "${archive}.sha256"
    fi
done

rsync -av --update "${BACKUP_DIR}/" "${LOCAL_VAULT}/" >> "${LOG_FILE}" 2>&1
find "${BACKUP_DIR}" -type f -name "*.sql.gz*" -mtime +${RETENTION_DAYS} -delete 2>/dev/null || true
find "${LOCAL_VAULT}" -type f -name "*.sql.gz*" -mtime +${RETENTION_DAYS} -delete 2>/dev/null || true
