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
    public LichThiDao(Database db) { super(db); }

    private static final String SELECT =
            "SELECT lich_thi.id, lich_thi.mon_hoc_id, mon_hoc.ten_mon, lich_thi.ngay_thi, "
            + "lich_thi.gio_thi, lich_thi.phong_thi, lich_thi.hinh_thuc "
            + "FROM lich_thi JOIN mon_hoc ON lich_thi.mon_hoc_id = mon_hoc.id ";

    private static LichThi map(ResultSet rs) throws SQLException {
        LichThi l = new LichThi(rs.getInt("mon_hoc_id"), rs.getString("ten_mon"),
                DateUtil.fromDb(rs.getString("ngay_thi")), rs.getString("gio_thi"),
                rs.getString("phong_thi"), rs.getString("hinh_thuc"));
        l.setId(rs.getInt("id"));
        return l;
    }

    public int them(LichThi l) {
        int id = insert("INSERT INTO lich_thi (mon_hoc_id, ngay_thi, gio_thi, phong_thi, hinh_thuc) "
                + "VALUES (?, ?, ?, ?, ?)", ps -> {
            ps.setInt(1, l.getMonHocId());
            ps.setString(2, DateUtil.toDb(l.getNgayThi()));
            ps.setString(3, l.getGioThi());
            ps.setString(4, l.getPhong());
            ps.setString(5, l.getHinhThuc());
        });
        l.setId(id);
        return id;
    }

    public void sua(LichThi l) {
        update("UPDATE lich_thi SET mon_hoc_id=?, ngay_thi=?, gio_thi=?, phong_thi=?, hinh_thuc=? WHERE id=?", ps -> {
            ps.setInt(1, l.getMonHocId());
            ps.setString(2, DateUtil.toDb(l.getNgayThi()));
            ps.setString(3, l.getGioThi());
            ps.setString(4, l.getPhong());
            ps.setString(5, l.getHinhThuc());
            ps.setInt(6, l.getId());
        });
    }

    public void xoa(int id) {
        update("DELETE FROM lich_thi WHERE id = ?", ps -> ps.setInt(1, id));
    }

    public LichThi layTheoId(int id) {
        return queryOne(SELECT + "WHERE lich_thi.id = ?", ps -> ps.setInt(1, id), LichThiDao::map);
    }

    /** Sắp theo ngày, rồi theo giờ thật (không theo chữ cái); giờ không đọc được xếp cuối ngày. */
    public List<LichThi> layTatCa() {
        List<LichThi> ds = new ArrayList<>(query(SELECT, NO_PARAMS, LichThiDao::map));
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
