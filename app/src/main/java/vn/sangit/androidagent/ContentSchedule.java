package vn.sangit.androidagent;

import java.time.DayOfWeek;
import java.time.LocalDate;

/** Simple local editorial guide; no server calls or unattended publishing. */
public final class ContentSchedule {
    private ContentSchedule() {}

    public static String ideaFor(LocalDate date) {
        if (date == null) throw new IllegalArgumentException("date is required");
        DayOfWeek day = date.getDayOfWeek();
        switch (day) {
            case MONDAY: return "Bài: chuyện đời thường và công việc";
            case TUESDAY: return "Reel: mẹo sửa chữa thực tế";
            case WEDNESDAY: return "Bài: giới thiệu bản thân / chuyện nghề";
            case THURSDAY: return "Bài: mẹo máy lạnh tiết kiệm điện";
            case FRIDAY: return "Bài: một ca sửa chữa thật";
            case SATURDAY: return "Reel: chuyện nghề vui và hữu ích";
            default: return "Bài: mẹo máy giặt / thiết bị gia dụng";
        }
    }

    public static boolean validReminder(long whenMillis, long nowMillis) {
        return whenMillis > nowMillis && whenMillis - nowMillis <= 366L * 24 * 60 * 60 * 1000;
    }
}
