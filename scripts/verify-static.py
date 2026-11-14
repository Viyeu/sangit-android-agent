#!/usr/bin/env python3
"""Offline static guards. Not a replacement for Android SDK build/device E2E."""
from pathlib import Path
import sys
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parents[1]
manifest = ET.parse(root / 'app/src/main/AndroidManifest.xml').getroot()
ns = '{http://schemas.android.com/apk/res/android}'
permissions = [x.get(ns + 'name') for x in manifest.findall('uses-permission')]
assert permissions == ['android.permission.INTERNET'], f'Unexpected Android permissions: {permissions}'
app = manifest.find('application')
assert app is not None and app.get(ns+'allowBackup') == 'false'
assert app.get(ns+'usesCleartextTraffic') == 'false'
activity = app.find('activity')
assert not app.findall('service'), 'Play Protect safe variant must not register services'
assert activity is not None and activity.get(ns+'exported') == 'true'
java = (root / 'app/src/main/java/vn/sangit/androidagent/MainActivity.java').read_text()
listener = (root / 'app/src/main/java/vn/sangit/androidagent/FacebookNotificationListener.java').read_text()
assert 'Intent.createChooser' in java
assert 'ACTION_GET_CONTENT' in java
assert 'Intent.ACTION_SEND' in java
assert 'ACTION_NOTIFICATION_LISTENER_SETTINGS' not in java
assert 'enabled_notification_listeners' not in java
assert 'com.facebook.katana' not in listener  # relies on single allowlist definition
assert 'EXTRA_TEXT' not in listener
assert 'EXTRA_TITLE' not in listener
assert 'getActiveNotifications' in listener
assert 'notification.getNotification().flags' in listener
assert 'onSaveInstanceState' in java
assert 'savedInstanceState' in java
assert 'android.permission.INTERNET' in permissions
assert 'android.permission.READ_CALENDAR' not in permissions
assert 'android.permission.WRITE_CALENDAR' not in permissions
assert 'AccessibilityService' not in java
assert 'android.accessibilityservice' not in ET.tostring(manifest, encoding='unicode')
assert 'new Intent(Intent.ACTION_INSERT)' in java
assert 'CalendarContract.Events.CONTENT_URI' in java
assert 'DatePickerDialog' in java and 'TimePickerDialog' in java
assert 'ReplyAssistant.suggest' in java
assert 'metricsDate' in java and 'saveMetrics' in java
assert 'MetricsMath.parseCount' in java
assert 'Intent.createChooser' in java
assert 'store.saveReminder' in java
assert 'ContentSchedule.ideaFor' in java
assert 'Không đăng tự động' in java
assert 'MCP độc lập' in java and 'mcpClient.pair' in java
assert "versionName '0.3.1'" in (root / 'app/build.gradle').read_text()
assert (root / 'app/src/main/java/vn/sangit/androidagent/ReplyAssistant.java').exists()
assert (root / 'app/src/main/java/vn/sangit/androidagent/MetricsMath.java').exists()
assert (root / 'app/src/main/java/vn/sangit/androidagent/ContentSchedule.java').exists()
assert (root / '.codex/codex.md').exists()
assert (root / '.codex/STATUS.md').exists()
print('PASS: offline manifest/permissions/share/notification/planner/dashboard guards')
