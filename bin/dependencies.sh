#!/usr/bin/env bash

./gradlew hurl-core:dependencies > docs/dependencies/hurl-core.txt
./gradlew hurl-cli:dependencies > docs/dependencies/hurl-cli.txt
./gradlew hurl-fmt:dependencies > docs/dependencies/hurl-fmt.txt
