package qlht.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import qlht.db.*;
import qlht.model.*;
import qlht.util.Const;

/** Tạo bộ dữ liệu minh họa trong một tài khoản trống, toàn bộ dữ liệu ghi trong một transaction. */
public final class DemoDataService {
    private final Database db;
    public DemoDataService(Database db) { this.db = db; }

    public void seed(User user) {
        MonHocDao mon = new MonHocDao(db, user);
        LichHocDao hoc = new LichHocDao(db, user);
        LichThiDao thi = new LichThiDao(db, user);
        BaiTapDao bai = new BaiTapDao(db, user);
        GhiChuDao ghi = new GhiChuDao(db, user);
        if (!mon.layTatCa().isEmpty() || !hoc.layThoiKhoaBieu().isEmpty() || !thi.layTatCa().isEmpty()
                || !bai.layTatCa(false).isEmpty() || !ghi.layTatCa().isEmpty())
            throw new ValidationException("Chỉ nạp dữ liệu minh họa vào tài khoản trống.");
        Connection c = db.connection();
        try {
            c.setAutoCommit(false);
            try {
                MonHocService subjects = new MonHocService(mon);
                String[] names = {"Lập trình hướng đối tượng", "Cơ sở dữ liệu", "Toán rời rạc", "Tiếng Anh", "Mạng máy tính"};
                int[] ids = new int[names.length];
                for (int i = 0; i < names.length; i++) {
                    MonHoc m = new MonHoc(names[i], i == 3 ? 2 : 3, "Giảng viên " + (i + 1), "Dữ liệu minh họa");
                    subjects.save(m); ids[i] = m.getId();
                }
                hoc.them(new LichHoc(ids[0], null, "Thứ 2", 1, 3, "A2-301"));
                hoc.them(new LichHoc(ids[1], null, "Thứ 2", 5, 7, "B1-204"));
                hoc.them(new LichHoc(ids[2], null, "Thứ 3", 2, 4, "C2-105"));
                hoc.them(new LichHoc(ids[0], null, "Thứ 4", 1, 3, "A2-301"));
                hoc.them(new LichHoc(ids[4], null, "Thứ 4", 2, 4, "D1-201"));
                hoc.them(new LichHoc(ids[3], null, "Thứ 5", 3, 4, "E2-406"));
                hoc.them(new LichHoc(ids[1], null, "Thứ 6", 5, 7, "B1-204"));
                LocalDate today = LocalDate.now();
                thi.them(new LichThi(ids[0], null, today.plusDays(7), "08:00", "A1-101", "Thực hành"));
                thi.them(new LichThi(ids[1], null, today.plusDays(14), "13:00", "B1-101", "Tự luận"));
                bai.them(new BaiTap(ids[0], null, "Hoàn thiện sơ đồ lớp", "Bổ sung quan hệ kế thừa và interface", today.plusDays(2), "Cao", Const.STATUS_TODO));
                bai.them(new BaiTap(ids[1], null, "Bài tập truy vấn JOIN", "Đối chiếu kết quả trên SQL Server", today.minusDays(1), "Cao", Const.STATUS_TODO));
                bai.them(new BaiTap(ids[2], null, "Ôn tập chương đồ thị", "", today.plusDays(6), "Trung bình", "Đang làm"));
                bai.them(new BaiTap(null, null, "Chuẩn bị slide bảo vệ", "", today.plusDays(3), "Cao", Const.STATUS_TODO));
                bai.them(new BaiTap(ids[3], null, "Bài tập từ vựng", "", today, "Thấp", Const.STATUS_DONE));
                ghi.them(new GhiChu(ids[0], null, "Bốn tính chất OOP", "Đóng gói, kế thừa, trừu tượng, đa hình. Ví dụ trong StudySpace: Entity.validate(), TabPanel.refresh(), EntityForm<T>.", today));
                ghi.them(new GhiChu(ids[1], null, "Khóa ngoại và xóa liên đới", "Xóa môn: lịch học/lịch thi bị xóa; bài tập/ghi chú giữ lại với mã môn NULL.", today));
                ghi.them(new GhiChu(null, null, "Kịch bản demo", "Đăng nhập, xem lịch tuần, thử lịch trùng, hoàn thành bài tập và kiểm tra tài khoản khác.", today));
                c.commit();
            } catch (SQLException | RuntimeException e) { c.rollback(); throw e; }
            finally { c.setAutoCommit(true); }
        } catch (SQLException e) { throw new DataAccessException(e); }
    }
}
