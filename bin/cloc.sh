#!/usr/bin/env bash
set -euxo pipefail

cloc --include-ext=kt --exclude-dir=test .
