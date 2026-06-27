#!/usr/bin/env bash
set -euo pipefail

APP_URL="${APP_URL:-http://localhost:8080}"
REQUESTS="${1:-20}"

echo "Sending ${REQUESTS} requests to ${APP_URL}..."
for _ in $(seq 1 "${REQUESTS}"); do
  curl -sS "${APP_URL}/students" -u "user:user123" >/dev/null || true
  curl -sS "${APP_URL}/health" >/dev/null || true
  curl -sS "${APP_URL}/actuator/health" -u "admin:admin123" >/dev/null || true
done
echo "Traffic generation finished."
