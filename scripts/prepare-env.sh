#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PROD_ROOT="${PROD_ROOT:-/tmp/midterm-production}"

command -v java >/dev/null || { echo "java is required"; exit 1; }
command -v curl >/dev/null || { echo "curl is required"; exit 1; }

mkdir -p "$PROD_ROOT"/{blue,green,shared,logs,data/blue,data/green}

cat > "$PROD_ROOT/shared/app.env" <<ENV
APP_PROD_ROOT=$PROD_ROOT
SPRING_PROFILES_ACTIVE=prod
BLUE_PORT=8081
GREEN_PORT=8082
ENV

if [ ! -f "$PROD_ROOT/shared/current" ]; then
	echo "blue" > "$PROD_ROOT/shared/current"
fi

if [ ! -f "$PROD_ROOT/shared/previous" ]; then
	echo "" > "$PROD_ROOT/shared/previous"
fi

"$ROOT_DIR/mvnw" -version >/dev/null

echo "Environment prepared at $PROD_ROOT"
echo "Config written to $PROD_ROOT/shared/app.env"
