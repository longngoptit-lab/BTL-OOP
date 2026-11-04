package qlht.tests;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import qlht.db.*;
import qlht.model.*;
import qlht.service.*;
import qlht.ui.*;
import qlht.util.*;

/** Kiểm thử SQL Server thật trong database tạm riêng; không dùng dữ liệu QLHT. */
public final class AuthIntegrationTest {
    private static final String[] TABLES = {"mon_hoc", "lich_hoc", "lich_thi", "bai_tap", "ghi_chu"};
    private static int assertions;

    private static void check(boolean value, String message) {
        assertions++;
        if (!value) throw new AssertionError(message);
    }

    private static void rejects(Class<? extends RuntimeException> type, Runnable action) {
        try { action.run(); }
        catch (RuntimeException e) {
            check(type.isInstance(e), "Expected " + type.getSimpleName() + ", got " + e.getClass().getSimpleName());
            return;
        }
        throw new AssertionError("Operation should have been rejected");
    }

    private static String setting(Properties config, String key, String fallback) {
        return System.getenv().getOrDefault("QLHT_DB_" + key.toUpperCase(Locale.ROOT), config.getProperty("db." + key, fallback));
    }

    public static void main(String[] args) throws Exception {
        String name = System.getenv("QLHT_DB_NAME");
        if (name == null || !name.matches("QLHT_AUTH_TEST_[0-9_]+"))
            throw new IllegalArgumentException("Test requires a dedicated QLHT_AUTH_TEST_* database name.");
        Properties config = new Properties();
        if (Files.exists(Path.of("db.properties"))) {
            try (Reader reader = Files.newBufferedReader(Path.of("db.properties"), StandardCharsets.UTF_8)) { config.load(reader); }
        }
        Properties jdbc = new Properties();
        jdbc.setProperty("serverName", setting(config, "host", "localhost"));
        jdbc.setProperty("portNumber", setting(config, "port", "1433"));
        jdbc.setProperty("user", setting(config, "user", "sa"));
        jdbc.setProperty("password", setting(config, "password", ""));
        jdbc.setProperty("databaseName", "master");
        jdbc.setProperty("encrypt", "true");
        jdbc.setProperty("trustServerCertificate", setting(config, "trust_certificate", "false"));
        jdbc.setProperty("loginTimeout", "10");
        try (Connection admin = DriverManager.getConnection("jdbc:sqlserver://", jdbc);
             Statement st = admin.createStatement()) {
            // Không ghi đè database có sẵn: CREATE sẽ thất bại nếu tên bị trùng.
            st.execute("CREATE DATABASE [" + name + "]");
            try {
                testMigrationAndAccounts();
                try (Database db = new Database()) {
                    check(new AuthService(db).login("alice", "ChangedPassword2026!".toCharArray()).username().equals("alice"),
                            "Authentication survives reconnect and repeated schema migration");
                    if (Arrays.asList(args).contains("--ui")) AuthUiTest.run(db);
                }
                System.out.println("PASS: " + assertions + " checks (registration, login, password change, legacy migration, 5-DAO isolation and CRUD).");
            } finally {
                st.execute("ALTER DATABASE [" + name + "] SET SINGLE_USER WITH ROLLBACK IMMEDIATE");
                st.execute("DROP DATABASE [" + name + "]");
            }
        }
    }

    private static void testMigrationAndAccounts() throws Exception {
        // Tạo dữ liệu theo schema trước khi có tài khoản, rồi nâng cấp lại để kiểm tra migration thực sự.
        try (Database db = new Database(); Statement st = db.connection().createStatement()) {
            for (String table : TABLES) {
                st.execute("ALTER TABLE dbo." + table + " DROP CONSTRAINT fk_" + table + "_user");
                st.execute("DROP INDEX ix_" + table + "_user ON dbo." + table);
                st.execute("ALTER TABLE dbo." + table + " DROP COLUMN user_id");
            }
            st.execute("DROP TABLE dbo.tai_khoan");
            st.execute("INSERT INTO mon_hoc (ten_mon, so_tin_chi) VALUES (N'Môn học cũ',3)");
            st.execute("INSERT INTO lich_hoc (mon_hoc_id, thu, tiet_bat_dau, tiet_ket_thuc) VALUES (1,N'Thứ 2',1,2)");
            st.execute("INSERT INTO lich_thi (mon_hoc_id, ngay_thi) VALUES (1,'2026-11-20')");
            st.execute("INSERT INTO bai_tap (mon_hoc_id, tieu_de, han_nop) VALUES (1,N'Bài tập cũ','2026-11-20')");
            st.execute("INSERT INTO ghi_chu (tieu_de, ngay_tao) VALUES (N'Ghi chú cũ','2026-11-04')");
        }
        try (Database db = new Database()) {
            AuthService auth = new AuthService(db);
            rejects(AuthException.class, () -> auth.register("A", "aa", "Password2026!".toCharArray(), "Password2026!".toCharArray()));
            rejects(AuthException.class, () -> auth.register("A", "alice", "short".toCharArray(), "short".toCharArray()));
            rejects(AuthException.class, () -> auth.register("A", "alice", "Password2026!".toCharArray(), "Mismatch2026!".toCharArray()));
            User alice = auth.register("Nguyễn Ánh", " Alice ", "Password2026!".toCharArray(), "Password2026!".toCharArray());
            User bob = auth.register("Trần Bình", "bob", "Password2026!".toCharArray(), "Password2026!".toCharArray());
            check(alice.username().equals("alice"), "Username normalization");
            check(auth.login(" ALICE ", "Password2026!".toCharArray()).displayName().equals("Nguyễn Ánh"), "Case-insensitive login and Vietnamese name");
            rejects(AuthException.class, () -> auth.register("Other", "ALICE", "Password2026!".toCharArray(), "Password2026!".toCharArray()));
            rejects(AuthException.class, () -> auth.login("alice", "Incorrect2026!".toCharArray()));
            rejects(AuthException.class, () -> auth.login("unknown", "Password2026!".toCharArray()));
            rejects(AuthException.class, () -> auth.login("' OR 1=1--", "Password2026!".toCharArray()));
            check(auth.login("bob", "Password2026!".toCharArray()).id() == bob.id(), "Login remains usable after duplicate registration rollback");
            try (Statement st = db.connection().createStatement(); ResultSet rs = st.executeQuery("SELECT password_hash FROM tai_khoan ORDER BY id")) {
                rs.next(); String a = rs.getString(1); rs.next(); String b = rs.getString(1);
                check(!a.equals(b), "Unique salts for identical passwords");
                check(!a.contains("Password2026!"), "No plaintext password");
                check(PasswordUtil.verify("Password2026!".toCharArray(), a), "Stored hash verifies");
                check(!PasswordUtil.verify("Password2026!".toCharArray(), "broken"), "Malformed hash rejected");
            }
            AppContext a = new AppContext(db, alice), b = new AppContext(db, bob);
            check(a.monHocDao.layTatCa().size() == 1, "First user receives legacy subjects");
            check(a.lichHocDao.layThoiKhoaBieu().size() == 1, "Legacy schedule retained");
            check(a.lichThiDao.layTatCa().size() == 1, "Legacy exams retained");
            check(a.baiTapDao.layTatCa(false).size() == 1, "Legacy assignments retained");
            check(a.ghiChuDao.layTatCa().size() == 1, "Legacy notes retained");
            check(b.monHocDao.layTatCa().isEmpty() && b.lichHocDao.layThoiKhoaBieu().isEmpty()
                    && b.lichThiDao.layTatCa().isEmpty() && b.baiTapDao.layTatCa(false).isEmpty()
                    && b.ghiChuDao.layTatCa().isEmpty(), "Second user starts empty across all 5 groups");
            testIsolation(a, b);
            User demo = auth.register("Demo", "demo_test", "DemoPassword2026!".toCharArray(), "DemoPassword2026!".toCharArray());
            new DemoDataService(db).seed(demo);
            AppContext demoContext = new AppContext(db, demo);
            check(demoContext.monHocDao.layTatCa().size() == 5 && demoContext.lichHocDao.layThoiKhoaBieu().size() == 7, "Demo subjects and weekly schedule");
            check(demoContext.baiTapDao.layTatCa(false).size() == 5 && demoContext.ghiChuDao.layTatCa().size() == 3 && demoContext.lichThiDao.layTatCa().size() == 2, "Complete demo data");
            rejects(ValidationException.class, () -> new DemoDataService(db).seed(demo));
            rejects(AuthException.class, () -> auth.changePassword(alice, "Wrong2026!".toCharArray(), "ChangedPassword2026!".toCharArray(), "ChangedPassword2026!".toCharArray()));
            rejects(AuthException.class, () -> auth.changePassword(alice, "Password2026!".toCharArray(), "ChangedPassword2026!".toCharArray(), "OtherPassword2026!".toCharArray()));
            rejects(AuthException.class, () -> auth.changePassword(alice, "Password2026!".toCharArray(), "Password2026!".toCharArray(), "Password2026!".toCharArray()));
            auth.changePassword(alice, "Password2026!".toCharArray(), "ChangedPassword2026!".toCharArray(), "ChangedPassword2026!".toCharArray());
            rejects(AuthException.class, () -> auth.login("alice", "Password2026!".toCharArray()));
            check(auth.login("alice", "ChangedPassword2026!".toCharArray()).id() == alice.id(), "New password works");
            check(auth.login("bob", "Password2026!".toCharArray()).id() == bob.id(), "Other user password unaffected");
        }
    }

    private static void testIsolation(AppContext a, AppContext b) {
        LocalDate date = LocalDate.of(2026, 11, 20);
        MonHoc subject = new MonHoc("Cơ sở dữ liệu", 3, "Giảng viên", "Ghi chú");
        int mid = a.monHocDao.them(subject);
        int bobMid = b.monHocDao.them(new MonHoc("Cơ sở dữ liệu", 3, "Khác", ""));
        check(b.monHocDao.timTrungTen("Cơ sở dữ liệu", null).getId() == bobMid, "Duplicate names scoped to user");
        rejects(ValidationException.class, () -> a.monHocService.save(new MonHoc("  CO SO DU LIEU  ", 3, "", "")));
        rejects(ValidationException.class, () -> a.monHocDao.them(new MonHoc("Invalid", 0, "", "")));
        rejects(ValidationException.class, () -> a.lichHocDao.them(new LichHoc(mid, null, "Thứ 2", 4, 2, "")));
        rejects(ValidationException.class, () -> a.lichThiDao.them(new LichThi(mid, null, null, "08:00", "", "")));
        rejects(ValidationException.class, () -> a.baiTapDao.them(new BaiTap(null, null, "Invalid", "", null, "Cao", Const.STATUS_TODO)));
        rejects(ValidationException.class, () -> a.ghiChuDao.them(new GhiChu(null, null, "Invalid", "", null)));
        rejects(ValidationException.class, () -> a.baiTapDao.capNhatTrangThai(1, "Unknown"));
        a.monHocService.save(subject);
        LichHoc schedule = new LichHoc(mid, null, "Thứ 3", 1, 3, "A1"); int hid = a.lichHocDao.them(schedule);
        LichThi exam = new LichThi(mid, null, date, "08:00", "A2", "Tự luận"); int tid = a.lichThiDao.them(exam);
        BaiTap task = new BaiTap(mid, null, "Bài tập", "Nội dung", date, "Cao", Const.STATUS_TODO); int bid = a.baiTapDao.them(task);
        GhiChu note = new GhiChu(mid, null, "Ghi chú", "Tiếng Việt", date); int gid = a.ghiChuDao.them(note);
        check(b.monHocDao.layTheoId(mid) == null && b.lichHocDao.layTheoId(hid) == null && b.lichThiDao.layTheoId(tid) == null
                && b.baiTapDao.layTheoId(bid) == null && b.ghiChuDao.layTheoId(gid) == null, "Cross-user lookups hidden for all DAOs");
        check(b.baiTapDao.layTatCa(true).isEmpty(), "Pending tasks scoped");
        check(b.lichHocDao.timXungDot("Thứ 3", 2, 4, null).isEmpty(), "Conflict detection scoped");
        rejects(DataAccessException.class, () -> b.monHocDao.sua(subject));
        rejects(DataAccessException.class, () -> b.lichHocDao.sua(schedule));
        rejects(DataAccessException.class, () -> b.lichThiDao.sua(exam));
        rejects(DataAccessException.class, () -> b.baiTapDao.sua(task));
        rejects(DataAccessException.class, () -> b.ghiChuDao.sua(note));
        // Kể cả khi đổi môn liên kết sang môn của Bob, ID bản ghi Alice vẫn không sửa được.
        LichHoc stolenSchedule = new LichHoc(bobMid, null, "Thứ 4", 4, 5, "B1"); stolenSchedule.setId(hid);
        LichThi stolenExam = new LichThi(bobMid, null, date, "09:00", "B2", "Khác"); stolenExam.setId(tid);
        BaiTap stolenTask = new BaiTap(bobMid, null, "Stolen", "", date, "Cao", Const.STATUS_DONE); stolenTask.setId(bid);
        GhiChu stolenNote = new GhiChu(bobMid, null, "Stolen", "", date); stolenNote.setId(gid);
        rejects(DataAccessException.class, () -> b.lichHocDao.sua(stolenSchedule));
        rejects(DataAccessException.class, () -> b.lichThiDao.sua(stolenExam));
        rejects(DataAccessException.class, () -> b.baiTapDao.sua(stolenTask));
        rejects(DataAccessException.class, () -> b.ghiChuDao.sua(stolenNote));
        rejects(DataAccessException.class, () -> b.baiTapDao.capNhatTrangThai(bid, Const.STATUS_DONE));
        rejects(DataAccessException.class, () -> b.monHocDao.xoa(mid));
        rejects(DataAccessException.class, () -> b.lichHocDao.xoa(hid));
        rejects(DataAccessException.class, () -> b.lichThiDao.xoa(tid));
        rejects(DataAccessException.class, () -> b.baiTapDao.xoa(bid));
        rejects(DataAccessException.class, () -> b.ghiChuDao.xoa(gid));
        rejects(DataAccessException.class, () -> b.lichHocDao.them(new LichHoc(mid, null, "Thứ 5", 1, 2, "")));
        rejects(DataAccessException.class, () -> b.lichThiDao.them(new LichThi(mid, null, date, "07:00", "", "")));
        rejects(DataAccessException.class, () -> b.baiTapDao.them(new BaiTap(mid, null, "X", "", date, "Cao", Const.STATUS_TODO)));
        rejects(DataAccessException.class, () -> b.ghiChuDao.them(new GhiChu(mid, null, "X", "", date)));
        // CRUD hợp lệ vẫn hoạt động với mỗi nhóm dữ liệu.
        a.monHocDao.sua(subject); a.lichHocDao.sua(schedule); a.lichThiDao.sua(exam); a.baiTapDao.sua(task); a.ghiChuDao.sua(note);
        a.baiTapDao.capNhatTrangThai(bid, Const.STATUS_DONE);
        check(a.baiTapDao.layTheoId(bid).getTrangThai().equals(Const.STATUS_DONE), "Own task status update");
        check(a.lichHocDao.layTheoId(hid).getTenMon().equals("Cơ sở dữ liệu"), "Vietnamese subject join");
        check(a.lichThiDao.layTheoId(tid).getNgayThi().equals(date), "Date round trip");
        int unlinkedTask = b.baiTapDao.them(new BaiTap(null, null, "Không gắn môn", "", date, "Cao", Const.STATUS_TODO));
        int unlinkedNote = b.ghiChuDao.them(new GhiChu(null, null, "Không gắn môn", "", date));
        check(b.baiTapDao.layTheoId(unlinkedTask) != null && a.baiTapDao.layTheoId(unlinkedTask) == null, "Unlinked task has owner");
        check(b.ghiChuDao.layTheoId(unlinkedNote) != null && a.ghiChuDao.layTheoId(unlinkedNote) == null, "Unlinked note has owner");
        a.monHocDao.xoa(mid);
        check(a.lichHocDao.layTheoId(hid) == null && a.lichThiDao.layTheoId(tid) == null, "Own cascade delete");
        check(a.baiTapDao.layTheoId(bid).getMonHocId() == null && a.ghiChuDao.layTheoId(gid).getMonHocId() == null, "SET NULL keeps child owner");
        check(b.monHocDao.layTheoId(bobMid) != null, "Other user subject unchanged");
        a.baiTapDao.xoa(bid); a.ghiChuDao.xoa(gid);
    }
}
