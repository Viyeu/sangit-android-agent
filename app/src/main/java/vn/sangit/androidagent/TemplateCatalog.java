package vn.sangit.androidagent;

/** Public content ideas: no personal messages, tokens or remote services. */
public final class TemplateCatalog {
    private TemplateCatalog() {}

    public static String[] titles() {
        return new String[] {"Giới thiệu", "Mẹo điện lạnh", "Chuyện nghề", "Bài viết trống"};
    }

    public static String textFor(int index) {
        switch (index) {
            case 0:
                return "🔧 CHUYỆN NGHỀ ĐIỆN LẠNH CỦA SANG 😄\n\n"
                    + "Chào mọi người! Mình là Sang, làm nghề điện lạnh tại Buôn Hồ, Đắk Lắk.\n\n"
                    + "Mình chia sẻ các ca sửa chữa thực tế, mẹo sử dụng máy lạnh, máy giặt và chuyện vui trong nghề. "
                    + "Ai thấy hữu ích thì theo dõi mình nhé!\n\n#SangIT #DienLanh #DakLak";
            case 1:
                return "❄️ MẸO NHỎ KHI DÙNG MÁY LẠNH\n\n"
                    + "Vệ sinh lưới lọc định kỳ và không che cửa gió giúp máy hoạt động ổn định. "
                    + "Nếu máy có dấu hiệu bất thường, hãy ngắt nguồn và nhờ kỹ thuật viên kiểm tra.\n\n"
                    + "Bạn muốn mình chia sẻ mẹo gì tiếp theo?\n\n#SangIT #MayLanh";
            case 2:
                return "😄 CHUYỆN ĐI LÀM HÔM NAY\n\n"
                    + "Hôm nay gặp một ca điện lạnh khá thú vị...\n\n"
                    + "[Điền tình huống có thật, kết quả kiểm tra và mẹo hữu ích; không tiết lộ thông tin khách hàng.]\n\n"
                    + "Theo dõi mình để xem thêm những câu chuyện nghề nhé!\n\n#SangIT #ChuyenNghe";
            default:
                return "";
        }
    }

    public static String exampleReply() {
        return "Cảm ơn bạn đã quan tâm! Bạn mô tả rõ dòng máy và dấu hiệu gặp phải để mình tư vấn bước kiểm tra an toàn nhé. 🔧";
    }
}
