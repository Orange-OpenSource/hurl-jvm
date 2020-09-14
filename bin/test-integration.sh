#!/usr/bin/env sh

./gradlew build -x check

cd integration
./run.sh tests/*.hurl