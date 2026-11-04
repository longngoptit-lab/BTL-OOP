#!/bin/sh
set -eu
cd "$(dirname "$0")"
./scripts/compile.sh --tests
QLHT_DB_NAME="QLHT_AUTH_TEST_$(date +%s)_$$" java -cp "target/classes:target/test-classes:.deps/*" qlht.tests.AuthIntegrationTest "$@"
