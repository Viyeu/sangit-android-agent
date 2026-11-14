#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
TEMP_DIR=$(mktemp -d)
trap 'rm -rf "$TEMP_DIR"' EXIT
javac -encoding UTF-8 -d "$TEMP_DIR" \
  app/src/main/java/vn/sangit/androidagent/TemplateCatalog.java \
  app/src/main/java/vn/sangit/androidagent/NotificationPolicy.java \
  app/src/main/java/vn/sangit/androidagent/ReplyAssistant.java \
  app/src/main/java/vn/sangit/androidagent/ContentSchedule.java \
  app/src/main/java/vn/sangit/androidagent/MetricsMath.java \
  app/src/test/java/vn/sangit/androidagent/OfflineSmokeTest.java
java -ea -cp "$TEMP_DIR" vn.sangit.androidagent.OfflineSmokeTest
java scripts/JavaSyntaxCheck.java $(find app/src/main/java app/src/test/java -name '*.java' -print | sort)
python3 scripts/verify-static.py
