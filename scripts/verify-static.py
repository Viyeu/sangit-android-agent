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
assert 'Không tự đăng bài' in java and 'không tự thu thập dữ liệu' in java.lower()
assert 'MCP độc lập' in java and 'mcpClient.pair' in java
assert 'activateTab(selectedTab, false)' in java
assert 'tabPages[i].setVisibility' in java
assert 'selected_tab' in java
assert 'refreshDailyContent()' in java and 'showPlanDays(today)' in java
assert 'mcp-android.example.com' not in java
assert 'show_all_plan_days' in java
assert 'tabIcons[i].setImageTintList' in java
assert 'applyIcon(button, actionIcon(label)' in java
assert 'setPairingStatus(state, busy ?' in java and 'updateMcpControls' in java
assert 'addCompactActions(inbox, facebookNotices, messenger)' in java
assert 'addCompactActions(post, save, selectImage)' in java
assert 'screenWidthDp < 360' in java
assert 'SOFT_INPUT_ADJUST_RESIZE' in java
assert 'activateTab(1, true)' in java
assert 'postEditor = editor(TemplateCatalog.textFor(restoredTemplate), 6)' in java
assert 'startActivity(Intent.createChooser' in java
assert 'writingPanels[i].setVisibility' in java and 'writing_mode' in java
assert 'setMetricsEditorVisible' in java and 'metricAmounts[0]' in java
assert 'updateDailyProgress(date)' in java and 'dayProgress' in java
assert 'updateMcpControls' in java and 'mcpClient.isPaired()' in java
assert 'send.setVisibility(paired ? View.VISIBLE : View.GONE)' in java
assert 'revoke.setVisibility(paired ? View.VISIBLE : View.GONE)' in java
assert 'statusButton.setOnClickListener' in java and 'disconnectButton.setOnClickListener' in java
assert 'startActivity(Intent.createChooser' in java

icons = sorted((root / 'app/src/main/res/drawable').glob('ic_sangit_*.xml'))
assert len(icons) == 20, f'Expected 20 vector icons, got {len(icons)}'
for icon in icons:
    vector = ET.parse(icon).getroot()
    assert vector.tag.endswith('vector')
    assert any(x.tag.endswith('path') and x.attrib.get(ns + 'pathData') for x in vector.iter()), icon

assert "versionName '0.3.6'" in (root / 'app/build.gradle').read_text()
assert (root / 'app/src/main/java/vn/sangit/androidagent/ReplyAssistant.java').exists()
assert (root / 'app/src/main/java/vn/sangit/androidagent/MetricsMath.java').exists()
assert (root / 'app/src/main/java/vn/sangit/androidagent/ContentSchedule.java').exists()
assert (root / '.codex/codex.md').exists()
assert (root / '.codex/STATUS.md').exists()
print('PASS: offline manifest/permissions/share/notification/planner/dashboard guards')
