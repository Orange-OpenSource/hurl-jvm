#!/usr/bin/env sh

./gradlew build -x check

integration/run.sh integration/tests/*.hurl