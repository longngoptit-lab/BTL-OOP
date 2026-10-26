package qlht.db;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import qlht.util.DateUtil;

/** Quản lý kết nối SQLite, tạo bảng và chuyển dữ liệu ngày cũ sang dạng ISO. */
public class Database implements AutoCloseable {
    private final Path path;
    private final Connection conn;

    /** File qlht.db nằm trong thư mục đang chạy chương trình. */
    public static Path defaultPath() {
        return Paths.get(System.getProperty("user.dir"), "qlht.db");
    }

    public Database(Path path) {
        this.path = path;
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + path);
            try (Statement st = conn.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON");
            }
            createTables();
            migrateDatesToIso();
        } catch (SQLException | IOException e) {
            throw new DataAccessException(e);
        }
    }

    public Connection connection() {
        return conn;
    }

    private void createTables() throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS mon_hoc (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    ten_mon TEXT NOT NULL,
                    so_tin_chi INTEGER DEFAULT 0,
                    giang_vien TEXT,
                    ghi_chu TEXT
                )""");
            st.execute("""
                CREATE TABLE IF NOT EXISTS lich_hoc (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    mon_hoc_id INTEGER NOT NULL,
                    thu TEXT NOT NULL,
                    tiet_bat_dau INTEGER NOT NULL,
                    tiet_ket_thuc INTEGER NOT NULL,
                    phong_hoc TEXT,
                    FOREIGN KEY (mon_hoc_id) REFERENCES mon_hoc(id) ON DELETE CASCADE
                )""");
            st.execute("""
                CREATE TABLE IF NOT EXISTS lich_thi (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    mon_hoc_id INTEGER NOT NULL,
                    ngay_thi TEXT NOT NULL,
                    gio_thi TEXT,
                    phong_thi TEXT,
                    hinh_thuc TEXT,
                    FOREIGN KEY (mon_hoc_id) REFERENCES mon_hoc(id) ON DELETE CASCADE
                )""");
            st.execute("""
                CREATE TABLE IF NOT EXISTS bai_tap (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    mon_hoc_id INTEGER,
                    tieu_de TEXT NOT NULL,
                    mo_ta TEXT,
                    han_nop TEXT NOT NULL,
                    muc_do_uu_tien TEXT DEFAULT 'Trung bình',
                    trang_thai TEXT DEFAULT 'Chưa hoàn thành',
                    FOREIGN KEY (mon_hoc_id) REFERENCES mon_hoc(id) ON DELETE SET NULL
                )""");
            st.execute("""
                CREATE TABLE IF NOT EXISTS ghi_chu (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    mon_hoc_id INTEGER,
                    tieu_de TEXT NOT NULL,
                    noi_dung TEXT,
                    ngay_tao TEXT NOT NULL,
                    FOREIGN KEY (mon_hoc_id) REFERENCES mon_hoc(id) ON DELETE SET NULL
                )""");
        }
    }

    private int userVersion() throws SQLException {
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("PRAGMA user_version")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /** Đổi ngày dd/MM/yyyy cũ (từ bản Python) sang yyyy-MM-dd. Chỉ chạy một lần, có sao lưu .bak. */
    private void migrateDatesToIso() throws SQLException, IOException {
        if (userVersion() >= 1) return;

        String[][] cols = {{"lich_thi", "ngay_thi"}, {"bai_tap", "han_nop"}, {"ghi_chu", "ngay_tao"}};
        List<Object[]> changes = new ArrayList<>();
        for (String[] c : cols) {
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT id, " + c[1] + " FROM " + c[0])) {
                while (rs.next()) {
                    String moi = DateUtil.legacyToIso(rs.getString(2));
                    if (moi != null) changes.add(new Object[]{c[0], c[1], rs.getInt(1), moi});
                }
            }
        }

        if (!changes.isEmpty()) {
            Files.copy(path, Paths.get(path + ".bak"), StandardCopyOption.REPLACE_EXISTING);
            conn.setAutoCommit(false);
            try {
                for (Object[] ch : changes) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE " + ch[0] + " SET " + ch[1] + " = ? WHERE id = ?")) {
                        ps.setString(1, (String) ch[3]);
                        ps.setInt(2, (Integer) ch[2]);
                        ps.executeUpdate();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
        try (Statement st = conn.createStatement()) {
            st.execute("PRAGMA user_version = 1");
        }
    }

    @Override
    public void close() {
        try {
            conn.close();
        } catch (SQLException ignored) {
            // đang đóng ứng dụng nên bỏ qua
        }
    }
}
