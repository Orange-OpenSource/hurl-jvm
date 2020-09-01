#!/bin/bash
set -u
set -e

echo "$hurl"

for hurl_file in "$@"; do
    echo "$hurl_file";
    set +e
    $hurl "$hurl_file" >/tmp/test.stdout
    EXITCODE_ACTUAL=$?
    set -e

    EXITCODE_EXPECTED=$(cat "${hurl_file%.*}.exit")

    if [ "$EXITCODE_EXPECTED" == 0 ] && [ "$EXITCODE_ACTUAL" == 0 ]; then
        expected=$(cat "${hurl_file%.*}.out")
        actual=$(cat /tmp/test.stdout)
        if [ "$actual" != "$expected" ]; then
            diff  <(echo "$actual" ) <(echo "$expected")
            exit 1
        fi
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
