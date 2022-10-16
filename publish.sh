#!/usr/bin/env bash

gradle -PreleaseVersion=$(date +%Y.%m.%d) --info clean publish -x test | tee publish.log

# login https://oss.sonatype.org/
