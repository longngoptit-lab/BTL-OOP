package qlht.tools;

import java.io.Console;
import java.util.Arrays;
import qlht.db.Database;
import qlht.model.User;
import qlht.service.AuthService;
import qlht.service.DemoDataService;

/** Công cụ terminal; không lưu mật khẩu demo vào mã nguồn hoặc file. */
public final class DemoData {
    private DemoData() { }
    public static void run() throws Exception {
        try (Database db = new Database()) {
            if (!db.connection().getCatalog().endsWith("_DEMO"))
                throw new IllegalArgumentException("Hãy dùng database riêng có tên kết thúc bằng _DEMO (ví dụ QLHT_DEMO) để nạp minh họa.");
            Console console = System.console();
            if (console == null) throw new IllegalArgumentException("Chạy --seed-demo trong terminal tương tác để nhập tài khoản và mật khẩu.");
            String username = console.readLine("Tên đăng nhập demo mới: ");
            char[] password = console.readPassword("Mật khẩu (8–128 ký tự): ");
            char[] confirmation = console.readPassword("Nhập lại mật khẩu: ");
            try {
                User user = new AuthService(db).register("Sinh viên Demo", username, password, confirmation);
                new DemoDataService(db).seed(user);
                console.printf("Đã tạo dữ liệu demo cho %s: 5 môn, 7 buổi học, 2 lịch thi, 5 bài tập, 3 ghi chú.%n", user.username());
            } finally {
                if (password != null) Arrays.fill(password, '\0');
                if (confirmation != null) Arrays.fill(confirmation, '\0');
            }
        }
    }
}
