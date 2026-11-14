# SangIT Android Agent v0.3.6 — UX cải tiến

Native Android debug APK. Tổng quan shows three manually entered metrics with a collapsible edit form and fast shortcuts. Bài viết switches between Soạn bài and Phản hồi while retaining both editors. Lịch places checklist/progress ahead of the seven-day guide. MCP shows the pairing form only before a successful response and send/disconnect only when a device session exists.

No new Android permissions or third-party libraries. All personal content stays local unless the user explicitly shares it. The MCP server is standalone, not SangIT Core, and live HTTPS pairing is not yet verified. This beta needs physical-device interaction testing.


## v0.3.6 debug cài song song (Preview)
- Android sẽ từ chối cập nhật APK có cùng package nhưng chữ ký khác. Các GitHub Actions cũ dùng debug keystore sinh lại ở mỗi runner; signer của các bản v0.3.1–0.3.6 đều khác nhau.
- Bản debug mới dùng `vn.sangit.androidagent.preview`, hiển thị **SangIT Agent Preview**, cài song song với ứng dụng cũ `vn.sangit.androidagent` — **không gỡ bản cũ**.
- Dữ liệu và bản nháp trong ứng dụng cũ **không tự chuyển** vào Preview vì mỗi package có bộ nhớ riêng. Bản cũ vẫn được giữ nguyên.
- Đây là giải pháp kiểm thử an toàn, **không phải cập nhật in-place**. Để cập nhật lâu dài cần ký Android bằng một keystore riêng cố định được quản lý bí mật; không commit keystore hoặc mật khẩu vào repository công khai.

## Tải APK Preview không qua ChatGPT Library

Bản v0.3.6 Preview là APK thử nghiệm được GitHub Actions xác minh SHA và publish dưới dạng **prerelease** trên cùng repository. Tải tại GitHub Releases; không gỡ ứng dụng SangIT Android Agent cũ.

- Định danh Preview: `vn.sangit.androidagent.preview`
- APK SHA-256 cố định: `959d3a336f74f78b3e9275a347a33de6ce20f1405c6d01aed4098a5960c1a1fd`
- Original package: `vn.sangit.androidagent` không thay đổi.
- Không có update-in-place khi debug signing key đổi. Cần thiết lập keystore cố định riêng trước các bản nâng cấp Preview tương lai.
