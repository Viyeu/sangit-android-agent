package vn.sangit.androidagent;

/** Explicit offline, rule-based suggestions. This is NOT a remote or generative AI model. */
public final class ReplyAssistant {
    private ReplyAssistant() {}

    public static String[] categories() {
        return new String[] {"Hỏi giá", "Hỏi lịch làm", "Máy gặp lỗi", "Cảm ơn / khen", "Chưa hài lòng", "Tình huống khác"};
    }

    public static String suggest(int index) {
        switch (index) {
            case 0:
                return "Cảm ơn bạn đã hỏi! Bạn cho mình biết loại máy, model và khu vực cần hỗ trợ nhé. "
                    + "Mình sẽ kiểm tra và báo giá cụ thể, không báo giá khi chưa rõ tình trạng. 🔧";
            case 1:
                return "Chào bạn! Bạn cho mình biết khu vực và khoảng thời gian thuận tiện để mình kiểm tra lịch nhé. "
                    + "Mình sẽ xác nhận lại trước khi hẹn. 😄";
            case 2:
                return "Bạn mô tả giúp mình loại máy, model và hiện tượng đang gặp nhé. "
                    + "Nếu có mùi khét, rò điện hoặc nước gần ổ cắm, hãy ngắt nguồn an toàn và đừng tự tháo máy. 🔧";
            case 3:
                return "Cảm ơn bạn nhiều vì đã ủng hộ! Mình sẽ tiếp tục chia sẻ mẹo hữu ích và chuyện nghề thực tế. 😄";
            case 4:
                return "Mình rất tiếc vì trải nghiệm chưa tốt. Bạn có thể nhắn riêng chi tiết để mình kiểm tra lại "
                    + "và tìm hướng xử lý phù hợp nhé. Xin cảm ơn bạn đã góp ý.";
            default:
                return "Cảm ơn bạn đã bình luận! Bạn chia sẻ thêm thông tin để mình phản hồi đúng vấn đề nhé. 🔧";
        }
    }
}
