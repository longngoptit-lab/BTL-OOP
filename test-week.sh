#!/bin/sh
set -eu
cd "$(dirname "$0")"
./scripts/compile.sh --tests
exec java -cp "target/classes:target/test-classes:.deps/*" qlht.tests.WeekScheduleTest "$@"
