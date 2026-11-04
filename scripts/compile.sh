#!/bin/sh
set -eu
cd "$(dirname "$0")/.."
mkdir -p .deps target/classes
fetch() {
    if [ ! -f ".deps/$1" ]; then
        curl -fL --connect-timeout 15 --output ".deps/$1.tmp" "$2"
        mv ".deps/$1.tmp" ".deps/$1"
    fi
}
fetch mssql-jdbc-13.4.0.jre11.jar https://repo.maven.apache.org/maven2/com/microsoft/sqlserver/mssql-jdbc/13.4.0.jre11/mssql-jdbc-13.4.0.jre11.jar
fetch flatlaf-3.7.2.jar https://repo.maven.apache.org/maven2/com/formdev/flatlaf/3.7.2/flatlaf-3.7.2.jar
find src/main/java -name '*.java' -print > target/main-sources.txt
javac -cp ".deps/*" --release 17 -encoding UTF-8 -d target/classes @target/main-sources.txt
if [ "${1:-}" = "--tests" ]; then
    mkdir -p target/test-classes
    find src/test/java -name '*.java' ! -name 'ProjectUnitTest.java' -print > target/test-sources.txt
    javac -cp "target/classes:.deps/*" --release 17 -encoding UTF-8 -d target/test-classes @target/test-sources.txt
fi
