package qlht.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import qlht.model.LichThi;
import qlht.util.DateUtil;
import qlht.util.TimeUtil;

public class LichThiDao extends BaseDao {
    public LichThiDao(Database db, qlht.model.User user) { super(db, user); }

    private static final String SELECT =
            "SELECT lich_thi.id, lich_thi.mon_hoc_id, mon_hoc.ten_mon, lich_thi.ngay_thi, "
            + "lich_thi.gio_thi, lich_thi.phong_thi, lich_thi.hinh_thuc "
            + "FROM lich_thi JOIN mon_hoc ON lich_thi.mon_hoc_id = mon_hoc.id AND lich_thi.user_id = mon_hoc.user_id ";

    private static LichThi map(ResultSet rs) throws SQLException {
        LichThi l = new LichThi(rs.getInt("mon_hoc_id"), rs.getString("ten_mon"),
                DateUtil.fromDb(rs.getString("ngay_thi")), rs.getString("gio_thi"),
                rs.getString("phong_thi"), rs.getString("hinh_thuc"));
        l.setId(rs.getInt("id"));
        return l;
    }

    public int them(LichThi l) {
        l.validate();
        requireOwnedSubject(l.getMonHocId());
        int id = insert("INSERT INTO lich_thi (mon_hoc_id, ngay_thi, gio_thi, phong_thi, hinh_thuc, user_id) "
                + "VALUES (?, ?, ?, ?, ?, ?)", ps -> {
            ps.setInt(1, l.getMonHocId());
            ps.setString(2, DateUtil.toDb(l.getNgayThi()));
            ps.setString(3, l.getGioThi());
            ps.setString(4, l.getPhong());
            ps.setString(5, l.getHinhThuc());
            ps.setInt(6, userId);
        });
        l.setId(id);
        return id;
    }

    public void sua(LichThi l) {
        l.validate();
        requireOwnedSubject(l.getMonHocId());
        update("UPDATE lich_thi SET mon_hoc_id=?, ngay_thi=?, gio_thi=?, phong_thi=?, hinh_thuc=? WHERE id=? AND user_id=?", ps -> {
            ps.setInt(1, l.getMonHocId());
            ps.setString(2, DateUtil.toDb(l.getNgayThi()));
            ps.setString(3, l.getGioThi());
            ps.setString(4, l.getPhong());
            ps.setString(5, l.getHinhThuc());
            ps.setInt(6, l.getId());
            ps.setInt(7, userId);
        });
    }

    public void xoa(int id) {
        update("DELETE FROM lich_thi WHERE id = ? AND user_id=?", ps -> { ps.setInt(1, id); ps.setInt(2, userId); });
    }

    public LichThi layTheoId(int id) {
        return queryOne(SELECT + "WHERE lich_thi.id = ? AND lich_thi.user_id=?", ps -> { ps.setInt(1, id); ps.setInt(2, userId); }, LichThiDao::map);
    }

    /** Sắp theo ngày, rồi theo giờ thật (không theo chữ cái); giờ không đọc được xếp cuối ngày. */
    public List<LichThi> layTatCa() {
        List<LichThi> ds = new ArrayList<>(query(SELECT + "WHERE lich_thi.user_id=?", ps -> ps.setInt(1, userId), LichThiDao::map));
        Comparator<LichThi> theoNgay =
                Comparator.comparing(LichThi::getNgayThi, Comparator.nullsLast(Comparator.<LocalDate>naturalOrder()));
        ds.sort(theoNgay
                .thenComparing(l -> {
                    Integer p = TimeUtil.gioRaPhut(l.getGioThi());
                    return p != null ? p : 24 * 60;
                })
                .thenComparingInt(LichThi::getId));
        return ds;
    }
}
