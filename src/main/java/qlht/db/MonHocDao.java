package qlht.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import qlht.model.MonHoc;
import qlht.util.TextUtil;

public class MonHocDao extends BaseDao {
    public MonHocDao(Database db, qlht.model.User user) { super(db, user); }

    private static MonHoc map(ResultSet rs) throws SQLException {
        MonHoc m = new MonHoc(rs.getString("ten_mon"), rs.getInt("so_tin_chi"),
                rs.getString("giang_vien"), rs.getString("ghi_chu"));
        m.setId(rs.getInt("id"));
        return m;
    }

    public int them(MonHoc m) {
        m.validate();
        int id = insert("INSERT INTO mon_hoc (ten_mon, so_tin_chi, giang_vien, ghi_chu, user_id) VALUES (?, ?, ?, ?, ?)", ps -> {
            ps.setString(1, m.getTenMon());
            ps.setInt(2, m.getSoTinChi());
            ps.setString(3, m.getGiangVien());
            ps.setString(4, m.getGhiChu());
            ps.setInt(5, userId);
        });
        m.setId(id);
        return id;
    }

    public void sua(MonHoc m) {
        m.validate();
        update("UPDATE mon_hoc SET ten_mon=?, so_tin_chi=?, giang_vien=?, ghi_chu=? WHERE id=? AND user_id=?", ps -> {
            ps.setString(1, m.getTenMon());
            ps.setInt(2, m.getSoTinChi());
            ps.setString(3, m.getGiangVien());
            ps.setString(4, m.getGhiChu());
            ps.setInt(5, m.getId());
            ps.setInt(6, userId);
        });
    }

    public void xoa(int id) {
        update("DELETE FROM mon_hoc WHERE id = ? AND user_id=?", ps -> { ps.setInt(1, id); ps.setInt(2, userId); });
    }

    public List<MonHoc> layTatCa() {
        return query("SELECT * FROM mon_hoc WHERE user_id=? ORDER BY id", ps -> ps.setInt(1, userId), MonHocDao::map);
    }

    public MonHoc layTheoId(int id) {
        return queryOne("SELECT * FROM mon_hoc WHERE id = ? AND user_id=?", ps -> { ps.setInt(1, id); ps.setInt(2, userId); }, MonHocDao::map);
    }

    /** Tìm theo tên, không phân biệt hoa thường và dấu. */
    public List<MonHoc> timKiem(String tuKhoa) {
        String k = TextUtil.boDau(tuKhoa == null ? "" : tuKhoa.trim());
        List<MonHoc> all = layTatCa();
        if (k.isEmpty()) return all;
        return all.stream().filter(m -> TextUtil.boDau(m.getTenMon()).contains(k)).toList();
    }

    /** Môn đã có cùng tên (không phân biệt hoa thường, dấu, khoảng trắng thừa). */
    public MonHoc timTrungTen(String ten, Integer boQuaId) {
        String chuan = TextUtil.chuanHoaTen(ten);
        for (MonHoc m : layTatCa()) {
            if (!Objects.equals(m.getId(), boQuaId) && TextUtil.chuanHoaTen(m.getTenMon()).equals(chuan)) {
                return m;
            }
        }
        return null;
    }
}
