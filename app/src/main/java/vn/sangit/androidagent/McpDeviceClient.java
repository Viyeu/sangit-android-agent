package vn.sangit.androidagent;

import android.os.Build;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/** Foreground-only explicit HTTPS pairing. No background jobs or private content transfer. */
final class McpDeviceClient {
    interface Callback { void complete(String message); }
    private volatile String baseUrl;
    private volatile String deviceToken;
    boolean isPaired() { return baseUrl != null && deviceToken != null; }

    private static String validOrigin(String value) {
        try {
            URI uri = new URI(value.trim());
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null ||
                uri.getRawUserInfo() != null || uri.getRawQuery() != null || uri.getRawFragment() != null ||
                (uri.getRawPath() != null && !uri.getRawPath().isEmpty() && !"/".equals(uri.getRawPath()))) {
                throw new IllegalArgumentException();
            }
            return "https://" + uri.getRawAuthority();
        } catch (Exception e) { throw new IllegalArgumentException("Cần địa chỉ HTTPS máy chủ MCP độc lập (không có /mcp)"); }
    }

    void pair(String url, String code, Callback done) {
        final String origin;
        try {
            if (deviceToken != null) throw new IllegalArgumentException("Ngắt phiên cũ trước khi ghép cặp lại");
            origin = validOrigin(url);
            if (code == null || !code.trim().matches("(?i)[0-9a-f]{18}")) throw new IllegalArgumentException("Mã ghép cặp phải có 18 ký tự hex");
        } catch (Exception e) { done.complete(e.getMessage()); return; }
        new Thread(() -> {
            try {
                JSONObject b = new JSONObject();
                b.put("code", code.trim().toUpperCase(java.util.Locale.ROOT));
                JSONObject response = post(origin, "/device/pair/claim", null, b);
                String token = response.getString("deviceToken");
                if (!token.matches("[a-f0-9]{64}") || !response.getString("deviceId").startsWith("agent-")) throw new IllegalStateException("Phản hồi ghép cặp không hợp lệ");
                baseUrl = origin; deviceToken = token;
                done.complete("Ghép cặp thành công · bấm Gửi trạng thái");
            } catch (Exception e) { done.complete("Ghép cặp thất bại: " + error(e)); }
        }, "android-mcp-pair").start();
    }

    void checkIn(Callback done) {
        final String origin = baseUrl, token = deviceToken;
        if (origin == null || token == null) { done.complete("Chưa ghép cặp"); return; }
        new Thread(() -> {
            try {
                JSONObject b = new JSONObject();
                b.put("appVersion", "0.3.6");
                b.put("sdk", Build.VERSION.SDK_INT);
                JSONObject response = post(origin, "/device/checkin", token, b);
                done.complete(response.optBoolean("accepted") ? "Đã gửi phiên bản và Android SDK (không gửi dữ liệu riêng tư)" : "Máy chủ chưa nhận trạng thái");
            } catch (Exception e) { done.complete("Không gửi được: " + error(e)); }
        }, "android-mcp-status").start();
    }

    void disconnect(Callback done) {
        final String origin = baseUrl, token = deviceToken;
        if (origin == null || token == null) { done.complete("Chưa kết nối"); return; }
        new Thread(() -> {
            try {
                JSONObject response = post(origin, "/device/disconnect", token, new JSONObject());
                if (!response.optBoolean("disconnected")) throw new IllegalStateException("Chưa thu hồi phiên");
                baseUrl = null; deviceToken = null;
                done.complete("Đã ngắt và thu hồi phiên");
            } catch (Exception e) { done.complete("Không thể ngắt trên máy chủ: " + error(e)); }
        }, "android-mcp-disconnect").start();
    }

    private static JSONObject post(String origin, String path, String token, JSONObject payload) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(origin + path).openConnection();
        c.setConnectTimeout(10000); c.setReadTimeout(10000);
        c.setRequestMethod("POST"); c.setInstanceFollowRedirects(false);
        c.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        c.setRequestProperty("Accept", "application/json");
        if (token != null) c.setRequestProperty("Authorization", "Bearer " + token);
        c.setDoOutput(true);
        try {
            byte[] bytes = payload.toString().getBytes(StandardCharsets.UTF_8);
            if (bytes.length > 1024) throw new IllegalArgumentException("Dữ liệu quá lớn");
            try (OutputStream out = c.getOutputStream()) { out.write(bytes); }
            int status = c.getResponseCode();
            if (status < 200 || status >= 300) throw new IllegalStateException("HTTP " + status);
            try (InputStream in = c.getInputStream()) {
                byte[] data = new byte[2048]; int total = 0, count;
                while (total < data.length && (count = in.read(data, total, data.length-total)) > 0) total += count;
                return new JSONObject(new String(data, 0, total, StandardCharsets.UTF_8));
            }
        } finally { c.disconnect(); }
    }

    private static String error(Exception e) {
        if (e instanceof java.net.SocketTimeoutException) return "quá thời gian";
        if (e instanceof java.net.UnknownHostException) return "không tìm thấy máy chủ";
        if (e instanceof javax.net.ssl.SSLException) return "HTTPS không hợp lệ";
        if (e instanceof IllegalStateException) return e.getMessage();
        return "không có kết nối HTTPS hợp lệ";
    }
}
