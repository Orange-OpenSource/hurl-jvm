#!/bin/bash
set -u
set -e

echo "$HURL_BIN"

for hurl_file in "$@"; do
    echo "$hurl_file";
    set +e
    $HURL_BIN "$hurl_file"
    EXITCODE_ACTUAL=$?
    set -e

    EXITCODE_EXPECTED=$(cat "${hurl_file%.*}.exit")

    if [ "$EXITCODE_EXPECTED" == 0 ] && [ "$EXITCODE_ACTUAL" == 0 ]; then
        continue
    fi

    if [ "$EXITCODE_EXPECTED" != 0 ] && [ "$EXITCODE_ACTUAL" != 0 ]; then
        continue
    fi

    echo "ERROR Exit Code"
    echo "  Expected: $EXITCODE_EXPECTED"
    echo "  Actual: $EXITCODE_ACTUAL"
    exit 1

done
