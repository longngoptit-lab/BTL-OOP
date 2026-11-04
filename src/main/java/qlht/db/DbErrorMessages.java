package qlht.db;

import java.util.Locale;

/** Đổi lỗi SQL Server thành thông báo tiếng Việt. */
public final class DbErrorMessages {
    private DbErrorMessages() { }

    public static String[] describe(Throwable loi) {
        String msg = String.valueOf(loi.getMessage());
        String ct = msg.toLowerCase(Locale.ROOT);
        String title = "Lỗi cơ sở dữ liệu SQL Server";
        String detail;
        if (ct.contains("login failed")) {
            detail = "Đăng nhập thất bại. Kiểm tra db.user và db.password trong db.properties.";
        } else if (ct.contains("cannot open database")) {
            detail = "Không mở được database. Kiểm tra db.name, database đã tồn tại và quyền của tài khoản.";
        } else if (ct.contains("tcp/ip") || ct.contains("connection refused") || ct.contains("timed out")) {
            detail = "Không kết nối được SQL Server. Hãy bật Docker và container sqlserver, kiểm tra host và port.";
        } else if (ct.contains("certificate") || ct.contains("pkix")) {
            detail = "Không xác thực được chứng chỉ SQL Server. Với server local dùng chứng chỉ tự ký, "
                    + "đặt db.trust_certificate=true trong db.properties.";
        } else if (ct.contains("no suitable driver")) {
            detail = "Thiếu Microsoft JDBC Driver. Hãy chạy bằng ./run.sh hoặc Maven.";
        } else {
            detail = "Không đọc hoặc ghi được dữ liệu. Kiểm tra cấu hình và quyền truy cập SQL Server.";
        }
        return new String[]{title, detail + "\n\nChi tiết: " + msg};
    }
}
