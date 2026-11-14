package vn.sangit.androidagent;

/** Strict platform package allowlist; deliberately does not inspect message bodies. */
public final class NotificationPolicy {
    public static final String FACEBOOK = "com.facebook.katana";
    public static final String MESSENGER = "com.facebook.orca";

    private NotificationPolicy() {}

    public static boolean isFacebook(String packageName) {
        return FACEBOOK.equals(packageName);
    }

    public static boolean isMessenger(String packageName) {
        return MESSENGER.equals(packageName);
    }

    public static boolean isSupported(String packageName) {
        return isFacebook(packageName) || isMessenger(packageName);
    }
}
