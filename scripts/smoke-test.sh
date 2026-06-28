#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${1:-http://localhost:8080}"
FAILURES=0

pass() {
	echo "PASS: $1"
}

fail() {
	echo "FAIL: $1"
	FAILURES=$((FAILURES + 1))
}

section() {
	echo ""
	echo "=== $1 ==="
}

section "Health Endpoints"

HEALTH=$(curl -sS -o /dev/null -w "%{http_code}" "$BASE_URL/health" 2>/dev/null || echo "000")
if [ "$HEALTH" = "200" ]; then
	pass "/health returned 200"
else
	fail "/health returned $HEALTH (expected 200)"
fi

ACTUATOR_HEALTH=$(curl -sS -o /dev/null -w "%{http_code}" "$BASE_URL/actuator/health" 2>/dev/null || echo "000")
if [ "$ACTUATOR_HEALTH" = "200" ]; then
	pass "/actuator/health returned 200"
else
	fail "/actuator/health returned $ACTUATOR_HEALTH (expected 200)"
fi

section "API Contract"

SWAGGER=$(curl -sS -o /dev/null -w "%{http_code}" "$BASE_URL/v3/api-docs" 2>/dev/null || echo "000")
if [ "$SWAGGER" = "200" ]; then
	pass "/v3/api-docs returned 200"
else
	fail "/v3/api-docs returned $SWAGGER (expected 200)"
fi

section "Metrics Endpoint"

PROMETHEUS=$(curl -sS -o /dev/null -w "%{http_code}" "$BASE_URL/manage/prometheus" 2>/dev/null || echo "000")
if [ "$PROMETHEUS" = "200" ]; then
	pass "/manage/prometheus returned 200"
else
	fail "/manage/prometheus returned $PROMETHEUS (expected 200)"
fi

section "Custom Metrics"

STUDENTS_METRIC=$(curl -sS "$BASE_URL/actuator/metrics/db.students.count" 2>/dev/null | jq -r '.measurements[0].value' 2>/dev/null || echo "unavailable")
if [ "$STUDENTS_METRIC" != "unavailable" ]; then
	pass "Custom metric db.students.count = $STUDENTS_METRIC"
else
	fail "Custom metric db.students.count not accessible"
fi

echo ""
echo "=== Result ==="
if [ "$FAILURES" -eq 0 ]; then
	echo "All smoke tests passed."
	exit 0
else
	echo "$FAILURES smoke test(s) failed."
	exit 1
fi
