package vn.sangit.androidagent;

import android.content.Context;
import android.content.SharedPreferences;

/** On-device preferences. Notification contents are never stored or exported. */
public final class LocalStore {
    private static final String NAME = "sangit_android_agent_v01";
    private final SharedPreferences prefs;

    public LocalStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public boolean notificationsEnabled() {
        return prefs.getBoolean("notifications_opt_in", false);
    }

    public void setNotificationsEnabled(boolean enabled) {
        SharedPreferences.Editor editor = prefs.edit().putBoolean("notifications_opt_in", enabled);
        if (!enabled) editor.putInt("active_facebook", 0).putInt("active_messenger", 0);
        editor.apply();
    }

    public void setActiveCounts(int facebook, int messenger) {
        if (!notificationsEnabled()) return;
        prefs.edit().putInt("active_facebook", Math.max(0, facebook))
            .putInt("active_messenger", Math.max(0, messenger)).apply();
    }

    public int facebookCount() { return notificationsEnabled() ? prefs.getInt("active_facebook", 0) : 0; }
    public int messengerCount() { return notificationsEnabled() ? prefs.getInt("active_messenger", 0) : 0; }
    public String draft() { return prefs.getString("draft", ""); }
    public void saveDraft(String draft) { prefs.edit().putString("draft", draft).apply(); }
    public String reply() { return prefs.getString("reply", TemplateCatalog.exampleReply()); }
    public void saveReply(String reply) { prefs.edit().putString("reply", reply).apply(); }

    public boolean taskDone(String taskId, String localDate) {
        return prefs.getBoolean("task_" + localDate + "_" + taskId, false);
    }

    public void setTaskDone(String taskId, String localDate, boolean done) {
        prefs.edit().putBoolean("task_" + localDate + "_" + taskId, done).apply();
    }
    /** Last and previous manual snapshots; not Facebook API data. */
    public String metricsDate() { return prefs.getString("metrics_date", ""); }
    public int followers() { return prefs.getInt("metrics_followers", 0); }
    public int views() { return prefs.getInt("metrics_views", 0); }
    public int interactions() { return prefs.getInt("metrics_interactions", 0); }
    public String previousMetricsDate() { return prefs.getString("prev_metrics_date", ""); }
    public int previousFollowers() { return prefs.getInt("prev_metrics_followers", 0); }

    public void saveMetrics(String localDate, int followers, int views, int interactions) {
        if (localDate == null || localDate.isEmpty() || followers < 0 || views < 0 || interactions < 0) {
            throw new IllegalArgumentException("Invalid metrics");
        }
        SharedPreferences.Editor edit = prefs.edit();
        String lastDate = metricsDate();
        if (!lastDate.isEmpty() && !lastDate.equals(localDate)) {
            edit.putString("prev_metrics_date", lastDate).putInt("prev_metrics_followers", followers());
        }
        edit.putString("metrics_date", localDate).putInt("metrics_followers", followers)
            .putInt("metrics_views", views).putInt("metrics_interactions", interactions).apply();
    }

    /** One pending reminder only, intentionally no scheduler/foreground service. */
    public String reminderTitle() { return prefs.getString("reminder_title", ""); }
    public String reminderContent() { return prefs.getString("reminder_content", ""); }
    public long reminderMillis() { return prefs.getLong("reminder_when", 0L); }
    public void saveReminder(String title, String content, long whenMillis) {
        prefs.edit().putString("reminder_title", title).putString("reminder_content", content)
            .putLong("reminder_when", whenMillis).apply();
    }
    public void clearReminder() {
        prefs.edit().remove("reminder_title").remove("reminder_content").remove("reminder_when").apply();
    }

}
