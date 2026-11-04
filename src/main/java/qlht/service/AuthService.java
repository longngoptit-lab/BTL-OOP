package qlht.service;

import java.sql.*;
import java.util.Arrays;
import java.util.Locale;
import qlht.db.DataAccessException;
import qlht.db.Database;
import qlht.model.User;
import qlht.util.PasswordUtil;

/** Đăng ký, xác thực và đổi mật khẩu bằng các truy vấn có tham số. */
public final class AuthService {
    private final Database db;

    public AuthService(Database db) { this.db = db; }

    public User login(String username, char[] password) {
        String normalized = normalizeUsername(username);
        if (password == null || password.length == 0 || password.length > 128)
            throw new AuthException("Vui lòng nhập mật khẩu (tối đa 128 ký tự).");
        try (PreparedStatement ps = db.connection().prepareStatement(
                "SELECT id, username, display_name, password_hash FROM dbo.tai_khoan WHERE username=?")) {
            ps.setString(1, normalized);
            User user = null;
            String hash = PasswordUtil.DUMMY_HASH;
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    user = new User(rs.getInt("id"), rs.getString("username"), rs.getString("display_name"));
                    hash = rs.getString("password_hash");
                }
            }
            boolean matches = PasswordUtil.verify(password, hash);
            if (user == null || !matches) throw new AuthException("Tên đăng nhập hoặc mật khẩu không đúng.");
            return user;
        } catch (SQLException e) { throw new DataAccessException(e); }
    }

    public User register(String displayName, String username, char[] password, char[] confirmation) {
        String name = displayName == null ? "" : displayName.strip();
        if (name.isEmpty() || name.length() > 80 || name.codePoints().anyMatch(Character::isISOControl))
            throw new AuthException("Tên hiển thị cần có từ 1 đến 80 ký tự.");
        String normalized = normalizeUsername(username);
        validatePassword(password, confirmation);
        String hash = PasswordUtil.hash(password);
        Connection c = db.connection();
        try {
            // Khóa dải khóa của bảng tài khoản để hai lần đăng ký đầu tiên không nhận cùng dữ liệu cũ.
            c.setAutoCommit(false);
            try {
                boolean first;
                try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(
                        "SELECT COUNT(*) FROM dbo.tai_khoan WITH (UPDLOCK, HOLDLOCK)")) {
                    rs.next(); first = rs.getInt(1) == 0;
                }
                int id;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO dbo.tai_khoan (username, display_name, password_hash) VALUES (?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, normalized); ps.setString(2, name); ps.setString(3, hash);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Không tạo được mã tài khoản.");
                        id = keys.getInt(1);
                    }
                }
                if (first) {
                    for (String table : new String[]{"mon_hoc", "lich_hoc", "lich_thi", "bai_tap", "ghi_chu"}) {
                        try (PreparedStatement ps = c.prepareStatement(
                                "UPDATE dbo." + table + " SET user_id=? WHERE user_id IS NULL")) {
                            ps.setInt(1, id); ps.executeUpdate();
                        }
                    }
                }
                c.commit();
                return new User(id, normalized, name);
            } catch (SQLException | RuntimeException e) {
                c.rollback(); throw e;
            } finally { c.setAutoCommit(true); }
        } catch (SQLException e) {
            if (e.getErrorCode() == 2601 || e.getErrorCode() == 2627)
                throw new AuthException("Tên đăng nhập đã được sử dụng. Hãy chọn tên khác.");
            throw new DataAccessException(e);
        }
    }

    public void changePassword(User user, char[] current, char[] password, char[] confirmation) {
        validatePassword(password, confirmation);
        try {
            String previous;
            try (PreparedStatement ps = db.connection().prepareStatement(
                    "SELECT password_hash FROM dbo.tai_khoan WHERE id=?")) {
                ps.setInt(1, user.id());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new AuthException("Tài khoản không còn tồn tại.");
                    previous = rs.getString(1);
                }
            }
            if (current == null || current.length > 128 || !PasswordUtil.verify(current, previous))
                throw new AuthException("Mật khẩu hiện tại không đúng.");
            if (Arrays.equals(current, password)) throw new AuthException("Mật khẩu mới cần khác mật khẩu hiện tại.");
            String hash = PasswordUtil.hash(password);
            try (PreparedStatement ps = db.connection().prepareStatement(
                    "UPDATE dbo.tai_khoan SET password_hash=? WHERE id=? AND password_hash=?")) {
                ps.setString(1, hash); ps.setInt(2, user.id()); ps.setString(3, previous);
                if (ps.executeUpdate() != 1) throw new AuthException("Mật khẩu vừa thay đổi. Vui lòng thử lại.");
            }
        } catch (SQLException e) { throw new DataAccessException(e); }
    }

    private static String normalizeUsername(String input) {
        String username = input == null ? "" : input.strip().toLowerCase(Locale.ROOT);
        if (!username.matches("[a-z0-9_.-]{3,40}"))
            throw new AuthException("Tên đăng nhập: 3–40 ký tự, gồm chữ không dấu, số, dấu chấm, _ hoặc -.");
        return username;
    }

    private static void validatePassword(char[] password, char[] confirmation) {
        if (password == null || password.length < 8 || password.length > 128)
            throw new AuthException("Mật khẩu cần có từ 8 đến 128 ký tự.");
        if (!Arrays.equals(password, confirmation)) throw new AuthException("Mật khẩu nhập lại chưa khớp.");
    }
}
