#!/bin/bash

# 실행 스크립트
# 포트: 8080

set -e

PORT=8080
ENV_FILE=".env"

# env 로드
if [ ! -f "$ENV_FILE" ]; then
  echo "[ERROR] $ENV_FILE 파일이 없습니다."
  exit 1
fi

set -a
source "$ENV_FILE"
set +a

echo "=========================================="
echo "  SKTX Application 실행 "
echo "=========================================="
echo "포트: $PORT"
echo "DB: $DB_NAME ($DB_HOST:$DB_PORT)"
echo "=========================================="
echo ""


./gradlew bootRun --args="--server.port=${PORT}"
