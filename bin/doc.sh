#!/usr/bin/env bash
set -euxo pipefail

./gradlew hurl-core:dokkaJavadoc
./gradlew hurl-cli:dokkaJavadoc
./gradlew hurl-fmt:dokkaJavadoc
