package vn.sangit.androidagent;

import android.app.Notification;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

/**
 * Optional passive listener. Only counts CURRENT active FB/Messenger notifications.
 * No notification text, sender, messages, cookies or tokens are read or persisted.
 */
public final class FacebookNotificationListener extends NotificationListenerService {
    @Override public void onListenerConnected() { updateCounts(); }
    @Override public void onNotificationPosted(StatusBarNotification sbn) { updateCounts(); }
    @Override public void onNotificationRemoved(StatusBarNotification sbn) { updateCounts(); }
    @Override public void onListenerDisconnected() {
        LocalStore store = new LocalStore(this);
        if (store.notificationsEnabled()) store.setActiveCounts(0, 0);
    }

    private void updateCounts() {
        LocalStore store = new LocalStore(this);
        if (!store.notificationsEnabled()) return;
        StatusBarNotification[] active = getActiveNotifications();
        if (active == null) return;
        int fb = 0, msg = 0;
        for (StatusBarNotification notification : active) {
            if (notification == null || notification.getNotification() == null) continue;
            if ((notification.getNotification().flags & Notification.FLAG_GROUP_SUMMARY) != 0) continue;
            String packageName = notification.getPackageName();
            if (NotificationPolicy.isFacebook(packageName)) fb++;
            else if (NotificationPolicy.isMessenger(packageName)) msg++;
        }
        store.setActiveCounts(fb, msg);
    }
}
