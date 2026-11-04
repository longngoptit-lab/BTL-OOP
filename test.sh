#!/bin/sh
set -eu
cd "$(dirname "$0")"
./scripts/compile.sh --tests
java -cp "target/classes:target/test-classes:.deps/*" qlht.tests.DomainValidationTest "$@"
java -Djava.awt.headless=true -cp "target/classes:target/test-classes:.deps/*" qlht.tests.WeekScheduleTest
