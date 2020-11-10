#!/usr/bin/env bash
set -eux

echo ">>> Updating gradle.properties to release"
sed -i -e 's/\-SNAPSHOT//g' gradle.properties
cat gradle.properties
echo "<<< Updating gradle.properties to release"
