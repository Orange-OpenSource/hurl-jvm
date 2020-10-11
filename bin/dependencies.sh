#!/usr/bin/env bash
set -euxo pipefail

./gradlew hurl-core:dependencies | grep --invert-match BUILD > docs/dependencies/hurl-core.txt
./gradlew hurl-cli:dependencies | grep --invert-match BUILD > docs/dependencies/hurl-cli.txt
./gradlew hurl-fmt:dependencies | grep --invert-match BUILD > docs/dependencies/hurl-fmt.txt
