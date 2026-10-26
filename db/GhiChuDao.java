package qlht.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import qlht.model.GhiChu;
import qlht.util.DateUtil;

public class GhiChuDao extends BaseDao {
    public GhiChuDao(Database db) { super(db); }

    private static final String SELECT =
            "SELECT g.id, g.mon_hoc_id, m.ten_mon, g.tieu_de, g.noi_dung, g.ngay_tao "
            + "FROM ghi_chu g LEFT JOIN mon_hoc m ON g.mon_hoc_id = m.id ";

    private static GhiChu map(ResultSet rs) throws SQLException {
        GhiChu g = new GhiChu(getIntOrNull(rs, "mon_hoc_id"), rs.getString("ten_mon"),
                rs.getString("tieu_de"), rs.getString("noi_dung"), DateUtil.fromDb(rs.getString("ngay_tao")));
        g.setId(rs.getInt("id"));
        return g;
    }

    public int them(GhiChu g) {
        int id = insert("INSERT INTO ghi_chu (mon_hoc_id, tieu_de, noi_dung, ngay_tao) VALUES (?, ?, ?, ?)", ps -> {
            setIntOrNull(ps, 1, g.getMonHocId());
            ps.setString(2, g.getTieuDe());
            ps.setString(3, g.getNoiDung());
            ps.setString(4, DateUtil.toDb(g.getNgayTao()));
        });
        g.setId(id);
        return id;
    }

    /** Sửa môn, tiêu đề, nội dung (giữ nguyên ngày tạo). */
    public void sua(GhiChu g) {
        update("UPDATE ghi_chu SET mon_hoc_id=?, tieu_de=?, noi_dung=? WHERE id=?", ps -> {
            setIntOrNull(ps, 1, g.getMonHocId());
            ps.setString(2, g.getTieuDe());
            ps.setString(3, g.getNoiDung());
            ps.setInt(4, g.getId());
        });
    }

    public void xoa(int id) {
        update("DELETE FROM ghi_chu WHERE id = ?", ps -> ps.setInt(1, id));
    }

    public GhiChu layTheoId(int id) {
        return queryOne(SELECT + "WHERE g.id = ?", ps -> ps.setInt(1, id), GhiChuDao::map);
    }

    /** Mới nhất trước. */
    public List<GhiChu> layTatCa() {
        return query(SELECT + "ORDER BY g.ngay_tao DESC, g.id DESC", NO_PARAMS, GhiChuDao::map);
    }
}
