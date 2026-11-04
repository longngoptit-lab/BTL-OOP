package qlht.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** Lớp cha của các DAO: gom code JDBC lặp lại (mở/đóng statement, bắt SQLException). */
public abstract class BaseDao {
    private final Database db;
    protected final int userId;

    protected BaseDao(Database db, qlht.model.User user) {
        this.db = java.util.Objects.requireNonNull(db);
        this.userId = java.util.Objects.requireNonNull(user, "Cần đăng nhập trước khi truy cập dữ liệu.").id();
    }

    /** Không cho gắn lịch/bài tập/ghi chú vào môn của tài khoản khác. */
    protected void requireOwnedSubject(Integer id) {
        if (id == null) return;
        exec(c -> {
            try (PreparedStatement ps = c.prepareStatement("SELECT id FROM dbo.mon_hoc WHERE id=? AND user_id=?")) {
                ps.setInt(1, id); ps.setInt(2, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new SQLException("Môn học không thuộc tài khoản đang đăng nhập.");
                }
            }
            return null;
        });
    }

    @FunctionalInterface
    protected interface SqlAction<R> { R run(Connection c) throws SQLException; }

    @FunctionalInterface
    protected interface Binder { void bind(PreparedStatement ps) throws SQLException; }

    @FunctionalInterface
    protected interface RowMapper<T> { T map(ResultSet rs) throws SQLException; }

    protected static final Binder NO_PARAMS = ps -> { };

    protected <R> R exec(SqlAction<R> action) {
        try {
            return action.run(db.connection());
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    /** INSERT, trả về ID vừa được tạo. */
    protected int insert(String sql, Binder binder) {
        return exec(c -> {
            try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                binder.bind(ps);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("Không lấy được mã dữ liệu vừa tạo.");
                    return keys.getInt(1);
                }
            }
        });
    }

    /** UPDATE / DELETE. */
    protected void update(String sql, Binder binder) {
        exec(c -> {
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                binder.bind(ps);
                if (ps.executeUpdate() != 1) throw new SQLException("Không tìm thấy dữ liệu trong tài khoản của bạn.");
            }
            return null;
        });
    }

    protected <T> List<T> query(String sql, Binder binder, RowMapper<T> mapper) {
        return exec(c -> {
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                binder.bind(ps);
                try (ResultSet rs = ps.executeQuery()) {
                    List<T> out = new ArrayList<>();
                    while (rs.next()) out.add(mapper.map(rs));
                    return out;
                }
            }
        });
    }

    protected <T> T queryOne(String sql, Binder binder, RowMapper<T> mapper) {
        List<T> list = query(sql, binder, mapper);
        return list.isEmpty() ? null : list.get(0);
    }

    protected static Integer getIntOrNull(ResultSet rs, String col) throws SQLException {
        int v = rs.getInt(col);
        return rs.wasNull() ? null : v;
    }

    protected static void setIntOrNull(PreparedStatement ps, int index, Integer v) throws SQLException {
        if (v == null) ps.setNull(index, Types.INTEGER);
        else ps.setInt(index, v);
    }
}
