#!/usr/bin/env bash

./gradlew --stacktrace coverageTestGrouping testTestGrouping
curl https://codecov.io/bash -o codecov.sh
chmod +x codecov.sh
./codecov.sh -X gcov -X coveragepy -X xcode -f build/reports/jacoco/coverageTestGrouping/coverageTestGrouping.xml
