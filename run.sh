#!/bin/sh
set -eu
cd "$(dirname "$0")"
./scripts/compile.sh
exec java -cp "target/classes:.deps/*" qlht.Main "$@"
