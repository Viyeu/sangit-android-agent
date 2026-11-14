package vn.sangit.androidagent;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.Context;
import android.graphics.Color;
import android.content.res.ColorStateList;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.graphics.Insets;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
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
    private static final int PRIMARY = Color.rgb(0, 112, 116);
    private static final int INK = Color.rgb(28, 42, 55);
    private static final int MUTED = Color.rgb(97, 113, 129);
    private static final int BACKGROUND = Color.rgb(245, 248, 250);
    private static final int BORDER = Color.rgb(221, 230, 235);
    private static final int SOFT = Color.rgb(229, 245, 243);
    private static final String PROFILE = "https://www.facebook.com/sangitinfo/";
    private static final String FACEBOOK_APP = "https://www.facebook.com/";

    private LocalStore store;
    private final McpDeviceClient mcpClient = new McpDeviceClient();
    private final LinearLayout[] tabPages = new LinearLayout[4];
    private final TextView[] tabLabels = new TextView[4];
    private final ImageView[] tabIcons = new ImageView[4];
    private final LinearLayout[] tabSurfaces = new LinearLayout[4];
    private ScrollView contentScroll;
    private TextView pageHeader;
    private TextView pageSubtitle;
    private int selectedTab = 0;
    private TextView metricsSummary;
    private TextView reminderSummary;
    private LinearLayout planIdeas;
    private LinearLayout dayChecklist;
    private TextView dayChecklistDate;
    private Button planExpandButton;
    private String displayedDay;
    private boolean showAllPlanDays;
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
            selectedTab = Math.max(0, Math.min(3, savedInstanceState.getInt("selected_tab", 0)));
            showAllPlanDays = savedInstanceState.getBoolean("show_all_plan_days", false);
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
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        render();
    }

    @Override protected void onSaveInstanceState(Bundle outState) {
        if (postEditor != null) outState.putString("draft_editor", postEditor.getText().toString());
        if (replyEditor != null) outState.putString("reply_editor", replyEditor.getText().toString());
        if (selectedImage != null) outState.putString("image_uri", selectedImage.toString());
        outState.putInt("selected_tab", selectedTab);
        outState.putBoolean("show_all_plan_days", showAllPlanDays);
        outState.putInt("template", restoredTemplate);
        if (chosenReminder != null) outState.putLong("reminder_choice", chosenReminder.toInstant().toEpochMilli());
        if (followerInput != null) outState.putString("metrics_followers_input", followerInput.getText().toString());
        if (viewInput != null) outState.putString("metrics_views_input", viewInput.getText().toString());
        if (interactionInput != null) outState.putString("metrics_interactions_input", interactionInput.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override protected void onResume() {
        super.onResume();
        if (metricsSummary != null) updateMetricsSummary();
        refreshDailyContent();
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
        contentScroll = scroll;
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.setVerticalScrollBarEnabled(false);
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(14), dp(10), dp(14), dp(20));
        scroll.addView(body);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));
        setContentView(root);

        TextView eyebrow = text("SANGIT  /  ANDROID AGENT 0.3.5", 10, PRIMARY, true);
        eyebrow.setLetterSpacing(0.06f);
        body.addView(eyebrow);
        pageHeader = text("Tổng quan", 24, INK, true);
        pageHeader.setPadding(0, dp(4), 0, dp(2));
        body.addView(pageHeader);
        pageSubtitle = text("Công cụ Facebook cá nhân", 12, MUTED, false);
        body.addView(pageSubtitle);
        body.addView(space(11));

        for (int i = 0; i < tabPages.length; i++) {
            LinearLayout page = new LinearLayout(this);
            page.setOrientation(LinearLayout.VERTICAL);
            body.addView(page);
            tabPages[i] = page;
        }
        LinearLayout homePage = tabPages[0];
        LinearLayout writingPage = tabPages[1];
        LinearLayout schedulePage = tabPages[2];
        LinearLayout connectPage = tabPages[3];

        LinearLayout navigation = new LinearLayout(this);
        navigation.setOrientation(LinearLayout.HORIZONTAL);
        navigation.setPadding(dp(6), dp(5), dp(6), dp(5));
        navigation.setBackgroundColor(Color.WHITE);
        navigation.setElevation(dp(8));
        String[] names = {"Tổng quan", "Bài viết", "Lịch", "MCP"};
        int[] icons = {R.drawable.ic_sangit_dashboard, R.drawable.ic_sangit_edit,
            R.drawable.ic_sangit_calendar, R.drawable.ic_sangit_link};
        for (int i = 0; i < names.length; i++) {
            final int index = i;
            LinearLayout tab = new LinearLayout(this);
            tab.setOrientation(LinearLayout.VERTICAL);
            tab.setGravity(Gravity.CENTER);
            tab.setFocusable(true);
            tab.setClickable(true);
            tab.setContentDescription("Mở mục " + names[i]);
            ImageView picture = new ImageView(this);
            picture.setImageResource(icons[i]);
            picture.setImageTintList(ColorStateList.valueOf(MUTED));
            tab.addView(picture, new LinearLayout.LayoutParams(dp(21), dp(21)));
            TextView label = text(names[i], 11, MUTED, false);
            label.setGravity(Gravity.CENTER);
            label.setPadding(0, dp(3), 0, 0);
            tab.addView(label);
            tab.setOnClickListener(v -> activateTab(index, true));
            navigation.addView(tab, new LinearLayout.LayoutParams(0, dp(58), 1f));
            tabLabels[i] = label;
            tabIcons[i] = picture;
            tabSurfaces[i] = tab;
        }
        root.addView(navigation, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout statusCard = card(homePage, "Facebook cá nhân");
        statusCard.addView(text("Không tự đăng bài, không đọc tin nhắn. Mọi chia sẻ đều do bạn xác nhận.", 12, MUTED, false));
        statusCard.addView(space(3));
        Button profileBtn = button("Mở hồ sơ Facebook", false);
        profileBtn.setOnClickListener(v -> openUrl(PROFILE));
        statusCard.addView(profileBtn);

        LinearLayout dashboard = card(homePage, "Chỉ số Facebook");
        dashboard.addView(text("Nhập từ Facebook; Agent không tự thu thập dữ liệu.", 12, MUTED, false));
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

        LinearLayout inbox = card(homePage, "Lối tắt Facebook");
        inbox.addView(text("Mở ứng dụng chính thức để xem và trả lời.", 12, MUTED, false));
        Button facebookNotices = button("Mở thông báo", false);
        facebookNotices.setOnClickListener(v -> openUrl("https://www.facebook.com/notifications/"));

        Button messenger = button("Mở tin nhắn", false);
        messenger.setOnClickListener(v -> openUrl("https://www.messenger.com/"));
        addCompactActions(inbox, facebookNotices, messenger);

        LinearLayout post = card(writingPage, "Soạn và chia sẻ bài viết");
        post.addView(text("Chọn mẫu, chỉnh sửa và tự xác nhận khi đăng.", 12, MUTED, false));
        post.addView(space(8));
        Spinner templates = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, TemplateCatalog.titles());
        templates.setAdapter(adapter);
        templates.setMinimumHeight(dp(48));
        templates.setSelection(restoredTemplate);
        post.addView(templates);
        postEditor = editor(TemplateCatalog.textFor(restoredTemplate), 6);
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
        Button save = button("Lưu nháp", false);
        save.setOnClickListener(v -> {
            store.saveDraft(postEditor.getText().toString());
            Toast.makeText(this, "Đã lưu nháp trên thiết bị", Toast.LENGTH_SHORT).show();
        });

        Button selectImage = button("Chọn ảnh", false);
        selectImage.setOnClickListener(v -> selectImage());
        addCompactActions(post, save, selectImage);
        imageSummary = text(selectedImage == null ? "Chưa chọn ảnh" : "Đã chọn ảnh · chỉ dùng trong phiên hiện tại", 12, MUTED, false);
        post.addView(imageSummary);
        Button share = button("Chia sẻ qua Facebook / ứng dụng", true);
        share.setOnClickListener(v -> shareDraft());
        post.addView(share);
        Button copy = button("Sao chép bài viết", false);
        copy.setOnClickListener(v -> copyToClipboard(postEditor.getText().toString()));
        post.addView(copy);
        post.addView(text("Nếu Facebook không nhận chữ kèm ảnh, hãy sao chép và dán thủ công.", 12, MUTED, false));

        LinearLayout reply = card(writingPage, "Gợi ý phản hồi");
        reply.addView(text("Gợi ý theo mẫu offline; không tự đọc tin nhắn.", 12, MUTED, false));
        Spinner category = new Spinner(this);
        category.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, ReplyAssistant.categories()));
        reply.addView(category);
        Button suggestReply = button("Gợi ý phản hồi", false);
        reply.addView(suggestReply);
        replyEditor = editor(restoredReply != null ? restoredReply : store.reply(), 3);
        reply.addView(replyEditor);
        suggestReply.setOnClickListener(v -> replyEditor.setText(ReplyAssistant.suggest(category.getSelectedItemPosition())));
        Button askAi = button("Nhờ ChatGPT chỉnh", false);
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
        reply.addView(text("Không đưa thông tin riêng tư của khách vào AI.", 12, MUTED, false));
        Button copyReply = button("Sao chép phản hồi và mở Facebook", true);
        copyReply.setOnClickListener(v -> {
            String value = replyEditor.getText().toString();
            if (value.trim().isEmpty()) { Toast.makeText(this, "Nhập phản hồi trước", Toast.LENGTH_SHORT).show(); return; }
            store.saveReply(value);
            copyToClipboard(value);
            openUrl(FACEBOOK_APP);
        });
        reply.addView(copyReply);


        LinearLayout planner = card(schedulePage, "Lịch nội dung 7 ngày");
        planner.addView(text("7 ngày gợi ý, tự duyệt trước khi đăng.", 12, MUTED, false));
        planIdeas = new LinearLayout(this);
        planIdeas.setOrientation(LinearLayout.VERTICAL);
        planner.addView(planIdeas);
        planExpandButton = button("Xem đủ 7 ngày", false);
        planExpandButton.setOnClickListener(v -> {
            showAllPlanDays = !showAllPlanDays;
            showPlanDays(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        });
        planner.addView(planExpandButton);
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
        Button chooseDate = button("Chọn ngày / giờ", false);
        chooseDate.setOnClickListener(v -> pickReminderDateTime());
        planner.addView(chooseDate);
        Button addCalendar = button("Tạo lời nhắc trong Lịch", true);
        addCalendar.setOnClickListener(v -> addCalendarReminder());
        planner.addView(addCalendar);
        Button clearReminder = button("Xóa nhắc đã lưu", false);
        clearReminder.setOnClickListener(v -> { store.clearReminder(); updateReminderSummary(); });
        planner.addView(clearReminder);
        planner.addView(text("Nhớ bấm Lưu trong ứng dụng Lịch để bật nhắc.", 12, MUTED, false));

        LinearLayout schedule = card(schedulePage, "Việc hôm nay");
        dayChecklistDate = text("", 13, MUTED, false);
        schedule.addView(dayChecklistDate);
        dayChecklist = new LinearLayout(this);
        dayChecklist.setOrientation(LinearLayout.VERTICAL);
        schedule.addView(dayChecklist);
        refreshDailyContent();

        LinearLayout mcpPanel = card(connectPage, "Kết nối ChatGPT · MCP độc lập");
        mcpPanel.addView(text("Kết nối máy chủ riêng bằng HTTPS và mã dùng một lần.", 12, MUTED, false));
        EditText endpoint = editor("", 1);
        endpoint.setSingleLine(true);
        endpoint.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        endpoint.setHint("Địa chỉ HTTPS máy chủ MCP");
        endpoint.setContentDescription("Địa chỉ máy chủ MCP HTTPS độc lập");
        applyIcon(endpoint, R.drawable.ic_sangit_link, MUTED, 20);
        mcpPanel.addView(endpoint);
        EditText pairingCode = editor("", 1);
        pairingCode.setSingleLine(true);
        pairingCode.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        pairingCode.setHint("Mã ghép cặp dùng một lần");
        pairingCode.setContentDescription("Mã ghép cặp một lần");
        applyIcon(pairingCode, R.drawable.ic_sangit_key, MUTED, 20);
        mcpPanel.addView(pairingCode);
        TextView pairingState = text("Chưa kết nối", 14, MUTED, true);
        setPairingStatus(pairingState, "Chưa kết nối");
        mcpPanel.addView(pairingState);
        Button pairButton = button("Ghép cặp an toàn", true);
        pairButton.setOnClickListener(v -> {
            setPairingStatus(pairingState, "Đang xác minh mã...");
            mcpClient.pair(endpoint.getText().toString(), pairingCode.getText().toString(),
                message -> runOnUiThread(() -> {
                    pairingCode.setText("");
                    setPairingStatus(pairingState, message);
                }));
        });
        mcpPanel.addView(pairButton);
        Button statusButton = button("Gửi trạng thái theo yêu cầu", false);
        statusButton.setOnClickListener(v -> mcpClient.checkIn(
            message -> runOnUiThread(() -> setPairingStatus(pairingState, message))));
        mcpPanel.addView(statusButton);
        Button disconnectButton = button("Ngắt kết nối / thu hồi phiên", false);
        disconnectButton.setOnClickListener(v -> mcpClient.disconnect(
            message -> runOnUiThread(() -> setPairingStatus(pairingState, message))));
        mcpPanel.addView(disconnectButton);
        mcpPanel.addView(text("Phiên chỉ lưu tạm. Mở lại ứng dụng cần ghép cặp mới.", 12, MUTED, false));

        TextView footer = text("SangIT Android Agent · v0.3.5 beta", 11, MUTED, false);
        footer.setGravity(Gravity.CENTER_HORIZONTAL);
        footer.setPadding(0, dp(10), 0, 0);
        connectPage.addView(footer);
        activateTab(selectedTab, false);
    }

    /** Page switching hides only page containers. Editors and drafts stay mounted. */
    private void activateTab(int index, boolean scrollTop) {
        if (index < 0 || index >= tabPages.length) return;
        selectedTab = index;
        String[] titles = {"Tổng quan", "Soạn nội dung", "Lịch chăm sóc", "Kết nối ChatGPT"};
        String[] subtitles = {
            "Công việc Facebook cá nhân trong một nơi",
            "Viết bài và phản hồi theo cách của bạn",
            "Kế hoạch mỗi ngày, không tự đăng bài",
            "MCP riêng, không dùng máy chủ khác"
        };
        pageHeader.setText(titles[index]);
        pageSubtitle.setText(subtitles[index]);
        for (int i = 0; i < tabPages.length; i++) {
            tabPages[i].setVisibility(i == index ? View.VISIBLE : View.GONE);
            boolean active = i == index;
            tabLabels[i].setTextColor(active ? PRIMARY : MUTED);
            tabLabels[i].setTypeface(null, active ? Typeface.BOLD : Typeface.NORMAL);
            tabIcons[i].setImageTintList(ColorStateList.valueOf(active ? PRIMARY : MUTED));
            tabSurfaces[i].setBackground(rounded(active ? SOFT : Color.TRANSPARENT, 13,
                active ? SOFT : Color.TRANSPARENT));
            tabLabels[i].setSelected(active);
        }
        if (index == 2) refreshDailyContent();
        if (scrollTop && contentScroll != null) contentScroll.post(() -> contentScroll.scrollTo(0, 0));
    }

    /** Refresh dated lists only; editors and the in-memory MCP pairing remain mounted. */
    private void refreshDailyContent() {
        if (planIdeas == null || dayChecklist == null || dayChecklistDate == null) return;
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        String date = today.toString();
        if (date.equals(displayedDay)) return;
        displayedDay = date;
        showPlanDays(today);
        dayChecklistDate.setText("Ngày " + today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            + " · Thử nghiệm, không tự đăng bài");
        dayChecklist.removeAllViews();
        addTask(dayChecklist, date, "morning", "07:00 · Soạn bài / đăng Story");
        addTask(dayChecklist, date, "noon", "12:00 · Xem thông báo / trả lời thủ công");
        addTask(dayChecklist, date, "evening", "19:00 · Duyệt và đăng bài");
        addTask(dayChecklist, date, "night", "21:00 · Kiểm tra kết quả");
    }

    private void showPlanDays(LocalDate today) {
        if (planIdeas == null || planExpandButton == null) return;
        planIdeas.removeAllViews();
        int count = showAllPlanDays ? 7 : 3;
        for (int i = 0; i < count; i++) {
            LocalDate day = today.plusDays(i);
            TextView row = text(day.format(DateTimeFormatter.ofPattern("dd/MM"))
                + "  ·  " + ContentSchedule.ideaFor(day), 13, INK, false);
            row.setPadding(0, dp(6), 0, dp(3));
            planIdeas.addView(row);
        }
        planExpandButton.setText(showAllPlanDays ? "Thu gọn còn 3 ngày" : "Xem đủ 7 ngày");
    }

    private EditText metricField(LinearLayout container, String label, String value) {
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 1f);
        lp.setMargins(0, dp(6), dp(5), dp(5));
        container.addView(col, lp);
        TextView fieldLabel = text(label, 12, MUTED, false);
        applyIcon(fieldLabel, "Theo dõi".equals(label) ? R.drawable.ic_sangit_person
            : "Lượt xem".equals(label) ? R.drawable.ic_sangit_chart
            : R.drawable.ic_sangit_message, PRIMARY, 14);
        col.addView(fieldLabel);
        EditText field = new EditText(this);
        field.setSingleLine(true);
        field.setInputType(InputType.TYPE_CLASS_NUMBER);
        field.setTextColor(INK);
        field.setTextSize(16f);
        field.setPadding(dp(7), dp(9), dp(7), dp(9));
        field.setMinHeight(dp(48));
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
            activateTab(1, true);
            Toast.makeText(this, "Hãy soạn bài trước khi thêm lịch", Toast.LENGTH_LONG).show();
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
        check.setTextSize(15f);
        check.setMinHeight(dp(48));
        check.setChecked(store.taskDone(id, date));
        check.setOnCheckedChangeListener((button, value) -> store.setTaskDone(id, date, value));
        container.addView(check);
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
        card.setPadding(dp(14), dp(13), dp(14), dp(14));
        card.setBackground(rounded(Color.WHITE, 20, BORDER));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(10);
        parent.addView(card, params);
        TextView title = text(heading, 17, INK, true);
        applyIcon(title, sectionIcon(heading), PRIMARY, 19);
        title.setPadding(0, 0, 0, dp(7));
        card.addView(title);
        return card;
    }

    private EditText editor(String value, int lines) {
        EditText input = new EditText(this);
        input.setTextSize(15f);
        input.setTextColor(INK);
        input.setGravity(Gravity.TOP | Gravity.START);
        input.setMinLines(lines);
        input.setMaxLines(Math.max(lines, 14));
        input.setSingleLine(false);
        input.setPadding(dp(14), dp(13), dp(14), dp(13));
        input.setMinHeight(dp(52));
        input.setHintTextColor(MUTED);
        input.setBackground(rounded(BACKGROUND, 14, BORDER));
        input.setText(value);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = dp(7);
        params.bottomMargin = dp(8);
        input.setLayoutParams(params);
        return input;
    }


    /** Two compact actions on normal phones, stacked on narrow devices. */
    private void addCompactActions(LinearLayout parent, Button left, Button right) {
        if (getResources().getConfiguration().screenWidthDp < 360) {
            parent.addView(left);
            parent.addView(right);
            return;
        }
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(-1, -2);
        rowParams.topMargin = dp(3);
        parent.addView(row, rowParams);
        LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(0, -2, 1f);
        leftParams.rightMargin = dp(5);
        LinearLayout.LayoutParams rightParams = new LinearLayout.LayoutParams(0, -2, 1f);
        rightParams.leftMargin = dp(5);
        row.addView(left, leftParams);
        row.addView(right, rightParams);
    }

    private Button button(String label, boolean primary) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setText(label);
        button.setTextSize(14f);
        button.setTypeface(null, Typeface.BOLD);
        button.setTextColor(primary ? Color.WHITE : INK);
        button.setMinHeight(dp(primary ? 52 : 48));
        button.setPadding(dp(10), dp(8), dp(10), dp(8));
        applyIcon(button, actionIcon(label), primary ? Color.WHITE : PRIMARY, 19);
        button.setGravity(Gravity.CENTER);
        button.setBackground(new RippleDrawable(ColorStateList.valueOf(
            primary ? 0x44FFFFFF : 0x22007074),
            rounded(primary ? PRIMARY : BACKGROUND, 14, primary ? PRIMARY : BORDER), null));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = dp(6);
        button.setLayoutParams(params);
        return button;
    }

    /** Lightweight bundled vector icons. Existing actions remain unchanged. */
    private void applyIcon(TextView view, int resource, int color, int size) {
        Drawable icon = getDrawable(resource);
        if (icon == null) return;
        icon = icon.mutate();
        icon.setTint(color);
        icon.setBounds(0, 0, dp(size), dp(size));
        view.setCompoundDrawables(icon, null, null, null);
        view.setCompoundDrawablePadding(dp(8));
    }

    private int sectionIcon(String name) {
        if (name.contains("Riêng tư")) return R.drawable.ic_sangit_shield;
        if (name.contains("Thống kê")) return R.drawable.ic_sangit_chart;
        if (name.contains("Facebook")) return R.drawable.ic_sangit_person;
        if (name.contains("Soạn")) return R.drawable.ic_sangit_edit;
        if (name.contains("phản hồi")) return R.drawable.ic_sangit_message;
        if (name.contains("Lịch")) return R.drawable.ic_sangit_calendar;
        if (name.contains("Việc")) return R.drawable.ic_sangit_check;
        if (name.contains("MCP") || name.contains("ChatGPT")) return R.drawable.ic_sangit_link;
        return R.drawable.ic_sangit_dashboard;
    }

    private int actionIcon(String label) {
        if (label.startsWith("Mở hồ sơ")) return R.drawable.ic_sangit_person;
        if (label.startsWith("Mở thông báo")) return R.drawable.ic_sangit_bell;
        if (label.startsWith("Mở tin nhắn")) return R.drawable.ic_sangit_message;
        if (label.startsWith("Lưu")) return R.drawable.ic_sangit_save;
        if (label.startsWith("Chọn ảnh")) return R.drawable.ic_sangit_image;
        if (label.startsWith("Chia sẻ")) return R.drawable.ic_sangit_share;
        if (label.startsWith("Sao chép")) return R.drawable.ic_sangit_copy;
        if (label.startsWith("Gợi ý") || label.startsWith("Nhờ ChatGPT")) return R.drawable.ic_sangit_sparkles;
        if (label.contains("7 ngày")) return R.drawable.ic_sangit_expand;
        if (label.startsWith("Chọn ngày") || label.startsWith("Chuẩn bị lời")) return R.drawable.ic_sangit_calendar;
        if (label.startsWith("Xóa")) return R.drawable.ic_sangit_delete;
        if (label.startsWith("Ghép cặp")) return R.drawable.ic_sangit_link;
        if (label.startsWith("Gửi trạng thái")) return R.drawable.ic_sangit_upload;
        if (label.startsWith("Ngắt kết nối")) return R.drawable.ic_sangit_unlink;
        return R.drawable.ic_sangit_check;
    }

    private void setPairingStatus(TextView view, String message) {
        view.setText(message);
        String info = message.toLowerCase(java.util.Locale.ROOT);
        boolean success = info.contains("thành công") || info.startsWith("đã gửi");
        boolean failure = info.contains("thất bại") || info.startsWith("không ") || info.contains("lỗi");
        int tint = success ? PRIMARY : failure ? Color.rgb(173, 83, 37) : MUTED;
        view.setTextColor(tint);
        view.setPadding(dp(12), dp(11), dp(12), dp(11));
        view.setBackground(rounded(success ? SOFT : failure ? 0xFFFDF1EB : BACKGROUND, 12, BORDER));
        applyIcon(view, success ? R.drawable.ic_sangit_check :
            failure ? R.drawable.ic_sangit_unlink : R.drawable.ic_sangit_link, tint, 19);
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