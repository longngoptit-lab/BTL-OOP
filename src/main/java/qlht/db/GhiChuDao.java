package qlht.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import qlht.model.GhiChu;
import qlht.util.DateUtil;

public class GhiChuDao extends BaseDao {
    public GhiChuDao(Database db, qlht.model.User user) { super(db, user); }

    private static final String SELECT =
            "SELECT g.id, g.mon_hoc_id, m.ten_mon, g.tieu_de, g.noi_dung, g.ngay_tao "
            + "FROM ghi_chu g LEFT JOIN mon_hoc m ON g.mon_hoc_id = m.id AND g.user_id = m.user_id ";

    private static GhiChu map(ResultSet rs) throws SQLException {
        GhiChu g = new GhiChu(getIntOrNull(rs, "mon_hoc_id"), rs.getString("ten_mon"),
                rs.getString("tieu_de"), rs.getString("noi_dung"), DateUtil.fromDb(rs.getString("ngay_tao")));
        g.setId(rs.getInt("id"));
        return g;
    }

    public int them(GhiChu g) {
        g.validate();
        if (g.getNgayTao() == null) throw new qlht.model.ValidationException("Ngày tạo ghi chú không hợp lệ.");
        requireOwnedSubject(g.getMonHocId());
        int id = insert("INSERT INTO ghi_chu (mon_hoc_id, tieu_de, noi_dung, ngay_tao, user_id) VALUES (?, ?, ?, ?, ?)", ps -> {
            setIntOrNull(ps, 1, g.getMonHocId());
            ps.setString(2, g.getTieuDe());
            ps.setString(3, g.getNoiDung());
            ps.setString(4, DateUtil.toDb(g.getNgayTao()));
            ps.setInt(5, userId);
        });
        g.setId(id);
        return id;
    }

    /** Sửa môn, tiêu đề, nội dung (giữ nguyên ngày tạo). */
    public void sua(GhiChu g) {
        g.validate();
        requireOwnedSubject(g.getMonHocId());
        update("UPDATE ghi_chu SET mon_hoc_id=?, tieu_de=?, noi_dung=? WHERE id=? AND user_id=?", ps -> {
            setIntOrNull(ps, 1, g.getMonHocId());
            ps.setString(2, g.getTieuDe());
            ps.setString(3, g.getNoiDung());
            ps.setInt(4, g.getId());
            ps.setInt(5, userId);
        });
    }

    public void xoa(int id) {
        update("DELETE FROM ghi_chu WHERE id = ? AND user_id=?", ps -> { ps.setInt(1, id); ps.setInt(2, userId); });
    }

    public GhiChu layTheoId(int id) {
        return queryOne(SELECT + "WHERE g.id = ? AND g.user_id=?", ps -> { ps.setInt(1, id); ps.setInt(2, userId); }, GhiChuDao::map);
    }

    /** Mới nhất trước. */
    public List<GhiChu> layTatCa() {
        return query(SELECT + "WHERE g.user_id=? ORDER BY g.ngay_tao DESC, g.id DESC", ps -> ps.setInt(1, userId), GhiChuDao::map);
    }
}
