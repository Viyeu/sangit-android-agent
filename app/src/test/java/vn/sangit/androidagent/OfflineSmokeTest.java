package vn.sangit.androidagent;

import java.time.LocalDate;

/** No Android runtime needed: tests all pure Java business decisions. */
public final class OfflineSmokeTest {
    private static int count = 0;
    private static void ok(boolean value) {
        if (!value) throw new AssertionError("assertion " + (count + 1));
        count++;
    }
    private static void fails(Runnable operation) {
        try { operation.run(); throw new AssertionError("Expected exception"); }
        catch (IllegalArgumentException expected) { count++; }
    }
    public static void main(String[] args) {
        ok(TemplateCatalog.titles().length == 4);
        ok(TemplateCatalog.textFor(0).contains("Buôn Hồ"));
        ok(TemplateCatalog.textFor(1).contains("lưới lọc"));
        ok(TemplateCatalog.textFor(2).contains("thật"));
        ok(TemplateCatalog.textFor(3).isEmpty());
        ok(TemplateCatalog.textFor(-1).isEmpty());
        ok(TemplateCatalog.exampleReply().contains("Cảm ơn"));
        ok(NotificationPolicy.isFacebook("com.facebook.katana"));
        ok(NotificationPolicy.isMessenger("com.facebook.orca"));
        ok(NotificationPolicy.isSupported("com.facebook.katana"));
        ok(NotificationPolicy.isSupported("com.facebook.orca"));
        ok(!NotificationPolicy.isFacebook("com.facebook.orca"));
        ok(!NotificationPolicy.isSupported("com.fake.facebook.katana"));
        ok(!NotificationPolicy.isSupported(null));
        ok(!NotificationPolicy.isSupported(""));

        ok(ReplyAssistant.categories().length == 6);
        ok(ReplyAssistant.suggest(0).contains("báo giá"));
        ok(ReplyAssistant.suggest(1).contains("lịch"));
        ok(ReplyAssistant.suggest(2).contains("ngắt nguồn"));
        ok(ReplyAssistant.suggest(3).contains("Cảm ơn"));
        ok(ReplyAssistant.suggest(4).contains("nhắn riêng"));
        ok(!ReplyAssistant.suggest(5).isEmpty());
        ok(!ReplyAssistant.suggest(-1).isEmpty());
        ok(!ReplyAssistant.suggest(50).isEmpty());

        ok(ContentSchedule.ideaFor(LocalDate.of(2026, 11, 9)).contains("đời thường"));
        ok(ContentSchedule.ideaFor(LocalDate.of(2026, 11, 10)).contains("Reel"));
        ok(ContentSchedule.ideaFor(LocalDate.of(2026, 11, 11)).contains("giới thiệu"));
        ok(ContentSchedule.ideaFor(LocalDate.of(2026, 11, 12)).contains("máy lạnh"));
        ok(ContentSchedule.ideaFor(LocalDate.of(2026, 11, 13)).contains("ca sửa"));
        ok(ContentSchedule.ideaFor(LocalDate.of(2026, 11, 14)).contains("Reel"));
        ok(ContentSchedule.ideaFor(LocalDate.of(2026, 11, 15)).contains("máy giặt"));
        fails(() -> ContentSchedule.ideaFor(null));
        long now = 1_000_000L;
        ok(!ContentSchedule.validReminder(now, now));
        ok(!ContentSchedule.validReminder(now - 1, now));
        ok(ContentSchedule.validReminder(now + 1, now));
        ok(ContentSchedule.validReminder(now + 366L * 24 * 60 * 60 * 1000, now));
        ok(!ContentSchedule.validReminder(now + 367L * 24 * 60 * 60 * 1000, now));

        ok(MetricsMath.parseCount("0") == 0);
        ok(MetricsMath.parseCount(" 1253 ") == 1253);
        ok(MetricsMath.parseCount("999999999") == 999999999);
        fails(() -> MetricsMath.parseCount(null));
        fails(() -> MetricsMath.parseCount("-1"));
        fails(() -> MetricsMath.parseCount(""));
        fails(() -> MetricsMath.parseCount("1.2"));
        fails(() -> MetricsMath.parseCount("1000000000"));
        fails(() -> MetricsMath.parseCount("3K"));
        ok(MetricsMath.delta(200, 230).equals("+30"));
        ok(MetricsMath.delta(230, 200).equals("-30"));
        ok(MetricsMath.delta(200, 200).equals("0"));
        System.out.println("PASS: " + count + " offline Java assertions");
    }
}
