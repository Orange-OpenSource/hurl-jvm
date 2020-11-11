#!/usr/bin/env bash
set -euxo pipefail

./gradlew publish --info -Dorg.gradle.internal.http.socketTimeout=60000 -Dorg.gradle.internal.http.connectionTimeout=60000