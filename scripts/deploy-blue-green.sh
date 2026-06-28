#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PROD_ROOT="${PROD_ROOT:-/tmp/midterm-production}"

"$ROOT_DIR/scripts/prepare-env.sh"
source "$PROD_ROOT/shared/app.env"

CURRENT="$(cat "$PROD_ROOT/shared/current")"
if [ "$CURRENT" = "blue" ]; then
	NEXT="green"
	NEXT_PORT="$GREEN_PORT"
else
	NEXT="blue"
	NEXT_PORT="$BLUE_PORT"
fi

echo "Building application..."
"$ROOT_DIR/mvnw" -q test package

JAR="$(find "$ROOT_DIR/target" -maxdepth 1 -name "*.jar" ! -name "*sources.jar" | head -n 1)"
if [ -z "$JAR" ]; then
	echo "Application jar not found in target/"
	exit 1
fi

cp "$JAR" "$PROD_ROOT/$NEXT/app.jar"

if [ -f "$PROD_ROOT/$NEXT/app.pid" ]; then
	OLD_PID="$(cat "$PROD_ROOT/$NEXT/app.pid")"
	if kill -0 "$OLD_PID" 2>/dev/null; then
		kill "$OLD_PID"
		sleep 2
	fi
fi

echo "Starting $NEXT environment on port $NEXT_PORT..."
APP_PROD_ROOT="$APP_PROD_ROOT" APP_ENV="$NEXT" nohup java -jar "$PROD_ROOT/$NEXT/app.jar" \
	--spring.profiles.active="$SPRING_PROFILES_ACTIVE" \
	--server.port="$NEXT_PORT" \
	> "$PROD_ROOT/logs/$NEXT.log" 2>&1 &
echo $! > "$PROD_ROOT/$NEXT/app.pid"

for attempt in {1..30}; do
	if curl -fsS "http://localhost:$NEXT_PORT/health" >/dev/null 2>&1; then
		echo "Health check passed for $NEXT."
		echo "Running post-deployment smoke tests..."
		if "$ROOT_DIR/scripts/smoke-test.sh" "http://localhost:$NEXT_PORT"; then
			echo "$CURRENT" > "$PROD_ROOT/shared/previous"
			echo "$NEXT" > "$PROD_ROOT/shared/current"
			echo "Deployment successful. Active environment: $NEXT on port $NEXT_PORT"
			exit 0
		else
			echo "Smoke tests failed for $NEXT. Rolling back..."
			kill "$(cat "$PROD_ROOT/$NEXT/app.pid")" 2>/dev/null || true
			exit 1
		fi
	fi
	sleep 2
done

echo "Health check failed for $NEXT. Keeping active environment as $CURRENT."
cat "$PROD_ROOT/logs/$NEXT.log" || true
exit 1
