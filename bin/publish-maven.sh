#!/usr/bin/env bash
set -euxo pipefail

./gradlew publish -Dhttp.keepAlive=false -Dmaven.wagon.http.pool=false --info