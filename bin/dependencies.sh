#!/usr/bin/env bash

./gradlew hurl-core:dependencies | grep -v BUILD > docs/dependencies/hurl-core.txt
./gradlew hurl-cli:dependencies | grep -v BUILD > docs/dependencies/hurl-cli.txt
./gradlew hurl-fmt:dependencies | grep -v BUILD > docs/dependencies/hurl-fmt.txt
