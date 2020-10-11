#!/usr/bin/env sh
set -eux

./gradlew build -x check

cd integration
./run.sh tests/*.hurl