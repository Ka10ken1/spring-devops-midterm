#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "Running lightweight lint checks..."

if find "$ROOT_DIR/src" -type f \( -name "*.java" -o -name "*.properties" \) -print0 \
	| xargs -0 grep -n $'\r' >/tmp/midterm-lint-crlf.txt; then
	echo "CRLF line endings found:"
	cat /tmp/midterm-lint-crlf.txt
	exit 1
fi

if find "$ROOT_DIR/src" "$ROOT_DIR/scripts" -type f \( -name "*.java" -o -name "*.properties" -o -name "*.sh" \) -print0 \
	| xargs -0 grep -nE "[[:blank:]]+$" >/tmp/midterm-lint-trailing.txt; then
	echo "Trailing whitespace found:"
	cat /tmp/midterm-lint-trailing.txt
	exit 1
fi

if find "$ROOT_DIR/src/main/java" -type f -name "*.java" -print0 \
	| xargs -0 grep -n "System\.out\.println" >/tmp/midterm-lint-stdout.txt; then
	echo "System.out.println found in main code:"
	cat /tmp/midterm-lint-stdout.txt
	exit 1
fi

echo "Lint checks passed."
