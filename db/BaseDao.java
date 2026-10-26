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

    protected BaseDao(Database db) {
        this.db = db;
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
                    return keys.next() ? keys.getInt(1) : -1;
                }
            }
        });
    }

    /** UPDATE / DELETE. */
    protected void update(String sql, Binder binder) {
        exec(c -> {
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                binder.bind(ps);
                ps.executeUpdate();
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
