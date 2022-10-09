#!/usr/bin/env bash

gradle -PreleaseVersion=$(date +%Y.%m.%d) --info publish | tee publish.log

# login https://oss.sonatype.org/
