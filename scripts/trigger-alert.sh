#!/usr/bin/env bash
set -euo pipefail

APP_URL="${APP_URL:-http://localhost:8080}"

echo "Triggering error burst on Spring Boot app..."

# Generate errors by hitting invalid student IDs
for i in $(seq 1 10); do
  curl -sS -u "user:user123" "${APP_URL}/api/students/99999" >/dev/null || true
done

echo ""
echo "Done. Wait ~30s then check:"
echo "  Prometheus alerts: http://localhost:9090/alerts"
echo "  Grafana alerting:    http://localhost:3000/alerting/list (admin/admin)"
