#!/usr/bin/env bash
set -euo pipefail

PROD_ROOT="${PROD_ROOT:-/tmp/midterm-production}"
ENV_FILE="$PROD_ROOT/shared/app.env"

if [ ! -f "$ENV_FILE" ]; then
	echo "Environment is not prepared. Run scripts/prepare-env.sh first."
	exit 1
fi

source "$ENV_FILE"

CURRENT="$(cat "$PROD_ROOT/shared/current")"
PREVIOUS="$(cat "$PROD_ROOT/shared/previous")"

if [ -z "$PREVIOUS" ]; then
	echo "No previous environment recorded. Rollback is not available."
	exit 1
fi

if [ "$PREVIOUS" = "blue" ]; then
	PORT="$BLUE_PORT"
else
	PORT="$GREEN_PORT"
fi

if ! curl -fsS "http://localhost:$PORT/health" >/dev/null; then
	echo "Previous environment $PREVIOUS is not healthy on port $PORT."
	exit 1
fi

echo "$CURRENT" > "$PROD_ROOT/shared/previous"
echo "$PREVIOUS" > "$PROD_ROOT/shared/current"
echo "Rollback complete. Active environment: $PREVIOUS on port $PORT"
