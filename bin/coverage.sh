#!/usr/bin/env sh
set -eux

./gradlew test
./gradlew hurl-core:jacocoTestReport
awk -F"," '{ instructions += $4 + $5; covered += $5 } END { print covered, "/", instructions, " instructions covered"; print 100*covered/instructions, "% covered" }' hurl-core/build/reports/jacoco/test/jacocoTestReport.csv
