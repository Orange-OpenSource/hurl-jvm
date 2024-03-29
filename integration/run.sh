#!/bin/bash
set -u
set -e


echo "$hurl"
echo "$hurlfmt"

for hurl_file in "$@"; do
    set +e

    options=("")
    if test -f "${hurl_file%.*}.options"; then
        options+=("$(cat "${hurl_file%.*}.options")")
    fi

    cmd="$hurl $hurl_file ${options[@]}"
    echo "$cmd"

    $cmd 2>/tmp/test.stderr >/tmp/test.stdout
    exit_code=$?
    set -eo pipefail

    exit_code_expected=$(cat "${hurl_file%.*}.exit")
    if [ "$exit_code" != "$exit_code_expected" ]; then
        echo "ERROR Exit Code"
        echo "  Expected: $exit_code_expected"
        echo "  Actual: $exit_code"

        cat /tmp/test.stderr
	exit 1
    fi

    if test -f "${hurl_file%.*}.out"; then
        expected=$(cat "${hurl_file%.*}.out")
        actual=$(cat /tmp/test.stdout)
        if [ "$actual" != "$expected" ]; then
	    echo "Diff in standard output"
            diff  <(echo "$actual" ) <(echo "$expected")
            exit 1
        fi
    fi

    # Test json export
    cmd="$hurlfmt --format json $hurl_file"
    echo "$cmd"

    $cmd > /tmp/test.json
    json_expected=$(cat "${hurl_file%.*}.json")
    json_actual=$(cat /tmp/test.json)
    if [ "$json_actual" != "$json_expected" ]; then
        echo "ERROR Exit Code"
        echo "  Expected: $json_expected"
        echo "  Actual  : $json_actual"
        diff -y --suppress-common-lines <(echo "$json_expected" | tr ',' '\n') <(echo "$json_actual" | tr ',' '\n')
	exit 1
    fi

done
