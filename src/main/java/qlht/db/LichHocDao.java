package qlht.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import qlht.model.LichHoc;

public class LichHocDao extends BaseDao {
    public LichHocDao(Database db, qlht.model.User user) { super(db, user); }

    private static final String SELECT =
            "SELECT lich_hoc.id, lich_hoc.mon_hoc_id, mon_hoc.ten_mon, lich_hoc.thu, "
            + "lich_hoc.tiet_bat_dau, lich_hoc.tiet_ket_thuc, lich_hoc.phong_hoc "
            + "FROM lich_hoc JOIN mon_hoc ON lich_hoc.mon_hoc_id = mon_hoc.id AND lich_hoc.user_id = mon_hoc.user_id ";

    private static LichHoc map(ResultSet rs) throws SQLException {
        LichHoc l = new LichHoc(rs.getInt("mon_hoc_id"), rs.getString("ten_mon"), rs.getString("thu"),
                rs.getInt("tiet_bat_dau"), rs.getInt("tiet_ket_thuc"), rs.getString("phong_hoc"));
        l.setId(rs.getInt("id"));
        return l;
    }

    public int them(LichHoc l) {
        l.validate();
        requireOwnedSubject(l.getMonHocId());
        int id = insert("INSERT INTO lich_hoc (mon_hoc_id, thu, tiet_bat_dau, tiet_ket_thuc, phong_hoc, user_id) "
                + "VALUES (?, ?, ?, ?, ?, ?)", ps -> {
            ps.setInt(1, l.getMonHocId());
            ps.setString(2, l.getThu());
            ps.setInt(3, l.getTietBd());
            ps.setInt(4, l.getTietKt());
            ps.setString(5, l.getPhong());
            ps.setInt(6, userId);
        });
        l.setId(id);
        return id;
    }

    public void sua(LichHoc l) {
        l.validate();
        requireOwnedSubject(l.getMonHocId());
        update("UPDATE lich_hoc SET mon_hoc_id=?, thu=?, tiet_bat_dau=?, tiet_ket_thuc=?, phong_hoc=? WHERE id=? AND user_id=?", ps -> {
            ps.setInt(1, l.getMonHocId());
            ps.setString(2, l.getThu());
            ps.setInt(3, l.getTietBd());
            ps.setInt(4, l.getTietKt());
            ps.setString(5, l.getPhong());
            ps.setInt(6, l.getId());
            ps.setInt(7, userId);
        });
    }

    public void xoa(int id) {
        update("DELETE FROM lich_hoc WHERE id = ? AND user_id=?", ps -> { ps.setInt(1, id); ps.setInt(2, userId); });
    }

    public LichHoc layTheoId(int id) {
        return queryOne(SELECT + "WHERE lich_hoc.id = ? AND lich_hoc.user_id=?", ps -> { ps.setInt(1, id); ps.setInt(2, userId); }, LichHocDao::map);
    }

    /** Thời khóa biểu sắp theo thứ trong tuần rồi theo tiết bắt đầu. */
    public List<LichHoc> layThoiKhoaBieu() {
        return query(SELECT + """
                WHERE lich_hoc.user_id=?
                ORDER BY CASE lich_hoc.thu
                    WHEN N'Thứ 2' THEN 2 WHEN N'Thứ 3' THEN 3 WHEN N'Thứ 4' THEN 4
                    WHEN N'Thứ 5' THEN 5 WHEN N'Thứ 6' THEN 6 WHEN N'Thứ 7' THEN 7
                    WHEN N'Chủ nhật' THEN 8 ELSE 9 END, lich_hoc.tiet_bat_dau, lich_hoc.id
                """, ps -> ps.setInt(1, userId), LichHocDao::map);
    }

    /** Các lịch học cùng thứ và có khoảng tiết giao nhau với (tietBd, tietKt). */
    public List<LichHoc> timXungDot(String thu, int tietBd, int tietKt, Integer boQuaId) {
        List<LichHoc> ds = query(SELECT
                + "WHERE lich_hoc.thu = ? AND lich_hoc.tiet_bat_dau <= ? AND lich_hoc.tiet_ket_thuc >= ? AND lich_hoc.user_id=?", ps -> {
            ps.setString(1, thu);
            ps.setInt(2, tietKt);
            ps.setInt(3, tietBd);
            ps.setInt(4, userId);
        }, LichHocDao::map);
        return ds.stream().filter(l -> !Objects.equals(l.getId(), boQuaId)).toList();
    }
}
