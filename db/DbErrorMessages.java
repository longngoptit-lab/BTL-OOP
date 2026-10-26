package qlht.db;

/** Đổi lỗi CSDL thành thông báo tiếng Việt dễ hiểu cho người dùng. */
public final class DbErrorMessages {
    private DbErrorMessages() {}

    /** Trả về {tiêu đề, nội dung}. */
    public static String[] describe(Throwable loi) {
        String ten = Database.defaultPath().getFileName().toString();
        String msg = String.valueOf(loi.getMessage());
        String ct = msg.toLowerCase();
        String tieuDe, noiDung;
        if (ct.contains("locked") || ct.contains("busy")) {
            tieuDe = "Cơ sở dữ liệu đang bị khóa";
            noiDung = "File " + ten + " đang được một chương trình khác sử dụng "
                    + "(có thể ứng dụng này đang mở ở cửa sổ khác).\nHãy đóng chương trình đó rồi thử lại.";
        } else if (ct.contains("readonly") || ct.contains("read-only")) {
            tieuDe = "Không ghi được dữ liệu";
            noiDung = "File " + ten + " hoặc thư mục chứa nó đang ở chế độ chỉ đọc.\n"
                    + "Hãy kiểm tra quyền ghi của file và thư mục.";
        } else if (ct.contains("malformed") || ct.contains("not a database") || ct.contains("corrupt")) {
            tieuDe = "Cơ sở dữ liệu bị hỏng";
            noiDung = "File " + ten + " có vẻ đã bị hỏng.\nNếu có file " + ten
                    + ".bak (bản sao lưu tự động), bạn có thể đổi tên nó thành " + ten
                    + " để khôi phục. Nên sao lưu file hỏng trước khi thay.";
        } else if (ct.contains("unable to open") || ct.contains("disk i/o") || ct.contains("full")) {
            tieuDe = "Không truy cập được cơ sở dữ liệu";
            noiDung = "Không mở, đọc hoặc ghi được file " + ten + ".\n"
                    + "Hãy kiểm tra đường dẫn, quyền truy cập và dung lượng ổ đĩa.";
        } else {
            tieuDe = "Lỗi cơ sở dữ liệu";
            noiDung = "Đã xảy ra lỗi khi làm việc với cơ sở dữ liệu.";
        }
        return new String[]{tieuDe, noiDung + "\n\nChi tiết: " + msg};
    }
}
