#!/usr/bin/env bash
set -euo pipefail

PROD_ROOT="${PROD_ROOT:-/tmp/midterm-production}"
INTERVAL_SECONDS="${INTERVAL_SECONDS:-30}"
LOG_FILE="${LOG_FILE:-$PROD_ROOT/logs/health-check.log}"

mkdir -p "$(dirname "$LOG_FILE")"

if [ ! -f "$PROD_ROOT/shared/app.env" ]; then
	echo "Environment is not prepared. Run scripts/prepare-env.sh first."
	exit 1
fi

source "$PROD_ROOT/shared/app.env"

echo "Writing health checks to $LOG_FILE every $INTERVAL_SECONDS seconds"

while true; do
	CURRENT="$(cat "$PROD_ROOT/shared/current")"
	if [ "$CURRENT" = "blue" ]; then
		PORT="$BLUE_PORT"
	else
		PORT="$GREEN_PORT"
	fi

	TIMESTAMP="$(date -u +"%Y-%m-%dT%H:%M:%SZ")"
	if RESPONSE="$(curl -fsS "http://localhost:$PORT/health" 2>/tmp/midterm-health-error.txt)"; then
		echo "$TIMESTAMP status=UP env=$CURRENT port=$PORT response=$RESPONSE" >> "$LOG_FILE"
	else
		ERROR="$(cat /tmp/midterm-health-error.txt)"
		echo "$TIMESTAMP status=DOWN env=$CURRENT port=$PORT error=$ERROR" >> "$LOG_FILE"
	fi

	sleep "$INTERVAL_SECONDS"
done
