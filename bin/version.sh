#!/usr/bin/env bash

if [[ -n "$TRAVIS_TAG" ]]; then
  echo ">>> Updating gradle.properties to release"
  sed -i -e 's/\-SNAPSHOT//g' gradle.properties
  cat gradle.properties
  echo "<<< Updating gradle.properties to release"
fi
