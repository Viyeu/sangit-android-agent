# SangIT Android Agent v0.3.5 — Compact mobile-first UI

Existing functionality is preserved: manual Facebook metrics, draft/preview/share, picture picking, offline reply templates, optional share-to-ChatGPT, 7-day plan/checklist, Calendar confirmation, and standalone HTTPS MCP pairing/status/disconnect.

Changes: compact header and cards, 20 native icons retained, adaptive side-by-side secondary actions on normal phones (one-column fallback on narrow screens), smaller default editing surfaces, clearer pairing state and improved keyboard behavior.

Only Android UI changes; no new permissions, dependencies, background tasks, API endpoints or Core integration. APK is a debug build; test device interactions before stable release.

# SangIT Android Agent v0.3.2 — Mobile UI/UX beta

- Bốn mục điều hướng phía dưới: Tổng quan, Bài viết, Lịch, MCP.
- Giao diện sáng xanh ngọc, card rõ ràng, nút từ 48–52dp, nhập và chuyển trang không xóa nháp.
- Giữ nguyên quyền INTERNET tối thiểu, MCP độc lập, các thao tác Facebook và Lịch; không bật Notification Listener.
- Build debug APK bằng GitHub Actions; kiểm thử trực tiếp trên Android 16 còn chờ xác minh.

---

# SangIT Android Agent v0.3.1 (Play Protect minimal permissions)

This variant removes the notification listener from the Android manifest and user interface after the Play Protect block reported on Android 16. It retains explicit, outbound HTTPS pairing to an independent MCP server, and manual Facebook/Messenger shortcuts. Sensitive notification access cannot be granted. This is a debug APK; installation on the physical device is pending. Do not disable Play Protect.

# SangIT Android Agent v0.2.0 — Chăm sóc Facebook cá nhân

**Trạng thái:** mã nguồn, chưa phải APK và chưa được kiểm thử Android 16. Phát triển từ bản v0.1, giữ applicationId `vn.sangit.androidagent` (phiên bản 2), giữ tương thích dữ liệu SharedPreferences v0.1.

## Tính năng đã triển khai trong mã nguồn

- Trang chủ tiếng Việt, mobile-first; nút mở hồ sơ `facebook.com/sangitinfo/`.
- **Tổng quan:** nhập tay follower, lượt xem, tương tác từ Bảng điều khiển Facebook chuyên nghiệp. Giữ số liệu lần gần nhất và lần trước (khác ngày) để so sánh chênh lệch follower. Không gán thông báo = bình luận / follower; không có API thống kê Facebook cá nhân.
- **Thông báo:** nếu đồng thời bật trong app và cấp quyền Notification access của Android, chỉ đếm thông báo *đang hiện* từ package Facebook/Messenger; không đọc, lưu hoặc chuyển tiếp tên/người gửi/nội dung.
- **Hộp việc:** nút mở trực tiếp danh sách thông báo và Messenger để người dùng tự xử lý. Không tự thu thập, đọc hoặc gửi tin nhắn.
- **Bài đăng:** mẫu bài nghề điện lạnh, lưu nháp, chọn ảnh, Android Sharesheet để bạn tự xác nhận đăng trong Facebook. Không auto-post.
- **Gợi ý phản hồi:** 6 mẫu dựa trên tình huống (quy tắc, **không phải mô hình AI**). Có nút *tự chọn* chia sẻ nháp sang ChatGPT/ứng dụng AI nếu muốn viết lại. Người dùng tự chọn ứng dụng, đọc lại và quyết định gửi. **Không** nhập dữ liệu riêng tư của khách vào AI.
- **Lịch nội dung:** 7 ngày gợi ý bài, Reel, Story; chọn ngày/giờ, mở giao diện thêm sự kiện trong ứng dụng Lịch Android với bài nháp. Bạn phải xác nhận **Lưu** bên trong app Lịch; lưu trong Agent không tự bật alarm. Không có quyền READ/WRITE_CALENDAR.
- Checklist cục bộ theo ngày, không có tác vụ tự động chạy ngầm.
- Card SangIT Core chỉ hiển thị tình trạng **chưa kết nối**, không có HTTP/Internet hoặc quyền điều khiển thiết bị.

## Ranh giới bảo mật

Không quyền INTERNET, ACCESSIBILITY, READ/WRITE_CALENDAR, CONTACTS, SMS, STORAGE, CAMERA, overlay. Không chứa OAuth, mật khẩu, cookie, token Facebook. Không gửi nội dung tới Core/Meta. `allowBackup=false`, `usesCleartextTraffic=false`, notification listener `exported=false`. Hoạt động offline trừ khi người dùng **chủ động** mở Facebook, Lịch hoặc Sharesheet tới ứng dụng khác.

Một số điện thoại/Facebook có thể từ chối tự điền chữ vào bài kèm ảnh. Nếu gặp, dùng Sao chép rồi dán thủ công; không sử dụng API Facebook riêng.

## Build APK qua Android Studio / Windows

Yêu cầu JDK 17, Android SDK Platform 35 + Build-Tools 35.0.0, Gradle 8.9 (AGP 8.7.3), `minSdk=26`, `targetSdk=35`.

1. Giải nén ZIP, mở thư mục `SangIT-Android-Agent-v0.2` trong Android Studio.
2. Cài Android SDK Platform 35 / Build-Tools 35.0.0 nếu chưa có; Gradle sync.
3. Chạy `gradle --no-daemon :app:assembleDebug` trong thư mục dự án, hoặc chạy `scripts/build-windows.ps1` khi đã cấu hình Gradle, SDK và JAVA_HOME.
4. Kiểm tra `app/build/outputs/apk/debug/app-debug.apk` và SHA-256. **Debug APK để kiểm thử, không phải bản phát hành chính thức.**

GitHub Actions `.github/workflows/android-ci.yml` dùng JDK 17, Gradle 8.9 và Android SDK 35 để tạo **debug APK**. Workflow chưa chạy vì chưa có repo/workflow được cấu hình và source chưa được đẩy lên GitHub. Không tự triển khai vào máy hoặc Core.

## Kiểm thử offline

```bash
bash scripts/test-offline.sh
```

Bộ test chạy 49 assertions Java thuần, phân tích cú pháp 9 tệp Java bằng JDK Compiler Parser và kiểm tra static manifest/permission/privacy. **Không chứng minh Android SDK compile, UI, tương tác, pin/RAM hoặc Android 16 E2E.**

## Checklist kiểm thử thực tế (cần làm sau khi có APK)

1. Build `:app:assembleDebug`; kiểm tra package ID, version, permission trong APK.
2. Trên Android 16: mở app, chỉnh chữ lớn, xoay màn hình, kiểm tra trạng thái và các vùng chạm.
3. Tắt/bật thống kê thông báo, cấp/thu hồi Notification access; đếm chỉ khi bật; không lộ nội dung.
4. Lưu nháp, đổi mẫu, thoát/mở app, chia sẻ text/ảnh rồi **hủy đăng**, kiểm tra Facebook không bị đăng tự động.
5. Chọn 6 tình huống phản hồi, thử mở ứng dụng AI bằng chia sẻ thủ công, dán/sửa/gửi bình luận bằng tay.
6. Nhập số liệu thực, tạo bản ghi ngày sau để so sánh chênh lệch; xoay màn hình trước khi lưu.
7. Chọn lịch nhắc: hủy tại Calendar rồi chọn lưu tại Calendar; Agent không được tự tạo sự kiện hoặc tự đăng.
8. Regression: không ảnh hưởng Meta/Page và Core, không chạy nền, đo RAM/CPU/pin.

## Tích hợp Core sau này

Core 1.5.0 khai báo `facebook.personal.publish` nhưng dispatcher chưa route; Social Provider xử lý Page-only và ở trạng thái 503 do thiếu OAuth. Không bật tuyến ghi, không sửa Core production hoặc chia sẻ token Page cho profile cá nhân. Pha Core pairing sau này cần API bảo mật riêng, xác nhận từng quyền và E2E.

Xem `.codex/codex.md` và `.codex/STATUS.md`.
