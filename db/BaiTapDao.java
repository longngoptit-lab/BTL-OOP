package qlht.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import qlht.model.BaiTap;
import qlht.util.Const;
import qlht.util.DateUtil;

public class BaiTapDao extends BaseDao {
    public BaiTapDao(Database db) { super(db); }

    private static final String SELECT =
            "SELECT b.id, b.mon_hoc_id, m.ten_mon, b.tieu_de, b.mo_ta, b.han_nop, "
            + "b.muc_do_uu_tien, b.trang_thai FROM bai_tap b LEFT JOIN mon_hoc m ON b.mon_hoc_id = m.id ";

    private static BaiTap map(ResultSet rs) throws SQLException {
        BaiTap b = new BaiTap(getIntOrNull(rs, "mon_hoc_id"), rs.getString("ten_mon"),
                rs.getString("tieu_de"), rs.getString("mo_ta"), DateUtil.fromDb(rs.getString("han_nop")),
                rs.getString("muc_do_uu_tien"), rs.getString("trang_thai"));
        b.setId(rs.getInt("id"));
        return b;
    }

    public int them(BaiTap b) {
        int id = insert("INSERT INTO bai_tap (mon_hoc_id, tieu_de, mo_ta, han_nop, muc_do_uu_tien, trang_thai) "
                + "VALUES (?, ?, ?, ?, ?, ?)", ps -> {
            setIntOrNull(ps, 1, b.getMonHocId());
            ps.setString(2, b.getTieuDe());
            ps.setString(3, b.getMoTa());
            ps.setString(4, DateUtil.toDb(b.getHanNop()));
            ps.setString(5, b.getMucDoUuTien());
            ps.setString(6, b.getTrangThai() == null ? Const.STATUS_TODO : b.getTrangThai());
        });
        b.setId(id);
        return id;
    }

    public void sua(BaiTap b) {
        update("UPDATE bai_tap SET mon_hoc_id=?, tieu_de=?, mo_ta=?, han_nop=?, muc_do_uu_tien=?, trang_thai=? "
                + "WHERE id=?", ps -> {
            setIntOrNull(ps, 1, b.getMonHocId());
            ps.setString(2, b.getTieuDe());
            ps.setString(3, b.getMoTa());
            ps.setString(4, DateUtil.toDb(b.getHanNop()));
            ps.setString(5, b.getMucDoUuTien());
            ps.setString(6, b.getTrangThai());
            ps.setInt(7, b.getId());
        });
    }

    public void capNhatTrangThai(int id, String trangThai) {
        update("UPDATE bai_tap SET trang_thai=? WHERE id=?", ps -> {
            ps.setString(1, trangThai);
            ps.setInt(2, id);
        });
    }

    public void xoa(int id) {
        update("DELETE FROM bai_tap WHERE id = ?", ps -> ps.setInt(1, id));
    }

    public BaiTap layTheoId(int id) {
        return queryOne(SELECT + "WHERE b.id = ?", ps -> ps.setInt(1, id), BaiTapDao::map);
    }

    /** Sắp theo hạn nộp (ISO nên đúng thứ tự thời gian), rồi theo ID. */
    public List<BaiTap> layTatCa(boolean chiChuaHoanThanh) {
        if (chiChuaHoanThanh) {
            return query(SELECT + "WHERE b.trang_thai != ? ORDER BY b.han_nop, b.id",
                    ps -> ps.setString(1, Const.STATUS_DONE), BaiTapDao::map);
        }
        return query(SELECT + "ORDER BY b.han_nop, b.id", NO_PARAMS, BaiTapDao::map);
    }
}
