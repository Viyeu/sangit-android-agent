package vn.sangit.androidagent;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Insets;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.CalendarContract;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;

/** Single-screen, local-first Android personal Facebook assistant. */
public final class MainActivity extends Activity {
    private static final int PICK_IMAGE = 401;
    private static final int PRIMARY = Color.rgb(14, 104, 220);
    private static final int INK = Color.rgb(25, 39, 57);
    private static final int MUTED = Color.rgb(93, 110, 132);
    private static final int BACKGROUND = Color.rgb(246, 249, 253);
    private static final int BORDER = Color.rgb(223, 232, 242);
    private static final String PROFILE = "https://www.facebook.com/sangitinfo/";
    private static final String FACEBOOK_APP = "https://www.facebook.com/";

    private LocalStore store;
    private final McpDeviceClient mcpClient = new McpDeviceClient();
    private TextView notificationSummary;
    private TextView metricsSummary;
    private TextView reminderSummary;
    private EditText followerInput;
    private EditText viewInput;
    private EditText interactionInput;
    private ZonedDateTime chosenReminder;

    private TextView imageSummary;
    private EditText postEditor;
    private EditText replyEditor;
    private Uri selectedImage;
    private boolean skipFirstSpinnerEvent = true;
    private String restoredPost;
    private String restoredReply;
    private int restoredTemplate;
    private long restoredReminderMillis;
    private String restoredFollowers;
    private String restoredViews;
    private String restoredInteractions;


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        store = new LocalStore(this);
        if (savedInstanceState != null) {
            restoredPost = savedInstanceState.getString("draft_editor");
            restoredReply = savedInstanceState.getString("reply_editor");
            restoredTemplate = savedInstanceState.getInt("template", 0);
            restoredReminderMillis = savedInstanceState.getLong("reminder_choice", 0L);
            restoredFollowers = savedInstanceState.getString("metrics_followers_input");
            restoredViews = savedInstanceState.getString("metrics_views_input");
            restoredInteractions = savedInstanceState.getString("metrics_interactions_input");
            String image = savedInstanceState.getString("image_uri");
            if (image != null) selectedImage = Uri.parse(image);
        }
        getWindow().setStatusBarColor(BACKGROUND);
        getWindow().setNavigationBarColor(BACKGROUND);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        render();
    }

    @Override protected void onSaveInstanceState(Bundle outState) {
        if (postEditor != null) outState.putString("draft_editor", postEditor.getText().toString());
        if (replyEditor != null) outState.putString("reply_editor", replyEditor.getText().toString());
        if (selectedImage != null) outState.putString("image_uri", selectedImage.toString());
        outState.putInt("template", restoredTemplate);
        if (chosenReminder != null) outState.putLong("reminder_choice", chosenReminder.toInstant().toEpochMilli());
        if (followerInput != null) outState.putString("metrics_followers_input", followerInput.getText().toString());
        if (viewInput != null) outState.putString("metrics_views_input", viewInput.getText().toString());
        if (interactionInput != null) outState.putString("metrics_interactions_input", interactionInput.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override protected void onResume() {
        super.onResume();
        if (notificationSummary != null) updateNotificationSummary();
        if (metricsSummary != null) updateMetricsSummary();
    }

    private void render() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BACKGROUND);
        if (Build.VERSION.SDK_INT >= 35) {
            root.setOnApplyWindowInsetsListener((view, insets) -> {
                Insets bars = insets.getInsets(WindowInsets.Type.systemBars());
                view.setPadding(0, bars.top, 0, bars.bottom);
                return insets;
            });
        }
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.setVerticalScrollBarEnabled(false);
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(16), dp(18), dp(16), dp(32));
        scroll.addView(body);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, -1));
        setContentView(root);

        TextView eyebrow = text("SANGIT · ANDROID AGENT 0.3", 12, PRIMARY, true);
        body.addView(eyebrow);
        TextView header = text("Chăm sóc Facebook cá nhân", 25, INK, true);
        header.setPadding(0, dp(8), 0, dp(5));
        body.addView(header);
        TextView subtitle = text("Phan Văn Sang · facebook.com/sangitinfo", 14, MUTED, false);
        body.addView(subtitle);
        body.addView(space(16));

        LinearLayout statusCard = card(body, "An toàn · Dữ liệu cá nhân chỉ trên thiết bị");
        statusCard.addView(text("Không đăng tự động, không đọc nội dung tin nhắn. Chỉ gửi trạng thái ứng dụng khi bạn chủ động ghép cặp và xác nhận.", 13, MUTED, false));
        statusCard.addView(space(9));
        Button profileBtn = button("Mở hồ sơ Facebook", false);
        profileBtn.setOnClickListener(v -> openUrl(PROFILE));
        statusCard.addView(profileBtn);

        LinearLayout dashboard = card(body, "1 · Tổng quan (nhập số liệu thật)");
        dashboard.addView(text("Lấy số liệu tại Bảng điều khiển chuyên nghiệp Facebook rồi nhập thủ công. "
            + "Agent không có API đọc thống kê cá nhân.", 12, MUTED, false));
        metricsSummary = text("Chưa nhập số liệu", 14, INK, true);
        dashboard.addView(metricsSummary);
        updateMetricsSummary();
        LinearLayout numbers = new LinearLayout(this);
        numbers.setOrientation(LinearLayout.HORIZONTAL);
        dashboard.addView(numbers);
        followerInput = metricField(numbers, "Theo dõi", restoredFollowers != null ? restoredFollowers :
            (store.metricsDate().isEmpty() ? "" : Integer.toString(store.followers())));
        viewInput = metricField(numbers, "Lượt xem", restoredViews != null ? restoredViews :
            (store.metricsDate().isEmpty() ? "" : Integer.toString(store.views())));
        interactionInput = metricField(numbers, "Tương tác", restoredInteractions != null ? restoredInteractions :
            (store.metricsDate().isEmpty() ? "" : Integer.toString(store.interactions())));
        Button saveMetrics = button("Lưu thống kê hôm nay", false);
        saveMetrics.setOnClickListener(v -> {
            try {
                int f = MetricsMath.parseCount(followerInput.getText().toString());
                int views = MetricsMath.parseCount(viewInput.getText().toString());
                int interactions = MetricsMath.parseCount(interactionInput.getText().toString());
                store.saveMetrics(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).toString(), f, views, interactions);
                updateMetricsSummary();
                Toast.makeText(this, "Đã lưu dữ liệu nhập tay", Toast.LENGTH_SHORT).show();
            } catch (IllegalArgumentException error) {
                Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        dashboard.addView(saveMetrics);

        LinearLayout notice = card(body, "2 · Thông báo được cho phép");
        notificationSummary = text("Đang kiểm tra quyền...", 14, INK, false);
        notice.addView(notificationSummary);
        notice.addView(space(8));
        Switch enable = new Switch(this);
        enable.setText("Bật thống kê thông báo cục bộ");
        enable.setTextColor(INK);
        enable.setTextSize(14f);
        enable.setChecked(store.notificationsEnabled());
        notice.addView(enable);
        enable.setOnCheckedChangeListener((unused, checked) -> {
            store.setNotificationsEnabled(checked);
            updateNotificationSummary();
            if (checked && !isNotificationAccessGranted()) {
                Toast.makeText(this, "Cần cấp quyền trong Cài đặt Android", Toast.LENGTH_LONG).show();
            }
        });
        Button permission = button("Mở cài đặt quyền thông báo", false);
        permission.setOnClickListener(v -> {
            try { startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)); }
            catch (Exception error) { Toast.makeText(this, "Không mở được phần cài đặt", Toast.LENGTH_SHORT).show(); }
        });
        notice.addView(permission);
        notice.addView(text("Chỉ đếm thông báo Facebook/Messenger đang hiển thị. Android cấp quyền này cho toàn bộ thông báo; Agent không đọc/lưu nội dung hoặc danh tính người gửi. Có thể tắt bất cứ lúc nào.", 12, MUTED, false));

        LinearLayout inbox = card(body, "3 · Hộp việc cần xử lý");
        inbox.addView(text("Thông báo hiện có được hiển thị ở mục trên. Bấm để tự xem nội dung ngay trong Facebook/Messenger; "
            + "Agent không lưu hoặc phân loại nội dung người gửi.", 12, MUTED, false));
        Button facebookNotices = button("Mở thông báo Facebook", false);
        facebookNotices.setOnClickListener(v -> openUrl("https://www.facebook.com/notifications/"));
        inbox.addView(facebookNotices);
        Button messenger = button("Mở tin nhắn Messenger", false);
        messenger.setOnClickListener(v -> openUrl("https://www.messenger.com/"));
        inbox.addView(messenger);

        LinearLayout post = card(body, "4 · Soạn và chia sẻ bài viết");
        post.addView(text("Chọn mẫu, sửa nội dung theo ca thực tế; bạn tự xác nhận trong Facebook.", 13, MUTED, false));
        post.addView(space(8));
        Spinner templates = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, TemplateCatalog.titles());
        templates.setAdapter(adapter);
        templates.setSelection(restoredTemplate);
        post.addView(templates);
        postEditor = editor(TemplateCatalog.textFor(restoredTemplate), 9);
        String saved = restoredPost != null ? restoredPost : store.draft();
        if (!saved.isEmpty()) postEditor.setText(saved);
        post.addView(postEditor);
        templates.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (skipFirstSpinnerEvent) { skipFirstSpinnerEvent = false; return; }
                restoredTemplate = position;
                postEditor.setText(TemplateCatalog.textFor(position));
                postEditor.setSelection(postEditor.length());
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        Button save = button("Lưu bản nháp", false);
        save.setOnClickListener(v -> {
            store.saveDraft(postEditor.getText().toString());
            Toast.makeText(this, "Đã lưu nháp trên thiết bị", Toast.LENGTH_SHORT).show();
        });
        post.addView(save);
        Button selectImage = button("Chọn ảnh từ thư viện", false);
        selectImage.setOnClickListener(v -> selectImage());
        post.addView(selectImage);
        imageSummary = text(selectedImage == null ? "Chưa chọn ảnh" : "Đã chọn ảnh · chỉ dùng trong phiên hiện tại", 12, MUTED, false);
        post.addView(imageSummary);
        Button share = button("Chia sẻ qua Facebook / ứng dụng", true);
        share.setOnClickListener(v -> shareDraft());
        post.addView(share);
        Button copy = button("Sao chép bài viết", false);
        copy.setOnClickListener(v -> copyToClipboard(postEditor.getText().toString()));
        post.addView(copy);
        post.addView(text("Lưu ý: Một số phiên bản Facebook không nhận nội dung chữ khi chia sẻ kèm ảnh. Có thể dùng Sao chép rồi dán vào bài trước khi đăng.", 12, MUTED, false));

        LinearLayout reply = card(body, "5 · Gợi ý phản hồi (offline)");
        reply.addView(text("Chọn loại tình huống, Agent tạo câu trả lời theo mẫu an toàn. "
            + "Đây là quy tắc cục bộ, KHÔNG phải AI sinh văn bản hay đọc tin nhắn Facebook.", 12, MUTED, false));
        Spinner category = new Spinner(this);
        category.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, ReplyAssistant.categories()));
        reply.addView(category);
        Button suggestReply = button("Gợi ý phản hồi", false);
        reply.addView(suggestReply);
        replyEditor = editor(restoredReply != null ? restoredReply : store.reply(), 4);
        reply.addView(replyEditor);
        suggestReply.setOnClickListener(v -> replyEditor.setText(ReplyAssistant.suggest(category.getSelectedItemPosition())));
        Button askAi = button("Nhờ ChatGPT chỉnh phản hồi (tùy chọn)", false);
        askAi.setOnClickListener(v -> {
            String text = replyEditor.getText().toString().trim();
            if (text.isEmpty()) { Toast.makeText(this, "Chọn gợi ý hoặc nhập nháp trước", Toast.LENGTH_SHORT).show(); return; }
            String prompt = "Hãy viết lại phản hồi Facebook cá nhân tự nhiên, thân thiện, ngắn gọn, "
                + "không tự nhận đã kiểm tra hay báo giá khi chưa có bằng chứng. "
                + "Không nhắc thông tin cá nhân của người khác. Chỉ đưa ra 1 câu trả lời để tôi tự duyệt:\n\n" + text;
            Intent aiShare = new Intent(Intent.ACTION_SEND);
            aiShare.setType("text/plain");
            aiShare.putExtra(Intent.EXTRA_TEXT, prompt);
            try { startActivity(Intent.createChooser(aiShare, "Chọn ChatGPT hoặc ứng dụng AI")); }
            catch (Exception error) { Toast.makeText(this, "Không mở được ứng dụng chia sẻ", Toast.LENGTH_SHORT).show(); }
        });
        reply.addView(askAi);
        reply.addView(text("Không nhập mật khẩu, số điện thoại, địa chỉ hay nội dung riêng tư của khách vào AI. "
            + "Bạn chọn ứng dụng và kiểm tra trước khi chia sẻ.", 12, MUTED, false));
        Button copyReply = button("Sao chép phản hồi và mở Facebook", true);
        copyReply.setOnClickListener(v -> {
            String value = replyEditor.getText().toString();
            if (value.trim().isEmpty()) { Toast.makeText(this, "Nhập phản hồi trước", Toast.LENGTH_SHORT).show(); return; }
            store.saveReply(value);
            copyToClipboard(value);
            openUrl(FACEBOOK_APP);
        });
        reply.addView(copyReply);
        reply.addView(text("Không dán thông tin riêng tư của khách hàng vào phản hồi công khai.", 12, MUTED, false));

        LinearLayout planner = card(body, "6 · Lịch nội dung & nhắc đăng");
        planner.addView(text("Gợi ý 7 ngày · 1 bài/ngày, 2 Story/ngày chỉ là mục tiêu thử nghiệm. "
            + "Nhắc qua ứng dụng Lịch do bạn tự xác nhận; không tự đăng Facebook.", 12, MUTED, false));
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        for (int i = 0; i < 7; i++) {
            LocalDate day = today.plusDays(i);
            planner.addView(text(day + " · " + ContentSchedule.ideaFor(day), 12, INK, false));
        }
        planner.addView(space(8));
        long savedWhen = store.reminderMillis();
        chosenReminder = restoredReminderMillis > 0
            ? Instant.ofEpochMilli(restoredReminderMillis).atZone(ZoneId.of("Asia/Ho_Chi_Minh"))
            : savedWhen > System.currentTimeMillis()
            ? Instant.ofEpochMilli(savedWhen).atZone(ZoneId.of("Asia/Ho_Chi_Minh"))
            : ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).withHour(19).withMinute(0).withSecond(0).withNano(0);
        if (!chosenReminder.isAfter(ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")))) chosenReminder = chosenReminder.plusDays(1);
        reminderSummary = text("", 14, INK, true);
        planner.addView(reminderSummary);
        updateReminderSummary();
        Button chooseDate = button("Chọn ngày và giờ nhắc", false);
        chooseDate.setOnClickListener(v -> pickReminderDateTime());
        planner.addView(chooseDate);
        Button addCalendar = button("Chuẩn bị lời nhắc trong Lịch", true);
        addCalendar.setOnClickListener(v -> addCalendarReminder());
        planner.addView(addCalendar);
        Button clearReminder = button("Xóa nhắc đăng đã lưu trong Agent", false);
        clearReminder.setOnClickListener(v -> { store.clearReminder(); updateReminderSummary(); });
        planner.addView(clearReminder);
        planner.addView(text("Lưu trong Agent không tự tạo chuông báo. Bạn phải bấm Lưu/Xác nhận trong ứng dụng Lịch.", 12, MUTED, false));

        LinearLayout schedule = card(body, "7 · Checklist chăm sóc hôm nay");
        String date = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).toString();
        schedule.addView(text("Ngày " + date + " · Thử nghiệm, không tự đăng bài", 12, MUTED, false));
        schedule.addView(space(6));
        addTask(schedule, date, "morning", "07:00 · Soạn bài / đăng Story");
        addTask(schedule, date, "noon", "12:00 · Xem thông báo / trả lời thủ công");
        addTask(schedule, date, "evening", "19:00 · Duyệt và đăng bài");
        addTask(schedule, date, "night", "21:00 · Kiểm tra kết quả");

        LinearLayout mcpPanel = card(body, "8 · ChatGPT ↔ Android (MCP độc lập)");
        mcpPanel.addView(text("Chỉ gửi phiên bản Agent và Android SDK khi bạn bấm nút. Không đọc tin nhắn, nháp hoặc thống kê; không chạy nền.", 12, MUTED, false));
        EditText endpoint = editor("", 1);
        endpoint.setSingleLine(true);
        endpoint.setHint("https://android-mcp.sangit.vn");
        mcpPanel.addView(endpoint);
        EditText pairingCode = editor("", 1);
        pairingCode.setSingleLine(true);
        pairingCode.setHint("Nhập mã ghép cặp do ChatGPT cấp");
        mcpPanel.addView(pairingCode);
        TextView pairingState = text("Chưa kết nối · cần máy chủ HTTPS MCP độc lập", 13, INK, true);
        mcpPanel.addView(pairingState);
        Button pairButton = button("Ghép cặp MCP", false);
        pairButton.setOnClickListener(v -> {
            pairingState.setText("Đang xác minh mã...");
            mcpClient.pair(endpoint.getText().toString(), pairingCode.getText().toString(),
                message -> runOnUiThread(() -> {
                    pairingCode.setText("");
                    pairingState.setText(message);
                }));
        });
        mcpPanel.addView(pairButton);
        Button statusButton = button("Gửi trạng thái theo yêu cầu", false);
        statusButton.setOnClickListener(v -> mcpClient.checkIn(
            message -> runOnUiThread(() -> pairingState.setText(message))));
        mcpPanel.addView(statusButton);
        Button disconnectButton = button("Ngắt kết nối và thu hồi phiên", false);
        disconnectButton.setOnClickListener(v -> mcpClient.disconnect(
            message -> runOnUiThread(() -> pairingState.setText(message))));
        mcpPanel.addView(disconnectButton);
        mcpPanel.addView(text("Máy chủ Android MCP độc lập, không qua SangIT Core. Phiên chỉ ở bộ nhớ; đóng ứng dụng cần ghép cặp lại.", 12, MUTED, false));

        TextView footer = text("SangIT Android Agent · v0.3.0 beta · Thông báo chỉ đếm khi bạn bật", 12, MUTED, false);
        footer.setGravity(Gravity.CENTER_HORIZONTAL);
        footer.setPadding(0, dp(10), 0, 0);
        body.addView(footer);
    }

    private EditText metricField(LinearLayout container, String label, String value) {
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 1f);
        lp.setMargins(0, dp(8), dp(6), dp(6));
        container.addView(col, lp);
        col.addView(text(label, 12, MUTED, false));
        EditText field = new EditText(this);
        field.setSingleLine(true);
        field.setInputType(InputType.TYPE_CLASS_NUMBER);
        field.setTextColor(INK);
        field.setTextSize(16f);
        field.setPadding(dp(8), dp(10), dp(8), dp(10));
        field.setBackground(rounded(BACKGROUND, 10, BORDER));
        field.setText(value);
        col.addView(field);
        return field;
    }

    private void updateMetricsSummary() {
        if (metricsSummary == null) return;
        if (store.metricsDate().isEmpty()) {
            metricsSummary.setText("Chưa có thống kê · nhập 3 chỉ số bên dưới");
            return;
        }
        String trend = store.previousMetricsDate().isEmpty() ? "Chưa có kỳ trước" :
            "Thay đổi follower so với " + store.previousMetricsDate() + ": " +
                MetricsMath.delta(store.previousFollowers(), store.followers());
        metricsSummary.setText("Cập nhật " + store.metricsDate() + " · " + store.followers() + " follower · " +
            store.views() + " lượt xem · " + store.interactions() + " tương tác\n" + trend);
    }

    private void updateReminderSummary() {
        if (reminderSummary == null) return;
        String info = "Dự kiến: " + chosenReminder.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        long saved = store.reminderMillis();
        if (saved > 0) info += "\nĐã lưu trong Agent: " +
            Instant.ofEpochMilli(saved).atZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + " (không phải báo thức)";
        reminderSummary.setText(info);
    }

    private void pickReminderDateTime() {
        ZonedDateTime selected = chosenReminder;
        DatePickerDialog date = new DatePickerDialog(this, (view, year, month, day) -> {
            TimePickerDialog time = new TimePickerDialog(this, (picker, hour, minute) -> {
                chosenReminder = LocalDate.of(year, month + 1, day).atTime(hour, minute)
                    .atZone(ZoneId.of("Asia/Ho_Chi_Minh"));
                updateReminderSummary();
            }, selected.getHour(), selected.getMinute(), true);
            time.show();
        }, selected.getYear(), selected.getMonthValue() - 1, selected.getDayOfMonth());
        date.getDatePicker().setMinDate(System.currentTimeMillis() - 86400000L);
        date.show();
    }

    private void addCalendarReminder() {
        String content = postEditor.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(this, "Hãy soạn nội dung trước khi thêm lịch", Toast.LENGTH_LONG).show();
            return;
        }
        long when = chosenReminder.toInstant().toEpochMilli();
        if (!ContentSchedule.validReminder(when, System.currentTimeMillis())) {
            Toast.makeText(this, "Chọn thời điểm tương lai trong vòng 1 năm", Toast.LENGTH_LONG).show();
            return;
        }
        store.saveDraft(content);
        store.saveReminder("Duyệt bài Facebook cá nhân", content, when);
        updateReminderSummary();
        Intent event = new Intent(Intent.ACTION_INSERT)
            .setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.Events.TITLE, "Duyệt đăng Facebook cá nhân - SangIT")
            .putExtra(CalendarContract.Events.DESCRIPTION, "Mở Facebook, kiểm tra rồi tự đăng. Nội dung nháp:\n" + content)
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, when)
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, when + 15 * 60 * 1000L);
        try {
            startActivity(event);
            Toast.makeText(this, "Chọn Lưu trong ứng dụng Lịch để bật nhắc", Toast.LENGTH_LONG).show();
        } catch (Exception error) {
            Toast.makeText(this, "Đã lưu trong Agent; máy chưa có ứng dụng Lịch phù hợp", Toast.LENGTH_LONG).show();
        }
    }

    private void addTask(LinearLayout container, String date, String id, String label) {
        CheckBox check = new CheckBox(this);
        check.setText(label);
        check.setTextColor(INK);
        check.setTextSize(14f);
        check.setChecked(store.taskDone(id, date));
        check.setOnCheckedChangeListener((button, value) -> store.setTaskDone(id, date, value));
        container.addView(check);
    }

    private void updateNotificationSummary() {
        if (!store.notificationsEnabled()) {
            notificationSummary.setText("Đang tắt. Không thống kê thông báo.");
        } else if (!isNotificationAccessGranted()) {
            notificationSummary.setText("Đã bật trong Agent, chưa cấp quyền Android.");
        } else {
            notificationSummary.setText("Đang hiển thị: Facebook " + store.facebookCount()
                + " · Messenger " + store.messengerCount() + "\n"
                + "Chỉ là số thông báo còn hiển thị trên máy; không phải số bình luận hay tin nhắn mới.");
        }
    }

    private boolean isNotificationAccessGranted() {
        String value = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        return value != null && value.contains(new ComponentName(this, FacebookNotificationListener.class).flattenToString());
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try { startActivityForResult(Intent.createChooser(intent, "Chọn một ảnh"), PICK_IMAGE); }
        catch (Exception error) { Toast.makeText(this, "Không mở được thư viện ảnh", Toast.LENGTH_SHORT).show(); }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImage = data.getData();
            imageSummary.setText("Đã chọn ảnh · chỉ dùng trong phiên hiện tại");
        }
    }

    private void shareDraft() {
        String content = postEditor.getText().toString();
        if (content.trim().isEmpty() && selectedImage == null) {
            Toast.makeText(this, "Nhập bài viết hoặc chọn ảnh", Toast.LENGTH_SHORT).show();
            return;
        }
        store.saveDraft(content);
        Intent send = new Intent(Intent.ACTION_SEND);
        send.putExtra(Intent.EXTRA_TEXT, content);
        if (selectedImage != null) {
            send.setType("image/*");
            send.putExtra(Intent.EXTRA_STREAM, selectedImage);
            send.setClipData(ClipData.newRawUri("Ảnh bài viết", selectedImage));
            send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            send.setType("text/plain");
        }
        try { startActivity(Intent.createChooser(send, "Chọn Facebook hoặc ứng dụng")); }
        catch (Exception error) { Toast.makeText(this, "Không tìm thấy ứng dụng chia sẻ", Toast.LENGTH_SHORT).show(); }
    }

    private void copyToClipboard(String value) {
        if (value.trim().isEmpty()) { Toast.makeText(this, "Chưa có nội dung", Toast.LENGTH_SHORT).show(); return; }
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText("SangIT nội dung", value));
            Toast.makeText(this, "Đã sao chép", Toast.LENGTH_SHORT).show();
        }
    }

    private void openUrl(String url) {
        try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); }
        catch (Exception error) { Toast.makeText(this, "Không mở được Facebook", Toast.LENGTH_SHORT).show(); }
    }

    private LinearLayout card(LinearLayout parent, String heading) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackground(rounded(Color.WHITE, 18, BORDER));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(12);
        parent.addView(card, params);
        TextView title = text(heading, 17, INK, true);
        title.setPadding(0, 0, 0, dp(9));
        card.addView(title);
        return card;
    }

    private EditText editor(String value, int lines) {
        EditText input = new EditText(this);
        input.setTextSize(14f);
        input.setTextColor(INK);
        input.setGravity(Gravity.TOP | Gravity.START);
        input.setMinLines(lines);
        input.setMaxLines(Math.max(lines, 14));
        input.setSingleLine(false);
        input.setPadding(dp(10), dp(10), dp(10), dp(10));
        input.setBackground(rounded(BACKGROUND, 12, BORDER));
        input.setText(value);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = dp(7);
        params.bottomMargin = dp(8);
        input.setLayoutParams(params);
        return input;
    }

    private Button button(String label, boolean primary) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setText(label);
        button.setTextSize(14f);
        button.setTypeface(null, Typeface.BOLD);
        button.setTextColor(primary ? Color.WHITE : INK);
        button.setMinHeight(dp(44));
        button.setBackground(rounded(primary ? PRIMARY : BACKGROUND, 12, primary ? PRIMARY : BORDER));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = dp(7);
        button.setLayoutParams(params);
        return button;
    }

    private GradientDrawable rounded(int background, int radiusDp, int stroke) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(background);
        drawable.setCornerRadius(dp(radiusDp));
        drawable.setStroke(dp(1), stroke);
        return drawable;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextSize(size);
        text.setTextColor(color);
        if (bold) text.setTypeface(null, Typeface.BOLD);
        return text;
    }

    private View space(int height) {
        View view = new View(this);
        view.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(height)));
        return view;
    }

    private int dp(float value) { return (int) (value * getResources().getDisplayMetrics().density + 0.5f); }
}
